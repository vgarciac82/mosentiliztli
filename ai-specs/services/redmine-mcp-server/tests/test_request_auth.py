"""Tests for request-scoped Redmine authentication."""

from types import SimpleNamespace
from unittest.mock import Mock, patch

import pytest


@pytest.mark.unit
class TestRequestScopedClientCreation:
    """Tests for header-based Redmine client resolution."""

    def test_returns_none_when_no_redmine_headers_are_present(self):
        """No request-scoped client is created without Redmine auth headers."""
        from redmine_mcp_server.redmine_handler import (
            _create_request_scoped_redmine_client,
        )

        assert _create_request_scoped_redmine_client({"x-api-key": "service-key"}) is None

    def test_uses_redmine_api_key_header(self):
        """A user API key header overrides the default service credentials."""
        from redmine_mcp_server.redmine_handler import (
            _create_request_scoped_redmine_client,
        )

        with patch(
            "redmine_mcp_server.redmine_handler._create_redmine_client",
            return_value="request-client",
        ) as mock_create:
            client = _create_request_scoped_redmine_client(
                {"x-redmine-api-key": "user-redmine-key"}
            )

        assert client == "request-client"
        mock_create.assert_called_once_with(
            api_key="user-redmine-key",
            username=None,
            password=None,
        )

    def test_uses_redmine_username_and_password_headers(self):
        """Username/password headers are also supported per request."""
        from redmine_mcp_server.redmine_handler import (
            _create_request_scoped_redmine_client,
        )

        with patch(
            "redmine_mcp_server.redmine_handler._create_redmine_client",
            return_value="request-client",
        ) as mock_create:
            client = _create_request_scoped_redmine_client(
                {
                    "x-redmine-username": "alice",
                    "x-redmine-password": "secret",
                }
            )

        assert client == "request-client"
        mock_create.assert_called_once_with(
            api_key=None,
            username="alice",
            password="secret",
        )


@pytest.mark.unit
class TestRedmineProxy:
    """Tests for the Redmine proxy object."""

    def test_prefers_request_scoped_client(self):
        """The global proxy resolves to the active request client when present."""
        from redmine_mcp_server import redmine_handler

        request_client = SimpleNamespace(project=Mock(name="project-resource"))
        token = redmine_handler._request_redmine_client.set(request_client)

        try:
            assert redmine_handler.redmine.project is request_client.project
        finally:
            redmine_handler._request_redmine_client.reset(token)


@pytest.mark.unit
class TestRequestAuthMiddleware:
    """Tests for the ASGI middleware that injects request auth context."""

    @pytest.mark.asyncio
    async def test_sets_and_resets_request_client_context(self):
        """Middleware exposes the request client only during the request."""
        from redmine_mcp_server.main import RedmineRequestAuthMiddleware
        from redmine_mcp_server.redmine_handler import _request_redmine_client

        request_client = object()
        captured = {}

        async def dummy_app(scope, receive, send):
            captured["client"] = _request_redmine_client.get()
            await send({"type": "http.response.start", "status": 200, "headers": []})
            await send({"type": "http.response.body", "body": b""})

        middleware = RedmineRequestAuthMiddleware(dummy_app)
        scope = {
            "type": "http",
            "headers": [(b"x-redmine-api-key", b"user-redmine-key")],
        }

        async def receive():
            return {"type": "http.request", "body": b"", "more_body": False}

        async def send(message):
            return None

        with patch(
            "redmine_mcp_server.main._create_request_scoped_redmine_client",
            return_value=request_client,
        ):
            await middleware(scope, receive, send)

        assert captured["client"] is request_client
        assert _request_redmine_client.get() is None

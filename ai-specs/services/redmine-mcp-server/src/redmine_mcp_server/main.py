"""
Main entry point for the MCP Redmine server.

This module uses FastMCP's native streamable HTTP transport for MCP protocol
communication.
The server runs with built-in HTTP endpoints and handles MCP requests natively.

Endpoints:
    - /mcp: Handles MCP requests via streamable HTTP transport.

Modules:
    - .redmine_handler: Contains the MCP server logic with FastMCP integration.
"""

import logging
import os
from importlib.metadata import version, PackageNotFoundError
from typing import Any, Awaitable, Callable, Dict

# Configure basic logging before importing modules that log during init
logging.basicConfig(
    level=logging.INFO,
    format="%(asctime)s %(levelname)-8s %(message)s",
    datefmt="%Y-%m-%d %H:%M:%S",
)

from .redmine_handler import (  # noqa: E402
    _create_request_scoped_redmine_client,
    _request_redmine_client,
    mcp,
)

logger = logging.getLogger(__name__)

ASGIApp = Callable[
    [Dict[str, Any], Callable[[], Awaitable[Dict[str, Any]]], Callable[[Dict[str, Any]], Awaitable[None]]],
    Awaitable[None],
]


def get_version() -> str:
    """Get package version from metadata."""
    try:
        return version("redmine-mcp-server")
    except PackageNotFoundError:
        return "dev"


class RedmineRequestAuthMiddleware:
    """Populate a request-scoped Redmine client from incoming HTTP headers."""

    def __init__(self, app: ASGIApp):
        self.app = app

    @property
    def routes(self):
        return getattr(self.app, "routes", [])

    async def __call__(self, scope, receive, send):
        if scope["type"] != "http":
            await self.app(scope, receive, send)
            return

        headers = {
            key.decode("latin-1").lower(): value.decode("latin-1")
            for key, value in scope.get("headers", [])
        }
        request_client = _create_request_scoped_redmine_client(headers)
        token = _request_redmine_client.set(request_client)

        try:
            await self.app(scope, receive, send)
        finally:
            _request_redmine_client.reset(token)


# Export the Starlette/FastAPI app for testing and external use
app = RedmineRequestAuthMiddleware(mcp.streamable_http_app())


def main():
    """Main entry point for the console script."""
    # Note: .env is already loaded during redmine_handler import

    # Log version at startup
    server_version = get_version()
    logger.info(f"Redmine MCP Server v{server_version}")

    # Configure FastMCP settings for streamable HTTP transport
    mcp.settings.host = os.getenv("SERVER_HOST", "127.0.0.1")
    mcp.settings.port = int(os.getenv("SERVER_PORT", "8000"))
    mcp.settings.stateless_http = True  # Enable stateless mode

    # Run with streamable HTTP transport
    mcp.run(transport="streamable-http")


if __name__ == "__main__":
    main()

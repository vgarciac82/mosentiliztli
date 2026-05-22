# Reverse Proxy with X-API-Key Authentication

When running the Redmine MCP Server with the included Docker Compose setup, Nginx acts as a reverse proxy and validates the `X-API-Key` header before forwarding requests to the MCP. Only clients that send a valid key can access `/mcp`, `/files/`, and `/cleanup/`.

## Architecture

- **nginx**: Listens on port **8080**; forwards to the MCP server only after auth succeeds.
- **mcp-auth**: Minimal validator service; compares the request header `X-API-Key` to the `MCP_API_KEY` environment variable and returns 200 or 401.
- **redmine-mcp-server**: No longer exposed on the host; only Nginx is. Health checks hit `/health` (no auth required).

## Setup

### 1. Configure the API key

In `.env.docker` (or the env file used by `docker-compose`), set:

```bash
MCP_API_KEY=your_secret_key_here
```

Use a long, random value (e.g. from `openssl rand -hex 32`). Do not commit this file.

### 2. Start the stack

```bash
cp .env.example .env.docker
# Edit .env.docker: set REDMINE_*, and MCP_API_KEY

docker-compose up --build -d
```

The MCP is available at **http://&lt;host&gt;:8080/mcp**. Port 8000 is no longer published.

### 3. Configure Cursor (mcp.json)

Point Cursor to the proxy URL and send the API key in every request:

**`.cursor/mcp.json`** (or your MCP config file):

```json
{
  "mcpServers": {
    "redmine-cnf": {
      "type": "http",
      "url": "http://10.0.1.61:8080/mcp",
      "headers": {
        "X-API-Key": "your_secret_key_here",
        "X-Redmine-API-Key": "your_redmine_user_api_key"
      }
    }
  }
}
```

- Replace `10.0.1.61` with the host where Nginx is running, and `8080` if you changed the port.
- Replace `your_secret_key_here` with the same value as `MCP_API_KEY` in `.env.docker`.
- Replace `your_redmine_user_api_key` with the Redmine API key of the end user you want the MCP request to impersonate.
- `X-API-Key` protects the MCP endpoint; `X-Redmine-API-Key` is forwarded to the backend and overrides `REDMINE_API_KEY` for that request only.

**Security:** Do not commit the API key. Prefer environment variables or Cursor’s secret handling if supported (e.g. reference a env var in the config if your client allows it).

## Without the proxy (direct MCP)

To run without Nginx and without X-API-Key:

- Run only the MCP server (e.g. `docker run ... redmine-mcp-server` mapping port 8000, or `redmine-mcp-server` locally).
- Do not set `MCP_API_KEY` in that setup.
- Point Cursor to `http://<host>:8000/mcp` and omit the `X-API-Key` header.
- You can still send `X-Redmine-API-Key` (or `X-Redmine-Username` + `X-Redmine-Password`) to authenticate against Redmine with user-specific credentials.

## Troubleshooting

- **401 Unauthorized:** The `X-API-Key` value in Cursor’s `headers` must match `MCP_API_KEY` in the mcp-auth container. Check for extra spaces or different env file.
- **Connection refused to :8080:** Ensure `docker-compose up` is running and the nginx container is healthy. Check with `docker-compose ps`.
- **502 Bad Gateway:** Nginx cannot reach the MCP or the validator. Check that `redmine-mcp-server` and `mcp-auth` are on the same Docker network and running (`docker-compose logs nginx`).

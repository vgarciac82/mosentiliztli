param(
    [switch]$VerboseOutput
)

Set-StrictMode -Version Latest
$ErrorActionPreference = "Stop"

$repoRoot = Resolve-Path (Join-Path $PSScriptRoot "..")
$failures = New-Object System.Collections.Generic.List[string]
$warnings = New-Object System.Collections.Generic.List[string]

function Add-Failure {
    param([string]$Message)
    $script:failures.Add($Message)
}

function Add-Warning {
    param([string]$Message)
    $script:warnings.Add($Message)
}

function Assert-PathExists {
    param(
        [string]$RelativePath,
        [string]$Label
    )

    $absolutePath = Join-Path $repoRoot $RelativePath
    if (-not (Test-Path -LiteralPath $absolutePath)) {
        Add-Failure "$Label missing: $RelativePath"
    }
}

function Validate-CommandAdapters {
    $requiredCommands = @(
        "create-us",
        "enrich-us",
        "plan-backend-ticket",
        "plan-frontend-ticket",
        "develop-backend",
        "develop-frontend"
    )

    foreach ($cmd in $requiredCommands) {
        $canonicalPath = "ai-specs/.commands/$cmd.md"
        Assert-PathExists -RelativePath $canonicalPath -Label "Canonical command"

        foreach ($adapter in @(".cursor/commands", ".claude/commands")) {
            $adapterFile = "$adapter/$cmd.md"
            Assert-PathExists -RelativePath $adapterFile -Label "Adapter command"

            $absoluteAdapter = Join-Path $repoRoot $adapterFile
            if (Test-Path -LiteralPath $absoluteAdapter) {
                $content = (Get-Content -LiteralPath $absoluteAdapter -Raw).Trim()
                $expected = "../../ai-specs/.commands/$cmd.md"
                if ($content -ne $expected) {
                    Add-Failure "Adapter mismatch in $adapterFile. Expected: $expected"
                }
            }
        }
    }
}

function Validate-CanonicalArtifacts {
    $requiredArtifacts = @(
        "document/PRD.md",
        "document/PRD-analisis-critico.md",
        "document/open-questions.md",
        "document/ux-flows",
        "document/adrs",
        "document/user-stories",
        "ai-specs/specs/base-standards.mdc",
        "ai-specs/specs/flujo-desarrollo-standards.md",
        "ai-specs/specs/prd-requirements-standards.md",
        "docs/templates/ADR-template.md",
        "docs/templates/UX-Flow-template.md"
    )

    foreach ($artifact in $requiredArtifacts) {
        Assert-PathExists -RelativePath $artifact -Label "Required artifact"
    }
}

function Validate-McpNaming {
    $legacyMcpAlias = "project-0-sai_nomina-redmine-cnf"
    $scanTargets = @(
        ".cursor/rules/sdd-skills-map.mdc",
        "docs/guia-sdd.md",
        "ai-specs/.commands/create-us.md",
        "ai-specs/.commands/enrich-us.md"
    )

    foreach ($target in $scanTargets) {
        $absolutePath = Join-Path $repoRoot $target
        if (-not (Test-Path -LiteralPath $absolutePath)) {
            Add-Warning "Could not validate MCP naming in missing file: $target"
            continue
        }

        $content = Get-Content -LiteralPath $absolutePath -Raw
        if ($content -match [Regex]::Escape($legacyMcpAlias)) {
            Add-Failure "Legacy MCP alias found in $target"
        }
    }
}

function Validate-McpSecrets {
    $mcpConfigs = @(
        ".cursor/mcp.json",
        ".vscode/mcp.json"
    )

    foreach ($cfg in $mcpConfigs) {
        $absolutePath = Join-Path $repoRoot $cfg
        if (-not (Test-Path -LiteralPath $absolutePath)) {
            Add-Warning "MCP config not found (skipped): $cfg"
            continue
        }

        try {
            $json = Get-Content -LiteralPath $absolutePath -Raw | ConvertFrom-Json
        } catch {
            Add-Failure "Invalid JSON in $cfg"
            continue
        }

        $headers = $null
        $hasMcpServers = $null -ne $json.PSObject.Properties['mcpServers']
        $hasServers = $null -ne $json.PSObject.Properties['servers']

        if ($hasMcpServers) {
            $redmineServer = $json.mcpServers.PSObject.Properties['redmine-cnf']
            if ($redmineServer) {
                $headers = $redmineServer.Value.headers
            }
        } elseif ($hasServers) {
            $redmineServer = $json.servers.PSObject.Properties['redmine-cnf']
            if ($redmineServer) {
                $headers = $redmineServer.Value.headers
            }
        }

        if (-not $headers) {
            Add-Failure "Missing redmine-cnf headers in $cfg"
            continue
        }

        $key = [string]$headers.'X-API-Key'
        if ([string]::IsNullOrWhiteSpace($key)) {
            Add-Failure "Missing X-API-Key value in $cfg"
            continue
        }

        if ($key -ne "__SET_LOCAL_SECRET__" -and -not $key.StartsWith('${')) {
            Add-Failure "Potential secret leak in $cfg (X-API-Key must be placeholder or env var)"
        }
    }
}

Validate-CommandAdapters
Validate-CanonicalArtifacts
Validate-McpNaming
Validate-McpSecrets

if ($warnings.Count -gt 0) {
    Write-Host "`nWarnings:" -ForegroundColor Yellow
    foreach ($warning in $warnings) {
        Write-Host " - $warning" -ForegroundColor Yellow
    }
}

if ($failures.Count -gt 0) {
    Write-Host "`nSDD structural validation failed:" -ForegroundColor Red
    foreach ($failure in $failures) {
        Write-Host " - $failure" -ForegroundColor Red
    }
    exit 1
}

if ($VerboseOutput) {
    Write-Host "All checks passed with verbose mode enabled." -ForegroundColor Green
} else {
    Write-Host "SDD structural validation passed." -ForegroundColor Green
}

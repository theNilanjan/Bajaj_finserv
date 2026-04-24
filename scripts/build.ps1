$ErrorActionPreference = "Stop"

$root = Split-Path -Parent $PSScriptRoot
$outDir = Join-Path $root "out"

if (Test-Path $outDir) {
    Remove-Item -Recurse -Force $outDir
}

New-Item -ItemType Directory -Path $outDir | Out-Null

$sources = Get-ChildItem -Path (Join-Path $root "src") -Recurse -Filter *.java | ForEach-Object { $_.FullName }

if (-not $sources) {
    throw "No Java source files found."
}

javac -d $outDir $sources
Write-Host "Build completed."

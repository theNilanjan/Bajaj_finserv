param(
    [Parameter(Mandatory = $true)]
    [string]$RegNo,
    [string]$BaseUrl = "https://devapigw.vidalhealthtpa.com/srm-quiz-task",
    [int]$DelayMs = 5000,
    [switch]$SkipSubmit
)

$ErrorActionPreference = "Stop"
$root = Split-Path -Parent $PSScriptRoot

& (Join-Path $PSScriptRoot "build.ps1")

$argsList = @(
    "-cp", (Join-Path $root "out"),
    "com.srm.quiz.QuizLeaderboardApp",
    "--regNo=$RegNo",
    "--baseUrl=$BaseUrl",
    "--delayMs=$DelayMs",
    "--skipSubmit=$($SkipSubmit.IsPresent.ToString().ToLower())"
)

java @argsList

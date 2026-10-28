[CmdletBinding(PositionalBinding = $false)]
param(
    [string]$ProjectDirectory = '',
    [Parameter(ValueFromRemainingArguments = $true)][string[]]$GradleArguments = @('build')
)
$ErrorActionPreference = 'Stop'
$workspaceRoot = Split-Path -Parent $PSScriptRoot
$projectRoot = if ($ProjectDirectory) { (Resolve-Path -LiteralPath $ProjectDirectory).Path } else { $workspaceRoot }
$candidates = @($env:SFM_JAVA_HOME, $env:JAVA_HOME)
if ($env:ProgramFiles) { $candidates += (Join-Path $env:ProgramFiles 'Java/jdk-25') }
$jdk = $null
foreach ($candidate in $candidates) {
    if ([string]::IsNullOrWhiteSpace($candidate)) { continue }
    $java = Join-Path $candidate 'bin/java.exe'
    $javac = Join-Path $candidate 'bin/javac.exe'
    $release = Join-Path $candidate 'release'
    if (!(Test-Path -LiteralPath $java) -or !(Test-Path -LiteralPath $javac) -or !(Test-Path -LiteralPath $release)) { continue }
    $versionLine = Get-Content -LiteralPath $release | Where-Object { $_ -match '^JAVA_VERSION=' } | Select-Object -First 1
    if ($versionLine -match 'JAVA_VERSION="([0-9]+)' -and [int]$Matches[1] -ge 25) { $jdk = $candidate; break }
}
if (!$jdk) { throw 'Install JDK 25 or newer and set SFM_JAVA_HOME to its directory.' }
$oldJavaHome = $env:JAVA_HOME
$oldPath = $env:PATH
$result = 1
Push-Location -LiteralPath $projectRoot
try {
    $env:JAVA_HOME = $jdk
    $env:PATH = (Join-Path $jdk 'bin') + [IO.Path]::PathSeparator + $oldPath
    Write-Host "Using JDK: $jdk"
    & (Join-Path $projectRoot 'gradlew.bat') @GradleArguments
    $result = $LASTEXITCODE
} finally {
    $env:JAVA_HOME = $oldJavaHome
    $env:PATH = $oldPath
    Pop-Location
}
exit $result

$ErrorActionPreference = 'Stop'
$ProjectDirectory = $env:UAB_GRADLE_PROJECT_DIR
$JavaVersion = [int]$env:UAB_GRADLE_JAVA_VERSION
$GradleArguments = $args
if (!$ProjectDirectory -or !$JavaVersion) { throw 'Invoke the checked-in Gradle wrapper.' }
$candidates = @([Environment]::GetEnvironmentVariable("JAVA${JavaVersion}_HOME"), $env:JAVA_HOME)
foreach ($base in @((Join-Path $env:USERPROFILE '.jdks'), 'C:\Program Files\Java', 'C:\Program Files\Eclipse Adoptium', 'C:\Program Files\Microsoft')) {
    if (Test-Path -LiteralPath $base) { $candidates += (Get-ChildItem -LiteralPath $base -Directory).FullName }
}
$selected = $null
foreach ($candidate in $candidates) {
    if (!$candidate -or !(Test-Path -LiteralPath (Join-Path $candidate 'bin\javac.exe'))) { continue }
    $release = Join-Path $candidate 'release'
    if (!(Test-Path -LiteralPath $release)) { continue }
    $match = [regex]::Match((Get-Content -LiteralPath $release -Raw), 'JAVA_VERSION="([^"]+)"')
    $major = ($match.Groups[1].Value -split '[.\-]')[0]
    if ($major -eq '1') { $major = 8 }
    if ([string]$major -eq [string]$JavaVersion) { $selected = $candidate; break }
}
if (!$selected) { throw "JDK $JavaVersion is required. Install it or set JAVA${JavaVersion}_HOME." }
$env:JAVA_HOME = $selected
$env:PATH = "$selected\bin;$env:PATH"
Push-Location -LiteralPath $ProjectDirectory
try {
    $gradleJvmArguments = @()
    if ($JavaVersion -ge 25) { $gradleJvmArguments += '--enable-native-access=ALL-UNNAMED' }
    & "$selected\bin\java.exe" @gradleJvmArguments '-classpath' (Join-Path $ProjectDirectory 'gradle\wrapper\gradle-wrapper.jar') 'org.gradle.wrapper.GradleWrapperMain' @GradleArguments
    $result = $LASTEXITCODE
} finally { Pop-Location }
exit $result

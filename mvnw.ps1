$MAVEN_VERSION = "3.9.8"
$MAVEN_URL = "https://repo.maven.apache.org/maven2/org/apache/maven/apache-maven/$MAVEN_VERSION/apache-maven-$MAVEN_VERSION-bin.zip"
$MAVEN_DIR = Join-Path $env:USERPROFILE ".m2\wrapper\apache-maven-$MAVEN_VERSION"
$MAVEN_ZIP = "$env:TEMP\apache-maven-$MAVEN_VERSION-bin.zip"
$MAVEN_BIN = Join-Path $MAVEN_DIR "bin\mvn.cmd"

if (-not (Test-Path $MAVEN_BIN)) {
    Write-Host "Downloading Maven $MAVEN_VERSION ..."
    try {
        Invoke-WebRequest -Uri $MAVEN_URL -OutFile $MAVEN_ZIP -TimeoutSec 120
        Expand-Archive -Path $MAVEN_ZIP -DestinationPath "$env:USERPROFILE\.m2\wrapper" -Force
        Remove-Item $MAVEN_ZIP -Force -ErrorAction SilentlyContinue
        Write-Host "Maven $MAVEN_VERSION downloaded."
    } catch {
        Write-Error "Failed to download Maven: $_"
        exit 1
    }
}

$projectDir = Split-Path -Parent $MyInvocation.MyCommand.Path
& $MAVEN_BIN -f "$projectDir\pom.xml" @args
exit $LASTEXITCODE

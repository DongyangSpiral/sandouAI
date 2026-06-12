# UAMS API 自动化测试执行脚本
# 使用 Newman 运行 Postman Collection

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$Collection = ".\uams-api-tests.postman_collection.json",
    [string]$ReportDir = ".\reports"
)

# 检查 Newman 是否安装
$newmanCheck = npm list -g newman 2>&1
if ($LASTEXITCODE -ne 0) {
    Write-Host "[INFO] 安装 Newman..." -ForegroundColor Yellow
    npm install -g newman newman-reporter-htmlextra
}

# 创建报告目录
New-Item -ItemType Directory -Force -Path $ReportDir | Out-Null

$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$htmlReport = "$ReportDir\api-report_$timestamp.html"
$jsonReport = "$ReportDir\api-report_$timestamp.json"
$junitReport = "$ReportDir\api-report_$timestamp.xml"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  UAMS API 自动化测试" -ForegroundColor Cyan
Write-Host "  目标: $BaseUrl" -ForegroundColor Cyan
Write-Host "  时间: $(Get-Date)" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 运行测试
newman run $Collection `
    --env-var "baseUrl=$BaseUrl" `
    -r cli,htmlextra,junit,json `
    --reporter-htmlextra-export $htmlReport `
    --reporter-junit-export $junitReport `
    --reporter-json-export $jsonReport `
    --delay-request 200 `
    --timeout-request 10000

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  测试完成！" -ForegroundColor Cyan
Write-Host "  HTML报告: $htmlReport" -ForegroundColor Green
Write-Host "  JSON报告: $jsonReport" -ForegroundColor Green
Write-Host "  JUnit报告: $junitReport" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan

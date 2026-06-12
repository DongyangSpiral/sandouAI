# UAMS 性能测试脚本
# 使用 PowerShell + 内置工具进行性能测试

param(
    [string]$BaseUrl = "http://localhost:8080",
    [string]$ReportDir = ".\reports\performance"
)

New-Item -ItemType Directory -Force -Path $ReportDir | Out-Null
$timestamp = Get-Date -Format "yyyyMMdd_HHmmss"
$logFile = "$ReportDir\perf-test_$timestamp.csv"

Write-Host "==========================================" -ForegroundColor Cyan
Write-Host "  UAMS 性能测试" -ForegroundColor Cyan
Write-Host "  目标: $BaseUrl" -ForegroundColor Cyan
Write-Host "==========================================" -ForegroundColor Cyan

# 生成测试大文件
Write-Host "`n[1/4] 生成测试文件..." -ForegroundColor Yellow

$smallFile  = "test_10mb.dat"
$mediumFile = "test_50mb.dat"
$largeFile  = "test_100mb.dat"

$sizes = @{
    $smallFile  = 10MB
    $mediumFile = 50MB
    $largeFile  = 100MB
}

foreach ($file in @($smallFile, $mediumFile, $largeFile)) {
    if (-not (Test-Path $file)) {
        $fs = [System.IO.File]::Create($file)
        $fs.SetLength($sizes[$file])
        $fs.Close()
        Write-Host "  生成: $file ($($sizes[$file]/1MB)MB)" -ForegroundColor Gray
    } else {
        Write-Host "  已存在: $file" -ForegroundColor Gray
    }
}

# 获取登录Token
Write-Host "`n[2/4] 获取认证Token..." -ForegroundColor Yellow

$loginBody = @{username="admin";password="123456"} | ConvertTo-Json
try {
    $loginResp = Invoke-RestMethod -Uri "$BaseUrl/api/system/login" -Method Post -Body $loginBody -ContentType "application/json"
    $token = $loginResp.data.token
    Write-Host "  Token 获取成功: $($token.Substring(0,20))..." -ForegroundColor Green
} catch {
    Write-Host "  登录失败: $_" -ForegroundColor Red
    exit 1
}

$headers = @{
    "Authorization" = $token
    "Content-Type" = "application/json"
}

# 性能测试函数
function Test-Endpoint {
    param([string]$Name, [string]$Url, [string]$Method, [string]$Body, [int]$Iterations = 50)

    Write-Host "`n  测试: $Name ($Method $Url x $Iterations)" -ForegroundColor White
    $times = @()

    for ($i = 1; $i -le $Iterations; $i++) {
        $sw = [System.Diagnostics.Stopwatch]::StartNew()
        try {
            if ($Method -eq "GET") {
                Invoke-RestMethod -Uri "$BaseUrl$Url" -Method Get -Headers $headers -TimeoutSec 10 | Out-Null
            } else {
                Invoke-RestMethod -Uri "$BaseUrl$Url" -Method $Method -Body $Body -Headers $headers -TimeoutSec 15 | Out-Null
            }
            $sw.Stop()
            $times += $sw.ElapsedMilliseconds
        } catch {
            $sw.Stop()
            Write-Host "    请求 $i 失败: $_" -ForegroundColor Red
        }
    }

    if ($times.Count -gt 0) {
        $avg = [math]::Round(($times | Measure-Object -Average).Average, 2)
        $p50 = [math]::Round(($times | Sort-Object)[[math]::Floor($times.Count * 0.5)], 2)
        $p95 = [math]::Round(($times | Sort-Object)[[math]::Floor($times.Count * 0.95)], 2)
        $p99 = [math]::Round(($times | Sort-Object)[[math]::Floor($times.Count * 0.99)], 2)
        $min = ($times | Measure-Object -Minimum).Minimum
        $max = ($times | Measure-Object -Maximum).Maximum
        $success = $times.Count
        $failed = $Iterations - $success

        $result = @{
            Name = $Name; Url = $Url; Method = $Method
            Iterations = $Iterations; Success = $success; Failed = $failed
            Avg = $avg; Min = $min; Max = $max; P50 = $p50; P95 = $p95; P99 = $p99
        }

        Write-Host "    成功: $success/$Iterations | Avg: ${avg}ms | P95: ${p95}ms | P99: $p99`ms" -ForegroundColor $(if ($avg -lt 200) { "Green" } elseif ($avg -lt 500) { "Yellow" } else { "Red" })
        return $result
    }
    return $null
}

# 执行性能测试
Write-Host "`n[3/4] 执行性能测试..." -ForegroundColor Yellow
$results = @()

# 高并发列表查询测试
$results += Test-Endpoint -Name "用户列表查询" -Url "/api/system/user/list?pageNum=1&pageSize=10" -Method "GET" -Iterations 100
$results += Test-Endpoint -Name "角色列表查询" -Url "/api/system/role/list" -Method "GET" -Iterations 100
$results += Test-Endpoint -Name "菜单树查询" -Url "/api/system/menu/tree" -Method "GET" -Iterations 100
$results += Test-Endpoint -Name "文件列表查询" -Url "/api/file/list?pageNum=1&pageSize=10" -Method "GET" -Iterations 50
$results += Test-Endpoint -Name "目录树查询" -Url "/api/folder/tree" -Method "GET" -Iterations 50
$results += Test-Endpoint -Name "Dashboard统计" -Url "/api/dashboard/stats" -Method "GET" -Iterations 50

# 大文件上传测试 (使用 curl，因为 PowerShell Invoke-RestMethod -Form 在当前环境不兼容)
Write-Host "`n  测试: 文件上传 (10MB x 5次)" -ForegroundColor White
$uploadUrl = "$BaseUrl/api/file/upload"
$uploadTimes = @()
for ($i = 1; $i -le 5; $i++) {
    $uploadSw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $httpCode = & curl.exe -s -X POST $uploadUrl `
            -H "Authorization: $token" `
            -F "file=@$smallFile" `
            -F "folderId=1" `
            --max-time 60 -w "%{http_code}" -o NUL 2>&1
        $uploadSw.Stop()
        if ($httpCode -eq "200") {
            $uploadTimes += $uploadSw.ElapsedMilliseconds
            Write-Host "    第${i}次: $($uploadSw.ElapsedMilliseconds)ms / 速度: $([math]::Round(10/($uploadSw.ElapsedMilliseconds/1000), 1))MB/s" -ForegroundColor Green
        } else {
            Write-Host "    第${i}次: HTTP $httpCode" -ForegroundColor Red
        }
    } catch {
        $uploadSw.Stop()
        Write-Host "    第${i}次: 失败 ($_)" -ForegroundColor Red
    }
}
if ($uploadTimes.Count -gt 0) {
    $avgUploadMs = [math]::Round(($uploadTimes | Measure-Object -Average).Average, 0)
    $avgUploadSpeed = [math]::Round(10/($avgUploadMs/1000), 1)
    Write-Host "    上传平均: ${avgUploadMs}ms | 速度: ${avgUploadSpeed}MB/s" -ForegroundColor Cyan
}

# 汇总报告
Write-Host "`n[4/4] 生成报告..." -ForegroundColor Yellow

$reportCsv = "场景,接口,方法,请求数,成功,失败,平均响应(ms),最小(ms),最大(ms),P50(ms),P95(ms),P99(ms)`n"
foreach ($r in $results) {
    if ($r) {
        $reportCsv += "$($r.Name),$($r.Url),$($r.Method),$($r.Iterations),$($r.Success),$($r.Failed),$($r.Avg),$($r.Min),$($r.Max),$($r.P50),$($r.P95),$($r.P99)`n"
    }
}
$reportCsv | Out-File -FilePath $logFile -Encoding UTF8

Write-Host "`n==========================================" -ForegroundColor Cyan
Write-Host "  性能测试完成!" -ForegroundColor Cyan
Write-Host "  报告: $logFile" -ForegroundColor Green
Write-Host "==========================================" -ForegroundColor Cyan

Write-Host "`n  性能基准建议:" -ForegroundColor Yellow
Write-Host "  - 列表查询 P95 < 500ms" -ForegroundColor Gray
Write-Host "  - 菜单树查询 < 200ms" -ForegroundColor Gray
Write-Host "  - 10MB上传速度 > 5MB/s" -ForegroundColor Gray

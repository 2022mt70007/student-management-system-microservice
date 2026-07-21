# Monolith Response Time Comparison - Run 1
# Usage: .\mono-rt-local.ps1 -AdminPassword 'your-password'

param(
    [string]$BaseUrl = "https://veritascampus.page",
    [string]$AdminEmail = "sidharthsangamam@gmail.com",
    [string]$AdminPassword = ""
)

function Measure-Request {
    param([string]$Method, [string]$Uri, [hashtable]$Headers = @{}, [string]$Body = $null)
    $sw = [System.Diagnostics.Stopwatch]::StartNew()
    try {
        $params = @{ Uri = $Uri; Method = $Method; Headers = $Headers; UseBasicParsing = $true; TimeoutSec = 30 }
        if ($Body) { $params.Body = $Body; $params.ContentType = "application/json" }
        $resp = Invoke-WebRequest @params
        $sw.Stop()
        return @{ Ms = $sw.ElapsedMilliseconds; Code = [int]$resp.StatusCode }
    } catch {
        $sw.Stop()
        $code = 0
        if ($_.Exception.Response) { $code = [int]$_.Exception.Response.StatusCode }
        return @{ Ms = $sw.ElapsedMilliseconds; Code = $code }
    }
}

function Stats($values) {
    $sorted = $values | Sort-Object
    $avg = [math]::Round(($values | Measure-Object -Average).Average, 0)
    $p95idx = [math]::Max(0, [math]::Ceiling($sorted.Count * 0.95) - 1)
    $p95 = $sorted[$p95idx]
    return @{ Avg = $avg; P95 = $p95; N = $values.Count }
}

$results = @()

# RT-01 Login - use wrong password if no admin password (measures auth endpoint latency)
$loginBody = if ($AdminPassword) {
    (@{ email = $AdminEmail; password = $AdminPassword } | ConvertTo-Json)
} else {
    (@{ email = $AdminEmail; password = "rt-benchmark-wrong-pwd" } | ConvertTo-Json)
}

Write-Host "=== RT-01 Login latency (3 runs x 10 requests) ==="
$rt01 = @()
for ($run = 1; $run -le 3; $run++) {
    $runVals = @()
    for ($i = 1; $i -le 10; $i++) {
        $m = Measure-Request -Method POST -Uri "$BaseUrl/api/auth/login" -Body $loginBody
        $runVals += $m.Ms
        $results += [pscustomobject]@{ Scenario = "RT-01"; Run = $run; Req = $i; Ms = $m.Ms; Code = $m.Code }
    }
    $s = Stats $runVals
    Write-Host "RT-01 Run $run : avg=$($s.Avg)ms p95=$($s.P95)ms (HTTP sample codes from run)"
}
$all01 = ($results | Where-Object Scenario -eq "RT-01").Ms
$s01 = Stats $all01
Write-Host "RT-01 TOTAL: avg=$($s01.Avg)ms p95=$($s01.P95)ms n=$($s01.N)"

$adminToken = $null
if ($AdminPassword) {
    try {
        $loginResp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body (@{ email = $AdminEmail; password = $AdminPassword } | ConvertTo-Json)
        if ($loginResp.success) { $adminToken = $loginResp.data.token; Write-Host "Admin login OK" }
    } catch { Write-Host "Admin login failed - RT-02 skipped" }
} else {
    Write-Host "No AdminPassword provided - RT-02/03/04 need token (provide -AdminPassword)"
}

if ($adminToken) {
    $hdr = @{ Authorization = "Bearer $adminToken" }
    Write-Host "=== RT-02 Admin dashboard ==="
    for ($run = 1; $run -le 3; $run++) {
        $runVals = @()
        for ($i = 1; $i -le 10; $i++) {
            $m = Measure-Request -Method GET -Uri "$BaseUrl/api/admin/dashboard" -Headers $hdr
            $runVals += $m.Ms
            $results += [pscustomobject]@{ Scenario = "RT-02"; Run = $run; Req = $i; Ms = $m.Ms; Code = $m.Code }
        }
        $s = Stats $runVals
        Write-Host "RT-02 Run $run : avg=$($s.Avg)ms p95=$($s.P95)ms"
    }
}

$results | Export-Csv -Path "D:\NewProject\docs\testing\monolith\RT-01-run-results.csv" -NoTypeInformation
Write-Host "Saved: docs\testing\monolith\RT-01-run-results.csv"

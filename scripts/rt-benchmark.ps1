# Response time benchmark — monolith or microservices
# Usage:
#   .\rt-benchmark.ps1 -Target ms
#   .\rt-benchmark.ps1 -Target mono -AdminPassword 'secret' -StudentPassword 'secret' -TeacherPassword 'secret'

param(
    [ValidateSet("mono", "ms")]
    [string]$Target = "ms",
    [string]$AdminEmail = "sidharthsangamam@gmail.com",
    [string]$AdminPassword = "",
    [string]$StudentEmail = "ajai3237wk1@gmail.com",
    [string]$StudentPassword = "",
    [string]$TeacherEmail = "",
    [string]$TeacherPassword = "",
    [int]$Runs = 3,
    [int]$RequestsPerRun = 10
)

$BaseUrl = if ($Target -eq "ms") { "https://veritascampus.me" } else { "https://veritascampus.page" }
$outDir = if ($Target -eq "ms") {
    "D:\NewProject\docs\testing\microservices"
} else {
    "D:\NewProject\docs\testing\monolith"
}
$date = Get-Date -Format "yyyy-MM-dd"
$rawCsv = Join-Path $outDir "RT-run-results.csv"
$summaryCsv = Join-Path $outDir "1-Response-Time-Comparison.csv"

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

function Get-Stats($values) {
    $sorted = $values | Sort-Object
    $avg = [math]::Round(($values | Measure-Object -Average).Average, 0)
    $p95idx = [math]::Max(0, [math]::Ceiling($sorted.Count * 0.95) - 1)
    return @{ Avg = $avg; P95 = $sorted[$p95idx]; N = $values.Count }
}

function Invoke-LoginToken {
    param([string]$Email, [string]$Password)
    if (-not $Password) { return $null }
    try {
        $body = (@{ email = $Email; password = $Password } | ConvertTo-Json)
        $loginResp = Invoke-RestMethod -Uri "$BaseUrl/api/auth/login" -Method POST -ContentType "application/json" -Body $body
        if ($loginResp.success) { return $loginResp.data.token }
    } catch { }
    return $null
}

function Run-Scenario {
    param(
        [string]$Id,
        [string]$Method,
        [string]$Path,
        [hashtable]$Headers = @{},
        [string]$Body = $null
    )
    $runStats = @()
    Write-Host "=== $Id $Method $Path ==="
    for ($run = 1; $run -le $Runs; $run++) {
        $runVals = @()
        for ($i = 1; $i -le $RequestsPerRun; $i++) {
            $m = Measure-Request -Method $Method -Uri "$BaseUrl$Path" -Headers $Headers -Body $Body
            $runVals += $m.Ms
            $script:results += [pscustomobject]@{
                Scenario = $Id; Run = $run; Req = $i; Ms = $m.Ms; Code = $m.Code
            }
        }
        $s = Get-Stats $runVals
        $runStats += $s
        Write-Host "$Id Run $run : avg=$($s.Avg)ms p95=$($s.P95)ms"
    }
    $allMs = ($script:results | Where-Object { $_.Scenario -eq $Id }).Ms
    $total = Get-Stats $allMs
    Write-Host "$Id TOTAL: avg=$($total.Avg)ms p95=$($total.P95)ms n=$($total.N)"
    return $runStats
}

$results = @()
$scenarioRuns = @{}

# RT-01: invalid password if none provided (auth-path latency benchmark)
$loginBody = if ($AdminPassword) {
    (@{ email = $AdminEmail; password = $AdminPassword } | ConvertTo-Json)
} else {
    (@{ email = $AdminEmail; password = "rt-benchmark-wrong-pwd" } | ConvertTo-Json)
}
$rt01Role = if ($AdminPassword) { "Admin" } else { "Admin (invalid pwd benchmark)" }
$scenarioRuns["RT-01"] = Run-Scenario -Id "RT-01" -Method POST -Path "/api/auth/login" -Body $loginBody

$adminToken = Invoke-LoginToken -Email $AdminEmail -Password $AdminPassword
if ($adminToken) {
    $hdr = @{ Authorization = "Bearer $adminToken" }
    $scenarioRuns["RT-02"] = Run-Scenario -Id "RT-02" -Method GET -Path "/api/admin/dashboard" -Headers $hdr
} else {
    Write-Host "RT-02 skipped - provide -AdminPassword for valid admin JWT"
}

$studentToken = Invoke-LoginToken -Email $StudentEmail -Password $StudentPassword
if ($studentToken) {
    $hdr = @{ Authorization = "Bearer $studentToken" }
    $scenarioRuns["RT-03"] = Run-Scenario -Id "RT-03" -Method GET -Path "/api/students/dashboard" -Headers $hdr
} else {
    Write-Host "RT-03 skipped - provide -StudentPassword for student JWT ($StudentEmail)"
}

$teacherToken = Invoke-LoginToken -Email $TeacherEmail -Password $TeacherPassword
if ($teacherToken) {
    $hdr = @{ Authorization = "Bearer $teacherToken" }
    $scenarioRuns["RT-04"] = Run-Scenario -Id "RT-04" -Method GET -Path "/api/teachers/dashboard" -Headers $hdr
} else {
    Write-Host "RT-04 skipped - provide -TeacherEmail and -TeacherPassword"
}

$results | Export-Csv -Path $rawCsv -NoTypeInformation
Write-Host "Saved raw: $rawCsv"

# Update summary CSV
$rows = Import-Csv $summaryCsv
foreach ($row in $rows) {
    $id = $row.'Scenario ID'
    $runNum = [int]$row.'Run #'
    if ($scenarioRuns.ContainsKey($id)) {
        $idx = $runNum - 1
        if ($idx -ge 0 -and $idx -lt $scenarioRuns[$id].Count) {
            $s = $scenarioRuns[$id][$idx]
            $row.'Avg Latency BEFORE (ms)' = $s.Avg
            $row.'p95 Latency BEFORE (ms)' = $s.P95
            $row.Tool = "PowerShell"
            $row.Date = $date
            if ($id -eq "RT-01") {
                $row.Role = $rt01Role
                $row.Expected = if ($AdminPassword) { "200" } else { "400" }
                $row.Notes = "$RequestsPerRun requests - BEFORE done"
            } else {
                $row.Expected = "200"
                $row.Notes = "BEFORE done"
            }
        }
    }
}
$rows | Export-Csv -Path $summaryCsv -NoTypeInformation
Write-Host "Updated: $summaryCsv"

# Update metrics summary
$metricsPath = Join-Path $outDir "7-Metrics-Summary.csv"
if (Test-Path $metricsPath) {
    $metrics = Import-Csv $metricsPath
    $map = @{
        "Login avg latency" = "RT-01"
        "Admin dashboard avg latency" = "RT-02"
        "Student dashboard avg latency" = "RT-03"
        "Teacher dashboard avg latency" = "RT-04"
    }
    foreach ($m in $metrics) {
        if ($m.'Testing Area' -eq "Response Time Comparison" -and $map.ContainsKey($m.Metric)) {
            $sid = $map[$m.Metric]
            if ($scenarioRuns.ContainsKey($sid)) {
                $allMs = ($results | Where-Object { $_.Scenario -eq $sid }).Ms
                $t = Get-Stats $allMs
                $m.'BEFORE Value' = $t.Avg
                if ($m.Metric -like "*p95*") {
                    $m.'BEFORE Value' = $t.P95
                }
                $m.Notes = "BEFORE $sid n=$($t.N)"
            }
        }
    }
    # Fix p95 rows separately
    foreach ($sid in @("RT-01", "RT-02", "RT-03", "RT-04")) {
        if ($scenarioRuns.ContainsKey($sid)) {
            $allMs = ($results | Where-Object { $_.Scenario -eq $sid }).Ms
            $t = Get-Stats $allMs
            $label = switch ($sid) {
                "RT-01" { "Login p95 latency" }
                "RT-02" { "Admin dashboard p95 latency" }
                "RT-03" { "Student dashboard p95 latency" }
                "RT-04" { "Teacher dashboard p95 latency" }
            }
            $p95Row = $metrics | Where-Object { $_.Metric -eq $label }
            if ($p95Row) {
                $p95Row.'BEFORE Value' = $t.P95
                $p95Row.Notes = "BEFORE $sid n=$($t.N)"
            }
            $avgLabel = switch ($sid) {
                "RT-01" { "Login avg latency" }
                "RT-02" { "Admin dashboard avg latency" }
                "RT-03" { "Student dashboard avg latency" }
                "RT-04" { "Teacher dashboard avg latency" }
            }
            $avgRow = $metrics | Where-Object { $_.Metric -eq $avgLabel }
            if ($avgRow) {
                $avgRow.'BEFORE Value' = $t.Avg
                $avgRow.Notes = "BEFORE $sid n=$($t.N)"
            }
        }
    }
    $metrics | Export-Csv -Path $metricsPath -NoTypeInformation
    Write-Host "Updated: $metricsPath"
}

Write-Host "Done - $Target @ $BaseUrl"

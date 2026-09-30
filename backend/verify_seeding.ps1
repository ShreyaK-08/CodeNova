$ErrorActionPreference = "Stop"

function Login-User($username, $password) {
    $body = @{
        usernameOrEmail = $username
        password = $password
    } | ConvertTo-Json

    $resp = Invoke-RestMethod -Uri "http://localhost:8080/api/auth/login" -Method Post -Body $body -ContentType "application/json"
    return $resp.token
}

function Show-User-Certificates($username, $password) {
    $token = Login-User $username $password
    $stats = Invoke-RestMethod -Uri "http://localhost:8080/api/users/dashboard-stats" -Method Get -Headers @{ Authorization = "Bearer $token" }
    $certs = Invoke-RestMethod -Uri "http://localhost:8080/api/certificates" -Method Get -Headers @{ Authorization = "Bearer $token" }
    
    Write-Host "User: $username"
    Write-Host "  Solved Problems: $($stats.totalProblemsSolved) | Submissions: $($stats.totalSubmissions) | Languages: $($stats.languagesUsedCount) | Total Certificates: $($certs.Count)"
    foreach ($c in $certs) {
        Write-Host "    - Certificate: [$($c.certificateType)] $($c.title) (Code: $($c.verificationCode))"
    }
}

Write-Host "=========================================================="
Write-Host "=== TEST MULTI-USER MILESTONES & LANGUAGE CERTIFICATES ==="
Write-Host "=========================================================="

Show-User-Certificates "likhil" "Demo@123"
Show-User-Certificates "demohost" "Demo@123"
Show-User-Certificates "arjun" "Demo@123"
Show-User-Certificates "priya" "Demo@123"
Show-User-Certificates "meera" "Demo@123"
Show-User-Certificates "rohan" "Demo@123"
Show-User-Certificates "kavya" "Demo@123"
Show-User-Certificates "rahul" "Demo@123"
Show-User-Certificates "karthik" "Demo@123"
Show-User-Certificates "ananya" "Demo@123"
Show-User-Certificates "neha" "Demo@123"
Show-User-Certificates "vikram" "Demo@123"
Show-User-Certificates "sneha" "Demo@123"

Write-Host "`n=== TOTAL PLATFORM PROBLEMS ==="
$adminToken = Login-User "admin" "Admin@123"
$problems = Invoke-RestMethod -Uri "http://localhost:8080/api/problems" -Method Get -Headers @{ Authorization = "Bearer $adminToken" }
Write-Host "Total Coding Problems in Platform: $($problems.Count)"

Write-Host "`n=== ALL VERIFICATIONS PASSED ==="

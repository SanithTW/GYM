$source = "d:\GYM\src\main\resources\templates\diet_plan.html"
$content = Get-Content $source -Raw
if ($content -match '(?s)(<aside[^>]*>.*?</aside>)') {
    $aside = $matches[1]
    
    $files = @(
        "d:\GYM\src\main\resources\templates\member_dashboard.html",
        "d:\GYM\src\main\resources\templates\Member_logout.html",
        "d:\GYM\src\main\resources\templates\member_payment_history.html",
        "d:\GYM\src\main\resources\templates\member_progress.html",
        "d:\GYM\src\main\resources\templates\member_sessions.html",
        "d:\GYM\src\main\resources\templates\exercises.html",
        "d:\GYM\src\main\resources\templates\Cycling Details.html",
        "d:\GYM\src\main\resources\templates\CrossFit Details.html",
        "d:\GYM\src\main\resources\templates\Boxing rings Details.html",
        "d:\GYM\src\main\resources\templates\announcements.html",
        "d:\GYM\src\main\resources\templates\Featured Exercises.html",
        "d:\GYM\src\main\resources\templates\Flexibility & Yoga Details.html",
        "d:\GYM\src\main\resources\templates\HIIT Workouts Details.html",
        "d:\GYM\src\main\resources\templates\Popular Workouts.html",
        "d:\GYM\src\main\resources\templates\popular-categories.html",
        "d:\GYM\src\main\resources\templates\profile.html",
        "d:\GYM\src\main\resources\templates\Strength Training Details.html",
        "d:\GYM\src\main\resources\templates\schedule.html",
        "d:\GYM\src\main\resources\templates\User-Settings.html"
    )

    $activeClass = "bg-gradient-to-r from-purple-600 to-blue-600 text-white"
    $inactiveClass = "text-gray-300 hover:bg-white/10 transition-all duration-300"
    
    foreach ($f in $files) {
        if (Test-Path $f) {
            $fcontent = Get-Content $f -Raw
            if ($fcontent -match '(?s)<aside.*?</aside>') {
                $basename = Split-Path $f -Leaf
                
                $targetHref = ""
                switch ($basename) {
                    "member_dashboard.html" { $targetHref = "/user/dashboard" }
                    "member_sessions.html" { $targetHref = "/member/sessions" }
                    "member_progress.html" { $targetHref = "/member/progress" }
                    "member_payment_history.html" { $targetHref = "/user/payment-history" }
                    "profile.html" { $targetHref = "/user/profile" }
                    "schedule.html" { $targetHref = "/user/schedule" }
                    "exercises.html" { $targetHref = "/exercises" }
                    "announcements.html" { $targetHref = "/user/announcements" }
                    "User-Settings.html" { $targetHref = "/user/settings" }
                }

                $newAside = $aside.Replace($activeClass, $inactiveClass)
                
                if ($targetHref -ne "") {
                    $pattern = "(?i)(<a[^>]*href=`"" + [regex]::Escape($targetHref) + "`"[^>]*)text-gray-300 hover:bg-white/10 transition-all duration-300([^>]*>)"
                    $replacement = "`${1}$activeClass`$2"
                    $newAside = [regex]::Replace($newAside, $pattern, $replacement)
                }

                # We need to preserve the group correctly in replace
                $fcontent = $fcontent -replace '(?s)<aside[^>]*>.*?</aside>', ($newAside -replace '\$', '$$$$')
                Set-Content -Path $f -Value $fcontent -Encoding UTF8
                Write-Host "Updated $f"
            } else {
                Write-Host "No aside found in $f"
            }
        } else {
            Write-Host "File not found $f"
        }
    }
} else {
    Write-Host "Error finding aside"
}

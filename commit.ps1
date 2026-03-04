# commit.ps1 - Quick commit helper
# Usage: .\commit.ps1 "commit message"

param(
    [Parameter(Mandatory=$true)]
    [string]$Message
)

git add -A
git commit -m "$Message`n`nCo-Authored-By: Claude Opus 4.6 <noreply@anthropic.com>"
git status

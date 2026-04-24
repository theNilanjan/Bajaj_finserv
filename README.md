# Quiz Leaderboard System

This project is my submission for the SRM Java internship assignment.

The task was to call the quiz API 10 times, collect all the events, remove duplicate entries using `roundId + participant`, calculate the final score for each participant, prepare the leaderboard, and submit it once.

## What I used

I wrote this in Java 21.  
I kept it simple and did not use any external libraries.

## How my logic works

For every poll response, I read all events and create one unique key like this:

```text
roundId|participant
```

If the same participant entry for the same round appears again in another poll, I skip it.  
After removing duplicates, I add the scores participant-wise and sort the leaderboard by total score in descending order.

## Project files

```text
src/
scripts/
README.md
```

Main code is inside `src/com/srm/quiz/`.

## How to run

Open the terminal in this project folder and run:

```powershell
.\scripts\test.ps1
.\scripts\run.ps1 -RegNo RA2311033010002
```

If you only want to test the logic and avoid final submission:

```powershell
.\scripts\run.ps1 -RegNo RA2311033010002 -SkipSubmit
```

If script execution is blocked in PowerShell, run:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 -RegNo RA2311033010002
```

## Output

After execution, output files are created inside the `output` folder.

- `poll-audit.log`
- `leaderboard.json`
- `submission-response.json`

## Extra note

- The required 5 second delay between polls is maintained.
- I also added a small smoke test to verify the deduplication and score calculation logic before running the actual API flow.

## GitHub repo

Repo link:

```text
https://github.com/theNilanjan/Bajaj_finserv.git
```

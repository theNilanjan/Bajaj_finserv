# Quiz Leaderboard System

This is my solution for the SRM Java internship assignment.

The program:

- polls the validator API 10 times using `poll=0` to `poll=9`
- waits 5 seconds between calls
- removes duplicate events using `roundId + participant`
- calculates each participant's total score
- sorts the leaderboard by total score in descending order
- computes the final total score
- submits the leaderboard once

## Project structure

```text
src/
  com/srm/quiz/
scripts/
README.md
```

## Tech choice

I kept this as a plain Java 21 project without external dependencies so it is easy to review and run in a basic environment.

## How the duplicate handling works

Each event is converted into a unique key in this format:

```text
roundId|participant
```

If the same event appears again in another poll response, it is ignored.

## How to run

Open PowerShell in the project folder and run:

```powershell
.\scripts\test.ps1
.\scripts\run.ps1 -RegNo RA2311033010002
```

If you want to test without submitting:

```powershell
.\scripts\run.ps1 -RegNo RA2311033010002 -SkipSubmit
```

If PowerShell blocks local scripts on your machine, use:

```powershell
powershell -ExecutionPolicy Bypass -File .\scripts\test.ps1
powershell -ExecutionPolicy Bypass -File .\scripts\run.ps1 -RegNo RA2311033010002
```

## Output files

After running, the program writes files inside `output/`:

- `poll-audit.log`
- `leaderboard.json`
- `submission-response.json`

## Notes

- The assignment mentions a mandatory 5 second delay, so the program keeps that by default.
- The register number is passed as an argument so the same code can be reused easily.
- A local smoke test is included to verify the deduplication and score aggregation logic.

## GitHub submission steps

Create a new GitHub repository and then run:

```powershell
git init
git add .
git commit -m "Add SRM quiz leaderboard assignment solution"
git branch -M main
git remote add origin https://github.com/YOUR_USERNAME/Bajaj_finserv.git
git push -u origin main
```

Public repo format expected in the form:

```text
https://github.com/your-username/your-repo.git
```

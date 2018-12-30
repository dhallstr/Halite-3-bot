@ECHO OFF
FOR /F "tokens=* USEBACKQ" %%F IN (`python convert.py %*`) DO (
SET var=%%F
)
ECHO ..\halite3.exe --replay-directory ..\replays\ --no-logs %var%
..\halite3.exe --replay-directory ..\replays\ --no-logs %var%

REM dangerous, delete this line if running other java processes.
taskkill /f /im java.exe
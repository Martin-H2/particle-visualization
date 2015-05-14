@echo off & setlocal EnableDelayedExpansion
rem SAVE BATCHFILE AS: UTF-8 (without BOM)
rem ====VARS =============================================================================================================
title ^>^> Mmpld Visualizer ^<^<
set "batPath=%~dps0"
set "DebugMode=0"
set "WaitOnFinish=1"
set "iniFile=settings.ini"



rem ====MAIN =============================================================================================================
call :envrnmnt

rem call :parseIniFile
rem call :reloadAdminRights



if /i "%~x1" NEQ ".MMPLD" (
	call :error "mmpld file needed as parameter"
) else (
	start "" /b /high /wait java -Djava.library.path="natives/" -Xmx4G -jar "mmpld_visualizer.jar" "%~dpnx1"
)










rem ====SUBR ==============================================================================================================
if "%WaitOnFinish%" == "1" (
	echo.
	echo ...All Done.  [ press any key ]
	pause>nul
)
exit

:envrnmnt
echo %random%>nul
set "bitArchitecture=64"
if /i "%PROCESSOR_ARCHITECTURE%" EQU "x86" (
	if not defined PROCESSOR_ARCHITEW6432 (
		set "bitArchitecture=32"
	)
)
if "%bitArchitecture%" EQU "32" (
	set "WowNode="
	chcp 1250
) else (
	set "WowNode=\Wow6432Node"
	chcp 65001
)
if "%DebugMode%" == "0" (
	set "debug=rem"
) else ( set "debug=echo" )
cd /d "%batPath%"
cls & color 78
echo. & echo.
GOTO :EOF

:standby
rundll32.exe powrprof.dll,SetSuspendState 0
GOTO :EOF

:off
shutdown -f -s -t 0
GOTO :EOF

:reloadAdminRights
reg add HKLM /F 1>NUL 2>NUL
if '%errorlevel%' == '0' (goto :EOF)
echo Set UAC = CreateObject^("Shell.Application"^) > "%temp%\getadmin.vbs"
echo UAC.ShellExecute "cmd.exe", "/C %~s0", "", "runas", 1 >> "%temp%\getadmin.vbs"
cscript "%temp%\getadmin.vbs" //nologo
exit

:parseIniFile
for /f "delims=" %%l in ('type %iniFile%') do (set "%%l")
GOTO :EOF

:loadUserFolderVars
call :GetRegSZ "personalFolder" "HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" "Personal"
call :GetRegSZ "startMenuFolder" "HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" "Start Menu"
call :GetRegSZ "startupFolder" "HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" "Startup"
call :GetRegSZ "desktopFolder" "HKCU\Software\Microsoft\Windows\CurrentVersion\Explorer\Shell Folders" "Desktop"
GOTO :EOF

:shorten
set "short=%~s1"
set "shortPath=%~dps1"
set "shortFile=%~nxs1"
GOTO :EOF

:error
	color 74
	echo. & echo *** ERROR: %*
	echo.
	echo ...process aborted.  [press any key]
	pause>nul
	exit
GOTO :EOF

:hint
	set "WaitOnFinish=1"
	echo. & echo *** HINT: %*
	echo.
GOTO :EOF

:update
xcopy "%~s1" "%~s2" /d /i /s /k /r /y /h
goto :EOF

:GetRegSZ
rem call :GetRegSZ "myHLPath" "HKCU\SOFTWARE\Valve\Half-Life" "InstallPath"
Set "%~1=NULL"
FOR /F "tokens=* skip=2" %%i in ('reg query "%~2" /v "%~3"') do (
	set "line=%%i"
	%debug% !line!
	set "line=!line:    =|!"
	set "line=!line:	=|!"
	%debug% !line!
	FOR /F "tokens=2* delims=|" %%j in ("!line!") do (
		if /i "%%j" EQU "REG_SZ" (
			set "%~1=%%k"
		)
	)
)
goto :EOF

:SetRegSZ
rem call :SetRegSZ "%cd%" "HKLM\SOFTWARE%WowNode%\Blizzard Entertainment\World of Warcraft" "InstallPath"
set "data=%~1???"
set "data=%data:\???=%"
set "data=%data:???=%"
reg add "%~2" /v "%~3" /t REG_SZ /f /d "%data%">nul
goto :EOF

:sleep
ping -n %~1 localhost>nul
goto :EOF

:exitOnApp
tasklist |find /i "%~1" >nul
if %errorlevel% == 0 (
	%debug% found %~1 & pause
	exit
)
set "aps=%aps%, %~1"
GOTO :EOF

:exitOnPC
ping -n 1 -w 100 "%~1" >nul
if %errorlevel% == 0 (
	%debug% found %~1 & pause
	exit
)
set "pcs=%pcs%, %~1"
GOTO :EOF

:getPowerCfgWin7
for /f "tokens=1-10 delims=(): " %%a in ('powercfg -q') do (
	if "%%a %%b %%c" equ "GUID der Energieeinstellung" (
		set "varName=%%e_%%f_%%g_%%h_%%i_%%j#"
		set "varName=!varName:-=_!"
		set "varName=!varName:_#=#!"
		set "varName=!varName:_#=#!"
		set "varName=!varName:_#=#!"
		set "varName=!varName:_#=#!"
		set "varName=!varName:_#=#!"
		set "varName=pwrVar_!varName:#=!"
	)
	if "%%a %%b %%c %%d" equ "Index der aktuellen Wechselstromeinstellung" (
		set /a "!varName!_AC=%%e"
	)
	if "%%a %%b %%c %%d" equ "Index der aktuellen Gleichstromeinstellung" (
		set /a "!varName!_DC=%%e"
	)
)
GOTO :EOF

:standbyDisable
call :getPowerCfgWin7
set /a "standby_Timeout_AC_Backup= %pwrVar_Deaktivierung_nach_AC% / 60"
powercfg -x -standby-timeout-ac 0
GOTO :EOF

:standbyEnable
if "%standby_Timeout_AC_Backup%" EQU "0" (
	set "standby_Timeout_AC_Backup=120"
)
powercfg -x -standby-timeout-ac %standby_Timeout_AC_Backup%
GOTO :EOF

:selectLineFrom
set /a lineNumber=0
for /F "delims=" %%f in ('type %~1') do (
	set /a lineNumber=lineNumber + 1
	set line!lineNumber!=%%f
	if "%~2"=="" (echo  !lineNumber! = %%f)
)
if "%~2" NEQ "" (
	if "%~2"=="/random" (
		set /a "input=1+%lineNumber%*!random!/32768"
	) else (
		set /a "input=%~2"
	)
	goto noui
)
echo.
echo select by entering number: 
echo.
set /p input=
if %input% GTR %lineNumber% goto selectLineFrom
if %input% LSS 1 goto selectLineFrom
:noui
set "selection=!line%input%!"
GOTO :EOF



:getSysLanguage
	call :GetRegSZ "languageCode" "HKLM\SYSTEM\CurrentControlSet\Control\Nls\Language" "Installlanguage"
	set "systemLanguage=null"
	if "%languageCode%" EQU "0409" ( set "systemLanguage=ENG" )
	if "%languageCode%" EQU "0407" ( set "systemLanguage=GER" )
GOTO :EOF



:getScreenHeight
FOR /F "skip=1" %%L IN ('wmic desktopmonitor where availability^=3 get screenHeight') DO (
	set "screenHeight=%%L"
	GOTO :EOF
)

:getScreenWidth
FOR /F "skip=1" %%L IN ('wmic desktopmonitor where availability^=3 get screenWidth') DO (
	set "screenWidth=%%L"
	GOTO :EOF
)


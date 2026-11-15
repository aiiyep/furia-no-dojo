@echo off
rem Compila todas as classes (model, view, controller) e inicia o jogo.
cd /d "%~dp0"
if exist bin rmdir /s /q bin
dir /s /b src\*.java > fontes.txt
javac -encoding UTF-8 -d bin @fontes.txt
if errorlevel 1 (
    del fontes.txt
    echo.
    echo Erro de compilacao. Verifique se o JDK esta instalado.
    pause
    exit /b 1
)
del fontes.txt
java -cp bin Main

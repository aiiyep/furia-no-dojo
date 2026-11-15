@echo off
rem Compila todas as classes (model, view, controller) e inicia o jogo.
rem Usa caminhos relativos para funcionar mesmo em pastas com acentos (ex.: "Area de Trabalho").
cd /d "%~dp0"
if exist bin rmdir /s /q bin
javac -encoding UTF-8 -d bin src\Main.java src\model\*.java src\view\*.java src\controller\*.java
if errorlevel 1 (
    echo.
    echo Erro de compilacao. Verifique se o JDK esta instalado.
    pause
    exit /b 1
)
start "" javaw -cp bin Main

@echo off
cd /d C:\Users\cmartinezs\IdeaProjects\keygo-server
echo Starting keygo-run with local,h2 profiles...
echo.
call mvnw.cmd -pl keygo-run spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local,h2 --spring.datasource.url=jdbc:h2:file:./db/keygo-test-temp;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=VALUE" -DskipTests
echo.
echo Exit code: %ERRORLEVEL%
pause

@echo off
cd /d C:\Users\cmartinezs\IdeaProjects\keygo-server
timeout /t 2
call mvnw.cmd -pl keygo-run spring-boot:run -Dspring-boot.run.arguments="--spring.profiles.active=local,h2 --spring.datasource.url=jdbc:h2:file:./db/keygo-local-test;DB_CLOSE_DELAY=-1;MODE=PostgreSQL;NON_KEYWORDS=VALUE" -q -DskipTests

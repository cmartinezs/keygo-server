SHELL := /bin/bash

MVNW ?= ./mvnw
COMPOSE ?= docker compose
ENV_FILE ?= .env

.PHONY: help bootstrap deps up down reset logs test build validate doctor

help:
	@printf '%s\n' \
	  'KeyGo developer commands' \
	  '' \
	  '  make bootstrap  Restore or initialize .env safely' \
	  '  make deps       Validate required local tooling' \
	  '  make up         Start local PostgreSQL + MailHog' \
	  '  make down       Stop local dependencies' \
	  '  make reset      Recreate local dependencies and data volumes' \
	  '  make logs       Follow local dependency logs' \
	  '  make test       Run the canonical local test suite' \
	  '  make build      Build the deployable artifact without re-running tests' \
	  '  make validate   Run the canonical repository validation gate' \
	  '  make doctor     Fast, side-effect-safe repository health check'

bootstrap:
	@./scripts/bootstrap-env.sh

deps:
	@./scripts/deps.sh

up:
	@$(COMPOSE) --env-file $(ENV_FILE) up -d postgres mailing

# Spring Boot application may run directly on the host for the fast dev loop.
# Infrastructure consistency is provided by Compose.
down:
	@$(COMPOSE) --env-file $(ENV_FILE) down

reset:
	@$(COMPOSE) --env-file $(ENV_FILE) down -v
	@$(COMPOSE) --env-file $(ENV_FILE) up -d postgres mailing

logs:
	@$(COMPOSE) --env-file $(ENV_FILE) logs -f postgres mailing

test:
	@$(MVNW) test --no-transfer-progress

build:
	@$(MVNW) clean package -DskipTests --no-transfer-progress

validate:
	@$(MVNW) verify --no-transfer-progress
	@./scripts/doctor.sh

doctor:
	@./scripts/doctor.sh

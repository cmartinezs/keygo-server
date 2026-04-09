# Dashboard SQL Rules

## Location
docs/sql/platform_dashboards/

## Structure

platform_dashboards/
keygo_admin/
keygo_account_admin/
keygo_user/

## Rule

Every aggregate MUST have detail.

Example:

aggregate:
- failed_logins_count.sql

detail:
- failed_logins_detail.sql

## Parameters

- :from_ts
- :to_ts
- :tenant_id
- :contractor_id
- :platform_user_id

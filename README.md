# Slack HR Notifier API 

A self-managing hiring board built in Oracle PL/SQL with a Spring Boot consumer for Slack delivery. HR does one thing — post a job. Everything else (notifying, expiring, archiving, clearing, tidying descriptions, counting applicants, and posting to Slack) happens automatically.

## What it does

1. **Post a job** → a row goes into `vacant_jobs`.
2. **A trigger fires automatically** → a "new post" notification is created, and the job is copied into `jobs_posting_archive`.
3. **Edit a description** → it's automatically converted to lowercase, and an "update" notification is logged.
4. **Time passes** → a scheduled job marks the post inactive once `expires_at` is reached, then removes it from `vacant_jobs`.
5. **A daily sweep** reduces each job's notifications down to the latest one and stamps it with the current applicant count.
6. **Someone applies** → a `job_applications` row is created, and an "application" notification is logged.
7. **A Spring Boot service** polls the database, pulls any notification not yet sent, posts it to Slack, and marks it sent.

## Requirements

- Oracle Database (tested against Oracle Database Free / FREEPDB1)
- Java 17
- Maven
- A Slack Incoming Webhook URL

## Database setup

Run the SQL script (tables → triggers → procedures → functions → scheduled jobs) against your Oracle instance, in order, using SQL Developer or SQL*Plus.

```sql
@vacancy_loop_schema.sql
```

This creates all four tables (`vacant_jobs`, `notifications`, `job_applications`, `jobs_posting_archive`), the triggers, procedures, the lowercase function, and the four scheduled jobs (expiry watch, archive, description sweep, daily notification roundup).

## Running the Spring Boot app

1. Set your environment variables:

   ```bash
   export DB_USERNAME=system
   export DB_PASSWORD=your_password
   export SLACK_WEBHOOK_URL=your_slack_webhook_url
   ```

2. Start the app:

   ```bash
   mvn clean spring-boot:run
   ```

3. That's it — the app polls for pending notifications automatically and posts them to Slack. No manual request needed.

   (There's also a manual endpoint if you want to trigger a drain on demand: `POST http://localhost:8080/notifications/drain`)

## Quick test

```sql
INSERT INTO vacant_jobs (job_name, description, expires_at)
VALUES ('Backend Developer', 'Build APIs in Spring Boot', SYSTIMESTAMP + 3);
COMMIT;
```

Within a few seconds, a "new post" notification should appear in your Slack channel.

## Notes

- Credentials are never hardcoded — they're read from environment variables at runtime.
- `notifications.job_id` references `jobs_posting_archive`, not `vacant_jobs`, so notification history survives even after a job is deleted from the active board.

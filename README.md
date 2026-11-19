# Supabase student registration email integration

This Spring Boot service receives Supabase Database Webhook events for inserts
into the `public.Students` table. It sends the inserted row's `email` a message
from `hr@valoriatech.page` with the subject `Internship Registration
Confirmation` and the Valoria Technology internship registration confirmation
message.

The Supabase REST URL is not used for database polling. Configure a Database
Webhook to call this service so each insert is delivered as an event.

## Requirements

- Java 21
- Maven 3.6.3 or later
- A Gmail app password for `valoriatechnology@gmail.com` (Google 2-Step
  Verification must be enabled)

## Configuration

Set these environment variables before starting the service:

| Variable | Description |
| --- | --- |
| `SUPABASE_WEBHOOK_SECRET` | A long random secret shared with the Supabase webhook configuration |
| `SMTP_USERNAME` | `valoriatechnology@gmail.com` |
| `SMTP_APP_PASSWORD` | Gmail app password; do not use the account's normal password |

Optional variables include `SMTP_HOST`, `SMTP_PORT`, `MAIL_FROM`,
`REGISTRATION_EMAIL_SUBJECT`, and `REGISTRATION_EMAIL_MESSAGE`. `MAIL_FROM`
defaults to `hr@valoriatech.page`. If using Gmail SMTP, configure this address
as a verified Gmail send-as alias or set `MAIL_FROM` to the authenticated
Gmail address.

PowerShell example (replace the placeholder values; do not commit them):

```powershell
$env:SUPABASE_WEBHOOK_SECRET = "use-a-long-random-secret"
$env:SMTP_USERNAME = "valoriatechnology@gmail.com"
$env:SMTP_APP_PASSWORD = "your-google-app-password"
mvn spring-boot:run
```

The service listens on port 8081. Publish it at an HTTPS URL reachable by
Supabase before configuring the webhook.

## Supabase webhook setup

1. In the Supabase project dashboard, open **Database → Webhooks** and create a
   webhook for schema `public`, table `Students`, and event `INSERT`.
2. Set the request method to `POST` and URL to
   `https://<your-public-host>/webhooks/supabase/students`.
3. Add the HTTP header `X-Webhook-Secret` with the exact value of
   `SUPABASE_WEBHOOK_SECRET`.
4. Save the webhook and insert a test student row with a valid `email` value.

The endpoint returns `202 Accepted` after the email is submitted to Gmail,
`204 No Content` for non-insert or non-Students events, and an error status for
unauthorized or invalid events. Keep the service URL private to the extent
possible and rotate the webhook secret if it is exposed. Never put a Supabase
`service_role` key in this application.

After an insert, check the Supabase webhook's delivery logs and the Spring Boot
console. The application logs each authenticated webhook's event type/table,
logs when SMTP accepts an email, and logs the exception type if SMTP sending
fails. A `401` usually means the `X-Webhook-Secret` header does not match; a
`500` indicates request processing or SMTP failure. Supabase cannot call
`localhost`; use an HTTPS endpoint reachable from Supabase.

## Tests

```powershell
mvn test
```

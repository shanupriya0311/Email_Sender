# Supabase Student Registration Email Integration

This Spring Boot service receives Supabase Database Webhook events for inserts into the `public.Students` table. It sends the inserted row's `email` a message from `hr@valoriatech.page` with the subject `Internship Registration Confirmation` and the Valoria Technology internship registration confirmation message.

The Supabase REST URL is not used for database polling. Configure a Database Webhook to call this service so each insert is delivered as an event.

## Requirements

- Java 21
- Maven 3.6.3 or later
- A Gmail app password for `valoriatechnology@gmail.com` (Google 2-Step Verification must be enabled)

## Configuration

Set these environment variables before starting the service:

| Variable | Description |
|---|---|
| `SUPABASE_WEBHOOK_SECRET` | A long random secret shared with the Supabase webhook configuration |
| `SMTP_USERNAME` | `valoriatechnology@gmail.com` |
| `SMTP_APP_PASSWORD` | Gmail app password; do not use the account's normal password |

Optional variables include `SMTP_HOST`, `SMTP_PORT`, `MAIL_FROM`, `REGISTRATION_EMAIL_SUBJECT`, and `REGISTRATION_EMAIL_MESSAGE`. `MAIL_FROM` defaults to `hr@valoriatech.page`. If using Gmail SMTP, configure this address as a verified Gmail send-as alias or set `MAIL_FROM` to the authenticated Gmail address.

## Run the Spring Boot Application

Open PowerShell in the project directory.

Set the environment variables:

```powershell
$env:SUPABASE_WEBHOOK_SECRET = "YOUR_WEBHOOK_SECRET"
$env:SMTP_USERNAME = "valoriatechnology@gmail.com"
$env:SMTP_APP_PASSWORD = "YOUR_GMAIL_APP_PASSWORD"
$env:MAIL_FROM = "hr@valoriatech.page"
```

Build the application:

```powershell
mvn clean package -DskipTests
```

Start the Spring Boot application:

```powershell
java -jar target\email-sender-0.0.1-SNAPSHOT.jar
```

The application listens on port `8081`.

## Test the Supabase Webhook Using cURL

After starting the application and making your Cloudflare Tunnel URL publicly accessible, execute the following command in CMD:

```cmd
curl -i -X POST "https://applied-hire-sitting-kruger.trycloudflare.com/webhooks/supabase/students" -H "Content-Type: application/json" -H "X-Webhook-Secret: YOUR_WEBHOOK_SECRET" -d "{\"type\":\"INSERT\",\"table\":\"Students\",\"record\":{\"email\":\"hr@valoriatech.page\"}}"
```

Replace `YOUR_WEBHOOK_SECRET` with the exact value of `SUPABASE_WEBHOOK_SECRET`.

Expected response:

```text
HTTP/1.1 202
```

A `202 Accepted` response means the webhook was accepted and the email sending operation completed without a reported SMTP exception. Check your recipient inbox and Spring Boot console logs.

## Supabase Webhook Setup

1. In the Supabase project dashboard, open **Database → Webhooks** and create a webhook for schema `public`, table `Students`, and event `INSERT`.
2. Set the request method to `POST` and URL to:

   `https://applied-hire-sitting-kruger.trycloudflare.com/webhooks/supabase/students`

3. Add the HTTP header `X-Webhook-Secret` with the exact value of `SUPABASE_WEBHOOK_SECRET`.
4. Save the webhook and insert a test student row with a valid `email` value.

## Troubleshooting

- `401 Unauthorized`: The webhook secret does not match.
- `500 Internal Server Error`: Request processing or SMTP failure.
- `202 Accepted`: The request was accepted and SMTP sending completed without an exception.
- Supabase cannot call `localhost`; use an HTTPS endpoint reachable from Supabase.

After an insert, check the Supabase webhook delivery logs and Spring Boot console.

Keep the service URL private to the extent possible and rotate the webhook secret if it is exposed. Never put a Supabase `service_role` key in this application.

## Tests

Run the test suite:

```powershell
mvn test
```

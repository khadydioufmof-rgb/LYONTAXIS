# Supabase setup

The complete database schema is in [supabase/schema.sql](supabase/schema.sql).
Run it in the Supabase Dashboard SQL Editor before connecting the production app.
The project reference is configured in [supabase/config.toml](supabase/config.toml).

## Apply the database migration

The Android `anon` key cannot create tables. Use an authenticated Supabase CLI session:

```powershell
supabase login
supabase link --project-ref jxfjcejodjnygkhftzpu
Get-Content .\supabase\schema.sql | supabase db query
```

Alternatively, paste [supabase/schema.sql](supabase/schema.sql) into **Supabase Dashboard > SQL Editor** and select **Run**. Then verify the tables under **Table Editor** and the policies under **Authentication > Policies**.

## Android configuration

Set the public project values in `~/.gradle/gradle.properties` or CI variables:

```properties
SUPABASE_URL=https://your-project.supabase.co
SUPABASE_ANON_KEY=your-public-anon-key
```

Only the `anon` key belongs in the Android app. Never use a `service_role` key in an APK.

Enable the required provider in Supabase Dashboard > Authentication > Providers:

- Email OTP for email login
- Phone OTP and an SMS provider for phone login

The Android client uses:

- `POST /auth/v1/otp` to request a code
- `POST /auth/v1/token?grant_type=otp` to verify a code

The application stays on the current screen when Supabase is not configured or rejects a request.

## Before release

1. Run `supabase/schema.sql` in the Supabase SQL Editor.
2. Configure redirect/provider settings in Supabase.
3. Add rate limits and CAPTCHA/abuse protection in the Supabase dashboard.
4. Session persistence and refresh-token handling are implemented with Android Keystore.
5. Profiles, ride creation, ride history, and notifications are connected to authenticated PostgREST calls; remaining in-memory features still need migration.
6. Add the production Supabase values as protected CI secrets, not as committed files.

## Remaining external services

- Google Maps API key and Maps SDK are required to replace the vector demo map.
- A trusted driver/dispatcher backend must assign rides and update `drivers` and `rides`; never grant those writes to passenger clients.
- A starter Stripe PaymentIntent function is in `supabase/functions/create-payment-intent/index.ts`. Deploy it with `STRIPE_SECRET_KEY` and keep that secret server-side; add a Stripe webhook before marking transactions as succeeded.
- Configure Firebase project credentials and upload `google-services.json` locally/through CI for push delivery.

## Build

The Gradle wrapper files are included. On Windows, install JDK 17, set `JAVA_HOME`, then run:

```powershell
.\gradlew.bat :app:testDebugUnitTest
.\gradlew.bat :app:assembleDebug
```

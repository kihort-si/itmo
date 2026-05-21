# auth

Kotlin/Ktor auth service for MyBrokerApp.

Current MVP scope:
- register
- login
- refresh
- logout current session
- me
- health

Registration creates an auth user with `PENDING_PROVISIONING` status.
Integration with downstream provisioning is intentionally left as a stub for now.

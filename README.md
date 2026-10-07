# User Management — Spring Boot

Java 8 user-management application packaged as a self-contained Spring Boot executable WAR.

## Technology

- Java 8
- Spring Boot 2.7.18
- Spring MVC, Spring Security, Spring Data JPA
- Oracle Database (`ojdbc8`)
- JSP, jQuery and Bootstrap
- BCrypt password hashes

## Database

The default connection is:

```properties
spring.datasource.url=jdbc:oracle:thin:@//localhost:1521/FREEPDB1
spring.datasource.username=PDBADMIN
```

Create the new schema before the first launch. SQL*Plus prompts for the password, so it does not
need to be placed on the command line:

```bash
sqlplus PDBADMIN@//localhost:1521/FREEPDB1 @src/main/resources/db/oracle/schema.sql
```

The schema intentionally does not preserve the legacy reversible AES passwords. New passwords are
stored as BCrypt hashes.

## Build and test

```bash
node --test src/test/js/api-client.test.js
mvn clean verify
```

The build enforces at least 80% line coverage. JSP requires WAR packaging; the resulting WAR still
runs directly with `java -jar`:

```text
target/userprocesses-0.0.1-SNAPSHOT.war
```

## Run

Provide the Oracle password through the environment; never put it in source control:

```bash
read -s DB_PASSWORD
export DB_PASSWORD
java -jar target/userprocesses-0.0.1-SNAPSHOT.war
```

Then open <http://localhost:8080/>.

In IntelliJ IDEA, run `userprocesses.UserManagementApplication` and add `DB_PASSWORD` to the Run
Configuration environment variables. No external Tomcat configuration is required.

JSP views are stored under `src/main/webapp/WEB-INF/jsp` and are resolved through Spring MVC
controllers. They cannot be requested directly through the browser. JSP/JSTL renders the shared
page structure, context-aware URLs, and simple server-side conditions; JavaScript renders API data
and owns table interactions, including a future jqGrid integration. The reusable page shell lives
in `src/main/webapp/WEB-INF/tags/layout/page.tag`; each JSP supplies its page content, styles,
scripts, and page-specific endpoint configuration through the layout's slots.

## User API

- `GET /api/users` — return the authenticated user's account
- `POST /api/users` — register an account and return `201 Created`
- `PUT /api/users/{id}` — update the authenticated user's own account
- `DELETE /api/users/{id}` — delete the authenticated user's own account
- `POST /api/sessions` — authenticate and create a session
- `DELETE /api/sessions/current` — end the authenticated session
- `POST /api/password-reset-requests` — accept a password-reset request with a generic response

All request bodies use `application/json`. Resource creation returns `201`, accepted asynchronous
requests return `202`, and updates and successful deletes return `200`. Every API response uses the
same wrapper; `data` is `null` when no payload is required:

```json
{
  "code": "SUCCESS",
  "msg": "Users retrieved.",
  "data": []
}
```

Errors keep their appropriate HTTP status and use the same structure, for example:

```json
{
  "code": "AUTHENTICATION_REQUIRED",
  "msg": "Authentication is required.",
  "data": null
}
```

All state-changing requests require the CSRF token returned by `GET /api/csrf` in the
`X-CSRF-TOKEN` header.

## Security behavior

- State-changing requests require a CSRF token.
- `/welcome` and user data require an authenticated session.
- A user can read, update, and delete only their own account.
- API responses never include a password or password hash.
- Database errors and internal exception details are not returned to clients.
- The legacy email-password reminder was removed. The compatibility endpoint returns the same
  generic response for every address; token-based password reset delivery is not configured yet.

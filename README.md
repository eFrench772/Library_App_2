# Library Web App
### COMP2850 Miniproject

A web-based library management system built with Kotlin and Ktor.

---

## Quick Start

```bash
./gradlew run
```

Then open **http://localhost:8080** in your browser.

---

## Features

- **Book Search** — Search by title, author, or ISBN. Anyone can search without an account.
- **Book Details** — View cover image, format, location, availability per copy, and notes.
- **User Accounts** — Register with username, email, password, and home address.
- **Borrow & Return** — Logged-in users can borrow available copies and return them via their profile.
- **Reservations** — Users can reserve unavailable books. When a copy is returned, it is automatically assigned to the next user in the queue.
- **User Profile** — View currently borrowed books, due dates, and return books directly.
- **Admin Controls** — Admin users can add, remove, and mark books as returned from the UI. Admin role is assigned via the H2 console.

---

## Tech Stack

| Layer | Technology |
|---|---|
| Language | Kotlin |
| Framework | Ktor (Netty) |
| Database | H2 (file-based, persistent) |
| ORM | Exposed |
| Templating | Pebble |
| Password Hashing | Password4j (Scrypt) |
| CSS | Pico CSS |

---

## Project Structure

```
Library_App_2/
├── library_booklist.csv        # Seed data for books
├── build.gradle.kts
└── src/main/
    ├── kotlin/
    │   ├── Application.kt      # App entry point, module setup
    │   ├── Authentication.kt   # Auth helpers
    │   ├── Database.kt         # DB init and seeding
    │   ├── Query.kt            # All database query functions
    │   ├── Routing.kt          # All routes (GET/POST handlers)
    │   ├── Tables.kt           # Exposed table definitions
    │   ├── Templating.kt       # Pebble config
    │   └── UserSession.kt      # Session data class
    └── resources/
        ├── application.yaml    # Server config (port etc.)
        └── templates/          # Pebble HTML templates
            ├── base.peb
            ├── search.peb
            ├── book.peb
            ├── seeAllBooks.peb
            ├── addBook.peb
            ├── login.peb
            ├── register.peb
            └── profile.peb
```

---

## Database

The app uses an H2 file-based database stored at `./data/library.mv.db`. Data persists between restarts.

**Tables:** `BOOKS`, `USERS`, `LOANS`, `RESERVATIONS`

To inspect the database, use the H2 console:

1. Run the app
2. Go to **http://localhost:8082**
3. Connect with:
   - **JDBC URL:** `jdbc:h2:file:./data/library;AUTO_SERVER=TRUE;`
   - **User Name:** *(leave blank)*
   - **Password:** *(leave blank)*

### Useful SQL

```sql
-- View all users
SELECT * FROM USERS;

-- View all active loans
SELECT * FROM LOANS WHERE RETURNED_DATE IS NULL;

-- View all reservations
SELECT * FROM RESERVATIONS WHERE FULFILLED_DATE IS NULL;

-- Grant admin role to a user
UPDATE USERS SET "role" = TRUE WHERE USERNAME = 'yourusername';
```

> H2 requires quoted lowercase for the `role` column: `"role"`

---

## Default Seed User

On first run, a default user is created:

| Field | Value |
|---|---|
| Username | `one` |
| Password | `one` |
| Role | User (not admin) |

To make yourself an admin, register an account then run the SQL above.

---

## Git Workflow

See `GIT_CHEATSHEET.md` for full details.

```bash
git add .
git commit -m "Your message"
git push origin YourBranch
```
# Movie Rating Database

A relational database and JDBC data-access layer for a movie rating platform: users rate films,
films belong to genres and carry tags, users keep watchlists and receive recommendations, the
system tracks how a film's popularity changes over time, and a procedure rewards users for
rating poorly-rated films in their favourite genre.

Built with **Microsoft SQL Server** (schema, constraints, triggers, stored procedures) and a
**Java + JDBC** layer that implements the assignment's interfaces against it.

> Course project for *Software Tools for Databases* — School of Electrical Engineering,
> University of Belgrade, 2025/2026.

---

## What the database enforces

The interesting part of this project is not the tables — it is the business rules that the
database itself guarantees, so the application cannot leave the data in an inconsistent state.

| Rule | Mechanism | What it does |
|---|---|---|
| `TR_BLOCK_EXTREME` | trigger | Blocks an extreme rating (1 or 10) within a genre once a user has given three such ratings and fewer than three neutral ones |
| `TR_UPDATE_MOVIE_TREND` | trigger | Maintains a movie's status — `Trending`, `Rising`, `Falling` or `Classic` — from its recent rating activity |
| `SP_REWARD_USER_Evaluate` | stored procedure | Rewards a user for rating poorly-rated films from their favourite genre |

On top of those, the schema supports recommendations from a user's favourite genres, thematic
specialisation derived from tags, and a computed user profile (`curious`, `focused`,
`undefined`).

Where the assignment text was ambiguous — for example the exact definition of *Trending*, or
which status takes priority when several apply — the chosen interpretation is documented as a
comment next to the rule in `rd210107.sql`, so the decision can be audited rather than guessed.

---

## Project structure

```
StudentMain.java              entry point: registers implementations, runs the tests
student/
  DBConnection.java           database connection (one shared connection for the program's lifetime)
  rd210107_*Operations.java   one implementation per assignment interface, plain JDBC
rd210107.sql                  full database creation: tables, constraints, triggers, procedures, functions
rd210107-tsql.sql             triggers, procedures and functions only
rd210107.png                  ER diagram
```

Naming is deliberate: `TR_` prefixes triggers, `SP_` prefixes stored procedures, so the object's
role is visible from its name in the schema browser.

---

## Running it

1. Execute `rd210107.sql` on a Microsoft SQL Server instance. This creates the database and all
   of its logic.
2. In `student/DBConnection.java`, set `DEFAULT_URL`, `DEFAULT_USER` and `DEFAULT_PASSWORD`.
   The defaults target `localhost:1433`, database `MovieDB`, user `sa`.
3. Put two jars on the classpath:
   - the JDBC driver, `mssql-jdbc-<version>.jar`
   - the course-provided interfaces and public tests, `SAB_projekat_2026_javni_test.jar`
4. Compile and run `StudentMain.main()`.

---

## Tests

The public test suite shipped with the assignment passes **15/15**.

---

## Design notes

**One shared connection.** `DBConnection` opens a single connection and reuses it for the whole
run instead of a pool. For a single-threaded console program that talks to a locally hosted
database, this keeps the data-access code short and the behaviour predictable. It is not a
pattern to carry into a web application: a shared `Connection` is not thread-safe, and a pool
(or a connection per request) is the correct choice as soon as there is concurrency.

**Logic pushed into the database.** Rules that must hold regardless of which client writes the
data live in triggers and procedures rather than in Java. The application can then be wrong
without corrupting the data — the constraint fires either way.

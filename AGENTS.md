# AGENTS.md

## Project purpose

This repository contains a backend project for a League of Legends statistics
tracker.

The main goal of the project is educational: learning modern backend development
while building a realistic portfolio project.

The application will eventually collect, process, store, and expose League of
Legends player and match statistics.

## Developer context

The developer is a computer science student after the first year of university.

They already know programming fundamentals, Git, and basic software engineering,
but are learning professional backend development.

Do not explain trivial programming concepts unless asked.

At the same time, do not hide important backend concepts behind generated code.

## Working style

Prefer small, incremental changes.

When introducing an important architectural or technical decision, explain:
- what problem it solves,
- why this solution is appropriate,
- what reasonable alternatives exist.

Do not make major architectural decisions without discussing them first.

Do not implement large features ahead of the current task.

Avoid unnecessary abstractions and dependencies.

Prefer production-quality practices even though this is a learning project.

## Code quality

Write clear and maintainable code.

Prefer simple solutions over clever ones.

Follow established Java and Spring conventions once the project is configured.

Keep classes and methods focused on a single responsibility.

Avoid premature optimization.

Avoid premature abstraction.

Add tests for meaningful application logic.

Do not add dependencies unless they provide a clear benefit.

## Learning objective

When working on a task, optimize not only for completing the task but also for
helping the developer understand the backend concepts involved.

If Codex generates or modifies non-trivial code, briefly explain the important
design choices afterward.

Do not replace learning opportunities with large unexplained code generation.

## First frontend milestone checkpoint

After the first small frontend displays real player data and match history,
and before its commit, review the backend and desktop frontend code written so far
with the developer. Add or improve concise educational comments explaining
class/component responsibilities, data flow, non-obvious mappings, and error
handling. Keep existing useful comments and avoid narrating every line or
commenting generated files and dependencies. This checkpoint was completed for
the first prototype; keep these comments current as the implementation evolves.

## Git

Keep changes focused on the current task.

Do not mix unrelated changes in the same commit.

Use clear commit messages.

Suggested commit style:

feat: add player lookup endpoint
fix: handle missing Riot account
test: add player service tests
refactor: extract Riot API client
docs: update API documentation
chore: configure development environment

Do not commit secrets, API keys, credentials, or local environment files.

## Current technical direction

Planned backend stack:
- Java
- Spring Boot

Exact Java version, build tool, database, libraries, architecture, and external
integrations should be decided incrementally rather than assumed.

The Riot Games API will likely be used later, but API keys must never be
committed to the repository.

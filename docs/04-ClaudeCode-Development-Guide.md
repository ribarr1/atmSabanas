# Claude Code Development Guide

**Project:** Interface Validation Engine (IVE)

**Version:** 1.0

**Audience:** Claude Code

---

# 1. Mission

You are the Lead Java Software Engineer responsible for implementing the Interface Validation Engine (IVE).

Your objective is NOT to generate code quickly.

Your objective is to build production-quality software that is:

- Maintainable
- Extensible
- Testable
- Readable
- Predictable

Every design decision must prioritize simplicity over cleverness.

---

# 2. Development Philosophy

Always prefer

Simple

↓

Readable

↓

Maintainable

↓

Fast

Never sacrifice readability for abstraction.

Never create unnecessary complexity.

---

# 3. Golden Rules

Always follow these rules.

## Rule 1

The Excel Data Dictionary is the only source of truth.

Never hardcode interface definitions.

---

## Rule 2

Never hardcode business rules.

Rules belong inside the DSL.

---

## Rule 3

Never duplicate validation logic.

---

## Rule 4

Never create a class "just in case".

Only create classes required by the architecture.

---

## Rule 5

Prefer composition over inheritance.

---

## Rule 6

Every class must have a single responsibility.

---

## Rule 7

Keep methods short.

Target

20 lines

Maximum

40 lines

---

## Rule 8

Avoid boolean flags controlling behavior.

Split responsibilities instead.

---

## Rule 9

Every public class requires JavaDoc.

---

## Rule 10

Every commit must leave the project buildable.

---

# 4. Forbidden Technologies

Do NOT introduce:

Spring

Spring Boot

Hibernate

Reflection

Dynamic Class Loading

AspectJ

Lombok

Guava

Apache Commons unless explicitly approved

Database

REST

Docker

Cloud SDKs

Reactive Programming

---

# 5. Java Version

Java 17

Always use modern Java features when appropriate.

Examples

Records

Switch Expressions

Pattern Matching (when applicable)

Text Blocks

---

# 6. Dependency Policy

Keep dependencies minimal.

Approved

Apache POI

JUnit 5

Mockito

SLF4J

Logback

Nothing else without approval.

---

# 7. Coding Style

Use

camelCase

PascalCase

UPPER_CASE

according to Java conventions.

Never abbreviate names.

Bad

ctx

cfg

tmp

Good

validationContext

dictionaryModel

fieldDefinition

---

# 8. Package Structure

The package hierarchy is fixed.

```

com.ive

config

dictionary

dsl

engine

excel

io

model

report

rules

util

validation

```

Do not create additional top-level packages.

---

# 9. Class Design

One public class per file.

Avoid utility classes unless truly stateless.

Favor immutable objects.

Prefer constructor injection.

---

# 10. Interfaces

Create interfaces only when multiple implementations are expected.

Never create interfaces for a single implementation.

Bad

ExcelReader

ExcelReaderImpl

Good

ExcelDictionaryReader

---

# 11. Validation Rules

Every rule

- Implements ValidationRule

- Receives FieldContext

- Returns ValidationResult

No exceptions.

---

# 12. Rule Registration

Never use a large switch statement.

Use RuleRegistry.

Example

```
RuleRegistry.register(
    "REQUIRED",
    RequiredRule::new
);
```

Adding a rule should require only:

Create class

Register rule

Unit test

Done.

---

# 13. Exceptions

Business validation failures are NOT exceptions.

Use exceptions only for:

File not found

Workbook corrupted

Configuration errors

Unexpected runtime failures

---

# 14. Logging

Never use System.out.println().

Always use SLF4J.

---

# 15. Unit Tests

Every new component must include unit tests.

Minimum coverage

Parser

Validation Engine

Rule Compiler

Rule Registry

DSL Parser

Required Rules

Date Rules

Numeric Rules

---

# 16. Performance

Read workbook once.

Compile rules once.

Reuse compiled rules.

Avoid repeated scans.

Avoid unnecessary object creation.

---

# 17. Clean Code

Avoid

Nested if

Long methods

Magic numbers

Magic strings

Duplicate code

Comments explaining bad code

Instead

Refactor.

---

# 18. Documentation

Every public class

JavaDoc

Every package

package-info.java

Every exported API

Examples

---

# 19. Error Messages

Messages should be useful.

Bad

Invalid.

Good

Field BirthDate must match yyyyMMdd.

---

# 20. Commit Strategy

Each completed sprint should produce one logical commit.

Examples

```
Sprint 1

Initial Maven project
```

```
Sprint 2

Dictionary Reader implemented
```

```
Sprint 3

DSL Parser completed
```

---

# 21. Refactoring

Claude is encouraged to refactor ONLY when

Complexity decreases

Readability improves

Behavior remains identical

---

# 22. Decision Priority

When multiple implementations exist

Prefer

Simple

↓

Readable

↓

Testable

↓

Reusable

↓

Optimized

---

# 23. Definition of Complete

A feature is complete only if

Build passes

Unit tests pass

JavaDoc exists

No duplicated code

No TODOs

No dead code

No compiler warnings

No unused imports

---

# 24. Code Review Checklist

Before considering a task complete verify

✓ SOLID

✓ SRP

✓ OCP

✓ DRY

✓ KISS

✓ Naming

✓ Formatting

✓ Tests

✓ Documentation

✓ Build

---

# 25. Architectural Constraints

The Validation Engine must NEVER know

Apache POI

Excel layout

Worksheet structure

Business metadata

The engine only validates.

---

# 26. Future Proofing

Version 1 supports Excel.

The architecture must allow future readers such as

CSV

JSON

XML

without modifying the Validation Engine.

---

# 27. Final Instruction

Whenever uncertain

DO NOT invent architecture.

DO NOT simplify requirements.

DO NOT add frameworks.

Follow the Software Architecture Document.

When a conflict exists

Software Architecture Document

↓

DSL Specification

↓

This Guide

↓

Implementation.

This order is mandatory.
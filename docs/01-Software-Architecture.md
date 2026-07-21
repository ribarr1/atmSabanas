# Software Architecture Document (SAD)

**Project:** Interface Validation Engine (IVE)

**Version:** 1.0

**Status:** Draft

**Language:** Java 17

---

# 1. Purpose

This document defines the complete software architecture for the Interface Validation Engine (IVE).

It serves as the authoritative technical specification for developers implementing the solution.

Every architectural decision described here should be respected during implementation.

---

# 2. Project Vision

IVE is a generic validation engine capable of validating interface files using business rules stored in an Excel Data Dictionary.

The objective is to eliminate hardcoded validations from source code and allow QA analysts to maintain validation rules without modifying Java classes.

The system must remain simple, modular, maintainable and extensible.

---

# 3. Problem Statement

QA teams frequently validate interface files manually.

Typical validations include:

- Required fields
- Data types
- Length
- Numeric values
- Dates
- Allowed values
- Business rules

Those validations are usually duplicated in multiple automation projects.

Maintenance becomes expensive because every rule change requires source code modification.

IVE solves this by reading the validation rules directly from the Data Dictionary.

---

# 4. Goals

The project shall:

- Read an Excel Data Dictionary.
- Detect interface metadata automatically.
- Read interface data files.
- Compile validation rules.
- Validate every field.
- Collect every validation error.
- Update the same Excel file.
- Never stop on the first error.
- Produce a validation summary.

---

# 5. Non Goals

Version 1 intentionally excludes:

- Spring Boot
- Database
- REST API
- HTML Reports
- Reflection
- Plugin Framework
- Dynamic Class Loading
- AI Runtime
- Cloud Services
- CI/CD Integration

Those capabilities may be added in future releases.

---

# 6. Design Principles

The project follows these principles:

- Keep It Simple
- SOLID
- Clean Architecture
- Low Coupling
- High Cohesion
- Single Responsibility
- Open Closed Principle
- Composition over Inheritance

---

# 7. High Level Architecture

```

+--------------------------+
| Excel Data Dictionary |
+------------+-------------+
|
v
+--------------------------+
| Dictionary Reader |
+------------+-------------+
|
v
+--------------------------+
| Rule Compiler |
+------------+-------------+
|
v
+--------------------------+
| Validation Engine |
+------------+-------------+
|
v
+--------------------------+
| Excel Writer |
+--------------------------+

```

---

# 8. Main Components

## Main

Application entry point.

Responsibilities

- Receive execution parameters
- Coordinate execution
- Handle fatal exceptions

---

## DictionaryReader

Responsible for reading the Excel dictionary.

Responsibilities

- Detect worksheet structure
- Detect metadata rows
- Detect rule columns
- Read field definitions

Output

DictionaryModel

---

## RuleCompiler

Transforms textual DSL into executable validation rules.

Input

```

REQUIRED

NUMERIC

DATE(dd/MM/yyyy)

```

Output

CompiledRule objects

Rules are compiled only once.

---

## ValidationEngine

Core component.

Responsibilities

- Validate every row
- Validate every field
- Execute compiled rules
- Collect errors
- Never stop execution

---

## ExcelWriter

Updates the original Excel.

Responsibilities

- Highlight invalid cells
- Write error descriptions
- Mark PASS / FAIL
- Save workbook

---

# 9. Processing Flow

1. Open Excel
2. Detect worksheet structure
3. Read metadata
4. Read rules
5. Compile rules
6. Read interface data
7. Validate rows
8. Collect errors
9. Update Excel
10. Save workbook

---

# 10. Error Strategy

Validation errors are expected.

The engine must never terminate because of validation failures.

Fatal errors include:

- Excel cannot be opened
- TXT file missing
- Invalid worksheet structure
- Corrupted workbook

Business validation errors must be collected and reported.

---

# 11. Rule Execution

Each field may contain multiple rules.

Example

```

REQUIRED
NUMERIC
MAX_LENGTH(10)

```

Execution order:

1. REQUIRED
2. NUMERIC
3. MAX_LENGTH

Execution continues even if one rule fails.

---

# 12. Performance

The engine should:

- Read Excel only once.
- Compile rules only once.
- Reuse compiled rules.
- Minimize object creation.
- Avoid nested workbook scans.

---

# 13. Logging

Use SLF4J + Logback.

Log levels:

INFO

Application flow

WARN

Recoverable problems

ERROR

Fatal problems

DEBUG

Detailed execution

---

# 14. Threading

Version 1 executes sequentially.

No parallel processing.

Future versions may introduce parallel validation.

---

# 15. Package Structure

```

com.ive

config/

dictionary/

dsl/

engine/

excel/

io/

model/

report/

rules/

util/

validation/

```

---

# 16. Rule Strategy

Every validation rule implements:

```

ValidationRule

```

Each rule receives

```

FieldContext

```

Returns

```

ValidationResult

```

---

# 17. Data Flow

```

Excel

↓

DictionaryReader

↓

DictionaryModel

↓

RuleCompiler

↓

Compiled Rules

↓

ValidationEngine

↓

ValidationResult

↓

ExcelWriter

↓

Updated Workbook

```

---

# 18. Architectural Decisions

ADR-001

The Data Dictionary is the single source of truth.

ADR-002

Rules are compiled only once.

ADR-003

Execution never stops on business validation errors.

ADR-004

The original Excel file is updated.

ADR-005

The engine must automatically detect worksheet structure.

ADR-006

No business logic shall exist inside Main.

ADR-007

Business rules shall not contain Apache POI code.

ADR-008

Validation rules must be independent.

---

# 19. Extensibility

Future versions may support:

CSV

JSON

XML

REST

Database

API Validation

Message Queues

without modifying the Validation Engine.

---

# 20. Definition of Success

The project is considered successful when:

✓ Any worksheet can be interpreted automatically.

✓ Rules are compiled.

✓ Every row is validated.

✓ Errors are collected.

✓ Invalid cells are highlighted.

✓ PASS / FAIL is generated.

✓ Original workbook is updated.

✓ The engine remains extensible.

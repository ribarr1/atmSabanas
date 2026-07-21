# Interface Validation Engine (IVE)

> Enterprise Data Interface Validation Engine for QA Automation

## Overview

Interface Validation Engine (IVE) is a Java-based validation framework designed to validate flat-file interfaces (TXT, CSV, etc.) against business rules defined in an Excel Data Dictionary.

The engine was created to automate interface validation activities performed by QA teams during integration and migration testing.

Instead of hardcoding validations into source code, IVE interprets validation rules directly from an Excel Data Dictionary, making the solution flexible, maintainable, and easy to extend.

---

## Main Features

- Excel-driven validation
- No hardcoded interfaces
- Rule-based validation engine
- DSL (Domain Specific Language)
- Business Rule Engine
- Field Validation Engine
- Automatic Excel highlighting
- Error summary generation
- Continue validation even if errors exist
- Extensible architecture
- Java 17
- Apache POI

---

## Current Scope (Version 1)

Supported Input

- Excel Data Dictionary
- TXT Interface File

Supported Output

- Same Excel file updated with:
    - Invalid cells highlighted
    - Error description
    - PASS / FAIL status

---

## Future Versions

### V2

- HTML Reports
- Dashboard
- GPT Rule Assistant

### V3

- CSV
- XML
- JSON
- Database validation

### V4

- REST API
- Jenkins Integration
- Azure DevOps Integration

---

## High Level Architecture

```

TXT File
│
▼

Excel Dictionary
│
▼

Parser
│
▼

Compiled Rules
│
▼

Validation Engine
│
▼

Excel Writer
│
▼

Updated Excel

```

---

## Technology Stack

| Technology | Version |
|------------|---------|
| Java | 17 |
| Maven | Latest |
| Apache POI | Latest |
| JUnit | 5 |
| SLF4J | Latest |
| Logback | Latest |

---

## Project Structure

```

interface-validation-engine

docs/

src/

examples/

test/

pom.xml

README.md

```

---

## Design Principles

- Keep It Simple
- SOLID
- Clean Code
- Low Coupling
- High Cohesion
- Open for Extension
- Closed for Modification

---

## Author

Designed as an enterprise-grade QA Automation validation engine.

Version 1.0

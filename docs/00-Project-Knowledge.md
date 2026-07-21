# Project Knowledge Base

**Project:** Interface Validation Engine (IVE)

**Version:** 1.0

---

# Purpose

This document captures the knowledge acquired during the analysis and design of the Interface Validation Engine.

Unlike the Software Architecture Document, this file explains **why** architectural decisions were made.

This document should evolve during the life of the project.

---

# Project Origin

The project was created to automate the validation of interface files used in QA integration testing.

Historically, every project implemented its own validation logic, resulting in:

- duplicated code
- inconsistent validations
- difficult maintenance
- expensive evolution

IVE was designed to solve this problem by making the Data Dictionary the single source of truth.

---

# Initial Analysis

A real Excel Data Dictionary was analyzed before designing the architecture.

One important discovery was that **worksheets are not structurally identical**.

Different worksheets contain different metadata.

Examples include:

- OBLIGATORIEDAD
- CAMPO
- POSICION
- REGLAS
- Long descriptions
- Sample values

Because of this, the engine must NEVER depend on fixed row numbers.

Instead, worksheet structure must be detected dynamically.

---

# Important Discoveries

## Discovery 1

Worksheets are heterogeneous.

Never assume identical layouts.

---

## Discovery 2

Some worksheets contain a REGLAS column.

Others do not.

The engine must support both.

---

## Discovery 3

Business rules appear in multiple formats.

Future versions should support richer DSL expressions.

---

## Discovery 4

Allowed values sometimes appear as lists rather than formal rules.

The parser should normalize them.

---

## Discovery 5

Validation metadata is significantly more stable than worksheet formatting.

The engine should rely on metadata rather than cell positions.

---

# Architectural Decisions

## ADR-001

Excel is the only source of truth.

Reason

Business users already maintain it.

---

## ADR-002

Rules are compiled only once.

Reason

Performance.

---

## ADR-003

Validation never stops after the first error.

Reason

QA requires a complete defect list.

---

## ADR-004

The original workbook is updated.

Reason

QA analysts already work directly with the Excel file.

---

## ADR-005

Business rules never contain Java.

Reason

Business logic must remain editable without developers.

---

## ADR-006

Validation rules are independent.

Reason

Easy extension.

---

## ADR-007

The Validation Engine knows nothing about Excel.

Reason

Separation of concerns.

---

## ADR-008

Rule registration uses a registry rather than a switch statement.

Reason

Better extensibility.

---

# Lessons Learned

Simple software is easier to maintain than clever software.

The architecture intentionally avoids unnecessary frameworks.

Version 1 focuses on solving one problem extremely well.

---

# Design Philosophy

The project follows these principles:

- KISS
- SOLID
- DRY
- Composition over inheritance
- Convention over configuration
- Explicit over implicit

---

# Current Scope

Supported

- Excel Data Dictionary
- TXT Interface File

Not Supported

- XML
- JSON
- CSV
- REST APIs
- Database validation

These capabilities belong to future releases.

---

# Version Roadmap

## Version 1

Excel

TXT

DSL

Validation Engine

Excel highlighting

---

## Version 2

CSV

JSON

XML

---

## Version 3

REST APIs

Database

---

## Version 4

Dashboard

Reports

AI Assistant

---

# Technical Constraints

Java 17

Apache POI

JUnit 5

SLF4J

No Spring

No Reflection

No Database

No Dynamic Loading

---

# Coding Philosophy

The objective is not to build the most generic framework.

The objective is to build the best validation engine for QA teams.

Generality should never compromise simplicity.

---

# Future Ideas

Potential future enhancements include:

- HTML reports
- PDF reports
- Rule optimizer
- DSL auto-completion
- AI-assisted rule generation
- Validation statistics
- Performance metrics
- Parallel execution
- Plugin system
- Configuration profiles

These ideas are intentionally postponed until Version 2 or later.

---

# Important Reminder

When making implementation decisions:

1. Prefer simplicity.
2. Avoid premature optimization.
3. Never sacrifice readability.
4. Preserve the architecture.
5. The Excel Data Dictionary is always the source of truth.

If a future change conflicts with these principles, revisit the architecture before modifying the implementation.
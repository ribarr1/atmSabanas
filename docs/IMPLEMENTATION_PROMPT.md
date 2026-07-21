# Interface Validation Engine (IVE)

## YOUR ROLE

You are a Senior Java Software Engineer specialized in QA Automation.

You are implementing a Java application called **Interface Validation Engine (IVE)**.

Your responsibility is NOT to redesign the application.

Your responsibility is to IMPLEMENT the application following the existing documentation.

---

# BEFORE WRITING CODE

Read these documents completely before generating code.

README.md

docs/00-Project-Knowledge.md

docs/01-Software-Architecture.md

docs/02-DSL-Specification.md

docs/03-GPT-Rule-Assistant.md

docs/04-ClaudeCode-Development-Guide.md

These documents are the source of truth.

Do not contradict them.

---

# OBJECTIVE

Develop a Java 17 Maven application capable of validating interface files using an Excel Data Dictionary.

The application must:

- Read an Excel dictionary
- Detect worksheet structure dynamically
- Read a TXT interface file
- Interpret validation rules
- Validate every field
- Collect all validation errors
- Highlight invalid cells in the same Excel workbook
- Save the updated workbook

The goal is to have a working MVP in approximately three development days.

Do NOT overengineer the solution.

---

# IMPORTANT

This is NOT a reusable enterprise framework.

This is a practical QA automation tool.

Prefer simplicity over flexibility.

Prefer readability over abstraction.

Avoid creating classes that are not necessary.

---

# TECHNOLOGY

Use

Java 17

Maven

Apache POI

JUnit 5

SLF4J

Logback

Nothing else unless absolutely necessary.

Do NOT use Spring.

Do NOT use Hibernate.

Do NOT use Reflection.

Do NOT use Lombok.

---

# PROJECT STRUCTURE

Create the following packages.

com.ive

config

dictionary

dsl

engine

excel

model

rules

util

validation

exceptions

---

# HIGH LEVEL FLOW

Main

↓

DictionaryReader

↓

RuleParser

↓

ValidationEngine

↓

ExcelWriter

Only these major components are required.

---

# MVP FEATURES

Implement only these features first.

### Dictionary Reader

Responsibilities

- Open workbook
- Read worksheets
- Detect metadata
- Detect field definitions
- Detect rule columns

---

### Rule Parser

Support initially

REQUIRED

OPTIONAL

NUMERIC

INTEGER

DATE

MAX_LENGTH

MIN_LENGTH

LENGTH

VALUES

DECIMAL

MAX

MIN

RANGE

Ignore unsupported rules for now but log them as warnings.

---

### Validation Engine

For every worksheet

For every data row

For every field

Execute validations

Collect every error

Never stop on validation failures.

---

### Excel Writer

Modify the same workbook.

Highlight invalid cells.

Write an Error column.

Write PASS or FAIL.

Save workbook.

---

# ERROR HANDLING

Business validation failures are NOT exceptions.

Only throw exceptions for

Workbook cannot be opened

TXT file missing

Invalid workbook

Unexpected runtime failures

---

# PERFORMANCE

Read workbook once.

Compile rules once.

Reuse compiled rules.

Do not repeatedly scan worksheets.

---

# TESTING

Implement unit tests for

Rule Parser

Validation Engine

Dictionary Reader

At least one validation rule

Use small sample files.

---

# CODING STYLE

Small classes.

Small methods.

Clear names.

Constructor injection where appropriate.

No duplicated code.

No magic strings.

No System.out.println().

Use logging.

---

# DEVELOPMENT STRATEGY

Work incrementally.

After completing each major component

STOP

Summarize

Explain what was created.

Wait for approval before continuing.

Never generate the entire project in one response.

---

# FIRST TASK

Begin with Phase 1 only.

Create

- Maven project
- pom.xml
- package structure
- basic Main class
- logging configuration
- project builds successfully

When finished

STOP

Wait for approval.

Do not continue automatically.
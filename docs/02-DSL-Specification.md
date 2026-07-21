# Domain Specific Language (DSL) Specification

**Project:** Interface Validation Engine (IVE)

**Version:** 1.0

---

# 1. Purpose

The Domain Specific Language (DSL) defines how validation rules are expressed inside the Excel Data Dictionary.

Instead of writing Java code, QA analysts define validation rules using a concise textual syntax.

The Rule Compiler interprets these rules and transforms them into executable validation objects.

The DSL is intentionally simple, readable, and extensible.

---

# 2. Design Goals

The DSL must be:

- Human readable
- Easy to learn
- Independent of Java
- Independent of Excel
- Extensible
- Deterministic
- Easy to parse

---

# 3. General Syntax

Each field may contain one or more validation rules.

Rules are separated by semicolons.

Example

```

REQUIRED;
NUMERIC;
MAX_LENGTH(10)

```

Whitespace is ignored.

The following is equivalent.

```

REQUIRED ; NUMERIC ; MAX_LENGTH ( 10 )

```

---

# 4. Rule Execution Order

Rules execute from left to right.

Example

```

REQUIRED;
NUMERIC;
MAX_LENGTH(10)

```

Execution order

1. REQUIRED
2. NUMERIC
3. MAX_LENGTH

The engine continues executing remaining rules even if one rule fails.

---

# 5. Built-in Rules

## REQUIRED

Description

Field cannot be empty.

Example

```

REQUIRED

```

---

## OPTIONAL

Description

Field may be empty.

Example

```

OPTIONAL

```

---

## NUMERIC

Accepts integers or decimals.

```

NUMERIC

```

---

## INTEGER

Accepts integers only.

```

INTEGER

```

---

## DATE

```

DATE(yyyyMMdd)

```

```

DATE(dd/MM/yyyy)

```

---

## BOOLEAN

Accepts

```

TRUE

FALSE

```

---

## MAX_LENGTH

```

MAX_LENGTH(20)

```

---

## MIN_LENGTH

```

MIN_LENGTH(5)

```

---

## LENGTH

Exact length.

```

LENGTH(12)

```

---

## MAX

```

MAX(9999)

```

---

## MIN

```

MIN(1)

```

---

## RANGE

```

RANGE(1,100)

```

---

## DECIMAL

```

DECIMAL(10,2)

```

Precision

Scale

---

## REGEX

```

REGEX([A-Z]{3}[0-9]{4})

```

---

## VALUES

Allowed values.

```

VALUES(A,B,C,D)

```

---

## STARTS_WITH

```

STARTS_WITH(ABC)

```

---

## ENDS_WITH

```

ENDS_WITH(XYZ)

```

---

## CONTAINS

```

CONTAINS(TEST)

```

---

## NOT_CONTAINS

```

NOT_CONTAINS(ERROR)

```

---

## EMAIL

```

EMAIL

```

---

## URL

```

URL

```

---

## UUID

```

UUID

```

---

# 6. Composite Rules

Multiple validations may be combined.

Example

```

REQUIRED;
DATE(dd/MM/yyyy);
MAX_LENGTH(10)

```

---

# 7. Allowed Value Lists

Example

```

VALUES(
APSC,
FCAL,
FGAR,
FCES
)

```

The parser ignores line breaks.

---

# 8. Error Messages

Each rule has a default message.

Example

| Rule | Default Message |
|------|-----------------|
| REQUIRED | Required field |
| NUMERIC | Invalid numeric value |
| DATE | Invalid date |
| MAX_LENGTH | Maximum length exceeded |
| VALUES | Invalid value |

Future versions may allow custom messages.

---

# 9. Validation Result

Every rule returns

```

ValidationResult

```

Containing

```

success

message

rule

field

value

```

---

# 10. Invalid Examples

```

MAX_LENGTH

```

Missing parameter.

---

```

DATE()

```

Empty format.

---

```

VALUES()

```

No values supplied.

---

```

MAX(ABC)

```

Numeric parameter expected.

---

# 11. Grammar (Simplified)

```

RULE

↓

NAME

↓

PARAMETERS

↓

VALIDATION

```

---

# 12. Rule Compilation

Rules are compiled only once.

```

REQUIRED

↓

RequiredRule()

```

```

DATE(dd/MM/yyyy)

↓

DateRule("dd/MM/yyyy")

```

Compiled rules are reused for every row.

---

# 13. Future Rules

The architecture allows new rules to be added without changing existing code.

Examples

```

IBAN

```

```

PHONE

```

```

CURRENCY

```

```

JSON

```

```

XML

```

```

CPF

```

```

NIT

```

```

RIF

```

```

CHECKSUM

```

---

# 14. Naming Convention

Rules use

UPPER_CASE

Examples

```

MAX_LENGTH

```

```

MIN_LENGTH

```

```

STARTS_WITH

```

---

# 15. Extensibility

Every new DSL rule must:

- Implement ValidationRule
- Register in RuleFactory
- Include documentation
- Include unit tests
- Provide default error message

No other framework component should require modification.

---

# 16. Examples

Customer Name

```

REQUIRED;
MAX_LENGTH(50)

```

Birth Date

```

REQUIRED;
DATE(dd/MM/yyyy)

```

Status

```

VALUES(
ACTIVE,
INACTIVE,
BLOCKED
)

```

Account Number

```

NUMERIC;
LENGTH(10)

```

Amount

```

DECIMAL(12,2);
MIN(0)

```

---

# 17. DSL Philosophy

The DSL is intentionally declarative.

It defines **what** should be validated.

The Validation Engine defines **how** it is validated.

This separation allows business rules to evolve without modifying application logic.
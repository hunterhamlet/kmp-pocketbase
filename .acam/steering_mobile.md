# ACAM v2.1 — Steering Core
## Mobile Architectural Specification — Language & Framework Agnostic

> **Scope:** This document defines the universal architectural principles governing
> all mobile development under the ACAM v2.1 specification. It is agnostic of
> language, framework, and platform. Concrete implementations per ecosystem are
> defined in the framework-specific steering documents.
>
> **Audience:** AI Engineering Agents and Human Developers. Every implementation
> decision must be validated against this document first.

---

## 1. Core Principle — The Dependency Rule

Dependencies between layers point **inward only**. Outer layers know inner layers.
Inner layers **never** know outer layers.

```
Layer 1 (Interface)
    │
    ▼
Layer 2 (Orchestration)      ← active only in Cross-Module Mode
    │
    ▼
Layer 3 (Domain)             ← core, zero external dependencies
    ▲
    │
Layer 4 (Data)
    │
    ▼
Layer 5 (Infrastructure)     ← active only in Cross-Module Mode
    │
    ▼
Layer 6 (Cross-Cutting)      ← active only in Cross-Module Mode
```

**Critical violation:** Any import from Layer 3 toward Layer 1, 4, 5, or 6 is an
architectural violation. It must be rejected without exception.

---

## 2. Layer Topology

### Layer 1 — Interface & Delivery
**Responsibility:** User interface rendering, user interaction capture, and
presentation state management.

**Mandatory contracts:**
- Views only read immutable state exposed by the presentation controller
- User actions are expressed as discrete, explicit events
- Data flow is unidirectional: action → transformation → new state
- Zero knowledge of data sources, network, or business rules

**Prohibitions:**
- Contains no business logic
- Makes no direct calls to data sources
- Does not import types from Layer 4, 5, or 6

---

### Layer 2 — Application / Orchestration *(Cross-Module only)*
**Responsibility:** Coordination of flows involving multiple use cases, complex
cross-module navigation, and synchronization of cross-cutting side effects.

**Mandatory contracts:**
- Combines use cases from Layer 3 to fulfill higher-level client workflows
- Contains no business rules of its own
- Activated only in Cross-Module Mode

**Prohibitions:**
- Does not access data sources directly
- Contains no presentation logic

---

### Layer 3 — Domain *(Core)*
**Responsibility:** Single source of truth for business rules. Contains entities,
use cases, and repository contracts.

**Mandatory contracts:**
- Absolute independence: no dependencies on frameworks, platform SDKs,
  or external libraries of any kind
- Uses only primitive types and types defined within the domain module itself
- Every operation returns a type that explicitly models two paths:
  **success** with a value or **failure** with a cause. Never propagates
  unhandled exceptions toward outer layers
- Repository contracts are declared here as abstract interfaces.
  Concrete implementations reside in Layer 4
- Each use case encapsulates **exactly one** business operation

**Prohibitions:**
- Does not import anything from Layer 1, 2, 4, 5, or 6
- Contains no references to UI, network, database, or platform frameworks
- Does not group multiple business operations into a single use case

---

### Layer 4 — Data
**Responsibility:** Implementation of repository contracts defined in Layer 3.
Manages caching strategies, synchronization, and mapping between transport models
and domain entities.

**Mandatory contracts:**
- Implements repository interfaces declared in Layer 3
- Manages the decision between local and remote source transparently for the domain
- Network transport objects (DTOs) live exclusively in this layer

**Prohibitions:**
- Does not expose infrastructure types toward Layer 3
- Contains no business logic

---

### Layer 5 — Infrastructure *(Cross-Module only)*
**Responsibility:** Low-level adapters toward external systems. HTTP clients,
database drivers, hardware drivers (sensors, bluetooth, biometrics), telemetry engines.

**Mandatory contracts:**
- Fully encapsulates the configuration and lifecycle of external systems
- Exposes simple interfaces toward Layer 4, hiding the complexity of the external system

**Prohibitions:**
- Contains no business logic or presentation logic
- Not activated in Simple Mode or Standard Mode

---

### Layer 6 — Core & Cross-Cutting *(Cross-Module only)*
**Responsibility:** Transversal shared foundations: dependency injection graphs,
functional utility types, log formatters, encryption utilities, and global configuration.

**Mandatory contracts:**
- Its components have no circular dependencies among themselves
- It is the only place where the module's dependency graph is configured

**Prohibitions:**
- Contains no business logic or presentation logic
- Not activated in Simple Mode or Standard Mode

---

## 3. Operational Boundary Modes

The mode determines which layers are activated. It must be evaluated **before**
writing any code. Choosing the wrong mode is an architectural error, not a minor detail.

---

### 🟢 Simple Mode
**Active layers:** Layer 1 only, with optional local state in the presentation controller.

**When to use it:**
The feature requires no external data, has no business rules to transform, and
persists nothing beyond immediate UI preferences.

**Validation question:** Would this feature work completely offline and without
a local database? If the answer is yes, it is Simple Mode.

**Mandatory restriction:** The moment a network call, a database query, or a
business rule appears, the mode escalates to Standard without exception.

**Real example:**
An "About the app" screen showing the version number, company name, and logo.
Or a theme selector (light/dark) that saves the preference to immediate local
memory. No server, no validation, no data transformation.

---

### 🟡 Standard Mode *(Production default)*
**Active layers:** Layer 1 + Layer 3 + Layer 4.

**When to use it:**
The feature consumes at least one data source (network or local persistence),
applies business rules to that data, and operates autonomously without depending
on other business modules.

**Validation question:** Does this feature have its own data and its own rules,
but does not need to coordinate with other modules or touch hardware? If so,
it is Standard Mode.

**When NOT to use it:** If the feature needs to communicate with another business
module, process in the background, integrate a third-party SDK with a complex
lifecycle, or access platform hardware, escalate to Cross-Module.

**Real example:**
A login screen with email and password. The use case validates the email format,
calls the authentication repository, receives a token or an error, and returns
the result to the UI. One module, one data source, its own rules.

---

### 🔴 Cross-Module Mode
**Active layers:** Layers 1 + 2 + 3 + 4 + 5 + 6.

**When to use it:**
The feature involves coordination between two or more business modules, requires
access to platform hardware, integrates third-party SDKs with a complex lifecycle,
or executes critical background operations.

**Validation question:** Does this feature affect the state of another module,
depend on hardware, or cannot fail silently without critical consequences?
If any answer is yes, it is Cross-Module Mode.

**When NOT to use it:** If the feature can be completed by reading and writing
only its own data, without affecting other modules, without special hardware, and
without background processes, it is Standard Mode.

**Real example:**
A card payment flow. The orchestrator (Layer 2) coordinates: the card validation
module, the session module to retrieve the user token, the payment SDK (Layer 5)
as an infrastructure driver, the orders module to update the status, and the
notifications module to confirm to the user. Five distinct modules, one external
SDK, one critical operation that cannot fail silently.

---

### Quick Decision Tree

```
Does it have external data or business logic?
        │
        NO ──────────────────► SIMPLE MODE
        │
        YES
        │
Does it coordinate with other modules, touch
hardware, or use complex-lifecycle SDKs?
        │
        NO ──────────────────► STANDARD MODE
        │
        YES
        │
        ▼
  CROSS-MODULE MODE
```

---

## 4. Feature Module — Conceptual Structure

Every feature is organized as an isolated module. The folders are conceptual;
the physical names and paths are defined in each framework-specific steering.

```
[feature-name]/
├── .context.md          → Module intent, local rules, dependency map
├── interface/           → Layer 1: views, presentation controller, UDF contracts
├── application/         → Layer 2: orchestrators (Cross-Module only)
├── domain/
│   ├── entities/        → Pure domain models
│   ├── usecases/        → One file per use case
│   └── repositories/    → Repository contracts (interfaces)
├── data/
│   ├── repositories/    → Domain contract implementations
│   ├── sources/         → Local/remote adapters
│   └── dtos/            → Transport models
└── infrastructure/      → Layer 5: low-level drivers (Cross-Module only)
```

**Isolation rule:** A module does not import internal types from another module.
Cross-module communication happens exclusively through public contracts declared
in Layer 3.

---

## 5. Universal Implementation Contracts

These contracts apply without exception in any language or framework.

### 5.1 Data Flow Contract (Layer 1)
```
user action
    → discrete immutable event
        → presentation controller evaluates the event
            → executes use case (Layer 3)
                → produces new immutable state
                    → view re-renders
```
State is never mutated directly. A new state is always produced.

### 5.2 Use Case Contract (Layer 3)
```
A use case:
    - Receives primitive parameters or domain types
    - Validates first (business rules before calling repositories)
    - Calls at most one repository
    - Returns: success-type(value) | failure-type(cause)
    - Has no UI side effects
    - Does not know the caller's concurrency mechanism
```

### 5.3 Repository Contract
```
The interface lives in Layer 3.
The implementation lives in Layer 4.
Layer 3 never imports the implementation.
Resolution happens in Layer 6 (Cross-Module) or at the entry point (Standard).
```

### 5.4 Error Handling Contract
```
Layer 3 → returns functional success/failure type. Never throws unhandled exceptions.
Layer 4 → catches infrastructure errors and maps them to domain failure types.
Layer 1 → consumes the functional type and decides how to present each path to the user.
```
The concrete implementation of the success/failure type is defined in each
framework steering. The principle is universal: **two explicit paths, no silent exceptions.**

---

## 6. Codebase Modification Rules

Every AI agent or human developer must follow these rules before modifying any file:

1. **Evaluate the Boundary Mode** of the feature before creating or modifying layers
2. **Respect module isolation:** do not introduce cross-module imports between modules
3. **One responsibility per file:** one use case, one entity, one repository
4. **Validate dependency direction:** no import from Layer 3 toward outer layers
5. **Consult the active framework steering** before deciding folder structure,
   file naming, or libraries to use
6. **Consult the corresponding skill** before implementing a presentation pattern,
   network client, or persistence mechanism
command v no funcion
---

## 7. What This Document Does NOT Define

The following aspects are **intentionally excluded** from the core and are defined
in the framework-specific steering documents:

- Physical folder names and paths
- File naming conventions
- Dependency injection libraries
- Specific presentation patterns (BLoC, MVVM, MVI, TCA, etc.)
- Network, persistence, and concurrency libraries
- Concrete success/failure type (Result, Either, sealed class, enum, etc.)
- Testing strategies and mocking libraries
- Navigation conventions

---

*ACAM v2.1 — steering_core_v2 — EN*

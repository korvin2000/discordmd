# Codex Authoring Guidelines

## Model
- model: gpt-5.2-codex
- model_reasoning_effort: high
- approval_policy: never

## Output Expectations
- Produce compact, well-structured Java and React code with minimal comments and docs while preserving clarity through naming and decomposition.
- Prefer reliable, modern language features appropriate for CLI tools (e.g., records where helpful, switch expressions, text blocks when suitable).
- Apply clean code and common design patterns; keep methods small and cohesive.
- Avoid JUnit or other test frameworks unless explicitly required.

## Project Context
- Target Java 21 Spring Boot Project (Gradle, Groovy DSL) with source/target compatibility retained at 21.
- Optimize for maintainability: immutable data where practical, clear configuration points, and sensible defaults.
- Favor explicit error handling and user-friendly UI without excessive verbosity.

## Style Preferences
- Keep formatting compact, avoiding superfluous blank lines and inline comments.
- Use descriptive identifiers instead of commentary to convey intent.
- Maintain consistent package structure and avoid God classes; extract helpers where they improve readability.
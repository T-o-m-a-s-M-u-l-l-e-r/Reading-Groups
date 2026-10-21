# Java coding preferences

Apply these preferences to new and modified Java code. Preserve existing
behavior and accessibility unless the requested task requires changing them.
Avoid unrelated formatting changes.

## Formatting

- Follow conventional Java formatting similar to Eclipse's automatic formatter.
- Use tabs for indentation, matching the existing Java files.
- Put opening braces on the same line as the declaration or statement.
- Always use braces for conditions and loops, even for a single statement.
- Expand methods and constructors into multiple lines, even when their bodies
  contain only one statement. Do not write single-line methods.
- Keep lines within approximately 120 characters, including indentation.
- Wrap earlier when it improves readability.
- Wrap parameter lists, throws clauses, method chains, and conditions at
  natural boundaries.
- Use additional indentation for continuation lines.
- For wrapped conditions, place operators such as && and || at the beginning
  of continuation lines, following the existing style.
- Separate logical stages within methods with blank lines.
- Keep related statements together; do not add blank lines after every statement.

## Classes and DTOs

- Prefer ordinary Java classes over records.
- Do not introduce records unless explicitly requested.
- For DTOs, use explicit fields, constructors as needed, and getters and setters
  as appropriate.
- Prefer explicitly written boilerplate.
- Do not introduce Lombok unless explicitly requested.

## Visibility

- Explicitly write public, protected, or private on declarations wherever
  applicable and consistent with the intended accessibility.
- Write public explicitly on interface methods that are implicitly public.
- Use private for implementation details and public for the intended public API.
- Use package-private visibility deliberately when appropriate. Java has no
  explicit modifier for package-private visibility.
- Do not make declarations public merely to add an explicit modifier.
- Preserve existing accessibility unless the task requires changing it.

## Naming and explicitness

- Use descriptive class, method, and variable names.
- Use UpperCamelCase for classes and lowerCamelCase for methods and variables.
- Name variables after their type or role when appropriate, for example:
  UserService userService = new UserService();
- Avoid cryptic abbreviations and unnecessary naming variations.
- Prefer explicit local variable types over var.
- Use descriptive intermediate variables when they explain a calculation,
  result, or meaningful step.
- Do not introduce intermediate variables mechanically before every return.
- Prefer readable, sequential code over dense expressions.
- Use lambda expressions instead of method references, for example:
  () -> invalidToken() rather than this::invalidToken.

## Imports

- Import types and use their simple names in declarations and expressions.
- Avoid fully qualified type names except when necessary to resolve a name
  conflict.
- Do not use wildcard imports.
- Organize imports into groups in this order:
  1. Java standard library imports.
  2. Third-party library imports.
  3. Project imports.
- Separate import groups with a blank line.

## Scope and consistency

- Follow these preferences without restructuring unrelated code.
- Respect the project's formatter configuration.
- If formatter configuration conflicts with these instructions, point out
  the conflict rather than repeatedly undoing formatter changes.

# Design principles

- Prefer reusing shared infrastructure where appropriate, with specialized
  services handling domain-specific logic.

# Configuration principles

- Give each setting one authoritative source; other layers should pass it
  through rather than duplicate it.
- Keep backend-only settings shared across environments hardcoded in
  application.properties.
- Keep shared infrastructure settings in the common Compose file and isolate
  local and production differences in their respective overrides.
- Define environment-specific values and secrets explicitly in .env or the
  shell. Avoid fallback values that hide missing configuration.
- Keep Compose variable forwarding straightforward, without conditional
  defaults or environment-specific value selection.
- Document every environment variable in .env.example, including intentionally
  empty values and settings unused in one environment. Use placeholders for
  secrets; do not commit real .env files or credentials.

# CI/CD and environment principles

- Keep local and production behavior aligned where it matters, especially
  public routing and access restrictions.
- Preserve production-style local testing alongside faster development modes.
- Use the reverse proxy as the normal app entry point. Keep local developer
  access on separate localhost-only ports, without exposing those ports in
  production.
- When changing Compose structure or deployment requirements, update the CI/CD
  commands and documentation together.
- Account explicitly for files required on the deployment host. Pulling images
  does not update Compose files, configuration, or database schemas.
- Distinguish build and test validation from runtime or deployment validation;
  report what was actually checked and any remaining gaps.

# Documentation and collaboration principles

- Keep README setup instructions and configuration examples portable, using
  generic domains, accounts, credentials, and deployment paths.
- Keep README content useful and durable. Avoid documenting incidental
  implementation or visual choices.
- Preserve screenshots and useful project presentation. Anonymize specific
  deployment details without removing unrelated content.
- Make conservative, focused edits that follow existing conventions. Avoid
  unrelated cleanup or restructuring.
- Treat brainstorming as discussion, not authorization to change files.
  Respect explicit limits on running builds, tests, services, or external actions.
- When explaining code, focus on the specific code or change the user points to.
- Keep answers concise and answer the specific question first.

# Git commit principles

- Agree on commit grouping and messages before committing, unless the user has
  already supplied or approved them.
- Keep commits focused. Stage shared files selectively when changes belong to
  different commits.
- Put prerequisite changes before features that depend on them. Aim for each
  commit to build and pass tests independently.
- Use concise commit titles. Add short bullets only where useful, with at most
  three or four bullets; avoid excessive detail.
- Commit only the requested scope and leave unrelated changes untouched.
- After committing, report the commit hashes and whether uncommitted changes
  remain.

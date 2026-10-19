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

---
name: coding-guidelines-java
description: A skill to be used whenever creating a piece of Java code  
---

Adhere to all the following coding guidelines in the order of precedence:

# Guidelines for all Java code

* Imports using '*' are forbidden.
* The maximum line length of Java code is 160 characters.
* The maximum line length of comments is 100 characters.
* All code is formatted according to .editorConfig in the project root.
* Comments and code are in the English language.
* Never use checked exceptions. All exceptions thrown must extend `java.lang.RuntimeException`. 
* Code blocks are never empty; they must at least contain a `//NOOP` comment. Exceptions are the bodies of records and pure marker interfaces.
* Make all fields, parameters and local variables **final** if possible.
* Comments never describe WHAT is being done, only WHY when necessary.
* Method comments that describe only a return value use the `{@return}` tag.
* Use the var keyword if possible.

# Additional guidelines for production code
* Use JSpecify annotations to mark the nullability of elements:
    * All unmarked fields, parameters and return types are considered `@NonNull`, so this annotation shall be omitted.
    * All nullable fields shall be explicitly marked with the Type-Annotation `@org.jspecify.ammotations.Nullable`
    * Add a package.java to all packages marking them as `@org.jspecify.nullness.NullMarked`

# Additional guidelines for Unit/Integration tests
* Tests are written using JUnit 5 and Mockito. Integration Tests use WireMock.
* Test method names shall be formatted in lower case with underscores: `should_return_the_expected_value()` and have a `@DisplayName` annotation clarifying what is tested.
* Assertions are written using AssertJ. Verifications of frameworks like Mockito, Spring MockMVC, etc are except from this rule.
* Individual tests shall be split into the following sections:
    * `// given` followed by fixture setup code. May be omitted when there's nothing to set up.
    * `// when` followed by the call to the SUT to be tested.
    * `// then` followed by assertions.
* Parameterized tests that use `@MethodSource` shall have a static method source method of the same name as the test.

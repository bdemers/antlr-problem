ANTLR 4.5.3+ change in behavior
============================

This example was extracted from [Apache SCIMple](https://github.com/apache/directory-scimple) to reproduce
an issue that prevents the project from upgrading to a newer ANTLR version.

It appears there was a change in behavior after 4.5.3 where an expected error is no longer detected.

How to run this example:

To run the tests with `4.5.3` run:

```bash
mvn clean verify -Pold-antlr4
```

or with `4.11.1`

```bash
mvn clean verify
```

> NOTE: A `clean` is required when switching between ANTLR versions.

For a _simplified_ test see the `antlrChangedBehavior()` test in `src/test/java/org/example/phonenumber/PhoneNumberTest.java`

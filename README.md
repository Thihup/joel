# Joel

A high-performance implementation of **Jakarta Expression Language (EL 6.0+)** for Java 17+.

[![build](https://github.com/Thihup/joel/actions/workflows/build.yml/badge.svg)](https://github.com/Thihup/joel/actions/workflows/build.yml)
[![Maven Central](https://img.shields.io/maven-central/v/dev.thihup.joel/dev.thihup.joel.svg)](https://central.sonatype.com/artifact/dev.thihup.joel/dev.thihup.joel)
[![License: MIT](https://img.shields.io/badge/License-MIT-blue.svg)](LICENSE)

---

## Features

- **Jakarta EL 6.0 Compliant**: Full compliance with the official Jakarta Expression Language Specification (validated against the Jakarta EL TCK).
- **High Performance**: Designed from the ground up for low-latency expression parsing and evaluation using `java.lang.invoke` / `MethodHandle` and ANTLR 4.
- **Modern Java & JPMS**: Built for Java 17+ with first-class Java Module System support.
- **Standard Service Provider**: Automatically registered via `java.util.ServiceLoader` for `jakarta.el.ExpressionFactory`.

---

## Installation

### Maven

Add the dependency to your `pom.xml`:

```xml
<dependency>
    <groupId>dev.thihup.joel</groupId>
    <artifactId>dev.thihup.joel</artifactId>
    <version>1.0.0-M1</version>
</dependency>
```

If you also require the bundled Jakarta EL API module:

```xml
<dependency>
    <groupId>dev.thihup.joel</groupId>
    <artifactId>jakarta.el</artifactId>
    <version>1.0.0-M1</version>
</dependency>
```

### Gradle

```groovy
implementation 'dev.thihup.joel:dev.thihup.joel:1.0.0-M1'
```

---

## Quick Start

Using `ELProcessor`:

```java
import jakarta.el.ELProcessor;

public class Main {
    public static void main(String[] args) {
        ELProcessor elp = new ELProcessor();
        
        elp.setValue("name", "World");
        Object greeting = elp.eval("'Hello, ' += name += '!'");
        System.out.println(greeting); // Hello, World!

        elp.setValue("a", 15);
        elp.setValue("b", 25);
        Object sum = elp.eval("a + b");
        System.out.println(sum); // 40
    }
}
```

Using `ExpressionFactory`:

```java
import jakarta.el.ELContext;
import jakarta.el.ExpressionFactory;
import jakarta.el.StandardELContext;
import jakarta.el.ValueExpression;

public class Main {
    public static void main(String[] args) {
        ExpressionFactory factory = ExpressionFactory.newInstance();
        ELContext context = new StandardELContext(factory);

        ValueExpression expr = factory.createValueExpression(
            context,
            "${1 + 2 * 3}",
            Integer.class
        );

        Integer result = (Integer) expr.getValue(context);
        System.out.println(result); // 7
    }
}
```

---

## Building from Source

Requires **JDK 17+** and **Apache Maven 3.8+**.

```bash
# Clone the repository
git clone https://github.com/Thihup/joel.git
cd joel

# Compile and run unit tests
mvn clean test

# Run tests including the official Jakarta EL TCK
mvn clean test -Ptck
```

---

## License

This project is licensed under the [MIT License](LICENSE).


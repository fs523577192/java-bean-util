# java-bean-util

## FieldGetter / FieldSetter
A utility for getting / setting field value of a Java bean

## ByFieldNameBeanConverter
A utility that convert a Java bean to another Java bean of another type.

The field value of the result Java bean is copied from the source Java bean if the source has a field with the same name.

# Notice on using
This library depends on `org.springframework:spring-core`.

The dependency is of scope "provided", so you have to provide spring-core at runtime, and you can provide  spring-core of any version as long as it is compatible with this library.

`java.util.logging` is used for logging in order to reduce external library dependencies. You may setup `java.util.logging` or provide an adapter to another logging library to enable logging inside this library.

# How to build
There are 5 profiles in pom.xml corresponding to various version of `spring-core`:
* 3.0.3
* 3.2.2
* 3.2.2
* 5.0.9 (corresponding to Spring Boot 2.0.5)
* 5.2.0 (corresponding to Spring Boot 2.2.0)

You can choose any one of the above profile to build this package.

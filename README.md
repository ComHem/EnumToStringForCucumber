EnumToStringForCucumber
=======================
Use enum classes to validate some values in your Cucumber specifications!

Processes an enum class annotated with `@GenerateCucumberString`, producing a new java source file which is also compiled.
The generated class has a static String field, which is the concatenation of the enum values, with delimiter, suffix and prefix as specified in the annotation.
These parameters (delimiter, suffix...) can be overridden.
This field can be used in any annotation (but intended for Cucumber annotations), as it's a compile time constant value.


# How to use:

### Maven
Specify dependency in pom:
```xml
        <dependency>
            <groupId>se.comhem.cucumber.annotations</groupId>
            <artifactId>EnumToStringForCucumber</artifactId>
            <version>...</version>
            <scope>compile</scope>
        </dependency>
```

Run `mvn testCompile` to generate source code and compiled classes from these source files.

### Java
Mark any enum class (say *MyEnum*) with `@GenerateCucumberString`; a java source file called *MyEnumToCucumberString* will be generated.


You can now have *compile time validation* in you Cucumber feature file, assuring it matches an entry in your enum class.

### Example
If you have an annotated enum class
```java
@GenerateCucumberString
public enum Services {
    IP_TV_MED,
    IP_TV_PLUS,
    IP_TV_BAS,
    IP_TV_MAX,
    TIVO_TV
}
```

The annotation will generate the class
```java
public class ServicesToCucumberString {
	public static final String value = "(IP TV MED|IP TV PLUS|IP TV BAS|IP TV MAX|TIVO TV)";
}
```

Use it in your the `@Given @When @Then` annotations of you step definition files:
```java
@Then("the customer is authorized to access the " + ServicesToCucumberString.value +  " package")
public void the_customer_is_authorized_to_access_package(String packageName) throws Throwable {
    verifyAuthForPackage(packageName);
}
```
Your feature file will basically be validated against the enum values:

**Given** a customer

**When** an agreement for the *IP TV MAX* package is created and an order is placed
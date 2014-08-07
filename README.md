EnumToStringForCucumber
=======================

Processes an @GenerateCucumberString annotation attached to an enum class, producing a new java source file. The generated class has a static String field, which is the concatenation of the enum values, with delimiter, suffix and prefix as specified in the annotation. These parameters (delimiter, suffix...) can be overridden. This field can be used in any annotation (but most useful in Cucumber annotations), as it's a compile time constant value.

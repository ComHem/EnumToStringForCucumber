package se.comhem.cucumber.annotations;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.TYPE)
@Retention(RetentionPolicy.SOURCE)
public @interface GenerateCucumberString {
    String prefix() default "(";
    String suffix() default ")";
    String delimiter() default "|";
    String[] replace() default {"_", " "};
}
package se.comhem.cucumber.annotations;

import com.google.common.base.Function;
import com.google.common.base.Joiner;
import com.google.common.base.Predicate;
import com.google.common.collect.FluentIterable;
import com.google.common.collect.ImmutableList;

import static javax.lang.model.SourceVersion.RELEASE_7;

import java.io.BufferedWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Set;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.annotation.processing.SupportedSourceVersion;

import javax.lang.model.element.*;

import javax.tools.Diagnostic;
import javax.tools.JavaCompiler;
import javax.tools.JavaFileObject;
import javax.tools.ToolProvider;

@SupportedAnnotationTypes("se.comhem.cucumber.annotations.GenerateCucumberString")
@SupportedSourceVersion(RELEASE_7)
public class EnumToConstStringAnnotationProcessor extends AbstractProcessor {
    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        if (!roundEnv.processingOver()) {
            Set<? extends Element> annotatedElements = roundEnv.getElementsAnnotatedWith(GenerateCucumberString.class);

            for (Element element : annotatedElements) {
                final GenerateCucumberString annotation = ((TypeElement) element).getAnnotation(GenerateCucumberString.class);
                if (!validated(annotation)) continue;
                if (!isAnnotatedClassAnEnum(element)) continue;

                final JavaFileObject sourceFile = generateJavaSourceFileFromClass((TypeElement) element);
                compileSource(sourceFile);
            }
        }

        return true;
    }

    /*
     * Compile the source file, so that the client will not have to do this as a pre step to normal compilation.
     */
    private void compileSource(JavaFileObject sourceFile) {
        // Compile source file.
        JavaCompiler compiler = ToolProvider.getSystemJavaCompiler();
        if (compiler != null && sourceFile != null) {
            compiler.run(null, null, null, sourceFile.toUri().getPath());
        }
    }

    private boolean isAnnotatedClassAnEnum(Element element) {
        if (element.getKind() == ElementKind.ENUM) {
            return true;
        }
        error("@" + GenerateCucumberString.class.getSimpleName() + " may only prefix an enum -- " + element +
                " is not an enum");
        return false;
    }

    private JavaFileObject generateJavaSourceFileFromClass(TypeElement classElement) {
        PackageElement packageElement =
                (PackageElement) classElement.getEnclosingElement();

        JavaFileObject jfo = null;
        final String generatedJavaSourceFileSuffix = "ToCucumberString";
        final String fileName = classElement.getQualifiedName() + generatedJavaSourceFileSuffix;
        try {
            jfo = processingEnv.getFiler().createSourceFile(fileName);
        } catch (IOException e) {
            error("Could not create file " + fileName);
        }
        try (
                BufferedWriter bw = new BufferedWriter(jfo.openWriter())
        ) {
            packageStatement(packageElement, bw);
            classBody(classElement, generatedJavaSourceFileSuffix, bw);
            bw.flush();
        } catch (IOException e) {
            error("Could not create class from Enum");
        }
        return jfo;
    }

    private void classBody(TypeElement classElement, String generatedJavaSourceFileSuffix, BufferedWriter bw) throws IOException {
        bw.append("public class ").append(classElement.getSimpleName()).append(generatedJavaSourceFileSuffix + " {\n");
        constantStringField(classElement, bw);
        toEnumStringValueMethod(classElement, bw);
        bw.append("}");
    }

    private void toEnumStringValueMethod(TypeElement classElement, BufferedWriter bw) throws IOException {
        final GenerateCucumberString annotation = classElement.getAnnotation(GenerateCucumberString.class);
        final String[] reverseReplacements = annotation.replace().clone();
        Collections.reverse(Arrays.asList(reverseReplacements));
        bw.append("\n\t/* Applies the text replacements in reverse */\n");
        bw.append("\tpublic static String toEnumStringValue(final String value) {\n");
        bw.append("\t\treturn value");
        for (int i = 0; i < reverseReplacements.length; i += 2) {
            bw.append(".replace(\"").append(reverseReplacements[i]).append("\", \"").append(reverseReplacements[i + 1]).append("\")");
        }
        bw.append(";\n");
        bw.append("\t}\n");
    }

    private void constantStringField(TypeElement classElement, BufferedWriter bw) throws IOException {
        bw.append("\tpublic static final String value = \"").append(createRegexpString(classElement)).append("\";\n");
    }

    private void packageStatement(PackageElement packageElement, BufferedWriter bw) throws IOException {
        bw.append("package ").append(packageElement.getQualifiedName()).append(";\n");
        bw.newLine();
    }

    private boolean validated(GenerateCucumberString annotation) {
        if (annotation.replace().length % 2 != 0) {
            error("Replace list length must be an even number with format: {textToReplace, replaceWith, ..., textToReplace, replaceWith}");
            return false;
        }
        return true;
    }

    private String createRegexpString(TypeElement classElement) {
        final GenerateCucumberString annotation = classElement.getAnnotation(GenerateCucumberString.class);
        final ImmutableList<String> enumConstantsWithWhiteSpace = FluentIterable.from(classElement.getEnclosedElements())
                .filter(new Predicate<Element>() {
                    @Override
                    public boolean apply(Element o) {
                        return o.getKind() == ElementKind.ENUM_CONSTANT;
                    }
                })
                .transform(new Function<Element, String>() {
                    @Override
                    public String apply(Element element) {
                        final String stringValue = element.accept(new ElemVisitor(), null);
                        final String[] replacements = annotation.replace();
                        return doTextReplacements(stringValue, replacements);
                    }
                })
                .toList();
        return annotation.prefix() + Joiner.on(annotation.delimiter()).join(enumConstantsWithWhiteSpace) + annotation.suffix();
    }

    private String doTextReplacements(final String stringValue, final String[] replacements) {
        String newString = stringValue;
        for (int i = 0; i < replacements.length; i += 2) {
            newString = newString.replace(replacements[i], replacements[i + 1]);
        }
        return newString;
    }

    void error(String msg) {
        processingEnv.getMessager().printMessage(Diagnostic.Kind.ERROR, msg);
    }

}
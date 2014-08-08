package se.comhem.cucumber.annotations;

import javax.lang.model.element.*;

/**
* Implements the visitor pattern. Used when traversing annotated class to get enum values.
*/
class ElemVisitor implements ElementVisitor<String, Void> {

    @Override
    public String visitVariable(VariableElement e, Void aVoid) {
        return e.getSimpleName().toString();
    }

    @Override
    public String visit(Element e, Void aVoid) {
        return null;
    }

    @Override
    public String visit(Element e) {
        return null;
    }

    @Override
    public String visitPackage(PackageElement e, Void aVoid) {
        return null;
    }

    @Override
    public String visitType(TypeElement e, Void aVoid) {
        return null;
    }

    @Override
    public String visitExecutable(ExecutableElement e, Void aVoid) {
        return null;
    }

    @Override
    public String visitTypeParameter(TypeParameterElement e, Void aVoid) {
        return null;
    }

    @Override
    public String visitUnknown(Element e, Void aVoid) {
        return null;
    }
}

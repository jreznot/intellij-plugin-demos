package com.haulmont.cuba.samples.reference;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple reference to class method.
 *
 * @author Alexander Budarov
 */
public class NamePatternMethodReference extends PsiReferenceBase<PsiLiteralExpression> implements EmptyResolveMessageProvider {

    private PsiClass owner;
    private String methodName;

    public NamePatternMethodReference(PsiClass ownerClass, PsiLiteralExpression element, TextRange rangeInElement,
                                      String methodName) {
        super(element, rangeInElement);
        this.methodName = methodName;
        this.owner = ownerClass;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiMethod[] methods = owner.findMethodsByName(methodName, true);
        for (PsiMethod method : methods) {
            if (!method.getModifierList().hasModifierProperty(PsiModifier.STATIC)
                    && method.getModifierList().hasModifierProperty(PsiModifier.PUBLIC)) {
                return method;
            }
        }
        return null;
    }

    @NotNull
    @Override
    public Object[] getVariants() {
        return PsiReference.EMPTY_ARRAY;
    }

    @NotNull
    @Override
    public String getUnresolvedMessagePattern() {
        // sadly, doesn't work
        String className = owner.getName();
        return String.format("Unknown method \"%s\" for entity \"%s\"", methodName, className);
    }
}

package com.haulmont.cuba.samples.reference;

import com.intellij.codeInsight.daemon.EmptyResolveMessageProvider;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * Simple reference to class field.
 *
 * @author Alexander Budarov
 */
public class NamePatternFieldReference extends PsiReferenceBase<PsiLiteralExpression> implements EmptyResolveMessageProvider {

    private PsiClass owner;
    private String fieldName;

    public NamePatternFieldReference(PsiClass owner, PsiLiteralExpression element, TextRange rangeInElement, String fieldName) {
        super(element, rangeInElement);
        this.fieldName = fieldName;
        this.owner = owner;
    }

    @Nullable
    @Override
    public PsiElement resolve() {
        PsiField field = owner.findFieldByName(fieldName, true);
        return field;
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
        return String.format("Unknown field \"%s\" for entity \"%s\"", fieldName, className);
    }
}

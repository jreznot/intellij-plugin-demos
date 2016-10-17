package com.haulmont.cuba.samples.reference;

import com.intellij.openapi.project.DumbService;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.util.ProcessingContext;
import org.apache.commons.lang.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * This class takes literal expression, checks if it is a CUBA "@NamePattern"
 * and tries to gather references to fields and method from it.
 *
 * @author Alexander Budarov
 */
public class NamePatternReferenceProvider extends PsiReferenceProvider {
    private static final String NAME_PATTERN = "com.haulmont.chile.core.annotations.NamePattern";

    // Example:
    // @NamePattern("#getCaption|login,name")
    // getCaption is reference to method
    // login and name are references to entity attributes

    @NotNull
    @Override
    public PsiReference[] getReferencesByElement(@NotNull PsiElement element, @NotNull ProcessingContext context) {
        // don't do anything when indexing is in progress
        if (DumbService.isDumb(element.getProject())) {
            return PsiReference.EMPTY_ARRAY;
        }
        // double check for literal expression
        if (!(element instanceof PsiLiteralExpression)) {
            return PsiReference.EMPTY_ARRAY;
        }
        // check that literal is string
        Object literalValue = ((PsiLiteralExpression) element).getValue();
        if (!(literalValue instanceof String)) {
            return PsiReference.EMPTY_ARRAY;
        }
        String pattern = String.valueOf(literalValue);
        // don't try to parse empty pattern
        if (StringUtils.isEmpty(pattern) || !pattern.contains("|")) {
            return PsiReference.EMPTY_ARRAY;
        }

        if (element.getParent() != null && element.getParent().getParent() != null
                && element.getParent().getParent().getParent() instanceof PsiAnnotation) {
            PsiAnnotation annotation = (PsiAnnotation) element.getParent().getParent().getParent();
            if (NAME_PATTERN.equals(annotation.getQualifiedName()) && annotation.getParent().getParent() instanceof PsiClass) {
                PsiClass ownerClass = (PsiClass) annotation.getParent().getParent();

                List<PsiReference> refs = gatherReferences(ownerClass, pattern, (PsiLiteralExpression) element, context);
                return refs != null ? refs.toArray(new PsiReference[refs.size()]) : PsiReference.EMPTY_ARRAY;

            }
        }
        return PsiReference.EMPTY_ARRAY;
    }

    @Nullable
    private List<PsiReference> gatherReferences(PsiClass ownerClass, String pattern, PsiLiteralExpression element, ProcessingContext context) {
        String[] parts = pattern.split("\\|");
        if (parts.length != 2) {
            // bad format
            return null;
        }

        String left = parts[0];
        String right = parts[1];
        // int rightPartStartOffset = element.getTextRange().getStartOffset() + 2 + left.length();
        int rightPartStartOffset = 2 + left.length();

        List<PsiReference> refList = processLeftPart(ownerClass, element, left);
        refList = processRightPart(ownerClass, refList, element, right, rightPartStartOffset);
        return refList;
    }

    @Nullable
    private List<PsiReference> processLeftPart(PsiClass ownerClass, PsiLiteralExpression element, String left) {
        List<PsiReference> refList = null;
        if (left.startsWith("#") && left.length() > 1) {
            String namePatternMethodName = left.substring(1);
            refList = new ArrayList<>();
            // int startOffset = element.getTextRange().getStartOffset() + 2; // one for " and other for #
            int startOffset = 2; // one for " and other for #
            TextRange range = new TextRange(startOffset, startOffset + namePatternMethodName.length());
            PsiReference ref = new NamePatternMethodReference(ownerClass, element, range, namePatternMethodName);
            refList.add(ref);
        }
        return refList;
    }

    @Nullable
    private List<PsiReference> processRightPart(PsiClass ownerClass, @Nullable List<PsiReference> refList, PsiLiteralExpression element,
                                                String rightPart, int rightPartStartOffset) {
        if (rightPart.isEmpty()) {
            return refList;
        }

        int prevIndex = -1;
        do {
            int nextIndex = rightPart.indexOf(',', prevIndex + 1);
            String field;
            if (nextIndex > 0) {
                field = rightPart.substring(prevIndex + 1, nextIndex);
            } else {
                field = rightPart.substring(prevIndex + 1);
            }

            TextRange range = TextRange.from(rightPartStartOffset + prevIndex + 1, field.length());
            PsiReference ref = new NamePatternFieldReference(ownerClass, element, range, field);
            if (refList == null) {
                refList = new ArrayList<>();
            }
            refList.add(ref);

            prevIndex = nextIndex;
        } while (prevIndex > 0);

        return refList;
    }
}

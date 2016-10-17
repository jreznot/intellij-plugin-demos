package com.haulmont.cuba.samples.reference;

import com.intellij.patterns.ElementPattern;
import com.intellij.patterns.PsiJavaPatterns;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReferenceContributor;
import com.intellij.psi.PsiReferenceProvider;
import com.intellij.psi.PsiReferenceRegistrar;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Budarov
 */
public class SamplesReferenceContributor extends PsiReferenceContributor {

    private PsiReferenceRegistrar registrar;

    @Override
    public void registerReferenceProviders(@NotNull PsiReferenceRegistrar registrar) {
        this.registrar = registrar;

        // register reference providers
        register(PsiJavaPatterns.literalExpression(), new NamePatternReferenceProvider());
    }

    private void register(ElementPattern<? extends PsiElement> element, PsiReferenceProvider provider) {
        registrar.registerReferenceProvider(element, provider);
    }
}

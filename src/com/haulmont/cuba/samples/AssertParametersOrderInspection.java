package com.haulmont.cuba.samples;

import com.intellij.codeInspection.*;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiUtil;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;

/**
 * @author Alexander Budarov
 */
public class AssertParametersOrderInspection extends LocalInspectionTool {

    private static final String JUNIT_TEST_CASE = "junit.framework.TestCase";
    private static final String JUNIT_ASSERT = "junit.framework.Assert";
    private static final String ORG_JUNIT_ASSERT = "org.junit.Assert";
    private static final String ASSERT_EQUALS = "assertEquals";

    @Nullable
    @Override
    public ProblemDescriptor[] checkFile(@NotNull PsiFile file, @NotNull InspectionManager manager, boolean isOnTheFly) {
        if (!(file instanceof PsiClassOwner)) {
            return null;
        }

        Visitor visitor = new Visitor(manager);
        file.accept(visitor);
        List<ProblemDescriptor> problems = visitor.getProblems();

        if (problems != null) {
            return problems.toArray(new ProblemDescriptor[problems.size()]);
        } else {
            return null;
        }
    }

    private static class Visitor extends JavaRecursiveElementVisitor {
        private final InspectionManager inspectionManager;
        private List<ProblemDescriptor> problems = null;

        public Visitor(InspectionManager manager) {
            inspectionManager = manager;
        }

        @Nullable
        public List<ProblemDescriptor> getProblems() {
            return problems;
        }

        @Override
        public void visitMethodCallExpression(PsiMethodCallExpression expression) {
            if (isMethodCall(expression, JUNIT_TEST_CASE, ASSERT_EQUALS)
                    || isMethodCall(expression, JUNIT_ASSERT, ASSERT_EQUALS)
                    || isMethodCall(expression, ORG_JUNIT_ASSERT, ASSERT_EQUALS)) {
                checkParametersOrder(expression.getArgumentList());
            }
            super.visitMethodCallExpression(expression);
        }

        private boolean isMethodCall(PsiMethodCallExpression methodCall, String className, String methodName) {
            PsiMethod resolvedMethod = methodCall.resolveMethod();
            if (resolvedMethod == null) {
                return false;
            }
            if (!methodName.equals(resolvedMethod.getName())) {
                return false;
            }

            PsiClass methodClass = resolvedMethod.getContainingClass();
            boolean res = methodClass != null && className.equals(methodClass.getQualifiedName());
            return res;
        }

        private void checkParametersOrder(PsiExpressionList argumentList) {
            PsiExpression[] arguments = argumentList.getExpressions();
            if (arguments.length == 2) {
                // assertEquals(expected, actual)
                checkExpectedActual(argumentList, arguments[0], arguments[1]);
            } else if (arguments.length == 3) {
                // assertEquals(message, expected, actual)
                checkExpectedActual(argumentList, arguments[1], arguments[2]);
            }
        }

        private void checkExpectedActual(PsiExpressionList argumentList, PsiExpression expectedArg, PsiExpression actualArg) {
            boolean expectedConstant = PsiUtil.isConstantExpression(expectedArg);
            boolean actualConstant = PsiUtil.isConstantExpression(actualArg);
            if (!expectedConstant && actualConstant) {
                if (problems == null) {
                    problems = new ArrayList<>();
                }
                String message = SamplesResourceBundle.message("samples.inspections.wrongAssertParameters");
                ProblemDescriptor problem = inspectionManager.createProblemDescriptor(argumentList, message, false,
                        LocalQuickFix.EMPTY_ARRAY, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);

                problems.add(problem);
            }
        }
    }
}

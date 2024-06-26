package fr.adrienbrault.idea.symfony2plugin.dic.inspection;

import com.intellij.codeInspection.LocalInspectionTool;
import com.intellij.codeInspection.ProblemHighlightType;
import com.intellij.codeInspection.ProblemsHolder;
import com.intellij.openapi.project.Project;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.xml.XmlText;
import fr.adrienbrault.idea.symfony2plugin.Symfony2ProjectComponent;
import fr.adrienbrault.idea.symfony2plugin.config.xml.XmlHelper;
import fr.adrienbrault.idea.symfony2plugin.dic.container.util.ServiceContainerUtil;
import org.apache.commons.lang3.StringUtils;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.yaml.psi.YAMLScalar;

/**
 * @author Daniel Espendiller <daniel@espendiller.net>
 */
public class ContainerConstantInspection extends LocalInspectionTool {
    public static final String MESSAGE = "Symfony: constant not found";

    public static class MyYamlLocalInspectionTool extends LocalInspectionTool {
        @Override
        public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
            Project project = holder.getProject();
            if (!Symfony2ProjectComponent.isEnabled(project)) {
                return super.buildVisitor(holder, isOnTheFly);
            }

            return new PsiElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    if (element instanceof YAMLScalar yamlScalar) {
                        visitYamlElement(yamlScalar, holder);
                    }

                    super.visitElement(element);
                }
            };
        }
    }

    public static class MyXmlLocalInspectionTool extends LocalInspectionTool {
        @Override
        public @NotNull PsiElementVisitor buildVisitor(@NotNull ProblemsHolder holder, boolean isOnTheFly) {
            Project project = holder.getProject();
            if (!Symfony2ProjectComponent.isEnabled(project)) {
                return super.buildVisitor(holder, isOnTheFly);
            }

            return new PsiElementVisitor() {
                @Override
                public void visitElement(@NotNull PsiElement element) {
                    visitXmlElement(element, holder);
                    super.visitElement(element);
                }
            };
        }
    }

    private static void visitYamlElement(@NotNull YAMLScalar psiElement, @NotNull ProblemsHolder holder) {
        String textValue = psiElement.getTextValue();
        if(textValue.startsWith("!php/const:")) {
            String constantName = textValue.substring(11);
            if(StringUtils.isNotBlank(constantName) && ServiceContainerUtil.getTargetsForConstant(holder.getProject(), constantName).isEmpty()) {
                holder.registerProblem(psiElement, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
            }
        }
    }

    private static void visitXmlElement(@NotNull PsiElement psiElement, @NotNull ProblemsHolder holder) {
        if(!XmlHelper.getArgumentValueWithTypePattern("constant").accepts(psiElement)) {
            return;
        }

        PsiElement xmlText = psiElement.getParent();
        if(!(xmlText instanceof XmlText)) {
            return;
        }

        String value = ((XmlText) xmlText).getValue();
        if(StringUtils.isBlank(value)) {
            return;
        }

        if(ServiceContainerUtil.getTargetsForConstant(holder.getProject(), value).isEmpty()) {
            holder.registerProblem(xmlText, MESSAGE, ProblemHighlightType.GENERIC_ERROR_OR_WARNING);
        }
    }
}

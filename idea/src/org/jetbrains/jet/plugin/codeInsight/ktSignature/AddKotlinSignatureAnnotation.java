/*
 * Copyright 2010-2012 JetBrains s.r.o.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package org.jetbrains.jet.plugin.codeInsight.ktSignature;

import com.intellij.codeInsight.ExternalAnnotationsListener;
import com.intellij.codeInsight.ExternalAnnotationsManager;
import com.intellij.codeInsight.intention.AddAnnotationFix;
import com.intellij.codeInsight.intention.impl.BaseIntentionAction;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.application.ModalityState;
import com.intellij.openapi.editor.Editor;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.util.Iconable;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.*;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.util.IncorrectOperationException;
import com.intellij.util.messages.MessageBusConnection;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;
import org.jetbrains.jet.di.InjectorForJavaSemanticServices;
import org.jetbrains.jet.lang.descriptors.*;
import org.jetbrains.jet.lang.resolve.BindingContext;
import org.jetbrains.jet.lang.resolve.java.JavaDescriptorResolver;
import org.jetbrains.jet.lang.resolve.name.FqName;
import org.jetbrains.jet.lang.resolve.name.Name;
import org.jetbrains.jet.lang.resolve.scopes.JetScope;
import org.jetbrains.jet.lang.types.JetType;
import org.jetbrains.jet.plugin.JetBundle;
import org.jetbrains.jet.plugin.JetIcons;
import org.jetbrains.jet.resolve.DescriptorRenderer;

import javax.swing.*;

import static org.jetbrains.jet.plugin.codeInsight.ktSignature.KotlinSignatureUtil.*;

/**
 * @author Evgeny Gerashchenko
 * @since 16 Aug 2012
 */
public class AddKotlinSignatureAnnotation extends BaseIntentionAction implements Iconable {
    private static final DescriptorRenderer RENDERER = new DescriptorRenderer() {
        @Override
        protected boolean shouldRenderDefinedIn() {
            return false;
        }

        @Override
        public String renderType(JetType type) {
            return renderTypeWithShortNames(type);
        }

        @Override
        protected boolean shouldRenderModifiers() {
            return false;
        }
    };

    public AddKotlinSignatureAnnotation() {
        setText(JetBundle.message("add.kotlin.signature.action.text"));
    }

    @NotNull
    @Override
    public String getFamilyName() {
        return JetBundle.message("add.kotlin.signature.action.family.name");
    }

    @Override
    public Icon getIcon(@IconFlags int flags) {
        return JetIcons.SMALL_LOGO;
    }

    @Override
    public boolean isAvailable(@NotNull Project project, Editor editor, PsiFile file) {
        PsiModifierListOwner annotationOwner = findAnnotationOwner(file, editor);
        if (annotationOwner == null) {
            return false;
        }

        PsiModifierList modifierList = annotationOwner.getModifierList();
        if (modifierList == null || modifierList.hasExplicitModifier(PsiModifier.PRIVATE)) {
            return false;
        }

        return createFix(annotationOwner, "").isAvailable(project, editor, file);
    }

    @Override
    public void invoke(@NotNull final Project project, final Editor editor, PsiFile file) throws IncorrectOperationException {
        final PsiMember annotatedElement = findAnnotationOwner(file, editor);

        assert (annotatedElement != null);

        String signature = getDefaultSignature(project, (PsiMember) annotatedElement.getOriginalElement());

        final MessageBusConnection busConnection = project.getMessageBus().connect();
        busConnection.subscribe(ExternalAnnotationsManager.TOPIC, new ExternalAnnotationsListener.Adapter() {
            @Override
            public void afterExternalAnnotationChanging(@NotNull PsiModifierListOwner owner, @NotNull String annotationFQName, boolean successful) {
                busConnection.disconnect();

                if (successful && owner == annotatedElement && KOTLIN_SIGNATURE_ANNOTATION.equals(annotationFQName)) {
                    ApplicationManager.getApplication().invokeLater(new Runnable() {
                        @Override
                        public void run() {
                            refreshMarkers(project);
                            EditSignatureAction.invokeEditSignature(annotatedElement, editor, null);
                        }
                    }, ModalityState.NON_MODAL);
                }
            }
        });
        createFix(annotatedElement, signature).invoke(project, editor, file);
    }

    @NotNull
    private static AddAnnotationFix createFix(@NotNull PsiModifierListOwner annotatedElement, @NotNull String signature) {
        return new AddAnnotationFix(KOTLIN_SIGNATURE_ANNOTATION, annotatedElement, signatureToNameValuePairs(annotatedElement.getProject(), signature));
    }

    private static String getDefaultSignature(@NotNull PsiMethod method, FqName classFqName, JavaDescriptorResolver javaDescriptorResolver, BindingContext context) {
        if (method.getReturnType() == null) {
            // For constructor
            ClassDescriptor classDescriptor = javaDescriptorResolver.resolveClass(classFqName);
            assert classDescriptor != null: "Couldn't resolve class descriptor for " + classFqName;
            classDescriptor.getConstructors();

            ConstructorDescriptor constructorDescriptor = context.get(BindingContext.CONSTRUCTOR, method);
            assert constructorDescriptor != null: "Couldn't find constructor descriptor for " + method.getName() + " in " + classFqName;
            return getDefaultConstructorAnnotation(constructorDescriptor, classFqName);
        }

        getMemberScope(method, classFqName, javaDescriptorResolver).getFunctions(Name.identifier(method.getName()));

        SimpleFunctionDescriptor functionDescriptor = context.get(BindingContext.FUNCTION, method);
        assert functionDescriptor != null: "Couldn't find function descriptor for " + method.getName() + " in " + classFqName;
        return RENDERER.render(functionDescriptor);
    }

    @NotNull
    private static String getDefaultSignature(@NotNull PsiField field, FqName classFqName, JavaDescriptorResolver javaDescriptorResolver, BindingContext context) {
        getMemberScope(field, classFqName, javaDescriptorResolver).getProperties(Name.identifier(field.getName()));

        VariableDescriptor variableDescriptor = context.get(BindingContext.VARIABLE, field);
        assert variableDescriptor != null: "Couldn't find variable descriptor for field " + field.getName() + " in " + classFqName;
        return RENDERER.render(variableDescriptor);
    }

    @NotNull
    private static String getDefaultSignature(@NotNull Project project, @NotNull PsiMember psiMember) {
        InjectorForJavaSemanticServices injector = new InjectorForJavaSemanticServices(project);
        JavaDescriptorResolver javaDescriptorResolver = injector.getJavaDescriptorResolver();

        PsiClass containingClass = psiMember.getContainingClass();
        assert containingClass != null;
        String qualifiedName = containingClass.getQualifiedName();
        assert qualifiedName != null;
        FqName classFqName = new FqName(qualifiedName);

        if (psiMember instanceof PsiMethod) {
            return getDefaultSignature((PsiMethod) psiMember, classFqName, javaDescriptorResolver, injector.getBindingTrace().getBindingContext());
        }

        if (psiMember instanceof PsiField) {
            return getDefaultSignature((PsiField) psiMember, classFqName, javaDescriptorResolver, injector.getBindingTrace().getBindingContext());
        }

        throw new IllegalStateException("PsiMethod or PsiField are expected");
    }

    @NotNull
    private static JetScope getMemberScope(PsiModifierListOwner psiModifierListOwner, FqName classFqName, JavaDescriptorResolver javaDescriptorResolver) {
        if (psiModifierListOwner.hasModifierProperty(PsiModifier.STATIC)) {
            NamespaceDescriptor namespaceDescriptor = javaDescriptorResolver.resolveNamespace(classFqName);
            assert namespaceDescriptor != null: "Couldn't resolve namespace descriptor for " + classFqName;
            return namespaceDescriptor.getMemberScope();
        }

        ClassDescriptor classDescriptor = javaDescriptorResolver.resolveClass(classFqName);
        assert classDescriptor != null: "Couldn't resolve class descriptor for " + classFqName;
        return classDescriptor.getDefaultType().getMemberScope();
    }

    private static String getDefaultConstructorAnnotation(ConstructorDescriptor constructorDescriptor, FqName classFqName) {
        return String.format("fun %s%s", classFqName.shortName(), RENDERER.renderFunctionParameters(constructorDescriptor));
    }

    @Nullable
    private static PsiMember findAnnotationOwner(@NotNull PsiElement file, Editor editor) {
        int offset = editor.getCaretModel().getOffset();
        PsiMember methodMember = findMethod(file, offset);
        if (methodMember != null) {
            return methodMember;
        }

        return findField(file, offset);
    }

    @Nullable
    private static PsiMethod findMethod(@NotNull PsiElement file, int offset) {
        PsiElement element = file.findElementAt(offset);
        PsiMethod res = PsiTreeUtil.getParentOfType(element, PsiMethod.class);
        if (res == null) return null;

        //Not available in method's body
        PsiCodeBlock body = res.getBody();
        if (body != null) {
            TextRange bodyRange = body.getTextRange();
            if (bodyRange != null && bodyRange.getStartOffset() <= offset) {
                return null;
            }
        }
        return res;
    }

    @Nullable
    private static PsiField findField(@NotNull PsiElement file, int offset) {
        return PsiTreeUtil.getParentOfType(file.findElementAt(offset), PsiField.class);
    }
}

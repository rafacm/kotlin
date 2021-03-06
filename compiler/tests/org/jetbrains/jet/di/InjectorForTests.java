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


package org.jetbrains.jet.di;

import org.jetbrains.jet.lang.resolve.DescriptorResolver;
import org.jetbrains.jet.lang.types.expressions.ExpressionTypingServices;
import org.jetbrains.jet.lang.resolve.TypeResolver;
import org.jetbrains.jet.lang.resolve.calls.CallResolver;
import org.jetbrains.jet.lang.types.lang.KotlinBuiltIns;
import org.jetbrains.jet.lang.ModuleConfiguration;
import com.intellij.openapi.project.Project;
import org.jetbrains.jet.lang.resolve.AnnotationResolver;
import org.jetbrains.jet.lang.resolve.QualifiedExpressionResolver;
import org.jetbrains.jet.lang.resolve.calls.CandidateResolver;
import org.jetbrains.jet.lang.resolve.calls.results.ResolutionResultsHandler;
import org.jetbrains.jet.lang.resolve.calls.results.OverloadingConflictResolver;
import org.jetbrains.annotations.NotNull;
import javax.annotation.PreDestroy;

/* This file is generated by org.jetbrains.jet.di.AllInjectorsGenerator. DO NOT EDIT! */
public class InjectorForTests {

    private DescriptorResolver descriptorResolver;
    private ExpressionTypingServices expressionTypingServices;
    private TypeResolver typeResolver;
    private CallResolver callResolver;
    private KotlinBuiltIns kotlinBuiltIns;
    private ModuleConfiguration moduleConfiguration;
    private final Project project;
    private AnnotationResolver annotationResolver;
    private QualifiedExpressionResolver qualifiedExpressionResolver;
    private CandidateResolver candidateResolver;
    private ResolutionResultsHandler resolutionResultsHandler;
    private OverloadingConflictResolver overloadingConflictResolver;

    public InjectorForTests(
        @NotNull Project project
    ) {
        this.descriptorResolver = new DescriptorResolver();
        this.expressionTypingServices = new ExpressionTypingServices();
        this.typeResolver = new TypeResolver();
        this.callResolver = new CallResolver();
        this.kotlinBuiltIns = KotlinBuiltIns.getInstance();
        this.moduleConfiguration = ModuleConfiguration.EMPTY;
        this.project = project;
        this.annotationResolver = new AnnotationResolver();
        this.qualifiedExpressionResolver = new QualifiedExpressionResolver();
        this.candidateResolver = new CandidateResolver();
        this.resolutionResultsHandler = new ResolutionResultsHandler();
        this.overloadingConflictResolver = new OverloadingConflictResolver();

        this.descriptorResolver.setAnnotationResolver(annotationResolver);
        this.descriptorResolver.setExpressionTypingServices(expressionTypingServices);
        this.descriptorResolver.setTypeResolver(typeResolver);

        this.expressionTypingServices.setCallResolver(callResolver);
        this.expressionTypingServices.setDescriptorResolver(descriptorResolver);
        this.expressionTypingServices.setProject(project);
        this.expressionTypingServices.setTypeResolver(typeResolver);

        this.typeResolver.setAnnotationResolver(annotationResolver);
        this.typeResolver.setDescriptorResolver(descriptorResolver);
        this.typeResolver.setModuleConfiguration(moduleConfiguration);
        this.typeResolver.setQualifiedExpressionResolver(qualifiedExpressionResolver);

        this.callResolver.setCandidateResolver(candidateResolver);
        this.callResolver.setExpressionTypingServices(expressionTypingServices);
        this.callResolver.setResolutionResultsHandler(resolutionResultsHandler);
        this.callResolver.setTypeResolver(typeResolver);

        annotationResolver.setCallResolver(callResolver);
        annotationResolver.setExpressionTypingServices(expressionTypingServices);

        candidateResolver.setExpressionTypingServices(expressionTypingServices);
        candidateResolver.setTypeResolver(typeResolver);

        resolutionResultsHandler.setOverloadingConflictResolver(overloadingConflictResolver);

    }

    @PreDestroy
    public void destroy() {
    }

    public DescriptorResolver getDescriptorResolver() {
        return this.descriptorResolver;
    }

    public ExpressionTypingServices getExpressionTypingServices() {
        return this.expressionTypingServices;
    }

    public TypeResolver getTypeResolver() {
        return this.typeResolver;
    }

    public CallResolver getCallResolver() {
        return this.callResolver;
    }

    public KotlinBuiltIns getKotlinBuiltIns() {
        return this.kotlinBuiltIns;
    }

    public Project getProject() {
        return this.project;
    }

}

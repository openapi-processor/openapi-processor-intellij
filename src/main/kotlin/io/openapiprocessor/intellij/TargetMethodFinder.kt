/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

data class TargetMethod(val project: Project, val uri: String, val method: String)

class TargetMethodFinder {

    fun findTargetMethods(targetMethod: TargetMethod): List<PsiElement> {
        return Annotations
            .withMethod(targetMethod.method)
            .map { findTargetMethods(it, targetMethod) }
            .flatten()
    }

    private fun findTargetMethods(annotation: Annotation, targetMethod: TargetMethod): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()
        val matches = getAnnotations(annotation, targetMethod.project)
        val matchPath = targetMethod.uri

        matches.forEach {
            if (!annotation.matches(it, matchPath))
                return@forEach

            val method = PsiTreeUtil.getParentOfType(it.navigationElement, PsiMethod::class.java)
            if (method != null)
                targets.add(method.navigationElement)
        }

        return targets
    }

    private fun getAnnotations(annotation: Annotation, project: Project): Collection<PsiAnnotation> {
        return JavaAnnotationIndex
            .getInstance()
            .getAnnotations(annotation.name, project, GlobalSearchScope.allScope(project))
    }
}

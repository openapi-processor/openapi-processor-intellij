/*
 * Copyright 2023 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.components.Service
import com.intellij.openapi.project.Project
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiMethod
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiTreeUtil

@Service
class PathTargetService {

    fun findPathTargets(project: Project, path: String): List<PsiMethod> {
        return Annotations.KNOWN
            .map { findPathTargets(project, path, it) }
            .flatten()
    }

    private fun findPathTargets(project: Project, path: String, annotation: Annotation): List<PsiMethod> {
        val targets = mutableListOf<PsiMethod>()
        val matches = getAnnotations(project, annotation)

        matches
            .filter { annotation.matches(it, path) }
            .forEach {
                val method = PsiTreeUtil.getParentOfType(it.navigationElement, PsiMethod::class.java)
                if (method != null)
                    targets.add(method)
            }

        return targets
    }

    private fun getAnnotations(project: Project, annotation: Annotation): Collection<PsiAnnotation> {
        return JavaAnnotationIndex
            .getInstance()
            .getAnnotations(annotation.name, project, GlobalSearchScope.allScope(project))
    }
}

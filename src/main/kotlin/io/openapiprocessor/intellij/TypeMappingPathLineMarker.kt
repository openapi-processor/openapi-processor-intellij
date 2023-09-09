/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiLiteralUtil
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TypeMappingPathLineMarker  : RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val isMapping = isMappingFile(element.containingFile)
        if (!isMapping)
            return

        if (element !is YAMLKeyValue)
            return

        if(element.keyText != "paths")
            return

        // TODO for each path, handle http methods?
        element.value?.children?.forEach { path ->
            if (path !is YAMLKeyValue)
                return@forEach

            val isPath = path.keyText.startsWith("/")
            if (!isPath) {
                log.warn("expected to find path but found {}", path.keyText)
            }

            val targets = findPathTargets(path)

            val builder = NavigationGutterIconBuilder
                .create(Support.ICON)
                .setTooltipTitle("Tooltip Title")
                .setTooltipText(Support.TOOLTIP_TEXT)
                .setPopupTitle(Support.POPUP_TITLE)
                .setEmptyPopupText("Empty popup text")
                .setTargets(targets)
            result.add(builder.createLineMarkerInfo(path.key!!))
        }
    }

    private fun findPathTargets(element: YAMLKeyValue): List<PsiElement> {
        return Support.ANNOTATIONS
            .map {
                findPathTargets(element, it)
            }
            .flatten()
    }

    private fun findPathTargets(uriKey: YAMLKeyValue, annotation: Annotation): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()
        val matches = getAnnotations(annotation, uriKey.project)
        val matchPath = uriKey.keyText

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
        return JavaAnnotationIndex.getInstance()
                     .get(annotation.name, project, GlobalSearchScope.allScope(project))
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        // static method on TypeMappingFileType
        return file.viewProvider.fileType is TypeMappingFileType
    }

    interface Annotation {
        val pkg: String
        val name: String
        val qualifiedName: String
            get() = "$pkg.$name"

        fun matches(psi: PsiAnnotation, path: String): Boolean
    }

    class MicronautAnnotation(override val name: String): Annotation {
        override val pkg: String = "io.micronaut.http.annotation"

        override fun matches(psi: PsiAnnotation, path: String): Boolean {
            if (!psi.hasQualifiedName(qualifiedName))
                return false

            val value = psi.findAttributeValue("uri")
            if (value !is PsiLiteralExpression)
                return false

            val uri = PsiLiteralUtil.getStringLiteralContent(value)
            if (uri != path)
                return false

            return true
        }
    }

    class SpringAnnotation(override val name: String): Annotation {
        override val pkg: String = "org.springframework.web.bind.annotation"

        override fun matches(psi: PsiAnnotation, path: String): Boolean {
            if (!psi.hasQualifiedName(qualifiedName))
                return false

            val value = psi.findAttributeValue("path")
            if (value !is PsiLiteralExpression)
                return false

            val uri = PsiLiteralUtil.getStringLiteralContent(value)
            if (uri != path)
                return false

            return true
        }
    }

    class SpringRequestAnnotation(val method: String): Annotation {
        override val pkg: String = "org.springframework.web.bind.annotation"
        override val name: String = "RequestMapping"

        override fun matches(psi: PsiAnnotation, path: String): Boolean {
            if (!psi.hasQualifiedName(qualifiedName))
                return false

            val methodRef = psi.findAttributeValue("method")
            if (methodRef !is PsiReferenceExpression)
                return false

            if ( methodRef.qualifiedName != "RequestMethod.$method")
                return false

            val value = psi.findAttributeValue("path")
            if (value !is PsiLiteralExpression)
                return false

            val uri = PsiLiteralUtil.getStringLiteralContent(value)
            if (uri != path)
                return false

            return true
        }

    }

    object Support {
        const val TOOLTIP_TEXT = "Navigate to endpoint interface methods"
        const val POPUP_TITLE = "Endpoint Interface Methods"

        val ICON = IconLoader.getIcon(
            "/icons/openapi-processor-p-interface.svg",
            TypeMappingPathLineMarker::class.java
        )
        val ANNOTATIONS = listOf(
            MicronautAnnotation("Delete"),
            MicronautAnnotation("Get"),
            MicronautAnnotation("Head"),
            MicronautAnnotation("Patch"),
            MicronautAnnotation("Post"),
            MicronautAnnotation("Put"),
            MicronautAnnotation("Trace"),
            SpringAnnotation("DeleteMapping"),
            SpringAnnotation("GetMapping"),
            SpringRequestAnnotation("HEAD"),
            SpringAnnotation("PatchMapping"),
            SpringAnnotation("PostMapping"),
            SpringAnnotation("PutMapping"),
            SpringRequestAnnotation("TRACE")
        )
    }

    companion object {}
}


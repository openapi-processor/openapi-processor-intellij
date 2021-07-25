/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.project.Project
import com.intellij.psi.*
import com.intellij.psi.impl.java.stubs.index.JavaAnnotationIndex
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.PsiLiteralUtil
import com.intellij.psi.util.PsiTreeUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TypeMappingPathLineMarker  : RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

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
                .create(ICON)
                .setTooltipTitle("tooltip title")
                .setTooltipText(TOOLTIP_TEXT)
                .setPopupTitle(POPUP_TITLE)
                .setEmptyPopupText("empty popup text")
                .setTargets(targets)
            result.add(builder.createLineMarkerInfo(path.key!!))
        }
    }

    private fun findPathTargets(element: YAMLKeyValue): List<PsiElement> {
        return listOf(
                Annotation("io.micronaut.http.annotation", "Get", "uri"),
                Annotation("io.micronaut.http.annotation", "Post", "uri"),
                Annotation("org.springframework.web.bind.annotation", "GetMapping", "path"),
                Annotation("org.springframework.web.bind.annotation", "PostMapping", "path")
            )
            .map {
                findPathTargets(element, it)
            }
            .flatten()
    }

    private fun findPathTargets(uriKey: YAMLKeyValue, annotation: Annotation): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()
        val matches = getAnnotations(annotation, uriKey.project)
        val matchUri = uriKey.keyText

        matches.forEach {
            if (!it.hasQualifiedName(annotation.qualifiedName))
                return@forEach

            val value = it.findAttributeValue(annotation.attribute)
            if (value !is PsiLiteralExpression)
                return@forEach

            val uri = PsiLiteralUtil.getStringLiteralContent(value)
            if (uri != matchUri)
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

    class Annotation(val pkg: String, val name: String, val attribute: String) {
        val qualifiedName = "$pkg.$name"
    }

    companion object {
        val ICON =  AllIcons.Gutter.ImplementedMethod

        const val TOOLTIP_TEXT = "Navigate to endpoint interface methods"
        const val POPUP_TITLE = "Endpoint Interface Methods"
    }

}


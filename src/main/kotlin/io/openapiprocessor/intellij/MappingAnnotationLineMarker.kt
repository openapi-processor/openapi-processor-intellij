/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.navigation.GotoRelatedItem
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.firstLeaf
import com.intellij.util.IconUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

/**
 * line marker to navigate from mapping annotation (from interface) to the path in the OpenAPI document.
 */
class MappingAnnotationLineMarker: RelatedItemLineMarkerProvider() {
    val log: Logger = LoggerFactory.getLogger(javaClass.name)

    class GotoOpenApi(element: PsiElement): GotoRelatedItem(element, Goto.I18n.GROUP) {

        override fun getCustomIcon(): javax.swing.Icon {
            return Icon.openapi
        }
    }

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val info = createLineMarkerInfo(element) ?: return
        result.add(info)
    }

    private fun createLineMarkerInfo(element: PsiElement): RelatedItemLineMarkerInfo<*>? {
        if (element !is PsiAnnotation) {
            return null
        }

        val match = Annotations.KNOWN.firstOrNull { it.matches(element) }
        if (match == null) {
            return null
        }

        val moduleService = service<ModuleService>()
        val modules = moduleService.findModules(element)

        var searchScope = GlobalSearchScope.EMPTY_SCOPE
        for (module in modules) {
            searchScope = searchScope.uniteWith(GlobalSearchScope.moduleScope(module))
        }

        val apiPath = match.path(element)
        val targets = findPsiElementsOfPath(apiPath!!, searchScope, element.project)
        if (targets.isEmpty()) {
            log.warn("found no targets!")
            return null
        }

        val builder = NavigationGutterIconBuilder
            .create<PsiElement>(
                Icon.openapi,
                { listOf(it) },
                { listOf(GotoOpenApi(it)) })
            .setTooltipText(I18n.TOOLTIP_TEXT)
            .setPopupTitle(I18n.POPUP_TITLE)
            .setTargets(targets)

        val id = element.nameReferenceElement?.firstChild!!

        return builder.createLineMarkerInfo(id)
    }

    private fun findPsiElementsOfPath(path: String, searchScope: GlobalSearchScope, project: Project): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()

        log.debug("looking for yaml file with key '$path'")
        val files = searchForPath(path, searchScope, project)
        files.forEach { f ->
            val psiFile = PsiManager.getInstance(project).findFile(f.file)
            log.debug(">> found psi file {}", f.file.path)

            val psiElement = psiFile?.findElementAt(f.offset)
            if (psiElement != null) {
                log.debug(">> found psi element at position {}", f.offset)
                targets.add(psiElement.firstLeaf())
            } else {
                log.debug(">> found no psi element at position {}", f.offset)
            }
        }

        return targets
    }

    private fun searchForPath(path: String, searchScope: GlobalSearchScope, project: Project): List<YamlKeyWithFile> {
      return findPathInYaml(path, searchScope, project)
    }

    object Icon {
        val openapi = IconUtil.scale(IconLoader.getIcon("/icons/openApi.svg", javaClass), null, 0.875f)
    }

    object I18n {
        val TOOLTIP_TEXT = i18n("line.marker.java.mapping.annotation.tooltip")
        val POPUP_TITLE = i18n("line.marker.java.mapping.annotation.title")
    }
}

/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.module.Module
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
    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

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

        val apiPath = match.path(element)
        val modules = findModules(element)

        var searchScope = GlobalSearchScope.EMPTY_SCOPE
        for (module in modules) {
            searchScope = searchScope.uniteWith(GlobalSearchScope.moduleScope(module))
        }

        val targets = findPsiElementsOfPath(apiPath!!, searchScope, element.project)
        if (targets.isEmpty()) {
            log.warn("found no targets!")
            return null
        }

        val builder = NavigationGutterIconBuilder
            .create(Icon.openapi)
            .setTooltipText(I18n.TOOLTIP_TEXT)
            .setPopupTitle(I18n.POPUP_TITLE)
            .setTargets(*targets.toTypedArray())

        val id = element.nameReferenceElement?.firstChild!!

        return builder.createLineMarkerInfo(id)
    }

    private fun findModules(element: PsiElement): List<Module> {
        val finder = ModuleFinder(element.project)
        val modules = finder.findModules(element.containingFile.virtualFile.presentableUrl)

        if (modules.isNotEmpty()) {
            modules.forEach {
                log.debug("related modules of file '{}'", element.containingFile.name)
                log.debug("found module '{}'", it.name)
            }
        } else {
            log.debug("could not find module of file '{}'", element.containingFile.name)
        }

        return modules
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

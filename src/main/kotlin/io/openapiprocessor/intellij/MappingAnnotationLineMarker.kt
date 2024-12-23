/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.module.Module
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.IconLoader
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiAnnotation
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiManager
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.util.CommonProcessors
import com.intellij.util.IconUtil
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.navigation.YAMLKeysIndex
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MappingAnnotationLineMarker: RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
       if (element !is PsiAnnotation) {
           return
       }

        val match = Annotations.KNOWN
            .firstOrNull { it.matches(element) }

        if (match == null) {
            return
        }

        val apiPath = match.path(element)
        val searchTerm = "paths.${apiPath}"
        val module = findModule(element) ?: return

        val scope = GlobalSearchScope.moduleScope(module)
        val targets = findPsiElement(element, searchTerm, scope)

        val builder = NavigationGutterIconBuilder
            .create(Support.NAVIGATE_TO_OPENAPI)
            .setTooltipTitle(POPUP_TITLE)
            .setTooltipText(TOOLTIP_TEXT)
            .setPopupTitle(POPUP_TITLE)
            .setEmptyPopupText("No match")
            .setTargets(*targets.toTypedArray())

        result.add(builder.createLineMarkerInfo(element))
    }

    private fun findModule(element: PsiElement): Module? {
        val found = ProjectRootManager.getInstance(element.project)
            .fileIndex
            .getModuleForFile(element.containingFile.virtualFile)

        if (found != null) {
            log.debug("found module '{}' of file '{}'", found.name, element.containingFile.name)
        } else {
            log.debug("could not find module of file '{}'", element.containingFile.name)
        }

        return found
    }

    private fun findPsiElement(element: PsiElement, searchTerm: String, searchScope: GlobalSearchScope): List<PsiElement> {
        val targets = mutableListOf<PsiElement>()

        log.debug("looking for yaml file with key '$searchTerm'")
        val files = CommonProcessors.CollectProcessor<VirtualFile>()
        FileBasedIndex.getInstance().getFilesWithKey(YAMLKeysIndex.KEY, setOf(searchTerm), files, searchScope)
        files.results.forEach { f ->
            log.debug(">> found file '${f.path}'")
        }

        log.debug("looking for position '$searchTerm'")
        files.results.forEach { f ->
            val position = FileBasedIndex.getInstance().getFileData(YAMLKeysIndex.KEY, f, element.project)[searchTerm]
            log.debug(">> found position {} in '${f.path}'", position)

            if(position == null) {
                return@forEach
            }

            val psiFile = PsiManager.getInstance(element.project).findFile(f)
            val psiElement = psiFile?.findElementAt(position)

            if (psiElement != null) {
                log.debug(">> found psi element at position {}", position)
                targets.add(psiElement)
            } else {
                log.debug(">> found no psi element at position {}", position)
            }
        }

        return targets
    }

    object Support {
        val NAVIGATE_TO_OPENAPI = IconUtil.scale(IconLoader.getIcon("/icons/openApi.svg", javaClass), null, 0.875f)
    }

    companion object {
        const val TOOLTIP_TEXT = "Navigate to OpenAPI path"
        const val POPUP_TITLE = "Navigate to OpenAPI"
    }
}

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
import com.intellij.openapi.roots.ProjectRootManager
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.search.GlobalSearchScope
import com.intellij.psi.util.firstLeaf
import com.intellij.util.IconUtil
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class MappingAnnotationLineMarker: RelatedItemLineMarkerProvider() {
    private val log: Logger = LoggerFactory.getLogger(javaClass.name)

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        if (element !is PsiIdentifier) {
            return
        }

        val annotation = getAnnotation(element) ?: return

        val match = Annotations.KNOWN.firstOrNull { it.matches(annotation) }
        if (match == null) {
            return
        }

        val apiPath = match.path(annotation)
        val module = findModule(element) ?: return

        val scope = GlobalSearchScope.moduleScope(module)
        val targets = findPsiElementsOfPath(apiPath!!, scope, element.project)

        if (targets.isEmpty()) {
            log.warn("found no targets!")
            return
        }

        val builder = NavigationGutterIconBuilder
            .create(icon)
            .setTooltipText(I18n.TOOLTIP_TEXT)
            .setPopupTitle(I18n.POPUP_TITLE)
            .setTargets(*targets.toTypedArray())

        result.add(builder.createLineMarkerInfo(element))
    }

    private fun getAnnotation(id: PsiIdentifier): PsiAnnotation? {
        val reference = id.parent as? PsiJavaCodeReferenceElement ?: return null
        val annotation = reference.parent as? PsiAnnotation ?: return null
        return annotation
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

    private val icon = IconUtil.scale(IconLoader.getIcon("/icons/openApi.svg", javaClass), null, 0.875f)

    object I18n {
        val TOOLTIP_TEXT = i18n("line.marker.java.mapping.annotation.tooltip")
        val POPUP_TITLE = i18n("line.marker.java.mapping.annotation.title")
    }
}

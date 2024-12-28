/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.components.service
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.IconUtil
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class TypeMappingPathLineMarker : RelatedItemLineMarkerProvider() {
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

        val pathTargetService = service<PathTargetService>()

        // TODO for each path, handle http methods?
        element.value?.children?.forEach { path ->
            if (path !is YAMLKeyValue)
                return@forEach

            val isPath = path.keyText.startsWith("/")
            if (!isPath) {
                log.warn("expected to find path but found {}", path.keyText)
            }

            val targets = pathTargetService.findPathTargets(path.project, path.keyText)

            if (targets.isEmpty()) {
                log.warn("found no targets!")
                return@forEach
            }

            val builder = NavigationGutterIconBuilder
                .create(icon)
                .setTooltipText(I18n.TOOLTIP_TEXT)
                .setPopupTitle(I18n.POPUP_TITLE)
                .setTargets(targets)
            result.add(builder.createLineMarkerInfo(path.key!!))
        }
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        // static method on TypeMappingFileType
        return file.viewProvider.fileType is TypeMappingFileType
    }

    private val icon = IconUtil.scale(AllIcons.Nodes.Interface, null, 0.875f)

    object I18n {
        val TOOLTIP_TEXT = i18n("line.marker.type.mapping.path.tooltip")
        val POPUP_TITLE = i18n("line.marker.type.mapping.path.title")
    }

    companion object {
        const val TOOLTIP_TEXT = "Navigate to endpoint interface methods"
    }
}


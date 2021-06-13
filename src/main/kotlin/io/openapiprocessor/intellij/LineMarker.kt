/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.project.guessModuleDir
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.util.IconLoader
import com.intellij.psi.*
import com.intellij.psi.impl.file.PsiJavaDirectoryFactory
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.serialization.ClassUtil
import com.intellij.testFramework.JavaPsiTestCase
import org.jetbrains.yaml.psi.YAMLKeyValue
import javax.swing.Icon
import com.intellij.openapi.roots.ProjectRootManager

import com.intellij.openapi.vfs.VirtualFile


class LineMarker: RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val isMapping = isMappingFile(element.containingFile)
        if (!isMapping)
            return

        val vfile = element.containingFile.virtualFile

        if (element.text == "package-name") {
            val keyValue = element.parent
            if (keyValue is YAMLKeyValue) {
                val pkgName = keyValue.valueText

                val project = element.parent.project
                val psi = JavaPsiFacade.getInstance(project)
                val pkg = psi.findPackage("$pkgName") // api
                val dirs = pkg?.directories
                dirs?.forEach {
                    println("pkg dir: $it")
                }

                val module = ModuleUtil.findModuleForFile(vfile, element.project)
                println("module name: " + module?.name)

                val moduleDir = module?.guessModuleDir()
                println("module dir: $moduleDir")

                val root = module?.rootManager
                root?.contentRootUrls?.forEach {
                    println("content root: $it")
                }
                root?.sourceRootUrls?.forEach {
                    println("source root: $it")
                }

                val moduleSourceRoot = ProjectRootManager.getInstance(project)
                    .fileIndex.getSourceRootForFile(vfile)
                println("module source root: $moduleSourceRoot")


//                val p = pkg?.parent

//                println(MyBundle.message("applicationService"))
                // content root => source roots
                // find navigate to dir in current project module

//                final ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
//                final Module module = index.getModuleForFile(virtualFile);

                val moduleContentScope = module?.moduleContentScope
//                val root = module?.rootManager

                val builder = NavigationGutterIconBuilder.create(getPackageIcon())
                    .setTargets(pkg)
                    .setTooltipText("Navigate to generated package")

                result.add(builder.createLineMarkerInfo(element))
            }
        }

        super.collectNavigationMarkers(element, result)
    }

    fun getPackageIcon(): Icon {
        return IconLoader.getIcon("/META-INF/pluginIcon.svg")
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        return file.viewProvider.fileType is TypeMapping
    }

}

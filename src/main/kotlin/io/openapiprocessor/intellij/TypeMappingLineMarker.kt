/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.codeInsight.navigation.NavigationGutterIconBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.compiler.CompilerPaths
import com.intellij.openapi.components.service
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.module.ModuleUtil
import com.intellij.openapi.project.rootManager
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.JavaPsiFacade
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiElement
import com.intellij.psi.PsiFile
import com.intellij.util.indexing.FileBasedIndex
import org.jetbrains.yaml.psi.YAMLKeyValue
import javax.swing.Icon


class TypeMappingLineMarker: RelatedItemLineMarkerProvider() {

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {
        val isMapping = isMappingFile(element.containingFile)
        if (!isMapping)
            return

//        val index = FileBasedIndex.getInstance()
//            .getFileData(TypeMappingIndex.INDEX_NAME, element.containingFile.virtualFile, element.project)


        if (element.text != "package-name")
            return

        val keyValue = element.parent
        if (keyValue !is YAMLKeyValue)
            return

        val service = element.project.service<PackageService>()
        service.foo()

        val generatedPackage = service.findGeneratedPackage(
            keyValue.valueText, keyValue.containingFile
        )

        val prj = element.project
        val psi = JavaPsiFacade.getInstance(prj)
        val pkg = psi.findPackage(keyValue.valueText)


//        val candidates = pkg?.directories
//        candidates?.forEach {
//            println("candidate pkg dir: ${it.virtualFile.path}")
//        }

//        val mappingPath = element.containingFile.virtualFile
//        println("mapping path: ${mappingPath.path}")

//        val moduleSourceRoot = ProjectRootManager.getInstance(prj).fileIndex
//            .getSourceRootForFile(mappingPath)

        val mappingModule = ModuleUtil.findModuleForFile(element.containingFile)
        println("mapping module: ${mappingModule?.name}")

        val root = mappingModule?.rootManager

        println("mapping module content roots")
        val contentRoots = mutableListOf<VirtualFile>()
        root?.contentRoots?.forEach {
            contentRoots.add(it)
            println("  content root: ${it.path}")
        }

        println("mapping module source roots")
        root?.sourceRootUrls?.forEach {
            println("  source root: ${it}")
        }

        // useless
//        val mOutputPath = CompilerPaths.getModuleOutputPath(mappingModule, false)
//        println("  output path: $mOutputPath")

        val contentRootPath: String
        val moduleCandidates = mutableListOf<PsiDirectory>()

        if (contentRoots.size == 1) {
            println("content root reduced packages packages")

            contentRootPath = contentRoots.first().path

            pkg?.directories?.forEach {
                if (it.virtualFile.path.startsWith(contentRootPath)) {
                    moduleCandidates.add(it)
                }
            }

            moduleCandidates.forEach {
                println("module candidates pkg dir: ${it.virtualFile.path}")
            }
        } else {
            contentRootPath = "-"
        }

        // find source root  that matches output path
        // find matching pkg dir

        println("#####")

//        root?.contentRootUrls?.forEach {
//            println("mapping module content root: $it")
//        }
//        root?.sourceRootUrls?.forEach {
//            println("mapping module source root: $it")
//        }

        println("all modules")

        val modulesAll = ModuleManager.getInstance(prj).modules
        val contentRootsAll = mutableListOf<VirtualFile>()
        val sourceRootsAll = mutableListOf<String>()
        val outputPathsAll = mutableListOf<String>()

        for (m in modulesAll) {
            val root = m?.rootManager
            root?.contentRoots?.forEach {
                if (it.path.startsWith(contentRootPath)) {
                    contentRootsAll.add(it)

                    println("  matching content root: ${m.name} $it")
                }
            }

            root?.sourceRootUrls?.forEach {
                if (it.startsWith(contentRootPath)) {
                    sourceRootsAll.add(it)

                    println("  matching source root: ${m.name} $it")
                }
            }

            val mOutputPath = CompilerPaths.getModuleOutputPath(m, false)
            if (mOutputPath != null && mOutputPath.startsWith(contentRootPath)) {
                outputPathsAll.add(mOutputPath)
                println("  matching output path: $mOutputPath")
            }
        }

        val prefixes = mutableSetOf<String>()
        outputPathsAll.forEach { o ->
            moduleCandidates.forEach { c ->
                val prefix = o.commonPrefixWith(c.virtualFile.path)
                if (prefix.isNotEmpty()) {
                    prefixes.add(prefix)
                }
            }
        }

        var longestPrefix: String = ""
        prefixes.forEach {
            println("common prefix: $it")
            if (it.length > longestPrefix.length) {
                longestPrefix = it
            }
        }

        println("longest common prefix: $longestPrefix")

        var targetPkg: PsiDirectory? = null
        moduleCandidates.forEach {
            if (it.virtualFile.path.startsWith(longestPrefix)) {
                targetPkg = it
                println("found target: $it")
            }
        }

        println("-----")

        val outputPaths = CompilerPaths.getOutputPaths(modulesAll)
        for (o in outputPaths) {
            println("modules output path: $o")
        }

//        val moduleDir = module?.guessModuleDir()
//        println("module dir: $moduleDir")


//        val moduleOutputPath = CompilerPaths.getModuleOutputPath(module, false)
//        val outputPaths = CompilerPaths.getOutputPaths(ModuleManager.getInstance(project).modules)






//        val vfile = element.containingFile.virtualFile
//
//        if (element.text == "package-name") {
//            val keyValue = element.parent
//            if (keyValue is YAMLKeyValue) {
//                val pkgName = keyValue.valueText
//
//                val project = element.parent.project
//                val psi = JavaPsiFacade.getInstance(project)
//                val pkg = psi.findPackage("$pkgName") // api
//                val dirs = pkg?.directories
//                dirs?.forEach {
//                    println("pkg dir: $it")
//                }
//
//                val module = ModuleUtil.findModuleForFile(vfile, element.project)
//                println("module name: " + module?.name)
//
//                val instance = CompilerModuleExtension.getInstance(module)
//                val compilerOutputPath = instance?.compilerOutputPath
//
//                val moduleOutputPath = CompilerPaths.getModuleOutputPath(module, false)
//                val outputPaths = CompilerPaths.getOutputPaths(ModuleManager.getInstance(project).modules)
//
//
//                val moduleDir = module?.guessModuleDir()
//                println("module dir: $moduleDir")
//
//                val root = module?.rootManager
//                root?.contentRootUrls?.forEach {
//                    println("content root: $it")
//                }
//                root?.sourceRootUrls?.forEach {
//                    println("source root: $it")
//                }
//
//                val moduleSourceRoot = ProjectRootManager.getInstance(project)
//                    .fileIndex.getSourceRootForFile(vfile)
//                println("module source root: $moduleSourceRoot")
//
//                val modules = ModuleUtil.getAllDependentModules(module!!)
//                println("modules:" + modules.toString())
//
//                val moduleGraph = ModuleManager.getInstance(project).moduleGraph()
//                val modules1 = ModuleManager.getInstance(project).modules
//                for (module in modules1) {
//                    val root = module?.rootManager
//                    root?.contentRootUrls?.forEach {
//                        println("content root: ${module.name} $it")
//                    }
//                    root?.sourceRootUrls?.forEach {
//                        println("source root: ${module.name} $it")
//                    }
//                }
////                val p = pkg?.parent
//
////                println(MyBundle.message("applicationService"))
//                // content root => source roots
//                // find navigate to dir in current project module
//
////                final ProjectFileIndex index = ProjectRootManager.getInstance(project).getFileIndex();
////                final Module module = index.getModuleForFile(virtualFile);
//
//                val moduleContentScope = module?.moduleContentScope
////                val root = module?.rootManager
//
//                val builder = NavigationGutterIconBuilder.create(getPackageIcon())
//                    .setTargets(targetPkg)
//                    .setTooltipText("Navigate to generated package")
//
//                result.add(builder.createLineMarkerInfo(element))
//            }
//        }

        val builder = NavigationGutterIconBuilder.create(getPackageIcon())
            .setTargets(targetPkg)
            .setTooltipText("Navigate to generated package")

        result.add(builder.createLineMarkerInfo(element))

        super.collectNavigationMarkers(element, result)
    }

    private fun getPackageIcon(): Icon {
        return AllIcons.Modules.GeneratedFolder
    }

    private fun isMappingFile(file: PsiFile): Boolean {
        return file.viewProvider.fileType is TypeMappingFileType
    }

}

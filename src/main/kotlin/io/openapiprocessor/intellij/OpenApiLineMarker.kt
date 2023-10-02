package io.openapiprocessor.intellij

import com.intellij.codeInsight.daemon.RelatedItemLineMarkerInfo
import com.intellij.codeInsight.daemon.RelatedItemLineMarkerProvider
import com.intellij.psi.PsiElement
import com.intellij.psi.search.searches.ReferencesSearch
//import com.intellij.swagger.index.OpenapiSpecificationContentIndex
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YAMLMapping
import org.slf4j.Logger
import org.slf4j.LoggerFactory

class OpenApiLineMarker: RelatedItemLineMarkerProvider()  {
    private val log: Logger = LoggerFactory.getLogger(this.javaClass.name)

    override fun collectNavigationMarkers(
        element: PsiElement,
        result: MutableCollection<in RelatedItemLineMarkerInfo<*>>
    ) {

        log.warn("language {} {}", element.language, element.text)
//        val isMapping = isMappingFile(element.containingFile)
//        if (!isMapping)

//        if (element.text != TypeMappingLineMarker.PACKAGE_KEY)
//            return

        val isMethod = Methods.methods.contains(element.text) // LeafPsiElement
        if (!isMethod) {
            return
        }


        // is path?
//        val path = element.text.startsWith("/")
//        if (!path) {
//            return
//        }

//        element.children.forEach {
//            log.warn("element child {}", it.text)
//        }

        val elementKeyValue = element.parent
        if (elementKeyValue !is YAMLKeyValue)
            return

        elementKeyValue.children.forEach {
            log.warn("parent child\n {}", it.text)
        }

        val yamlMapping = elementKeyValue.parent
        if (yamlMapping !is YAMLMapping)
            return

        val xxx = yamlMapping.parent
        log.warn("xxx {}", xxx.text)


        yamlMapping.children.forEach {
            log.warn("parent 2 child {}", it.text)
        }

        val file = element.containingFile

//        val apiFiles = OpenapiSpecificationContentIndex.getAllIndexedFiles(element.project)
//        log.warn("apiFiles {}", apiFiles.toString())
//
//        val basePaths = OpenapiSpecificationContentIndex.getBasePathsForFile(file)
//        log.warn("basePaths {}", basePaths.toString())

        val searchResult = ReferencesSearch.search(file)
        searchResult.forEach {
            log.warn("result {}", it.canonicalText)
            // it.element  => bar.yaml
            // parent => $ref
            // parent => ...
            // parent => /bar
//            val rrs = it.resolveReference()
//            rrs.forEach { itrrs ->
//                log.warn("rrs {}", itrrs.toString())
//            }

            val referencing = it.element
            log.warn("referencing {} {}", referencing.text, referencing.containingFile)

            val resolve = it.resolve()
            log.warn("resolved {} {}", resolve?.text, resolve?.containingFile)
        }

//        PsiManager.getInstance(element.project).
//        FileTypeIndex.getFiles()
//        FilenameIndex.getFilesByName()

//        val paths = keyValue.keyText == "paths"

        // element.parent.parent.parent.text
        // key start with / and parent is paths:

//        log.warn("yaml paths {}", paths)
    }

    object Methods {
        val methods = listOf("delete", "get", "head", "patch", "post", "put", "trace")
    }
}

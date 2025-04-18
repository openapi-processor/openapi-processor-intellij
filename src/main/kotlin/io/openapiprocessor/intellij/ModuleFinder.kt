/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleManager
import com.intellij.openapi.project.Project
import com.intellij.platform.backend.workspace.WorkspaceModel
import com.intellij.platform.workspace.jps.entities.ModuleEntity
import com.intellij.platform.workspace.jps.entities.sourceRoots
import java.nio.file.Path
import kotlin.io.path.name
import kotlin.math.min

class ModuleFinder(private val project: Project) {

    fun findModules(sourceUrl: String): List<Module> {
        val source = getRelativeUrl(sourceUrl)

        val results = mutableMapOf<ModuleEntity, List<String>>()

        val wm = WorkspaceModel.getInstance(project)

        wm.currentSnapshot.entities(ModuleEntity::class.java)
            .filter { it.sourceRoots.isNotEmpty() }
            .forEach { moduleEntity ->
                val sourceRoots = mutableListOf<Path>()

                moduleEntity.sourceRoots.forEach {
                    sourceRoots.add(Path.of(getRelativeUrl(it.url.presentableUrl)))
                }

                val matches = matchPaths(Path.of(source), sourceRoots)

                if (matches.isNotEmpty()) {
                    results.put(moduleEntity, matches)
                }
            }

        val moduleManager = ModuleManager.getInstance(project)
        val modules = results.filter {  it.value.isNotEmpty() }
            .keys
            .map { m -> moduleManager.findModuleByName(m.name)!! }

        return modules
    }

    private fun matchPaths(source: Path, candidates: List<Path>): List<String> {
        var matchingItems = listOf<String>()

        val sourceItems = splitPath(source)

        for (candidate in candidates) {
            val canSplit = splitPath(candidate)

            val min = min(sourceItems.size, canSplit.size)
            val matching = mutableListOf<String>()

            for (i in 0.. min - 1) {
                if (sourceItems[i] != canSplit[i]) {
                    break
                }

                matching.add(sourceItems[i])
            }

            if (matching.size > matchingItems.size) {
                matchingItems = matching
            }
        }

        return matchingItems
    }

    private fun splitPath(path: Path): List<String> {
        val result = mutableListOf<String>()
        path.forEach { result.add(it.name) }
        return result
    }

    private fun getRelativeUrl(sourceUrl: String): String {
        return sourceUrl.substring(project.presentableUrl!!.length)
    }
}

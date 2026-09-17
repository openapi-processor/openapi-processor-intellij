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
        val sourcePath = Path.of(source)

        val results = mutableMapOf<ModuleEntity, List<String>>()

        val wm = WorkspaceModel.getInstance(project)
        val allEntities = wm.currentSnapshot.entities(ModuleEntity::class.java)
            .filter { it.sourceRoots.isNotEmpty() }
            .toList()

        // 1. Find the single best matching module for the source file
        var bestMatchEntity: ModuleEntity? = null
        var longestMatchSize = 0

        for (moduleEntity in allEntities) {
            val sourceRoots = moduleEntity.sourceRoots.map {
                Path.of(getRelativeUrl(it.url.presentableUrl))
            }
            val matches = matchPaths(sourcePath, sourceRoots)

            if (matches.size > longestMatchSize) {
                longestMatchSize = matches.size
                bestMatchEntity = moduleEntity
            }
        }

        if (bestMatchEntity == null) {
            return emptyList()
        }

        // 2. Extract the base subproject name (e.g. "dis-plant" from "dis-plant.main")
        val bestName = bestMatchEntity.name
        val baseSubprojectName = if (bestName.contains(".")) {
            bestName.substringBeforeLast(".")
        } else {
            bestName
        }

        // 3. Collect all modules belonging to this same subproject (e.g. dis-plant.api, dis-plant.test)
        val siblingEntities = allEntities.filter { entity ->
            entity.name == baseSubprojectName || entity.name.startsWith("$baseSubprojectName.")
        }

        val moduleManager = ModuleManager.getInstance(project)
        return siblingEntities.mapNotNull { moduleManager.findModuleByName(it.name) }
    }

    private fun matchPaths(source: Path, candidates: List<Path>): List<String> {
        var matchingItems = listOf<String>()
        val sourceItems = splitPath(source)

        for (candidate in candidates) {
            val canSplit = splitPath(candidate)
            val minSize = min(sourceItems.size, canSplit.size)
            val matching = mutableListOf<String>()

            for (i in 0..< minSize) {
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
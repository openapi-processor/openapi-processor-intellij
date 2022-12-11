/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.openapi.module.Module
import com.intellij.openapi.module.ModuleType
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.openapi.vfs.VirtualFileManager
import com.intellij.psi.PsiDirectory
import com.intellij.psi.PsiFile
import com.intellij.testFramework.HeavyPlatformTestCase
import java.nio.file.Path

/**
 * improve/extend api of [HeavyPlatformTestCase]
 */
abstract class HeavyBaseTestCase : HeavyPlatformTestCase() {

    val virtualFileManager: VirtualFileManager
        get() = VirtualFileManager.getInstance()

    lateinit var base: VirtualFile

    fun initBase(): VirtualFile {
        base = virtualFileManager.refreshAndFindFileByNioPath(Path.of(project.basePath!!))!!
        return base
    }

    fun createEmptyModule(moduleName: String, modulePath: String): Module {
        return createModuleAt(moduleName, project, ModuleType.EMPTY, Path.of(modulePath))
    }

    fun getBaseRelativePsiFile(path: String): PsiFile {
        return psiManager.findFile(getBaseRelativeFile(path))!!
    }

    fun getBaseRelativePsiDir(path: String): PsiDirectory {
        return psiManager.findDirectory(getBaseRelativeFile(path))!!
    }

    private fun getBaseRelativeFile(path: String): VirtualFile {
        return base.findFileByRelativePath(path)!!
    }
}

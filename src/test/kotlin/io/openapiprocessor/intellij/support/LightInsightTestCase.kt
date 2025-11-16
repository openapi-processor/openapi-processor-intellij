/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.openapi.vfs.VfsUtil.getUrlForLibraryRoot
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.fixtures.LightJavaCodeInsightFixtureTestCase5
import java.io.File

open class LightInsightTestCase: LightJavaCodeInsightFixtureTestCase5() {

    fun openFile(path: String): VirtualFile {
        return fixture.configureByFile(path).virtualFile
    }

    fun createDir(path: String): VirtualFile {
        return fixture.tempDirFixture.findOrCreateDir(path)
    }

    fun addJar(path: String, name: String) {
        PsiTestUtil.addLibrary(
            fixture.module,
            name,
            getUrlForLibraryRoot(File("${path}/${name}"))
        )
    }
}

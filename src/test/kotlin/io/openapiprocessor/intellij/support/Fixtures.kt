/*
 * Copyright 2025 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij.support

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture

fun TestFixture<PsiDirectory>.virtualDirFixture(path: String): TestFixture<VirtualFile> {
    return testFixture {
        val parent = this@virtualDirFixture.init()

        val dir = edtWriteAction {
            VfsUtil.createDirectoryIfMissing(parent.virtualFile, path)
        }

        initialized(dir) {
          edtWriteAction {
            dir.delete(parent)
          }
        }
    }
}

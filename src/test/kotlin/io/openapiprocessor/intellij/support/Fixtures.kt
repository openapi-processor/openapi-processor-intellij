package io.openapiprocessor.intellij.support

import com.intellij.openapi.application.edtWriteAction
import com.intellij.openapi.module.Module
import com.intellij.openapi.vfs.VfsUtil
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.PsiDirectory
import com.intellij.testFramework.PsiTestUtil
import com.intellij.testFramework.junit5.fixture.TestFixture
import com.intellij.testFramework.junit5.fixture.testFixture
import com.intellij.util.io.directoryContent
import com.intellij.util.io.generateInVirtualTempDir

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

/*
 * Copyright 2021 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.util.indexing.*
import com.intellij.util.indexing.*
import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.EnumeratorStringDescriptor
import com.intellij.util.io.KeyDescriptor
import org.jetbrains.yaml.psi.YAMLKeyValue
import org.jetbrains.yaml.psi.YamlRecursivePsiElementVisitor
import java.io.DataInput
import java.io.DataOutput

class TypeMappingIndex: FileBasedIndexExtension<String, String>() {

    override fun getName(): ID<String, String> {
        return INDEX_NAME
    }

    override fun getInputFilter(): FileBasedIndex.InputFilter {
        return DefaultFileTypeSpecificInputFilter(TypeMappingFileType())
    }

    override fun dependsOnFileContent(): Boolean {
        return true
    }

    override fun getKeyDescriptor(): KeyDescriptor<String> {
        return EnumeratorStringDescriptor.INSTANCE
    }

    override fun getValueExternalizer(): DataExternalizer<String> {

        return object : DataExternalizer<String> {
            override fun save(out: DataOutput, value: String) {
                out.writeUTF(value)
            }

            override fun read(`in`: DataInput): String {
                return `in`.readUTF()
            }
        }
    }

    override fun getIndexer(): DataIndexer<String, String, FileContent> {
        return DataIndexer<String, String, FileContent> { inputData ->
            val m = mutableMapOf<String, String>()
            println("DataIndexer")

            m.put("foo", "bar")

            inputData.psiFile.accept(object : YamlRecursivePsiElementVisitor() {
                override fun visitKeyValue(keyValue: YAMLKeyValue) {
                    println("visitKeyValue")
                    super.visitKeyValue(keyValue)
                }
            })

            m
        }
    }

    override fun getVersion(): Int {
        return 1
    }

    companion object {
        val INDEX_NAME = ID.create<String, String>("io.openapiprocessor.mapping")
    }

}

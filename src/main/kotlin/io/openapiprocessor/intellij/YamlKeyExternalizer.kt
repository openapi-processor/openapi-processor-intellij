/*
 * Copyright 2024 https://github.com/openapi-processor/openapi-processor-intellij
 * PDX-License-Identifier: Apache-2.0
 */

package io.openapiprocessor.intellij

import com.intellij.util.io.DataExternalizer
import com.intellij.util.io.DataInputOutputUtil
import com.intellij.util.io.EnumeratorStringDescriptor
import java.io.DataInput
import java.io.DataOutput

class YamlKeyExternalizer : DataExternalizer<List<YamlKey>> {

    override fun save(out: DataOutput, value: List<YamlKey>) {
        DataInputOutputUtil.writeSeq(out, value) {
            EnumeratorStringDescriptor.INSTANCE.save(out, it.key)
            DataInputOutputUtil.writeINT(out, it.offset)
        }
    }

    override fun read(`in`: DataInput): List<YamlKey> {
        return DataInputOutputUtil.readSeq(`in`) {
            val key = EnumeratorStringDescriptor.INSTANCE.read(`in`)
            val offset = DataInputOutputUtil.readINT(`in`)
            YamlKey(key, offset)
        }
    }
}

package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class FieldToMdcParamMapperTest {
    @Test
    fun `should get right value`() {
        val field = TestFieldsClass::class.java
            .declaredFields
            .first { it.name == "entityId" }
        field.trySetAccessible()

        val mapper = MethodArgFieldToMdcFieldMapper("entityId", 0, field)

        val testFields = TestFieldsClass("initialValue").apply { bar = 123 }
        testFields.bar = 54345

        assertEquals(testFields.entityId, mapper.getValue(arrayOf(testFields)))
    }
}

open class TestFieldsClass(
    initialValue: String?
) {
    open var entityId: String? = initialValue
    var bar: Long? = null
}
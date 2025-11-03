package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class MethodArgFieldToMdcFieldMapperTest {
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

    class TestFieldsClass(
        initialValue: String?
    ) {
        var entityId: String? = initialValue
        var bar: Long? = null
    }
}
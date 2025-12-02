package ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Test
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestDataClass

class MethodArgSpElToMdcFieldMapperTest {
    @Test
    fun `should get right value`() {
        val mapper = MethodArgSpElToMdcFieldMapper("field", 0, "field1")
        val testDataClass = TestDataClass("qw4351-20i", true)
        assertEquals(testDataClass.field1, mapper.getValue(arrayOf(testDataClass)))
    }

    @Test
    fun `should get right value from class with map`() {
        val mapper = MethodArgSpElToMdcFieldMapper("field", 0, "field2['key2']")
        val testDataClass = TestDataClassWithMap("qw4351-20i", mapOf("key" to "value", "key2" to "value2"))
        assertEquals(testDataClass.field2["key2"], mapper.getValue(arrayOf(testDataClass)))
    }
}

private data class TestDataClassWithMap(
    val field1: String,
    val field2: Map<String, String>,
)

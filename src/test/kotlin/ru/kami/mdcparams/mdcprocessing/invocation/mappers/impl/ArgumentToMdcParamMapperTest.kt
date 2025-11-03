package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test

class ArgumentToMdcParamMapperTest {
    @Test
    fun `should get right value`() {
        val mapper = MethodArgToMdcFieldMapper("paramName", 1)

        assertEquals("param1", mapper.getValue(arrayOf("param0", "param1", "param2")))
        assertNull(mapper.getValue(arrayOf("param0")))
        assertNull(mapper.getValue(arrayOf("param0", null, "param2")))
        assertNull(mapper.getValue(null))

        assertEquals(1L, mapper.getValue(arrayOf(2, 1L, 3)))
        assertEquals(TestEnum.FOOBAR, mapper.getValue(arrayOf<Any?>("param0", TestEnum.FOOBAR, 4345)))
    }
}

private enum class TestEnum {
    FOO,
    BAR,
    FOOBAR,
}
package ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.SimpleTestBean
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.WrongSimpleTestBean
import kotlin.reflect.jvm.javaMethod

class MdcBeanPostProcessorParseMethodTest {
    // parseMethod
    //   - должен парсить методы, не аннотированные запрещающими анноташками
    //   - должен падать ? если у метода несколько параметров аннотировано одинаковыми mdc-полями
    //   - должен пропускать методы без аннотированных параметров

    private val processor = MdcBeanPostProcessor()

    @Test
    fun `should not parse method with denied annotation`() {
        val deniedMethod = SimpleTestBean::deniedMethod.javaMethod!!

        val methodParseResult = processor.parseMethod(deniedMethod)
        assertNull(methodParseResult)
    }

    @Test
    fun `should not parse method without annotated parameters`() {
        val methodWithoutAnnotatedParams = SimpleTestBean::methodWithoutAnnotatedParams.javaMethod!!

        val methodParseResult = processor.parseMethod(methodWithoutAnnotatedParams)
        assertNull(methodParseResult)
    }

    @Test
    fun `should parse method with annotated parameters`() {
        val methodWithAnnotatedParams = SimpleTestBean::methodWithParamFromDataClass.javaMethod!!

        val methodParseResult = processor.parseMethod(methodWithAnnotatedParams)
        assertNotNull(methodParseResult)
        assertEquals(4, methodParseResult!!.mapper.argMappers.size)
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "methodName" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "entityId" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "field1" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "field2" })
    }

    @Test
    fun `should fail if method has several annotated parameters with the same mdc field`() {
        val method = WrongSimpleTestBean::methodWithAmbiguousAnnotatedParams.javaMethod!!

        assertThrows<IllegalArgumentException> { processor.parseMethod(method) }
    }

    @Test
    fun `should not fail if method has several annotated parameters with the same mdc field and ignoreDuplicates = true`() {
        val method = WrongSimpleTestBean::methodWithAmbiguousAnnotatedParamsAllowed.javaMethod!!

        val methodParseResult = processor.parseMethod(method)
        assertNotNull(methodParseResult)
        assertEquals(4, methodParseResult!!.mapper.argMappers.size)
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "methodName" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "entityId" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "field1" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "field2" })
    }
}

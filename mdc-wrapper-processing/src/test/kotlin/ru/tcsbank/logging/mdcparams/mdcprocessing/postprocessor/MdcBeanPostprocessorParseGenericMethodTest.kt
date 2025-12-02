package ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Assertions.assertNull
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.LongEvent
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestBeanWithLongEvent
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestBeanWithWrongTypes
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestStringBean
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestStringInterfaceImpl

class MdcBeanPostprocessorParseGenericMethodTest {
    // parseMethod
    //   - должен парсить методы, не аннотированные запрещающими анноташками
    //   - должен падать ? если у метода несколько параметров аннотировано одинаковыми mdc-полями
    //   - должен пропускать методы без аннотированных параметров

    private val processor = MdcBeanPostProcessor()

    @Test
    fun `should not parse method with denied annotation`() {
        val method = TestStringBean::class.java.methods
            .first { it.name == "deniedFoo" }

        val methodParseResult = processor.parseMethod(method)
        assertNull(methodParseResult)
    }

    @Test
    fun `should parse method with generic type`() {
        val methodWithAnnotatedParams = TestStringBean::class.java.methods
            .first { it.name == "foo" }

        val methodParseResult = processor.parseMethod(methodWithAnnotatedParams)
        assertNotNull(methodParseResult)
        assertEquals(2, methodParseResult!!.mapper.argMappers.size)
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "methodName" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "entityId" })
        assertEquals("123", methodParseResult.mapper.argMappers.first().getValue(arrayOf("123")))
    }

    @Test
    fun `should parse method with ancestor generic type`() {
        val methodWithAnnotatedParams = TestBeanWithWrongTypes::class.java.methods
            .first { it.name == "foo" }

        val methodParseResult = processor.parseMethod(methodWithAnnotatedParams)
        assertNotNull(methodParseResult)
        assertEquals(2, methodParseResult!!.mapper.argMappers.size)
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "methodName" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "entityId" })
        assertEquals("123", methodParseResult.mapper.argMappers.first().getValue(arrayOf("123")))
    }

    @Test
    fun `should parse method with generic type from interface`() {
        val methodWithAnnotatedParams = TestStringInterfaceImpl::class.java.methods
            .first { it.name == "foo" }

        val methodParseResult = processor.parseMethod(methodWithAnnotatedParams)
        assertNotNull(methodParseResult)
        assertEquals(2, methodParseResult!!.mapper.argMappers.size)
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "methodName" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "entityId" })
        assertEquals("123", methodParseResult.mapper.argMappers.first().getValue(arrayOf("123")))
    }

    @Test
    fun `should parse method from generic class`() {
        val methodWithAnnotatedParams = TestBeanWithLongEvent::class.java.methods
            .first { it.name == "foo" }

        val methodParseResult = processor.parseMethod(methodWithAnnotatedParams)
        assertNotNull(methodParseResult)
        assertEquals(2, methodParseResult!!.mapper.argMappers.size)
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "methodName" })
        assertNotNull(methodParseResult.mapper.argMappers.first { it.getFieldName() == "entityId" })
        val event = LongEvent(1L)
        assertEquals(event.id, methodParseResult.mapper.argMappers.first().getValue(arrayOf(event)))
    }

    @Test
    fun `should throw exception on duplicate annotated params`() {
        val methodWithAnnotatedParams = TestBeanWithWrongTypes::class.java.methods
            .first { it.name == "fooBar" }

        assertThrows<IllegalArgumentException> {
            processor.parseMethod(methodWithAnnotatedParams)
        }
    }
}

package ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestBaseClass
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestClass
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestDataClass
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestInterfaceImpl
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.SimpleTestBean
import kotlin.reflect.jvm.javaMethod

class MdcBeanPostProcessorParseParametersTest {
    // проверяем:
    // ParseParameter - done
    //   - должен находить параметры, напрямую аннотированные
    //   - датаклассы с аннотированными полями
    //   - обычные классы с аннотированными полями
    //   - интерфейсы с аннотированными геттерами
    //   - базовые классы с аннотированными полями

    private val parser = AnnotatedParametersParser()

    @Test
    fun `should parse direct annotated parameter`() {
        val methodWithDirectParam = SimpleTestBean::methodWithDirectParam.javaMethod!!

        val fieldMappers = parser.parseAnnotatedParameter(0, methodWithDirectParam.parameters.first())
        assertEquals(1, fieldMappers.size)
        assertEquals("entityId", fieldMappers.first().getFieldName())
        assertEquals("applicationId", fieldMappers.first().getValue(arrayOf("applicationId")))

        val methodWithParamFromDataClass = SimpleTestBean::methodWithParamFromDataClass.javaMethod!!
        val fieldMappers2 = parser.parseAnnotatedParameter(0, methodWithParamFromDataClass.parameters.first())
        assertEquals(1, fieldMappers2.size)
        assertEquals("entityId", fieldMappers2.first().getFieldName())
        assertEquals("applicationId", fieldMappers2.first().getValue(arrayOf("applicationId")))
    }

    @Test
    fun `should parse parameters from data class`() {
        val methodWithParamFromDataClass = SimpleTestBean::methodWithParamFromDataClass.javaMethod!!

        val fieldMappers = parser.parseComplexParameter(1, methodWithParamFromDataClass.parameters[1])
        assertEquals(2, fieldMappers.size)
        val firstMapper = fieldMappers.first { it.getFieldName() == "field1" }
        val secondMapper = fieldMappers.first { it.getFieldName() == "field2" }
        assertNotNull(firstMapper)
        assertNotNull(secondMapper)
        assertEquals("123", firstMapper.getValue(arrayOf("applicationId", TestDataClass("123", true))))
    }

    @Test
    fun `should parse parameters from regular class`() {
        val methodWithParamFromBaseClass = SimpleTestBean::methodWithParamFromBaseClass.javaMethod!!

        val fieldMappers = parser.parseComplexParameter(1, methodWithParamFromBaseClass.parameters[1])
        assertEquals(2, fieldMappers.size)
        val firstMapper = fieldMappers.first { it.getFieldName() == "field1" }
        val secondMapper = fieldMappers.first { it.getFieldName() == "field2" }
        assertNotNull(firstMapper)
        assertNotNull(secondMapper)
        assertEquals(123L, firstMapper.getValue(arrayOf("applicationId", TestBaseClass().apply { field1 = 123L })))
    }

    @Test
    fun `should parse parameters from sub class`() {
        val methodWithParamFromSubClass = SimpleTestBean::methodWithParamFromSubClass.javaMethod!!

        val fieldMappers = parser.parseComplexParameter(1, methodWithParamFromSubClass.parameters[1])
        assertEquals(3, fieldMappers.size)
        val firstMapper = fieldMappers.first { it.getFieldName() == "field1" }
        val secondMapper = fieldMappers.first { it.getFieldName() == "field2" }
        val thirdMapper = fieldMappers.first { it.getFieldName() == "field3" }
        assertNotNull(firstMapper)
        assertNotNull(secondMapper)
        assertNotNull(thirdMapper)
        assertEquals(123L, firstMapper.getValue(arrayOf("applicationId", TestBaseClass().apply { field1 = 123L })))
        assertEquals("123", thirdMapper.getValue(arrayOf("applicationId", TestClass().apply { field3 = "123" })))
    }

    @Test
    fun `should parse parameters from interface`() {
        val methodWithParamFromInterface = SimpleTestBean::methodWithParamFromInterface.javaMethod!!

        val fieldMappers = parser.parseComplexParameter(1, methodWithParamFromInterface.parameters[1])
        assertEquals(1, fieldMappers.size)
        val firstMapper = fieldMappers.first { it.getFieldName() == "field" }
        assertNotNull(firstMapper)
        assertEquals(
            TestInterfaceImpl().getValue(),
            firstMapper.getValue(arrayOf("applicationId", TestInterfaceImpl()))
        )
    }
}

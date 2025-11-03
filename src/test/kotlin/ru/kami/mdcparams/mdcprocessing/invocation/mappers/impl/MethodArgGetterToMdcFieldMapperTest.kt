package ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl

import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertNull

class MethodArgGetterToMdcFieldMapperTest {
    // проверяем:
    // - геттеры в датаклассах
    // - геттеры в обычных классах
    // - геттеры в базовых классах, когда на входе - наследник
    // - геттеры в интерфейсе

    @Test
    fun `get value from data class`() {
        val getter = TestDataClass::class.java
            .methods
            .first { it.name == "getField1" }

        val mapper = MethodArgGetterToMdcFieldMapper("field", 0, getter)
        val testDataClass = TestDataClass("qw4351-20i", true)
        assertEquals(testDataClass.field1, mapper.getValue(arrayOf(testDataClass)))
        assertNull(mapper.getValue(emptyArray()))
        assertNull(mapper.getValue(null))

        val testDataClass1 = TestDataClass(null, false)
        assertNull(mapper.getValue(arrayOf(testDataClass1)))
    }

    @Test
    fun `get value from regular class`() {
        val getter = TestBaseClass::class.java
            .methods
            .first { it.name == "getField2" }

        val mapper = MethodArgGetterToMdcFieldMapper("field", 1, getter)
        val testClass = TestBaseClass()
        testClass.field1 == 1L
        testClass.field2 = "asfdtgqwer"

        assertEquals(testClass.field2, mapper.getValue(arrayOf("arg0", testClass)))
        assertNull(mapper.getValue(arrayOf(testClass)))
        assertNull(mapper.getValue(null))

        testClass.field2 = null
        assertNull(mapper.getValue(arrayOf("arg0", testClass)))
    }

    @Test
    fun `get value from regular subclass`(){
        val getter = TestClass::class.java
            .methods
            .first { it.name == "getField2" }

        val mapper = MethodArgGetterToMdcFieldMapper("field", 1, getter)
        val testClass = TestClass()
        testClass.field1 == 1L
        testClass.field2 = "asfdtgqwer"
        testClass.field3 = "23453245b"

        assertEquals(testClass.field2, mapper.getValue(arrayOf("arg0", testClass)))
        assertNull(mapper.getValue(arrayOf(testClass)))
        assertNull(mapper.getValue(null))

        testClass.field2 = null
        assertNull(mapper.getValue(arrayOf("arg0", testClass)))
    }

    @Test
    fun `get value from interface`(){
        val getter = TestInterface::class.java
            .methods
            .first { it.name == "getValue" }

        val mapper = MethodArgGetterToMdcFieldMapper("field", 1, getter)
        val testClass: TestInterface = TestInterfaceImpl()
        assertEquals(testClass.getValue(), mapper.getValue(arrayOf("arg0", testClass)))
    }


    private data class TestDataClass(
        val field1: String?,
        val field2: Boolean,
    )

    private open class TestBaseClass {
        var field1: Long? = null
        var field2: String? = null
    }

    private class TestClass : TestBaseClass() {
        var field3: String? = null
    }

    private interface TestInterface {
        fun getValue(): String
    }

    private class TestInterfaceImpl : TestInterface {
        override fun getValue(): String {
            return "123123123"
        }

    }
}
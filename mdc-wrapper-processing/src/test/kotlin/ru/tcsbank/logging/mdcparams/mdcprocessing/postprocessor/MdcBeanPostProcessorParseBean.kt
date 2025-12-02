package ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor

import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotEquals
import org.junit.jupiter.api.Test
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.SimpleTestBean
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestBeanWithoutAnnotations
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestStringBean
import ru.tcsbank.logging.mdcparams.mdcprocessing.beans.TestStringInterfaceImpl

class MdcBeanPostProcessorParseBean {

    // postProcessBeforeInitialization
    //   - должен пропускать не аннотированные классы
    //   - должен пропускать аннотированные классы без аннотированных методов
    //   - проксировать классы с аннотированными методами
    //   - проксировать классы с аннотированными в предках методами

    private val processor = MdcBeanPostProcessor()

    @Test
    fun `should not proxy bean without annotation`() {
        val bean = TestStringInterfaceImpl()

        val proxy = processor.postProcessBeforeInitialization(bean, "testStringInterfaceImpl")
        assertEquals(bean, proxy)
    }

    @Test
    fun `should not proxy bean with annotation without annotated methods`() {
        val bean = TestBeanWithoutAnnotations()

        val proxy = processor.postProcessBeforeInitialization(bean, "bean")
        assertEquals(bean, proxy)
    }

    @Test
    fun `should proxy bean with annotation with annotated methods`() {
        val bean = SimpleTestBean()

        val proxy = processor.postProcessBeforeInitialization(bean, "bean")
        assertNotEquals(bean, proxy)

        val typedBean = proxy as SimpleTestBean
        assertEquals(
            bean.methodWithoutAnnotatedParams("entityId", emptyMap()),
            typedBean.methodWithoutAnnotatedParams("entityId", emptyMap())
        )
        assertEquals(bean.methodWithDirectParam("entityId"), typedBean.methodWithDirectParam("entityId"))
    }

    @Test
    fun `should proxy bean with annotation with annotated methods in parent`() {
        val bean = TestStringBean()

        val proxy = processor.postProcessBeforeInitialization(bean, "bean")
        assertNotEquals(bean, proxy)

        val typedBean = proxy as TestStringBean
        assertEquals(bean.foo("entityId"), typedBean.foo("entityId"))
        assertEquals(bean.deniedFoo("entityId"), typedBean.deniedFoo("entityId"))
    }
}

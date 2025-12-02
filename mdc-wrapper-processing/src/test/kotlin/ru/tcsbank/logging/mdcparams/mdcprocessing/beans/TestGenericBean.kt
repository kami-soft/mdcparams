package ru.tcsbank.logging.mdcparams.mdcprocessing.beans

import ru.tcsbank.logging.mdcparams.annotations.MDCEntityId
import ru.tcsbank.logging.mdcparams.annotations.MDCWrapper
import ru.tcsbank.logging.mdcparams.annotations.MdcAllowance
import ru.tcsbank.logging.mdcparams.mdcprocessing.args.TestDataClass

@MDCWrapper
open class TestGenericBean<T : Any> {
    open fun foo(@MDCEntityId bar: T): T {
        return bar
    }

    @MDCWrapper(allow = MdcAllowance.DISALLOW)
    open fun deniedFoo(@MDCEntityId bar: T): T {
        return bar
    }
}

open class TestStringBean : TestGenericBean<String>() {
    override fun deniedFoo(bar: String): String {
        return super.deniedFoo(bar) + bar
    }

    override fun foo(bar: String): String {
        return super.foo(bar) + bar
    }
}

open class TestGenericBeanWithWrongTypes<T : Any, U : Any> : TestGenericBean<T>() {
    open fun fooBar(bar: T, fooBar1: U, fooBar2: U): T {
        return bar
    }
}

class TestBeanWithWrongTypes : TestGenericBeanWithWrongTypes<String, TestDataClass>() {
    override fun fooBar(bar: String, fooBar1: TestDataClass, fooBar2: TestDataClass): String {
        return super.fooBar(bar, fooBar1, fooBar2) + (fooBar1.field1 ?: "") + (fooBar2.field1 ?: "")
    }
}

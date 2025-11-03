package ru.kami.mdcparams.mdcprocessing.postprocessor

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import ru.kami.mdcparams.mdcprocessing.annotations.MDCField
import ru.kami.mdcparams.mdcprocessing.annotations.MDCWrapper
import ru.kami.mdcparams.mdcprocessing.invocation.*
import ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl.MethodArgToMdcFieldMapper
import ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl.MethodArgFieldToMdcFieldMapper
import ru.kami.mdcparams.mdcprocessing.invocation.mappers.impl.MethodArgGetterToMdcFieldMapper
import ru.kami.mdcparams.mdcprocessing.invocation.mappers.MdcFieldMapper
import java.lang.reflect.AnnotatedElement
import java.lang.reflect.Field
import java.lang.reflect.Method
import java.lang.reflect.Parameter

@Component
class MdcBeanPostProcessor : BeanPostProcessor {
    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any? {
        return if (isMdcBean(bean)) {
            createMdcProxy(bean)
        } else {
            bean
        }
    }

    private fun createMdcProxy(bean: Any): Any {
        // бин точно кандидат на оборачивание в LogContext
        // пробегаемся по всем его методам, ищем аннотированные параметры
        // и аннотированные поля внутри параметров
        // и если они есть - в мапу их для дальнейшего использования в оборачивании,
        // а если нет - возвращаем исходный бин
        val methodsMappers = parseMethods(bean)
        if (methodsMappers.isEmpty())
            return bean

        val interceptor = MdcMethodInvocationInterceptor(methodsMappers)
        val aspectFactory = AspectJProxyFactory(bean)
        aspectFactory.addAdvice(interceptor)

        return aspectFactory.getProxy<Any>()
    }

    private fun parseMethods(bean: Any): Map<String, MethodArgsToMDCDataMapper> {
        val beanClass = if (AopUtils.isAopProxy(bean)) {
            AopUtils.getTargetClass(bean)
        } else {
            bean::class.java
        }

        return beanClass.methods
            .filter { method -> method.parameterCount != 0 }
            .filter { method -> AnnotationUtils.findAnnotation(method, MDCWrapper::class.java)?.allow != false }
            .mapNotNull { method -> parseMethod(method) }
            .associate { Pair(it.methodName, it.mapper) }
    }

    internal fun parseMethod(method: Method): MethodParseResult? {
        val argumentMapperGroups = method.parameters
            .flatMapIndexed { index, parameter -> parseParameter(index, parameter) }
            .groupBy { it.getFieldName() }

        argumentMapperGroups
            .filter { it.value.size > 1 }
            .forEach { println("in $method there are several MDC params with name ${it.key}. Will use only one") }

        val argumentMappers = argumentMapperGroups
            .map { it.value.first() }

        return if (argumentMappers.isNotEmpty())
            MethodParseResult(method.name, MethodArgsToMDCDataMapper(argumentMappers))
        else
            null
    }

    internal fun parseParameter(parameterIndex: Int, parameter: Parameter): Collection<MdcFieldMapper> {
        // Параметр может быть как простым типом, так и объектом.
        // Внутри объекта может быть несколько полей, помеченных разными MDC-аннотациями, надо смаппить все
        val parameterMapper = mapAnnotatedParameter(parameterIndex, parameter)

        val parameterType = parameter.type

        val fieldsInParameterMappers = parameterType
            .fields
            .mapNotNull { field -> mapAnnotatedParameterField(parameterIndex, field) }

        val gettersInParameterMappers = parameterType
            .methods
            .filter { method -> method.parameterCount == 0 && method.returnType != Void.TYPE }
            .mapNotNull { method -> mapAnnotatedGetter(parameterIndex, method) }

        return if (parameterMapper != null)
            fieldsInParameterMappers.plus(gettersInParameterMappers).plus(parameterMapper)
        else
            fieldsInParameterMappers.plus(gettersInParameterMappers)
    }

    private fun mapAnnotatedGetter(parameterIndex: Int, getter: Method): MdcFieldMapper? {
        return constructArgumentMapper(parameterIndex, getter) { paramName, argumentIndex ->
            MethodArgGetterToMdcFieldMapper(paramName, argumentIndex, getter)
        }
    }

    private fun mapAnnotatedParameter(parameterIndex: Int, parameter: Parameter): MdcFieldMapper? {
        return constructArgumentMapper(parameterIndex, parameter) { paramName, argumentIndex ->
            MethodArgToMdcFieldMapper(paramName, argumentIndex)
        }
    }

    private fun mapAnnotatedParameterField(parameterIndex: Int, field: Field): MdcFieldMapper? {
        return constructArgumentMapper(parameterIndex, field) { paramName, argumentIndex ->
            MethodArgFieldToMdcFieldMapper(paramName, argumentIndex, field)
        }
    }

    private fun constructArgumentMapper(
        parameterIndex: Int,
        element: AnnotatedElement,
        mapperConstructor: (paramName: String, argumentIndex: Int) -> MdcFieldMapper?
    ): MdcFieldMapper? {
        val annotation = AnnotationUtils.findAnnotation(element, MDCField::class.java)
        return if (annotation != null) {
            mapperConstructor(annotation.mdcFieldName, parameterIndex)
        } else {
            null
        }
    }


    private fun isMdcBean(bean: Any): Boolean {
        val directMDCAnnotation = AnnotationUtils.findAnnotation(bean::class.java, MDCWrapper::class.java)
        return directMDCAnnotation?.allow
            ?: PREDEFINED_MDC_ANNOTATIONS
                .any { AnnotationUtils.findAnnotation(bean::class.java, it) != null }
    }

    companion object {
        private val PREDEFINED_MDC_ANNOTATIONS = listOf(Controller::class.java)
    }
}

internal data class MethodParseResult(
    val methodName: String,
    val mapper: MethodArgsToMDCDataMapper,
)
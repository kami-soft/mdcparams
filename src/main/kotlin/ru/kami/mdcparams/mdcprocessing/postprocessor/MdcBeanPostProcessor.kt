package ru.kami.mdcparams.mdcprocessing.postprocessor

import org.springframework.aop.aspectj.annotation.AspectJProxyFactory
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import org.springframework.stereotype.Component
import org.springframework.stereotype.Controller
import ru.kami.mdcparams.mdcprocessing.annotations.MDCField
import ru.kami.mdcparams.mdcprocessing.annotations.MDCWrapper
import ru.kami.mdcparams.mdcprocessing.invocation.ArgumentsToMdcParamMapper
import ru.kami.mdcparams.mdcprocessing.invocation.MdcData
import ru.kami.mdcparams.mdcprocessing.invocation.MdcMethodInvocationInterceptor
import ru.kami.mdcparams.mdcprocessing.invocation.MethodParamsToMDCDataMapper
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
        // и если они есть - в мапу их для дальнейшего использования в оборачивании
        // а если нет - возвращаем исходный бин
        val methodsMappers = parseMethods(bean)
        if (methodsMappers.isEmpty())
            return bean

        val interceptor = MdcMethodInvocationInterceptor(methodsMappers)
        val aspectFactory = AspectJProxyFactory(bean)
        aspectFactory.addAdvice(interceptor)

        return aspectFactory.getProxy<Any>()
    }

    private fun parseMethods(bean: Any): Map<String, MethodParamsToMDCDataMapper> {
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

    private fun parseMethod(method: Method): MethodParseResult? {
        val argumentMappers = method.parameters
            .flatMapIndexed { index, parameter -> parseParameter(index, parameter) }

        return if (argumentMappers.isNotEmpty())
            MethodParseResult(method.name, MethodParamsToMDCDataMapper(argumentMappers))
        else
            null
    }

    private fun parseParameter(parameterIndex: Int, parameter: Parameter): Collection<ArgumentsToMdcParamMapper> {
        // Параметр может быть как простым типом, так и объектом.
        // Внутри объекта может быть несколько полей, помеченных разными MDC-аннотациями, надо смаппить все
        val parameterMapper = mapAnnotatedParameter(parameterIndex, parameter)

        val parameterType = parameter.type

        val fieldsInParameterMappers = parameterType
            .fields
            .mapNotNull { field -> mapAnnotatedParameterField(parameterIndex, field) }

        val gettersInParameterMappers = parameterType
            .methods
            .filter { method -> method.parameterCount == 0 }
            .mapNotNull { method -> mapAnnotatedGetter(parameterIndex, method) }

        return if (parameterMapper != null)
            fieldsInParameterMappers.plus(gettersInParameterMappers).plus(parameterMapper)
        else
            fieldsInParameterMappers.plus(gettersInParameterMappers)
    }

    private fun mapAnnotatedGetter(parameterIndex: Int, getter: Method): ArgumentsToMdcParamMapper? {
        return constructArgumentMapper(parameterIndex, getter) {
            getter.invoke(it)
        }
    }

    private fun mapAnnotatedParameter(parameterIndex: Int, parameter: Parameter): ArgumentsToMdcParamMapper? {
        return constructArgumentMapper(parameterIndex, parameter) { arg -> arg }
    }

    private fun mapAnnotatedParameterField(parameterIndex: Int, field: Field): ArgumentsToMdcParamMapper? {
        return constructArgumentMapper(parameterIndex, field) { arg -> field.get(arg) }
    }

    private fun constructArgumentMapper(
        parameterIndex: Int,
        element: AnnotatedElement,
        block: (Any) -> Any?
    ): ArgumentsToMdcParamMapper? {
        val annotation = AnnotationUtils.findAnnotation(element, MDCField::class.java)
        if (annotation != null) {
            val mdcFieldName = annotation.mdcFieldName
            return ArgumentsToMdcParamMapper { args ->
                var result: MdcData? = null
                if (!args.isNullOrEmpty() && args.size > parameterIndex) {
                    val arg = args[parameterIndex]
                    if (arg != null) {
                        val value = block(arg)
                        if (value != null) {
                            result = MdcData(mdcFieldName, value)
                        }
                    }
                }
                result
            }
        }
        return null
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

private data class MethodParseResult(
    val methodName: String,
    val mapper: MethodParamsToMDCDataMapper,
)
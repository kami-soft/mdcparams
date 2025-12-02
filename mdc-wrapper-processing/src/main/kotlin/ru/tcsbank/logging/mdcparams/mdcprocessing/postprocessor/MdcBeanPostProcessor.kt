package ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor

import org.springframework.aop.framework.ProxyFactory
import org.springframework.aop.support.AopUtils
import org.springframework.beans.factory.config.BeanPostProcessor
import org.springframework.core.annotation.AnnotationUtils
import ru.tcsbank.logging.mdcparams.annotations.MDCField
import ru.tcsbank.logging.mdcparams.annotations.MDCWrapper
import ru.tcsbank.logging.mdcparams.annotations.MdcAllowance
import ru.tcsbank.logging.mdcparams.data.CombinedAnnotationAttributes
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.MdcMethodInvocationInterceptor
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.MethodToMDCDataWrapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.MdcFieldMapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.fieldmappers.impl.MethodNameToMdcFieldMapper
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.methodwrapper.impl.DefaultMdcMethodInvocationWrapperFactory
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapperFactory
import java.lang.reflect.Method

/**
 * This class is a Spring {@link BeanPostProcessor} implementation that enhances beans by wrapping
 * methods annotated (directly or indirectly) with {@link MDCWrapper} in a custom invocation
 * interceptor ({@link MdcMethodInvocationInterceptor}). This allows automatic population of
 * Mapped Diagnostic Context (MDC) data during method execution, based on parameters annotated
 * with {@link MDCField} or fields within parameter objects marked with the same annotation.
 *
 * The processor supports:
 *   - Direct parameter annotations via {@link MDCField}</li>
 *   - Field-level annotations inside parameter objects (one level deep)</li>
 *   - Getter methods in parameter objects annotated with {@link MDCField}</li>
 *   - SpEL expressions in {@link MDCField#spEl()} for dynamic value resolution</li>
 *   - Inheritance and interface-based method declarations</li>
 *
 * Proxying is done using Spring AOP's {@link ProxyFactory}, and both interface and class-based
 * proxies are supported. The actual MDC population logic is delegated to {@link MethodArgsToMDCDataMapper}
 * and its associated fieldmappers.
 *
 * By default, the processor considers beans as candidates if they are annotated with
 * {@link Controller} or belong to predefined classes/annotations. This behavior can be customized
 * via constructor arguments.
 *
 * @see MDCWrapper
 * @see MDCField
 * @see MdcMethodInvocationInterceptor
 * @see MethodToMDCDataWrapper
 * @see MdcMethodInvocationWrapperFactory
 */

@Suppress("TooManyFunctions")
class MdcBeanPostProcessor(
    private val mdcMethodInvocationWrapperFactory: MdcMethodInvocationWrapperFactory =
        DefaultMdcMethodInvocationWrapperFactory(),
    private val predefinedAnnotations: Collection<Class<out Annotation>> = emptyList(),
    private val predefinedClasses: Collection<Class<*>> = emptyList()
) : BeanPostProcessor {
    private val annotatedParametersParser = AnnotatedParametersParser()

    override fun postProcessBeforeInitialization(bean: Any, beanName: String): Any {
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
        val methodMappers = parseMethods(bean)
        if (methodMappers.isEmpty()) {
            return bean
        }

        val interceptor = MdcMethodInvocationInterceptor(methodMappers)
        // следующие гребаные три строчки съели кучу времени, пока подобрал комбинацию
        // чтобы и интерфейсы и классы проксировались корректно
        val proxyFactory = ProxyFactory(bean)
        proxyFactory.addAdvice(interceptor)
        proxyFactory.isProxyTargetClass = true

        return proxyFactory.proxy
    }

    private fun parseMethods(bean: Any): Map<String, MethodToMDCDataWrapper> {
        val beanClass = if (AopUtils.isAopProxy(bean)) {
            AopUtils.getTargetClass(bean)
        } else {
            bean::class.java
        }

        return beanClass.methods
            .mapNotNull { method -> parseMethod(method) }
            .associate { it.methodName to it.mapper }
    }

    internal fun parseMethod(method: Method): MethodParseResult? {
        val combinedMethodAttributes = combineMethodAnnotationAttributes(method)
        if (method.parameterCount == 0 || !combinedMethodAttributes.allow) {
            return null
        }

        // объединяем параметры, их могли навтыкать с идентичными именами
        val argumentMapperGroups =  annotatedParametersParser.parseMethodParameters(method)
            .groupBy { it.getFieldName() }

        checkAmbiguousNames(argumentMapperGroups, combinedMethodAttributes, method)

        val mappers = argumentMapperGroups
            .filter { it.value.isNotEmpty() }
            .map { it.value.first() }

        return if (mappers.isNotEmpty()) {
            MethodParseResult(
                methodName = method.name,
                mapper = MethodToMDCDataWrapper(
                    mappers + MethodNameToMdcFieldMapper(method),
                    mdcMethodInvocationWrapperFactory.obtain(combinedMethodAttributes, method),
                )
            )
        } else {
            null
        }
    }

    private fun combineMethodAnnotationAttributes(method: Method): CombinedAnnotationAttributes {
        val clazz = method.declaringClass
        val methodAnnotation = AnnotationUtils.findAnnotation(method, MDCWrapper::class.java)
        val classAnnotation = AnnotationUtils.findAnnotation(clazz, MDCWrapper::class.java)

        return CombinedAnnotationAttributes(
            allow = combineValue(
                methodAnnotation?.allow,
                classAnnotation?.allow,
                MdcAllowance.ALLOW
            ) == MdcAllowance.ALLOW,
            ignoreDuplicates = combineValue(
                methodAnnotation?.ignoreDuplicates,
                classAnnotation?.ignoreDuplicates,
                MdcAllowance.DISALLOW
            ) == MdcAllowance.ALLOW,
            forceLogExceptions = combineValue(
                methodAnnotation?.forceLogExceptions,
                classAnnotation?.forceLogExceptions,
                MdcAllowance.ALLOW
            ) == MdcAllowance.ALLOW,
        )
    }

    private fun combineValue(methodValue: MdcAllowance?, classValue: MdcAllowance?, defaultValue: MdcAllowance): MdcAllowance {
        val correctedMethodValue = methodValue ?: MdcAllowance.NOT_SPECIFIED
        val correctedClassValue = classValue ?: MdcAllowance.NOT_SPECIFIED
        return correctedMethodValue.takeIf { it != MdcAllowance.NOT_SPECIFIED }
            ?: correctedClassValue.takeIf { it != MdcAllowance.NOT_SPECIFIED }
            ?: defaultValue
    }

    private fun checkAmbiguousNames(
        argumentMapperGroups: Map<String, List<MdcFieldMapper>>,
        annotationAttributes: CombinedAnnotationAttributes,
        method: Method
    ) {
        val ambiguousMDCParams = argumentMapperGroups
            .filter { it.value.size > 1 }
            .map { it.key }
            .distinct()
        if (ambiguousMDCParams.isNotEmpty() && !annotationAttributes.ignoreDuplicates) {
            throw IllegalArgumentException(
                "There are several MDC params with name ${ambiguousMDCParams.joinToString()}" +
                    "in ${method.declaringClass.name}.${method.name}. " +
                    "Review @MDCField annotations or use @MDCWrapper(ignoreDuplicates = true) to ignore this error"
            )
        }
    }

    private fun isMdcBean(bean: Any): Boolean {
        val directMDCAnnotation = AnnotationUtils.findAnnotation(bean::class.java, MDCWrapper::class.java)

        return if (directMDCAnnotation != null) {
            directMDCAnnotation.allow != MdcAllowance.DISALLOW
        } else {
            predefinedClasses.any { it.isAssignableFrom(bean::class.java) } ||
                predefinedAnnotations.any { AnnotationUtils.findAnnotation(bean::class.java, it) != null }
        }
    }
}

internal data class MethodParseResult(
    val methodName: String,
    val mapper: MethodToMDCDataWrapper,
)

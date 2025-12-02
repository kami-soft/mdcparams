package ru.tcsbank.logging.mdcparams

import org.springframework.boot.autoconfigure.AutoConfiguration
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean
import org.springframework.context.annotation.Bean
import org.springframework.stereotype.Controller
import ru.tcsbank.logging.mdcparams.methodwrapper.MdcMethodInvocationWrapperFactory
import ru.tcsbank.logging.mdcparams.mdcprocessing.invocation.methodwrapper.impl.DefaultMdcMethodInvocationWrapperFactory
import ru.tcsbank.logging.mdcparams.mdcprocessing.postprocessor.MdcBeanPostProcessor
import ru.tcsbank.logging.mdcparams.properties.MdcPredefinedProperties

@AutoConfiguration
class MdcParamsConfiguration {

    @Bean
    @ConditionalOnMissingBean
    fun mdcMethodInvocationWrapperFactory(): MdcMethodInvocationWrapperFactory {
        return DefaultMdcMethodInvocationWrapperFactory()
    }

    @Bean
    @ConditionalOnMissingBean
    fun mdcPredefinedProperties(): MdcPredefinedProperties {
        return object : MdcPredefinedProperties {
            override val predefinedAnnotations: Collection<Class<out Annotation>>
                get() = listOf(Controller::class.java)
        }
    }

    @Bean
    fun mdcBeanPostprocessor(
        mdcMethodInvocationWrapperFactory: MdcMethodInvocationWrapperFactory,
        mdcPredefinedProperties: MdcPredefinedProperties,
    ): MdcBeanPostProcessor {
        return MdcBeanPostProcessor(
            mdcMethodInvocationWrapperFactory = mdcMethodInvocationWrapperFactory,
            predefinedAnnotations = mdcPredefinedProperties.predefinedAnnotations,
            predefinedClasses = mdcPredefinedProperties.predefinedClasses,
        )
    }
}

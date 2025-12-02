package ru.tcsbank.logging.mdcparams

import org.junit.jupiter.api.Disabled
import org.junit.jupiter.api.Test
import org.junit.jupiter.api.assertThrows
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import org.springframework.util.StopWatch
import ru.tcsbank.logging.mdcparams.beans.Sample
import ru.tcsbank.logging.mdcparams.beans.SuperApi
import ru.tcsbank.logging.mdcparams.beans.SuperController
import java.util.Random
import java.util.concurrent.TimeUnit

@SpringBootTest(classes = [SuperController::class])
class MdcparamsApplicationTests {

    @Autowired
    lateinit var proxiedController: SuperApi

    private var nonProxiedController = SuperController()

    @Test
    @Disabled
    fun contextLoads() {
        val repeatCount = 1000000
        val random = Random()
        // прогрев
        (0..100).forEach { i ->
            val sample = Sample(random.nextInt(), "test ${random.nextInt()}")
            println(proxiedController.foo("234234", sample))
            println(nonProxiedController.foo("234234", sample))
        }

        var ret = "test"

        val proxiedStopWatch = StopWatch("proxied controller")
        (0..repeatCount).forEach { i ->
            val sample = Sample(random.nextInt(), "$ret ${random.nextInt()}")

            proxiedStopWatch.start("foo method")
            ret = proxiedController.foo("234234", sample)
            proxiedStopWatch.stop()

            ret = ret.substring(0, 4)
        }

        val nonProxiedStopWatch = StopWatch("non proxied controller")
        (0..repeatCount).forEach { i ->
            val sample = Sample(random.nextInt(), "$ret ${random.nextInt()}")

            nonProxiedStopWatch.start("foo method")
            ret = nonProxiedController.foo("234234", sample)
            nonProxiedStopWatch.stop()

            ret = ret.substring(0, 4)
        }

        assert(
            (proxiedStopWatch.getTotalTime(TimeUnit.MILLISECONDS) - nonProxiedStopWatch.getTotalTime(TimeUnit.MILLISECONDS) < 500)
        )

        println("proxied controller: ${proxiedStopWatch.getTotalTime(TimeUnit.MILLISECONDS)}")
        println("non proxied controller: ${nonProxiedStopWatch.getTotalTime(TimeUnit.MILLISECONDS)}")
    }

    @Test
    @Disabled
    fun `should throw concrete exception`() {
        assertThrows<NotImplementedError> { proxiedController.throwException("test") }
    }
}

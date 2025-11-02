package ru.kami.mdcparams

import org.junit.jupiter.api.Test
import org.springframework.beans.factory.annotation.Autowired
import org.springframework.boot.test.context.SpringBootTest
import ru.kami.mdcparams.test.MdcService
import ru.kami.mdcparams.test.Sample

@SpringBootTest
class MdcparamsApplicationTests {

    @Autowired
    lateinit var service: MdcService

	@Test
	fun contextLoads() {
        val ret = service.foo("234234", Sample(1, "eeeee"))

        println(ret)
	}
}

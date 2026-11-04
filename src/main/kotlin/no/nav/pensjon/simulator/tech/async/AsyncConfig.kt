package no.nav.pensjon.simulator.tech.async

import mu.KotlinLogging
import org.slf4j.MDC
import org.springframework.context.annotation.Bean
import org.springframework.context.annotation.Configuration
import org.springframework.scheduling.annotation.EnableAsync
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.concurrent.DelegatingSecurityContextRunnable
import java.util.concurrent.Executor

@Configuration
@EnableAsync
class AsyncConfig {
    private val log = KotlinLogging.logger {}

    @Bean(name = ["loggingExecutor"])
    fun taskExecutor(): Executor =
        ThreadPoolTaskExecutor().apply {
            corePoolSize = 5
            maxPoolSize = 10
            queueCapacity = 100
            setRejectedExecutionHandler { _, _ ->
                log.warn { "AFP-sammenligning droppes fordi executoren ikke tar imot flere oppgaver" }
            }
            setTaskDecorator { task ->
                val callerContext = MDC.getCopyOfContextMap()

                val mdcTask = Runnable {
                    val previousContext = MDC.getCopyOfContextMap()
                    try {
                        if (callerContext != null) {
                            MDC.setContextMap(callerContext)
                        } else {
                            MDC.clear()
                        }

                        task.run()
                    } finally {
                        if (previousContext != null) {
                            MDC.setContextMap(previousContext)
                        } else {
                            MDC.clear()
                        }
                    }
                }

                DelegatingSecurityContextRunnable(mdcTask)
            }
            initialize()
        }
}
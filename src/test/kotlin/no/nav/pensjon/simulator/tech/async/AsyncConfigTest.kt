package no.nav.pensjon.simulator.tech.async

import io.kotest.assertions.throwables.shouldThrow
import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.tech.security.egress.EgressAccess
import no.nav.pensjon.simulator.tech.security.egress.EnrichedAuthentication
import no.nav.pensjon.simulator.tech.security.egress.config.EgressService
import no.nav.pensjon.simulator.tech.security.egress.config.EgressTokenSuppliersByService
import no.nav.pensjon.simulator.tech.security.egress.token.RawJwt
import org.slf4j.MDC
import org.springframework.scheduling.concurrent.ThreadPoolTaskExecutor
import org.springframework.security.authentication.TestingAuthenticationToken
import org.springframework.security.core.context.SecurityContextHolder
import java.util.concurrent.Callable
import java.util.concurrent.ExecutionException
import java.util.concurrent.FutureTask
import java.util.concurrent.TimeUnit
import java.util.function.Supplier

class AsyncConfigTest : FunSpec({
    lateinit var executor: ThreadPoolTaskExecutor

    fun onWorker(task: () -> Unit) {
        val future = FutureTask(Callable { task() })
        executor.threadPoolExecutor.queue.put(future)
        future.get(5, TimeUnit.SECONDS)
    }

    beforeTest {
        SecurityContextHolder.clearContext()
        MDC.clear()
        executor = (AsyncConfig().taskExecutor() as ThreadPoolTaskExecutor).apply {
            corePoolSize = 1
            maxPoolSize = 1
        }
        executor.threadPoolExecutor.prestartAllCoreThreads()
    }

    afterTest {
        executor.shutdown()
        SecurityContextHolder.clearContext()
        MDC.clear()
    }

    test("propagates egress token access and MDC to the worker without leaking to later tasks") {
        val token = RawJwt("test-token")
        val authentication = EnrichedAuthentication(
            initialAuth = TestingAuthenticationToken("test-user", null),
            egressTokenSuppliersByService = EgressTokenSuppliersByService(
                mapOf(EgressService.AFP_BEHOLDNING_API to Supplier { token })
            )
        )
        val callerThread = Thread.currentThread()
        SecurityContextHolder.getContext().authentication = authentication
        MDC.put("callId", "test-call")

        executor.submit(Callable {
            (Thread.currentThread() == callerThread) shouldBe false
            EgressAccess.token(EgressService.AFP_BEHOLDNING_API) shouldBe token
            MDC.get("callId") shouldBe "test-call"
        }).get(5, TimeUnit.SECONDS)

        SecurityContextHolder.getContext().authentication shouldBe authentication
        MDC.get("callId") shouldBe "test-call"

        onWorker {
            SecurityContextHolder.getContext().authentication shouldBe null
            MDC.get("callId") shouldBe null
        }

        SecurityContextHolder.clearContext()
        MDC.clear()

        executor.submit(Callable {
            SecurityContextHolder.getContext().authentication shouldBe null
            MDC.get("callId") shouldBe null
        }).get(5, TimeUnit.SECONDS)
    }

    listOf(false, true).forEach { taskFails ->
        test("restores the worker's previous security context and MDC when taskFails=$taskFails") {
            val workerAuthentication = TestingAuthenticationToken("worker-user", null)
            onWorker {
                SecurityContextHolder.getContext().authentication = workerAuthentication
                MDC.put("callId", "worker-call")
            }

            val callerAuthentication = TestingAuthenticationToken("caller-user", null)
            SecurityContextHolder.getContext().authentication = callerAuthentication
            MDC.put("callId", "caller-call")
            val failure = IllegalStateException("test failure")

            val future = executor.submit(Callable {
                SecurityContextHolder.getContext().authentication shouldBe callerAuthentication
                MDC.get("callId") shouldBe "caller-call"
                if (taskFails) throw failure
            })

            if (taskFails) {
                shouldThrow<ExecutionException> {
                    future.get(5, TimeUnit.SECONDS)
                }.cause shouldBe failure
            } else {
                future.get(5, TimeUnit.SECONDS)
            }

            onWorker {
                SecurityContextHolder.getContext().authentication shouldBe workerAuthentication
                MDC.get("callId") shouldBe "worker-call"
            }

            SecurityContextHolder.getContext().authentication shouldBe callerAuthentication
            MDC.get("callId") shouldBe "caller-call"
        }
    }
})

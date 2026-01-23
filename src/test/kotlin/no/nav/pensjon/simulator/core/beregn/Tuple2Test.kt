package no.nav.pensjon.simulator.core.beregn

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class Tuple2Test : FunSpec({

    test("should store first and second values") {
        val tuple = Tuple2("hello", 42)

        tuple.first shouldBe "hello"
        tuple.second shouldBe 42
    }

    test("should work with different types") {
        val tuple = Tuple2(listOf(1, 2, 3), mapOf("key" to "value"))

        tuple.first shouldBe listOf(1, 2, 3)
        tuple.second shouldBe mapOf("key" to "value")
    }

    test("should work with nullable values") {
        val tuple = Tuple2<String?, Int?>(null, null)

        tuple.first shouldBe null
        tuple.second shouldBe null
    }
})

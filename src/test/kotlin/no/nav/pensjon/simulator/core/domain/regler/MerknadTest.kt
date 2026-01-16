package no.nav.pensjon.simulator.core.domain.regler

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe

class MerknadTest : StringSpec({

    "'asString' should return object fields in specific format" {
        Merknad().apply {
            kode = "k"
            argumentListe = mutableListOf("a", "b")
        }.asString() shouldBe "k:a¤b"
    }
})

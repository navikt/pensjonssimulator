package no.nav.pensjon.simulator.core.domain.regler

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe

class PakkseddelTest : FunSpec({

    test("merknader as string") {
        Pakkseddel(
            mutableListOf(
                Merknad().apply {
                    kode = "M1"
                    argumentListe = mutableListOf("A11", "A12")
                },
                Merknad().apply {
                    kode = "M2"
                    argumentListe = mutableListOf("A21", "A22")
                }
            )
        ).merknaderAsString() shouldBe "M1:A11,A12, M2:A21,A22"
    }
})

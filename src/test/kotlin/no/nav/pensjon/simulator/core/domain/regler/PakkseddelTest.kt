package no.nav.pensjon.simulator.core.domain.regler

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe

class PakkseddelTest : ShouldSpec({

    context("merknaderAsString") {
        should("be string containing kode and argumentliste with special separator") {
            pakkseddel(
                merknadListe = listOf(
                    Merknad().apply {
                        kode = "M1"
                        argumentListe = mutableListOf("A11", "A12")
                    },
                    Merknad().apply {
                        kode = "M2"
                        argumentListe = mutableListOf("A21", "A22")
                    }
                )
            ).merknaderAsString() shouldBe "M1:A11¤A12, M2:A21¤A22"
        }
    }

    context("kontrollTjenesteOk when no merknad") {
        should("be true") {
            pakkseddel(merknadListe = emptyList()).kontrollTjenesteOk shouldBe true
        }
    }

    context("kontrollTjenesteOk when merknad") {
        should("be false") {
            pakkseddel(
                merknadListe = listOf(
                    Merknad().apply {
                        kode = null
                        argumentListe = emptyList()
                    }
                )
            ).kontrollTjenesteOk shouldBe false
        }
    }
})

private fun pakkseddel(merknadListe: List<Merknad>) =
    Pakkseddel().apply {
        this.merknadListe = merknadListe
    }
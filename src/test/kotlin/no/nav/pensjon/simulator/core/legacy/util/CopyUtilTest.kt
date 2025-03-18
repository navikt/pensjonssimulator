package no.nav.pensjon.simulator.core.legacy.util

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpKompensasjonstillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpKronetillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatBeregning
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening
import org.junit.jupiter.api.Assertions.*
import org.junit.jupiter.api.Test
import java.time.LocalDate

class CopyUtilTest : FunSpec({

    /* Fails with IOException: "The object and all its subobjects MUST be serializable"
    test("copy") {
        CopyUtil.copy(BeregningsResultatAfpPrivat().apply {
            afpPrivatBeregning = AfpPrivatBeregning().apply {
                afpLivsvarig = AfpLivsvarig().apply { justeringsbelop = 999 }
                afpKompensasjonstillegg = AfpKompensasjonstillegg().apply {
                    referansebelop = 123
                    netto = 234
                }
                afpKronetillegg = AfpKronetillegg().apply { brutto = 345 }
                afpOpptjening = AfpOpptjening().apply {
                    ar = 2025
                    totalbelop = 12345.67
                    merknadListe = mutableListOf(
                        Merknad().apply { kode = "K1" },
                        Merknad().apply { kode = "K2" }
                    )
                }
            }
        }) shouldBe BeregningsResultatAfpPrivat().apply {
            afpPrivatBeregning = AfpPrivatBeregning().apply {
                afpLivsvarig = AfpLivsvarig().apply { justeringsbelop = 999 }
                afpKompensasjonstillegg = AfpKompensasjonstillegg().apply {
                    referansebelop = 123
                    netto = 234
                }
                afpKronetillegg = AfpKronetillegg().apply { brutto = 345 }
                afpOpptjening = AfpOpptjening().apply {
                    ar = 2025
                    totalbelop = 12345.67
                    merknadListe = mutableListOf(
                        Merknad().apply { kode = "K1" },
                        Merknad().apply { kode = "K2" }
                    )
                }
            }
        }
    }
    */
})

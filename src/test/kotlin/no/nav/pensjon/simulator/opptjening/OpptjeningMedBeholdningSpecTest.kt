package no.nav.pensjon.simulator.opptjening

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.testutil.TestObjects.pid

class OpptjeningMedBeholdningSpecTest : ShouldSpec({

    context("medBeholdninger") {
        should("sette 'hent beholdninger'-flagget til 'true'") {
            OpptjeningMedBeholdningSpec(
                pid = pid,
                hentPensjonspoeng = true,
                hentGrunnlagForOpptjeninger = false,
                hentBeholdninger = false,
                harUfoeretrygdKravlinje = true,
                regelverkType = null,
                sakType = null,
                personSpecListe = emptyList(),
                soekerSpec = OpptjeningMedBeholdningPersonSpec(
                    pid = pid,
                    sisteGyldigeOpptjeningAar = 2000,
                    isGrunnlagRolleSoeker = false
                )
            ).medBeholdninger() shouldBe OpptjeningMedBeholdningSpec(
                pid = pid,
                hentPensjonspoeng = true,
                hentGrunnlagForOpptjeninger = false,
                hentBeholdninger = true, // endret
                harUfoeretrygdKravlinje = true,
                regelverkType = null,
                sakType = null,
                personSpecListe = emptyList(),
                soekerSpec = OpptjeningMedBeholdningPersonSpec(
                    pid = pid,
                    sisteGyldigeOpptjeningAar = 2000,
                    isGrunnlagRolleSoeker = false
                )
            )
        }
    }
})
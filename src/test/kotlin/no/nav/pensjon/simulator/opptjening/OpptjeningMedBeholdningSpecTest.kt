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
                hentOpptjeningsgrunnlag = false,
                hentBeholdninger = false,
                harUfoeretrygdKravlinje = true,
                regelverkType = null,
                sakType = null,
                personSpecListe = emptyList(),
                soekerSpec = OpptjeningMedBeholdningPersonSpec(
                    pid = pid,
                    sisteGyldigeOpptjeningsaar = 2000,
                    gjelderSoeker = false
                )
            ).medBeholdninger() shouldBe OpptjeningMedBeholdningSpec(
                pid = pid,
                hentPensjonspoeng = true,
                hentOpptjeningsgrunnlag = false,
                hentBeholdninger = true, // endret
                harUfoeretrygdKravlinje = true,
                regelverkType = null,
                sakType = null,
                personSpecListe = emptyList(),
                soekerSpec = OpptjeningMedBeholdningPersonSpec(
                    pid = pid,
                    sisteGyldigeOpptjeningsaar = 2000,
                    gjelderSoeker = false
                )
            )
        }
    }
})
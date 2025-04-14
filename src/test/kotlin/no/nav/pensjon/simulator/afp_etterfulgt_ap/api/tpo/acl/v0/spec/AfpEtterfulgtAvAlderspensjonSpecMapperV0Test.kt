package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.spec.SimuleringSpec

class AfpEtterfulgtAvAlderspensjonSpecMapperV0Test : StringSpec({

    "fromDto mapper AfpEtterfulgtAvAlderspensjonValidatedSpecV0 korrekt til SimuleringSpec" {
        // Given
        val dto = AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0(
            personId = "12345678901",
            sivilstandVedPensjonering = "UGIF",
            uttakFraOgMedDato = "2023-01-01",
            fremtidigAarligInntektTilAfpUttak = 500000,
            inntektSisteMaanedOver1G = true,
            fremtidigAarligInntektUnderAfpUttak = 400000,
            aarIUtlandetEtter16 = 2,
            epsPensjon = true,
            eps2G = true
        )

        // When
        val result: SimuleringSpec = AfpEtterfulgtAvAlderspensjonSpecMapperV0.fromDto(dto, { 100000 }, { 500000 })

        // Then
        result.pid?.value shouldBe "12345678901"
        result.sivilstatus shouldBe SivilstatusType.UGIF
        result.foersteUttakDato.toString() shouldBe "2023-01-01"
        result.fremtidigInntektListe.size shouldBe 0
        result.brukFremtidigInntekt shouldBe false
        result.inntektEtterHeltUttakBeloep shouldBe 0
        result.inntektUnderGradertUttakBeloep shouldBe 400000
        result.inntektEtterHeltUttakAntallAar shouldBe null
        result.forventetInntektBeloep shouldBe 500000
        result.utlandAntallAar shouldBe 2
    }
})

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
        val result: SimuleringSpec = AfpEtterfulgtAvAlderspensjonSpecMapperV0.fromDto(dto, 100000, { 12345 })

        // Then
        result.pid?.value shouldBe dto.personId
        result.sivilstatus shouldBe SivilstatusType.UGIF
        result.foersteUttakDato.toString() shouldBe dto.uttakFraOgMedDato
        result.fremtidigInntektListe.size shouldBe 0
        result.brukFremtidigInntekt shouldBe false
        result.inntektEtterHeltUttakBeloep shouldBe 0
        result.inntektUnderGradertUttakBeloep shouldBe dto.fremtidigAarligInntektUnderAfpUttak
        result.inntektEtterHeltUttakAntallAar shouldBe null
        result.forventetInntektBeloep shouldBe dto.fremtidigAarligInntektTilAfpUttak
        result.utlandAntallAar shouldBe dto.aarIUtlandetEtter16
        result.pre2025OffentligAfp?.inntektUnderAfpUttakBeloep shouldBe dto.fremtidigAarligInntektUnderAfpUttak
        result.pre2025OffentligAfp?.inntektMaanedenFoerAfpUttakBeloep shouldBe 100000
    }
})

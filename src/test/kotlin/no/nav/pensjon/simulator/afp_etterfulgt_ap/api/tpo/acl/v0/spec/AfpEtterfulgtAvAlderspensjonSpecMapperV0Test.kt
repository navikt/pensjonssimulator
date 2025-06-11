package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid

class AfpEtterfulgtAvAlderspensjonSpecMapperV0Test : StringSpec({

    "fromDto mapper AfpEtterfulgtAvAlderspensjonValidatedSpecV0 korrekt til SimuleringSpec" {
        val dto = AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0(
            personId = pid.value,
            sivilstandVedPensjonering = "UGIF",
            uttakFraOgMedDato = "2023-01-01",
            fremtidigAarligInntektTilAfpUttak = 500000,
            inntektSisteMaanedOver1G = true,
            fremtidigAarligInntektUnderAfpUttak = 400000,
            aarIUtlandetEtter16 = 2,
            epsPensjon = true,
            eps2G = true
        )

        val result: SimuleringSpec = AfpEtterfulgtAvAlderspensjonSpecMapperV0(
            personService = Arrange.foedselsdato(1963, 4, 5),
            inntektService = arrangeInntekt()
        ).fromDto(source = dto)

        with(result) {
            pid?.value shouldBe dto.personId
            sivilstatus shouldBe SivilstatusType.UGIF
            foersteUttakDato.toString() shouldBe dto.uttakFraOgMedDato
            fremtidigInntektListe.size shouldBe 0
            brukFremtidigInntekt shouldBe false
            inntektEtterHeltUttakBeloep shouldBe 0
            inntektUnderGradertUttakBeloep shouldBe dto.fremtidigAarligInntektUnderAfpUttak
            inntektEtterHeltUttakAntallAar shouldBe null
            forventetInntektBeloep shouldBe dto.fremtidigAarligInntektTilAfpUttak
            utlandAntallAar shouldBe dto.aarIUtlandetEtter16
            pre2025OffentligAfp?.inntektUnderAfpUttakBeloep shouldBe dto.fremtidigAarligInntektUnderAfpUttak
            pre2025OffentligAfp?.inntektMaanedenFoerAfpUttakBeloep shouldBe 100000
        }
    }
})

private fun arrangeInntekt(): InntektService =
    mockk<InntektService>().apply {
        every { hentSisteLignetInntekt(pid) } returns 12345
        every { hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G = true) } returns 100000
    }

package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.inntekt.Inntekt
import no.nav.pensjon.simulator.inntekt.InntektService
import no.nav.pensjon.simulator.testutil.Arrange
import no.nav.pensjon.simulator.testutil.TestObjects.pid
import java.time.LocalDate

class AfpEtterfulgtAvAlderspensjonSpecMapperV0Test : StringSpec({

    /**
     * Funksjonen 'fromDto' skal bl.a.:
     * - mappe fra spesifikasjons-DTO til motsvarende domeneobjekt
     * - sette isHentPensjonsbeholdninger 'true' (slik at garantipensjonsbeholdning kan utledes)
     * - sette isOutputSimulertBeregningsinformasjonForAllKnekkpunkter 'true'
     * - innhente inntekten for måneden før AFP-uttak
     */
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
            isHentPensjonsbeholdninger shouldBe true
            with(pre2025OffentligAfp!!) {
                inntektUnderAfpUttakBeloep shouldBe dto.fremtidigAarligInntektUnderAfpUttak
                inntektMaanedenFoerAfpUttakBeloep shouldBe 100000
            }
        }
    }
})

private fun arrangeInntekt(): InntektService =
    mockk<InntektService>().apply {
        every { hentSisteLignetInntekt(pid) } returns Inntekt(aarligBeloep = 12345, fom = LocalDate.of(2025, 1, 1))
        every { hentSisteMaanedsInntektOver1G(harInntektSisteMaanedOver1G = true) } returns 100000
    }

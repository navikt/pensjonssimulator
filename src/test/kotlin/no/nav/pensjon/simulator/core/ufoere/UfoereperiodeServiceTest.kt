package no.nav.pensjon.simulator.core.ufoere

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import io.mockk.every
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class UfoereperiodeServiceTest : ShouldSpec({

    val foedselsdato = LocalDate.of(1970, 1, 15)
    val persongrunnlag = Persongrunnlag().apply { fodselsdatoLd = foedselsdato }

    val normalderService: NormertPensjonsalderService = mockk {
        every {
            normalderOppnaasDato(foedselsdato)
        } returns LocalDate.of(2037, 1, 15)
    }

    val ufoereperiodeService = UfoereperiodeService(normalderService)

    context("tidlig helt uttak av alderspensjon") {
        should("gi datoen som er dagen før uttaksdato") {
            val spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2033, 2, 1), // tidlig uttak
                type = SimuleringTypeEnum.ALDER,
                uttaksgrad = UttakGradKode.P_100 // skal terminere uføreperiode ved uttaksdato
            )

            ufoereperiodeService.ufoereperiodeTom(spec, persongrunnlag) shouldBe
                    LocalDate.of(2033, 1, 31)
        }
    }

    context("tidlig gradert uttak med privat AFP") {
        should("gi datoen som er dagen før uttaksdato") {
            val spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2033, 2, 1), // tidlig uttak
                type = SimuleringTypeEnum.ALDER_M_AFP_PRIVAT,
                uttaksgrad = UttakGradKode.P_60 // uttaksgrad har ingen betydning her
            )

            ufoereperiodeService.ufoereperiodeTom(spec, persongrunnlag) shouldBe
                    LocalDate.of(2033, 1, 31)
        }
    }

    context("tidlig gradert uttak av alderspensjon med gjenlevenderett") {
        should("gi datoen som er dagen før ubetinget uttaksdato") {
            val spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2033, 2, 1), // tidlig uttak
                type = SimuleringTypeEnum.ALDER_M_GJEN,
                uttaksgrad = UttakGradKode.P_80 // skal terminere uføreperiode ved ubetinget uttaksdato
            )

            ufoereperiodeService.ufoereperiodeTom(spec, persongrunnlag) shouldBe
                    LocalDate.of(2037, 1, 31)
        }
    }

    context("uttak etter normert pensjonsalder") {
        should("gi datoen som er dagen før ubetinget uttaksdato") {
            val spec = simuleringSpec(
                foersteUttakDato = LocalDate.of(2037, 12, 1), // etter normert pensjonsalder
                type = SimuleringTypeEnum.ALDER,
                uttaksgrad = UttakGradKode.P_100
            )

            ufoereperiodeService.ufoereperiodeTom(spec, persongrunnlag) shouldBe
                    LocalDate.of(2037, 1, 31)
        }
    }
})
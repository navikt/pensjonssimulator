package no.nav.pensjon.simulator.core.person

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.collections.shouldHaveSize
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import no.nav.pensjon.simulator.trygdetid.UtlandPeriode
import java.time.LocalDate

class PersongrunnlagMapperTest : ShouldSpec({

    should("mappe kun første forekomst i AFP-historikken") {
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mockk(relaxed = true),
            personService = mockk(),
            time = { LocalDate.of(2021, 1, 1) }
        )

        val result = mapper.mapToPersongrunnlag(
            person = PenPerson().apply {
                afpHistorikkListe = mutableListOf(
                    AfpHistorikk().apply { afpPensjonsgrad = 2 },
                    AfpHistorikk().apply { afpPensjonsgrad = 1 }
                )
            },
            spec = simuleringSpec
        )

        with(result) {
            afpHistorikkListe shouldHaveSize 1
            afpHistorikkListe[0].afpPensjonsgrad shouldBe 2
        }
    }

    should("bruke utenlandsperioder") {
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mockk(relaxed = true),
            personService = mockk(),
            time = { LocalDate.of(2021, 1, 1) }
        )

        val result = mapper.mapToPersongrunnlag(
            person = PenPerson(),
            spec = simuleringSpec(
                utlandAntallAar = 0,
                utlandPeriodeListe = listOf(
                    utlandPeriode(
                        fom = LocalDate.of(2025, 7, 1),
                        tom = LocalDate.of(2026, 6, 30)
                    )
                ),
                foedselsdato = LocalDate.of(1963, 1, 15)
            )
        )

        result.antallArUtland shouldBe 1
    }
})

private fun utlandPeriode(fom: LocalDate, tom: LocalDate?) =
    UtlandPeriode(fom, tom, land = LandkodeEnum.ALB, arbeidet = false)

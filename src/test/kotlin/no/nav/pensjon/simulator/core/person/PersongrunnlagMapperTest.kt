package no.nav.pensjon.simulator.core.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import io.mockk.mockk
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.trygd.UtlandPeriode
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import java.time.LocalDate

class PersongrunnlagMapperTest : FunSpec({

    test("mapToPersongrunnlag should map only first AFP-historikk item") {
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mockk(),
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

        with(result.afpHistorikkListe) {
            size shouldBe 1
            this[0].afpPensjonsgrad shouldBe 2
        }
    }

    test("mapToPersongrunnlag skal bruke utenlandsperioder") {
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mockk(),
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

package no.nav.pensjon.simulator.beholdning.api.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.beholdning.BeholdningPeriode
import no.nav.pensjon.simulator.beholdning.FolketrygdBeholdning
import no.nav.pensjon.simulator.beholdning.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.domain.regler.enum.GarantiPensjonsnivaSatsEnum
import java.time.LocalDate

class FolketrygdBeholdningResultMapperV1Test : FunSpec({

    test("resultV1 should map values") {
        FolketrygdBeholdningResultMapperV1.resultV1(
            source = FolketrygdBeholdning(
                pensjonBeholdningPeriodeListe = listOf(
                    BeholdningPeriode(
                        pensjonBeholdning = 1,
                        garantipensjonBeholdning = 2,
                        garantitilleggBeholdning = 3,
                        garantipensjonNivaa = GarantipensjonNivaa(
                            beloep = 4,
                            satsType = GarantiPensjonsnivaSatsEnum.ORDINAER,
                            sats = 5,
                            anvendtTrygdetid = 6
                        ),
                        fom = LocalDate.of(2021, 1, 1)
                    )
                )
            )
        ) shouldBe FolketrygdBeholdningResultV1(
            pensjonsBeholdningsPeriodeListe = listOf(
                PensjonsbeholdningPeriodeV1(
                    pensjonsBeholdning = 1,
                    garantiPensjonsBeholdning = 2,
                    garantitilleggsbeholdning = 3,
                    garantiPensjonsNiva = GarantipensjonNivaaV1(
                        belop = 4,
                        satsType = "ORDINAER",
                        sats = 5,
                        anvendtTrygdetid = 6
                    ),
                    fraOgMedDato = LocalDate.of(2021, 1, 1)
                )
            )
        )
    }
})

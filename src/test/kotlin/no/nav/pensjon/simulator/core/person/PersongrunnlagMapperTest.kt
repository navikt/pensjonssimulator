package no.nav.pensjon.simulator.core.person

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.testutil.TestObjects.simuleringSpec
import org.mockito.Mockito.mock
import java.time.LocalDate

class PersongrunnlagMapperTest : FunSpec({

    test("mapToPersongrunnlag should map only first AFP-historikk item") {
        val mapper = PersongrunnlagMapper(
            generelleDataHolder = mock(GenerelleDataHolder::class.java),
            personService = mock(GeneralPersonService::class.java),
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
})

package no.nav.pensjon.simulator.person.client.pdl.acl

import io.kotest.core.spec.style.ShouldSpec
import io.kotest.matchers.shouldBe
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Sivilstandstype
import no.nav.pensjon.simulator.person.Person
import java.time.LocalDate

class PdlPersonMapperTest : ShouldSpec({

    should("pick first in lists of fødselsdato, sivilstand, statsborgerskap") {
        PdlPersonMapper.fromDto(
            PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 5)), // first in the list
                            PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 3))
                        ),
                        sivilstand = listOf(
                            PdlSivilstand(type = "UGIFT"), // first in the list
                            PdlSivilstand(type = "GIFT")
                        ),
                        statsborgerskap = listOf(
                            PdlStatsborgerskap(land = "LAO"),
                            PdlStatsborgerskap(land = "NOR")
                        )
                    )
                ),
                extensions = null,
                errors = null
            )
        ) shouldBe Person(
            foedselsdato = LocalDate.of(1963, 4, 5),
            sivilstand = Sivilstandstype.UGIFT,
            statsborgerskap = LandkodeEnum.LAO
        )
    }
})

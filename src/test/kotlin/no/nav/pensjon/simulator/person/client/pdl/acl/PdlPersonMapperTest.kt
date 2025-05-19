package no.nav.pensjon.simulator.person.client.pdl.acl

import io.kotest.core.spec.style.FunSpec
import io.kotest.matchers.shouldBe
import java.time.LocalDate

class PdlPersonMapperTest : FunSpec({

    test("fromDto should return first foedselsdato in list") {
        PdlPersonMapper.fromDto(
            PdlPersonResult(
                data = PdlPersonEnvelope(
                    hentPerson = PdlPerson(
                        foedselsdato = listOf(
                            PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 5)), // first in list
                            PdlFoedselsdato(foedselsdato = LocalDate.of(1963, 4, 3))
                        )
                    )
                ),
                extensions = null,
                errors = null
            )
        ) shouldBe LocalDate.of(1963, 4, 5)
    }
})

package no.nav.pensjon.simulator.person.client.pdl.acl

import mu.KotlinLogging
import java.time.LocalDate

object PdlPersonMapper {
    private val log = KotlinLogging.logger {}

    fun fromDto(dto: PdlPersonResult): LocalDate? =
        dto.data?.hentPerson?.foedselsdato.orEmpty().let(::fromDto).also { logError(dto) }

    private fun fromDto(dto: List<PdlFoedselsdato>): LocalDate? =
        dto.firstOrNull()?.foedselsdato

    private fun logError(dto: PdlPersonResult) {
        dto.errors?.firstOrNull()?.message?.let {
            log.warn { "PDL error message: $it" }
        }
    }
}

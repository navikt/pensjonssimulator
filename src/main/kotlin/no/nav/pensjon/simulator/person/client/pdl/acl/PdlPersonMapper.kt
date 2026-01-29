package no.nav.pensjon.simulator.person.client.pdl.acl

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.person.Sivilstandstype
import no.nav.pensjon.simulator.person.Person
import java.time.LocalDate

object PdlPersonMapper {
    private val log = KotlinLogging.logger {}

    fun fromDto(dto: PdlPersonResult): Person? =
        dto.data?.hentPerson?.let {
            Person(
                foedselsdato = it.foedselsdato.orEmpty().let(::foedselsdato).also { logError(dto) },
                sivilstand = it.sivilstand.orEmpty().let(::sivilstand).also { logError(dto) },
                statsborgerskap = it.statsborgerskap.orEmpty().let(::statsborgerskap).also { logError(dto) }
            )
        }

    private fun foedselsdato(dto: List<PdlFoedselsdato>): LocalDate? =
        dto.firstOrNull()?.foedselsdato

    private fun sivilstand(dto: List<PdlSivilstand>): Sivilstandstype =
        PdlSivilstandType.internalValue(dto.firstOrNull()?.type)

    private fun statsborgerskap(dto: List<PdlStatsborgerskap>): LandkodeEnum =
        dto.firstOrNull()?.land?.let(LandkodeEnum::valueOf) ?: LandkodeEnum.P_UKJENT

    private fun logError(dto: PdlPersonResult) {
        dto.errors?.firstOrNull()?.message?.let {
            log.warn { "PDL error message: $it" }
        }
    }
}

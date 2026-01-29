package no.nav.pensjon.simulator.person.client.pdl.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class PdlPersonResult(
    val data: PdlPersonEnvelope?,
    val extensions: PdlExtensions?,
    val errors: List<PdlError>?
)

data class PdlPersonEnvelope(val hentPerson: PdlPerson?) // null if person not found

data class PdlPerson(
    val foedselsdato: List<PdlFoedselsdato>?,
    val sivilstand: List<PdlSivilstand>?,
    val statsborgerskap: List<PdlStatsborgerskap>?
)

data class PdlFoedselsdato(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val foedselsdato: LocalDate?
)

data class PdlSivilstand(val type: String?)

data class PdlStatsborgerskap(val land: String?)

data class PdlError(val message: String)

data class PdlExtensions(val warnings: List<PdlWarning>?)

data class PdlWarning(
    val query: String?,
    val id: String?,
    val code: String?,
    val message: String?,
    val details: Any?
)

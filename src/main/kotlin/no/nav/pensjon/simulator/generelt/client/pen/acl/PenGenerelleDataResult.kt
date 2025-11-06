package no.nav.pensjon.simulator.generelt.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class PenGenerelleDataResult(
    val person: PenPersonData?, // null if anonym
    val privatAfpSatser: PenPrivatAfpSatser?,
    val satsResultatListe: List<PenVeietSatsResultat>?,
    //TODO val sisteGyldigeOpptjeningsaar: Int
)

data class PenPersonData(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val foedselDato: LocalDate?,
    val statsborgerskap: String?
)

/**
 * PEN: SimulatorPrivatAfpSatser (based on no.nav.service.pensjon.fpen.HentSatserAFPPrivatResponse)
 */
data class PenPrivatAfpSatser(
    val forholdstall: Double? = null,
    val kompensasjonstilleggForholdstall: Double? = null,
    val justeringsbeloep: Long? = null,
    val referansebeloep: Long? = null
)

data class PenVeietSatsResultat(
    val aar: Int = 0,
    val verdi: Double = 0.0
)

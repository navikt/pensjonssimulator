package no.nav.pensjon.simulator.generelt.client.pen.acl

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

/**
 * Corresponds to SimulatorGenerelleData in pensjon-pen
 */
data class PenGenerelleDataResult(
    val person: PenPersonData?, // null if anonym
    val privatAfpSatser: PenPrivatAfpSatser?,
    val delingstallUtvalg: PenDelingstallUtvalg?,
    val forholdstallUtvalg: PenForholdstallUtvalg?,
    val satsResultatListe: List<PenVeietSatsResultat>?
)

data class PenPersonData(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val foedselDato: LocalDate?,
    val statsborgerskap: String?
)

data class PenPrivatAfpSatser(
    val forholdstall: PenAarskullTall? = null,
    /* TODO:
    val justeringsbelop: Justeringsbelop? = null,
    val ftKomp: ForholdstallKompensasjonstillegg? = null,
    val referansebelop: Referansebelop? = null
    */
)

data class PenAarskullTall(
    val aarskull: Long? = null, // Årskull forholdstallet gjelder for. Eks. 1964.
    val alderAar: Long? = null, // Alder for det gitte årskullet
    val maaneder: Long? = null, // Angir måned i året. 0 = januar, 11 = desember
    val tall: Double = 0.0 // Det gitte tall for et årskull, på en gitt alder, i en gitt måned
)

data class PenDelingstallUtvalg(
    val dt: Double = 0.0,
    val dt67soker: Double = 0.0,
    val dt67virk: Double = 0.0,
    val delingstallListe: MutableList<PenAarskullTall> = mutableListOf()
)

data class PenForholdstallUtvalg(
    val ft: Double = 0.0,
    val forholdstallListe: MutableList<PenAarskullTall> = mutableListOf(),
    val ft67soeker: Double = 0.0,
    val ft67virkning: Double = 0.0,
    val reguleringFaktor: Double = 0.0
)

data class PenVeietSatsResultat(
    val aar: Int = 0,
    val verdi: Double = 0.0
)

package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.core.domain.regler.enum.AFPtypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.listAsString
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.util.*

/**
 * Data transfer object (DTO) som representerer inn-data (spesifikasjon) for
 * simulering av alderspensjon "versjon 3" (som brukes av Navs pensjonskalkulator).
 */
@JsonInclude(NON_NULL)
data class NavSimuleringSpecV3(
    val pid: String,
    val sivilstand: NavSivilstandSpecV3,
    val uttaksar: Int? = null,
    val sisteInntekt: Int,
    val simuleringstype: NavSimuleringTypeSpecV3,
    val gradertUttak: NavSimuleringGradertUttakSpecV3? = null,
    val heltUttak: NavSimuleringHeltUttakSpecV3,
    val aarUtenlandsEtter16Aar: Int? = null,
    val epsHarPensjon: Boolean? = null,
    val epsHarInntektOver2G: Boolean? = null,
    val fremtidigInntektListe: List<NavSimuleringInntektSpecV3>? = null,
    val utenlandsperiodeListe: List<NavSimuleringUtlandSpecV3>? = null,
    val afpInntektMaanedFoerUttak: Boolean? = null,
    val afpOrdning: AFPtypeEnum? = null
) {
    /**
     * toString with redacted person ID
     */
    override fun toString() =
        "{ \"pid\": ${textAsString(redact(pid))}, " +
                "\"sivilstand\": ${textAsString(sivilstand)}, " +
                "\"uttaksar\": $uttaksar, " +
                "\"sisteInntekt\": $sisteInntekt, " +
                "\"simuleringstype\": ${textAsString(simuleringstype)}, " +
                "\"gradertUttak\": $gradertUttak, " +
                "\"heltUttak\": $heltUttak, " +
                "\"aarUtenlandsEtter16Aar\": $aarUtenlandsEtter16Aar, " +
                "\"epsHarPensjon\": $epsHarPensjon, " +
                "\"epsHarInntektOver2G\": $epsHarInntektOver2G, " +
                "\"fremtidigInntektListe\": ${listAsString(fremtidigInntektListe)}, " +
                "\"utenlandsperiodeListe\": ${listAsString(utenlandsperiodeListe)}, " +
                "\"afpInntektMaanedFoerUttak\": $afpInntektMaanedFoerUttak, " +
                "\"afpOrdning\": ${textAsString(afpOrdning)} }"
}

@JsonInclude(NON_NULL)
data class NavSimuleringGradertUttakSpecV3(
    val grad: UttakGradKode? = null,
    val uttakFomAlder: NavSimuleringAlderSpecV3? = null,
    val aarligInntekt: Int? = null
) {
    override fun toString() =
        "{ \"grad\": ${textAsString(grad)}, " +
                "\"uttakFomAlder\": $uttakFomAlder, " +
                "\"aarligInntekt\": $aarligInntekt }"
}

data class NavSimuleringHeltUttakSpecV3(
    val uttakFomAlder: NavSimuleringAlderSpecV3,
    val aarligInntekt: Int,
    val inntektTomAlder: NavSimuleringAlderSpecV3
) {
    override fun toString() =
        "{ \"uttakFomAlder\": $uttakFomAlder, " +
                "\"aarligInntekt\": $aarligInntekt, " +
                "\"inntektTomAlder\": $inntektTomAlder }"
}

@JsonInclude(NON_NULL)
data class NavSimuleringInntektSpecV3(
    val aarligInntekt: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: Date? = null
) {
    override fun toString() =
        "{ \"aarligInntekt\": $aarligInntekt, " +
                "\"fom\": ${textAsString(fom)} }"
}

@JsonInclude(NON_NULL)
data class NavSimuleringUtlandSpecV3(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: Date,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val tom: Date?,
    val land: String,
    val arbeidetUtenlands: Boolean
) {
    override fun toString() =
        "{ \"fom\": ${textAsString(fom)}, " +
                "\"tom\": ${textAsString(tom)}, " +
                "\"land\": ${textAsString(land)}, " +
                "\"arbeidetUtenlands\": $arbeidetUtenlands }"
}

data class NavSimuleringAlderSpecV3(
    val aar: Int,
    val maaneder: Int
) {
    override fun toString() =
        "{ \"aar\": $aar, " +
                "\"maaneder\": $maaneder }"
}


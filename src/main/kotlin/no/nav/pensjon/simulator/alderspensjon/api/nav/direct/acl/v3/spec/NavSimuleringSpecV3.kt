package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.acl.v3.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import java.util.*

// PEN: SimuleringSpecDtoAlderspensjon1963Plus
/**
 * Data transfer object som representerer inn-data (spesifikasjon) for
 * simulering av alderspensjon for brukere født 1963 eller senere.
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
    val afpOrdning: AfpOrdningType? = null
) {
    /**
     * toString with redacted person ID
     */
    override fun toString() =
        "\"pid\": \"${redact(pid)}\", " +
                "\"sivilstand\": \"$sivilstand\", " +
                "\"uttaksar\": $uttaksar, " +
                "\"sisteInntekt\": $sisteInntekt, " +
                "\"simuleringstype\": \"$simuleringstype\", " +
                "\"gradertUttak\": { $gradertUttak }, " +
                "\"heltUttak\": { $heltUttak }, " +
                "\"aarUtenlandsEtter16Aar\": $aarUtenlandsEtter16Aar, " +
                "\"epsHarPensjon\": $epsHarPensjon, " +
                "\"epsHarInntektOver2G\": $epsHarInntektOver2G, " +
                "\"fremtidigInntektListe\": [ $fremtidigInntektListe ], " +
                "\"utenlandsperiodeListe\": [ $utenlandsperiodeListe ]," +
                "\"afpInntektMaanedFoerUttak\": $afpInntektMaanedFoerUttak," +
                "\"afpOrdning\": \"$afpOrdning\","
}

@JsonInclude(NON_NULL)
data class NavSimuleringGradertUttakSpecV3(
    val grad: UttakGradKode? = null,
    val uttakFomAlder: NavSimuleringAlderSpecV3? = null,
    val aarligInntekt: Int? = null
)

data class NavSimuleringHeltUttakSpecV3(
    val uttakFomAlder: NavSimuleringAlderSpecV3,
    val aarligInntekt: Int,
    val inntektTomAlder: NavSimuleringAlderSpecV3
)

@JsonInclude(NON_NULL)
data class NavSimuleringInntektSpecV3(
    val aarligInntekt: Int? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: Date? = null
)

@JsonInclude(NON_NULL)
data class NavSimuleringUtlandSpecV3(
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: Date,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val tom: Date?,
    val land: String,
    val arbeidetUtenlands: Boolean
)

data class NavSimuleringAlderSpecV3(
    val aar: Int,
    val maaneder: Int
)

package no.nav.pensjon.simulator.alderspensjon.nav.api.acl.v1in

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.util.*

// SimuleringSpecDtoAlderspensjon1963Plus
/**
 * Data transfer object som representerer inn-data (spesifikasjon) for
 * simulering av alderspensjon for brukere født 1963 eller senere.
 */
data class NavSimuleringSpecV1(
    val pid: String,
    val sivilstand: SivilstatusType,
    val harEps: Boolean? = false, // TODO remove (unused)
    val uttaksar: Int,
    val sisteInntekt: Int,
    val simuleringstype: NavSivilstandSpecV1,
    val gradertUttak: NavSimuleringGradertUttakSpecV1? = null,
    val heltUttak: NavSimuleringHeltUttakSpecV1,
    val aarUtenlandsEtter16Aar: Int? = null,
    val epsHarPensjon: Boolean? = null,
    val epsHarInntektOver2G: Boolean? = null,
    val fremtidigInntektListe: List<NavSimuleringInntektSpecV1>? = null,
    val utenlandsperiodeListe: List<NavSimuleringUtlandSpecV1>? = null
)

data class NavSimuleringGradertUttakSpecV1(
    val grad: UttakGradKode? = null,
    val uttakFomAlder: NavSimuleringAlderSpecV1? = null,
    val aarligInntekt: Int? = null
)

data class NavSimuleringHeltUttakSpecV1(
    val uttakFomAlder: NavSimuleringAlderSpecV1,
    val aarligInntekt: Int,
    val inntektTomAlder: NavSimuleringAlderSpecV1
)

data class NavSimuleringInntektSpecV1(
    val aarligInntekt: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: Date? = null
)

data class NavSimuleringUtlandSpecV1(
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fom: Date,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val tom: Date?,
    val land: String,
    val arbeidetUtenlands: Boolean
)

data class NavSimuleringAlderSpecV1(
    val aar: Int,
    val maaneder: Int
)

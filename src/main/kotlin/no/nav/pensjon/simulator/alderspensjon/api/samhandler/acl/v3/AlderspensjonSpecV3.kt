package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import com.fasterxml.jackson.annotation.JsonFormat
import java.util.*

/**
 * Version 3 of specification for 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonRequestV3
 */
data class AlderspensjonSpecV3(
    val fnr: String? = null,
    val forsteUttak: UttaksperiodeSpecV3? = null,
    val heltUttak: UttaksperiodeSpecV3? = null,
    val arIUtlandetEtter16: Int? = null,
    val sivilstandVedPensjonering: SivilstatusSpecV3? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val fremtidigInntektListe: List<InntektSpecV3>? = null,
    val simulerMedAfpPrivat: Boolean = false
)

/**
 * Version 3 of specification for 'inntekt'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.FremtidigInntekt
 */
data class InntektSpecV3(
    val arligInntekt: Int? = null,

    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val fomDato: Date? = null
)

/**
 * Version 3 of specification for 'uttaksperiode'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.UttaksperiodeV3
 */
data class UttaksperiodeSpecV3(
    @param:JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val datoFom: Date? = null,

    val grad: Int? = null
)

/**
 * Version 3 of specification for 'sivilstatus'.
 * PEN: no.nav.domain.pensjon.kjerne.kodetabeller.SivilstatusTypeCode
 */
enum class SivilstatusSpecV3 {
    /**
     * Enke/-mann
     */
    ENKE,

    /**
     * Gift
     */
    GIFT,

    /**
     * Gjenlevende etter samlivsbrudd
     */
    GJES,

    /**
     * Gjenlevende partner
     */
    GJPA,

    /**
     * Gjenlevende samboer
     */
    GJSA,

    /**
     * Gift, lever adskilt
     */
    GLAD,

    /**
     * -
     */
    NULL,

    /**
     * Registrert partner, lever adskilt
     */
    PLAD,

    /**
     * Registrert partner
     */
    REPA,

    /**
     * Samboer
     */
    SAMB,

    /**
     * Separert partner
     */
    SEPA,

    /**
     * Separert
     */
    SEPR,

    /**
     * Skilt
     */
    SKIL,

    /**
     * Skilt partner
     */
    SKPA,

    /**
     * Ugift
     */
    UGIF
}

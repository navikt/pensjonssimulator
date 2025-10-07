package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.listAsString
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.util.*

/**
 * Version 3 of specification for 'simuler alderspensjon'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonRequestV3
 *   + SimulerAlderspensjonRequestV3Converter
 */
data class AlderspensjonSpecV3(
    val fnr: String, // required
    val sivilstandVedPensjonering: SivilstatusSpecV3, // required
    val forsteUttak: UttaksperiodeSpecV3, // required
    val heltUttak: UttaksperiodeSpecV3? = null, //TODO verifyUttaksperiode, verifyForsteUttakAndHeltUttakCombination
    val arIUtlandetEtter16: Int? = null,
    val epsPensjon: Boolean? = null, //TODO required for GIFT/REPA/SAMB
    val eps2G: Boolean? = null, //TODO required for GIFT/REPA/SAMB
    val fremtidigInntektListe: List<InntektSpecV3>? = null, //TODO verifyFieldsInFremtidigInntekt
    val simulerMedAfpPrivat: Boolean? = null
) {
    override fun toString() = asRedactedJson()

    private fun asRedactedJson() = """{
    "fnr": ${textAsString(redact(fnr))},
    "sivilstandVedPensjonering": ${textAsString(sivilstandVedPensjonering)},
    "forsteUttak": $forsteUttak,
    "heltUttak": $heltUttak,
    "arIUtlandetEtter16": $arIUtlandetEtter16,
    "epsPensjon": $epsPensjon,
    "eps2G": $eps2G,
    "fremtidigInntektListe": ${listAsString(fremtidigInntektListe)},
    "simulerMedAfpPrivat": $simulerMedAfpPrivat
}"""
}

/**
 * Version 3 of specification for 'inntekt'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.FremtidigInntekt
 */
data class InntektSpecV3(
    val arligInntekt: Int, // required
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fomDato: Date // required
) {
    override fun toString() = asJson()

    private fun asJson() = """{
        "fomDato": ${textAsString(fomDato)},
        "arligInntekt": $arligInntekt
    }"""
}


/**
 * Version 3 of specification for 'uttaksperiode'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.UttaksperiodeV3
 */
data class UttaksperiodeSpecV3(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val datoFom: Date, // required
    val grad: Int // required
) {
    override fun toString() = asJson()

    private fun asJson() = """{
        "datoFom": ${textAsString(datoFom)},
        "grad": $grad
    }"""
}

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

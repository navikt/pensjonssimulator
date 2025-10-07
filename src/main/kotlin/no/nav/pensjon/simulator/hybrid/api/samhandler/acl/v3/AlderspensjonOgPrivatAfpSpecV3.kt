package no.nav.pensjon.simulator.hybrid.api.samhandler.acl.v3

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.time.LocalDate
import java.util.*

/**
 * Version 3 of the data transfer object representing a specification for the
 * 'simuler alderspensjon & privat AFP' service.
 * ----------
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.meldinger.HentSimulertPensjonRequest
 * (cf. no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.SimulerAlderspensjonRequestV3)
 */
data class AlderspensjonOgPrivatAfpSpecV3(
    val personident: String, // required
    val sivilstatusVedPensjonering: ApOgPrivatAfpSivilstatusSpecV3, // required
    val foersteUttak: ApOgPrivatAfpUttakSpecV3, // required
    val heltUttak: ApOgPrivatAfpUttakSpecV3? = null,
    val aarligInntektFoerUttak: Int? = null,
    val antallInntektsaarEtterHeltUttak: Int? = null,
    val aarIUtlandetEtter16: Int? = null,
    val harEpsPensjon: Boolean? = null,
    val harEpsPensjonsgivendeInntektOver2G: Boolean? = null,
    //val fremtidigInntektListe: List<ApOgPrivatAfpInntektSpecV3>? = null,
    val simulerPrivatAfp: Boolean? = null
) {
    override fun toString() = asRedactedJson()

    private fun asRedactedJson() = """{
    "personident": ${textAsString(redact(personident))},
    "sivilstatusVedPensjonering": ${textAsString(sivilstatusVedPensjonering)},
    "foersteUttak": $foersteUttak,
    "heltUttak": $heltUttak,
    "aarligInntektFoerUttak": $aarligInntektFoerUttak,
    "antallInntektsaarEtterHeltUttak": $antallInntektsaarEtterHeltUttak,
    "aarIUtlandetEtter16": $aarIUtlandetEtter16,
    "harEpsPensjon": $harEpsPensjon,
    "harEpsPensjonsgivendeInntektOver2G": $harEpsPensjonsgivendeInntektOver2G,
    "simulerPrivatAfp": $simulerPrivatAfp
}"""
}

/**
 * Version 3 of specification for 'inntekt' in context of 'simuler alderspensjon og privat AFP'.
 * PEN: no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.FremtidigInntekt
 */
data class ApOgPrivatAfpInntektSpecV3(
    val aarligInntekt: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fomDato: Date? = null
) {
    override fun toString() = asJson()

    private fun asJson() = """{
        "fomDato": ${textAsString(fomDato)},
        "aarligInntekt": $aarligInntekt
    }"""
}

/**
 * Version 3 of specification for 'uttak' in context of 'simuler alderspensjon og privat AFP'.
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.informasjon.Uttaksperiode
 * (cf. no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.UttaksperiodeV3)
 */
data class ApOgPrivatAfpUttakSpecV3(
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fomDato: LocalDate, // required
    val grad: Int, // required
    val aarligInntekt: Int? = null
) {
    override fun toString() = asJson()

    private fun asJson() = """{
        "fomDato": ${textAsString(fomDato)},
        "grad": $grad,
        "aarligInntekt": $aarligInntekt
    }"""
}

/**
 * Version 3 of specification for 'sivilstatus' in context of 'simuler alderspensjon og privat AFP'.
 * Sivilstatus = sivilstand + samboerskap
 * -------------------------------------------
 * PEN: no.nav.tjeneste.ekstern.simulerepensjon.v1.meldinger.SivilstandVedPensjonering
 *  + no.nav.domain.pensjon.kjerne.kodetabeller.SivilstatusTypeCode
 */
enum class ApOgPrivatAfpSivilstatusSpecV3 {
    /**
     * Enke/enkemann
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

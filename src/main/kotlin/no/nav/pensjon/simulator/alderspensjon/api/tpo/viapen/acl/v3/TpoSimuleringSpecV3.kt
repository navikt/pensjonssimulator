package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.listAsString
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.time.LocalDate

/**
 * This class is basically a subset of SimuleringSpec.
 * It maps 1-to-1 with SimuleringSpecLegacyV3 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringSpecV3(
    val pid: String? = null,
    val sivilstatus: SivilstatusType? = null,
    val epsPensjon: Boolean? = false,
    val eps2G: Boolean? = false,
    val utenlandsopphold: Int? = 0,
    val simuleringType: SimuleringTypeEnum? = null,
    val fremtidigInntektList: List<InntektSpecLegacyV3>? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val foersteUttakDato: LocalDate? = null,
    val uttakGrad: UttakGradKode? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val heltUttakDato: LocalDate? = null
) {
    /**
     * toString with redacted person ID
     */
    override fun toString() =
        "{ \"pid\": ${textAsString(redact(pid))}, " +
                "\"sivilstatus\": ${textAsString(sivilstatus)}, " +
                "\"epsPensjon\": $epsPensjon, " +
                "\"eps2G\": $eps2G, " +
                "\"utenlandsopphold\": $utenlandsopphold, " +
                "\"simuleringType\": ${textAsString(simuleringType)}, " +
                "\"fremtidigInntektList\": ${listAsString(fremtidigInntektList)}, " +
                "\"foersteUttakDato\": ${textAsString(foersteUttakDato)}, " +
                "\"uttakGrad\": ${textAsString(uttakGrad)}, " +
                "\"heltUttakDato\": ${textAsString(heltUttakDato)} }"
}

// Corresponds to no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.FremtidigInntekt
data class InntektSpecLegacyV3(
    val arligInntekt: Int = 0,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fomDato: LocalDate
) {
    override fun toString(): String =
        "{ \"arligInntekt\": $arligInntekt, " +
                "\"fomDato\": ${textAsString(fomDato)} }"
}


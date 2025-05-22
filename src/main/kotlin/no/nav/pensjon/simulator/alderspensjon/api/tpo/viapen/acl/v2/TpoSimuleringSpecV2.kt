package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v2

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
 * It maps 1-to-1 with SimuleringSpecLegacyV2 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringSpecV2(
    val pid: String? = null,
    val sivilstatus: SivilstatusType? = null,
    val epsPensjon: Boolean? = false,
    val eps2G: Boolean? = false,
    val utenlandsopphold: Int? = 0,
    val simuleringType: SimuleringTypeEnum? = null,
    val fremtidigInntektList: List<InntektSpecLegacyV2>? = null, // V2, V3 only
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val foersteUttakDato: LocalDate? = null,
    val uttakGrad: UttakGradKode? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val heltUttakDato: LocalDate? = null,
    val antallArInntektEtterHeltUttak: Int? = null // V1, V2 only
    // forventetInntekt & inntektUnderGradertUttak: V1 only (instead of fremtidigInntektList)
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
                "\"heltUttakDato\": ${textAsString(heltUttakDato)}, " +
                "\"antallArInntektEtterHeltUttak\": $antallArInntektEtterHeltUttak }"
}


data class InntektSpecLegacyV2(
    val arligInntekt: Int = 0,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val fomDato: LocalDate
) {
    override fun toString(): String =
        "{ \"arligInntekt\": $arligInntekt, " +
                "\"fomDato\": ${textAsString(fomDato)} }"
}

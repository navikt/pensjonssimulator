package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.time.LocalDate

/**
 * This class is basically a subset of SimuleringSpec.
 * It maps 1-to-1 with SimuleringSpecLegacyV1 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringSpecV1(
    val pid: String? = null,
    val sivilstatus: SivilstatusType? = null,
    val epsPensjon: Boolean? = false,
    val eps2G: Boolean? = false,
    val utenlandsopphold: Int? = 0,
    val simuleringType: SimuleringTypeEnum? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val foersteUttakDato: LocalDate? = null,
    val uttakGrad: UttakGradKode? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val heltUttakDato: LocalDate? = null,
    val antallArInntektEtterHeltUttak: Int? = null, // V1 only
    val forventetInntekt: Int? = null, // V1 only
    val inntektUnderGradertUttak: Int? = null, // V1 only
    val inntektEtterHeltUttak: Int? = null // V1 only
    // fremtidigInntektList: V3 only
) {
    /**
     * toString with redacted person ID
     */
    override fun toString(): String =
        "{ \"pid\": \"${redact(pid)}\", " +
                "\"sivilstatus\": ${textAsString(sivilstatus)}, " +
                "\"epsPensjon\": $epsPensjon, " +
                "\"eps2G\": $eps2G, " +
                "\"utenlandsopphold\": $utenlandsopphold, " +
                "\"simuleringType\": ${textAsString(simuleringType)}, " +
                "\"foersteUttakDato\": ${textAsString(foersteUttakDato)}, " +
                "\"uttakGrad\": ${textAsString(uttakGrad)}, " +
                "\"heltUttakDato\": ${textAsString(heltUttakDato)}, " +
                "\"antallArInntektEtterHeltUttak\": $antallArInntektEtterHeltUttak, " +
                "\"forventetInntekt\": $forventetInntekt, " +
                "\"inntektUnderGradertUttak\": $inntektUnderGradertUttak, " +
                "\"inntektEtterHeltUttak\": $inntektEtterHeltUttak }"
}

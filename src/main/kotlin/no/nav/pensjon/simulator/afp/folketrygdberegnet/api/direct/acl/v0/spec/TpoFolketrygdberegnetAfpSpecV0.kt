package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.direct.acl.v0.spec

import no.nav.pensjon.simulator.person.Pid.Companion.redact
import no.nav.pensjon.simulator.tech.json.Stringifier.textAsString
import java.util.Date

/**
 * Specification for 'simuler folketrygdberegnet AFP' for tjenestepensjonsordning.
 */
data class TpoFolketrygdberegnetAfpSpecV0(
    val simuleringType: TpoFolketrygdberegnetAfpSimuleringTypeSpecV0? = null,
    val fnr: TpoFolketrygdberegnetAfpPersonIdComboSpecV0? = null,
    val forventetInntekt: Int? = null,
    val forsteUttakDato: Date? = null,
    val inntektUnderGradertUttak: Int? = null,
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val sivilstatus: TpoFolketrygdberegnetAfpSivilstandSpecV0? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val afpOrdning: String? = null, // PEN: AfpOrdningTypeCode
    val afpInntektMndForUttak: Int? = null
) {
    override fun toString(): String =
        "{ \"simuleringType\": ${textAsString(simuleringType)}, " +
                "\"fnr:\" \"$fnr\", " +
                "\"forventetInntekt\": $forventetInntekt, " +
                "\"forsteUttakDato\": ${textAsString(forsteUttakDato)}, " +
                "\"inntektUnderGradertUttak\": $inntektUnderGradertUttak, " +
                "\"inntektEtterHeltUttak\": $inntektEtterHeltUttak, " +
                "\"antallArInntektEtterHeltUttak\": $antallArInntektEtterHeltUttak, " +
                "\"utenlandsopphold\": $utenlandsopphold, " +
                "\"sivilstatus\": ${textAsString(sivilstatus)}, " +
                "\"epsPensjon\": $epsPensjon, " +
                "\"eps2G\": $eps2G, " +
                "\"afpOrdning\": ${textAsString(afpOrdning)}, " +
                "\"afpInntektMndForUttak\": $afpInntektMndForUttak }"
}

data class TpoFolketrygdberegnetAfpPersonIdComboSpecV0(
    val pid: String,
    val dnummer: Boolean? = false,
    val npid: Boolean? = false,
    val pidInvalidWithBostnummer: Boolean? = false
) {
    override fun toString(): String =
        "{ pid: \"${redact(pid)}\" }"
}

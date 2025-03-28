package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec

import no.nav.pensjon.simulator.person.Pid.Companion.redact
import java.util.Date

/**
 * Specification for 'simuler folketrygdberegnet AFP'.
 * Corresponds to no.nav.pensjon.pen.domain.api.beregning.FolketrygdberegnetAfpSimuleringSpec in PEN.
 */
data class FolketrygdberegnetAfpSpecV1(
    val simuleringType: FolketrygdberegnetAfpSimuleringTypeSpecV1? = null,
    val fnr: String? = null,
    val forventetInntekt: Int? = null,
    val forsteUttakDato: Date? = null,
    val inntektUnderGradertUttak: Int? = null,
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val sivilstatus: FolketrygdberegnetAfpSivilstandSpecV1? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val afpOrdning: String? = null, // PEN: AfpOrdningTypeCode
    val afpInntektMndForUttak: Int? = null
) {
    override fun toString(): String =
        "\"simuleringType\": \"$simuleringType\", " +
                "\"fnr:\" \"${redact(fnr)}\", " +
                "\"forventetInntekt\": $forventetInntekt, " +
                "\"forsteUttakDato\": \"$forsteUttakDato\", " +
                "\"inntektUnderGradertUttak\": $inntektUnderGradertUttak, " +
                "\"inntektEtterHeltUttak\": $inntektEtterHeltUttak, " +
                "\"antallArInntektEtterHeltUttak\": $antallArInntektEtterHeltUttak, " +
                "\"utenlandsopphold\": $utenlandsopphold, " +
                "\"sivilstatus\": \"$sivilstatus\", " +
                "\"epsPensjon\": $epsPensjon, " +
                "\"eps2G\": $eps2G, " +
                "\"afpOrdning\": \"$afpOrdning\", " +
                "\"afpInntektMndForUttak\": $afpInntektMndForUttak"
}

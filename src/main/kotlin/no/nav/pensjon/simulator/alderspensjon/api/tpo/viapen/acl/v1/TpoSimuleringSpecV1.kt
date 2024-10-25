package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v1

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate

/**
 * This class is basically a subset of SimuleringSpec.
 * It maps 1-to-1 with SimuleringSpecLegacyV1 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringSpecV1  (
    val pid: String? = null,
    val sivilstatus: SivilstatusType? = null,
    val epsPensjon: Boolean? = false,
    val eps2G: Boolean? = false,
    val utenlandsopphold: Int? = 0,
    val simuleringType: SimuleringType? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val foersteUttakDato: LocalDate? = null,

    val uttakGrad: UttakGradKode? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val heltUttakDato: LocalDate? = null,

    val antallArInntektEtterHeltUttak: Int? = null, // V1, V2 only
    val forventetInntekt: Int? = null, // V1 only
    val inntektUnderGradertUttak: Int? = null, // V1 only
    val inntektEtterHeltUttak: Int? = null // V1 only
    // fremtidigInntektList: V2, V3 only
)

package no.nav.pensjon.simulator.alderspensjon.tpo.api.acl.v2in

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import java.time.LocalDate

/**
 * This class is basically a subset of SimuleringSpec.
 * It maps 1-to-1 with SimuleringSpecLegacyV2 in PEN
 * (except that dates are represented by java.time.LocalDate here and by java.util.Date in PEN).
 */
data class TpoSimuleringSpecV2  (
    val pid: String? = null,
    val sivilstatus: SivilstatusType? = null,
    val epsPensjon: Boolean? = false,
    val eps2G: Boolean? = false,
    val utenlandsopphold: Int? = 0,
    val simuleringType: SimuleringType? = null,
    val fremtidigInntektList: List<InntektSpecLegacyV2>? = null, // V2, V3 only

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val foersteUttakDato: LocalDate? = null,

    val uttakGrad: UttakGradKode? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val heltUttakDato: LocalDate? = null,

    val antallArInntektEtterHeltUttak: Int? = null // V1, V2 only
    // forventetInntekt & inntektUnderGradertUttak: V1 only (instead of fremtidigInntektList)
)

data class InntektSpecLegacyV2(
    val arligInntekt: Int = 0,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val fomDato: LocalDate
)

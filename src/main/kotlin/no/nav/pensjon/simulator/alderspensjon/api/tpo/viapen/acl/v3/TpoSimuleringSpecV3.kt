package no.nav.pensjon.simulator.alderspensjon.api.tpo.viapen.acl.v3

import com.fasterxml.jackson.annotation.JsonFormat
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
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
    val simuleringType: SimuleringType? = null,
    val fremtidigInntektList: List<InntektSpecLegacyV3>? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val foersteUttakDato: LocalDate? = null,

    val uttakGrad: UttakGradKode? = null,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val heltUttakDato: LocalDate? = null
) {
    /**
     * toString excluding personId
     */
    override fun toString(): String =
        "sivilstatus: $sivilstatus\n" +
                "epsPensjon: $epsPensjon\n" +
                "eps2G: $eps2G\n" +
                "utenlandsopphold: $utenlandsopphold\n" +
                "simuleringType: $simuleringType\n" +
                "fremtidigInntektList: $fremtidigInntektList,\n" +
                "foersteUttakDato: $foersteUttakDato,\n" +
                "uttakGrad: $uttakGrad,\n" +
                "heltUttakDato: $heltUttakDato"
}

// Corresponds to no.nav.pensjon.pen_app.provider.ws.simuleralderspensjon.v3.model.FremtidigInntekt
data class InntektSpecLegacyV3(
    val arligInntekt: Int = 0,

    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET")
    val fomDato: LocalDate
)

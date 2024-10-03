package no.nav.pensjon.simulator.alderspensjon.anonym.api.acl.v1in

import com.fasterxml.jackson.annotation.JsonFormat
import java.time.LocalDate

// SimuleringEtter2011
data class AnonymSimuleringSpecV1(
    val simuleringType: String? = null,
    val fodselsar: Int? = null,
    val forventetInntekt: Int? = null,
    val antArInntektOverG: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val forsteUttakDato: LocalDate? = null,
    val utg: String? = null,
    val inntektUnderGradertUttak: Int? = null,
    @JsonFormat(shape = JsonFormat.Shape.STRING, pattern = "yyyy-MM-dd", timezone = "CET") val heltUttakDato: LocalDate? = null,
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val sivilstatus: String? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null
)

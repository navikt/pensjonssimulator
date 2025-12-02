package no.nav.pensjon.simulator.alderspensjon.api.nav.direct.anonym.acl.v1.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

data class AnonymSimuleringSpecV1(
    val simuleringType: String? = null,
    val fodselsar: Int? = null,
    val forventetInntekt: Int? = null,
    val antArInntektOverG: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val forsteUttakDato: LocalDate? = null,
    val utg: String? = null,
    val inntektUnderGradertUttak: Int? = null,
    @param:JsonFormat(shape = STRING, pattern = "yyyy-MM-dd") val heltUttakDato: LocalDate? = null,
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val sivilstatus: String? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null
)

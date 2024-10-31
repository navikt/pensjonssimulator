package no.nav.pensjon.simulator.afp.folketrygdberegnet.api.viapen.acl.v1.spec

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import java.time.LocalDate

/**
 * Specification for 'simuler folketrygdberegnet AFP'.
 * Maps 1-to-1 with no.nav.pensjon.pen.domain.api.beregning.FolketrygdberegnetAfpSimuleringSpec in PEN.
 */
data class FolketrygdberegnetAfpSpecV1(
    val simuleringType: FolketrygdberegnetAfpSimuleringTypeSpecV1? = null,
    val fnr: String? = null,
    val forventetInntekt: Int? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val forsteUttakDato: LocalDate? = null,
    val inntektUnderGradertUttak: Int? = null,
    val inntektEtterHeltUttak: Int? = null,
    val antallArInntektEtterHeltUttak: Int? = null,
    val utenlandsopphold: Int? = null,
    val sivilstatus: FolketrygdberegnetAfpSivilstandSpecV1? = null,
    val epsPensjon: Boolean? = null,
    val eps2G: Boolean? = null,
    val afpOrdning: String? = null, // PEN: AfpOrdningTypeCode
    val afpInntektMndForUttak: Int? = null
)

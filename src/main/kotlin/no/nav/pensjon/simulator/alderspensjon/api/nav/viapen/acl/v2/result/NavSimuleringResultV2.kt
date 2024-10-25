package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import com.fasterxml.jackson.annotation.JsonFormat
import com.fasterxml.jackson.annotation.JsonFormat.Shape.STRING
import com.fasterxml.jackson.annotation.JsonInclude
import com.fasterxml.jackson.annotation.JsonInclude.Include.NON_NULL
import no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.spec.NavSimuleringSpecV2
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import java.time.LocalDate

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011ResultatResponse in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011ResultatResponse in PSELV)
data class NavSimuleringSpecAndResultV2(
    val simulering: NavSimuleringSpecV2,
    val simuleringsresultat: NavSimuleringResultV2
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011Resultat in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.SimuleringEtter2011Resultat in PSELV)
@JsonInclude(NON_NULL)
data class NavSimuleringResultV2(
    val ap: SimulertAlderspensjonV2?,
    val afpPrivat: List<SimulertPrivatAfpPeriodeV2>,
    val afpOffentlig: SimuleringResultatV2?,
    val opptjeningListe: List<SimulertOpptjeningV2>,
    val grunnbelop: Int?,
    val sivilstand: SivilstandEnum,
    val epsPensjon: Boolean?,
    val eps2G: Boolean?
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimulertAlderspensjon in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.SimulertAlderspensjon in PSELV)
@JsonInclude(NON_NULL)
data class SimulertAlderspensjonV2(
    val pensjonsperiodeListe: List<PensjonPeriodeV2>,
    val uttaksgradListe: List<UttakGradV2>,
    val andelKap19: Double?,
    val andelKap20: Double?
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.Pensjonsperiode in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.Pensjonsperiode in PSELV)
@JsonInclude(NON_NULL)
data class PensjonPeriodeV2(
    val belop: Int?,
    val alder: Int?,
    val simulertBeregningsinformasjonListe: List<SimulertBeregningInformasjonV2>
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimulertAfpPrivatperiode in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.SimulertAfpPrivatperiode in PSELV)
@JsonInclude(NON_NULL)
data class SimulertPrivatAfpPeriodeV2(
    val alder: Int?,
    val belopArlig: Int?,
    val belopMnd: Int?,
    val livsvarig: Int?,
    val kronetillegg: Int?,
    val komptillegg: Int?
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.Simuleringsresultat in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.Simuleringsresultat in PSELV)
@JsonInclude(NON_NULL)
data class SimuleringResultatV2(
    val status: VedtakResultatEnum? = null, // VilkarsvedtakResultatCode
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val virk: LocalDate? = null,
    val beregning: NavBeregningResultV2? = null,
    val delberegninger: Map<Long, NavBeregningResultV2> = emptyMap(),
    val merknader: List<MerknadV2> = emptyList()
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.Merknad in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.Merknad in PSELV)
@JsonInclude(NON_NULL)
data class MerknadV2(
    val ar: Int? = null, // will not be set
    val argumentListeString: String? = null,
    val kode: String? = null
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.beregning.BeregningRelasjon in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.beregning.BeregningRelasjon in PSELV)
@JsonInclude(NON_NULL)
data class BeregningRelasjonV2(
    val delBeregning: Long? = null,
    val bruk: Boolean = false
    // not used in PSELV:
    // beregningRelasjonId
    // inngarIBeregning
    // beregning2011
    // inngarIBeregning2011
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimulertOpptjening in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.SimulertOpptjening in PSELV)
@JsonInclude(NON_NULL)
data class SimulertOpptjeningV2(
    val arstall: Int?,
    val pensjonsgivendeInntekt: Int?,
    val pensjonsbeholdning: Int?,
    val pensjonspoengOmsorg: Double?,
    val pensjonspoengPi: Double?,
    val dagpenger: Boolean?,
    val dagpengerFiskere: Boolean?,
    val forstegangstjeneste: Boolean?,
    val omsorg: Boolean?,
    val harUfore: Boolean?,
    val harAfpOffentlig: Boolean?
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.kalkulator.SimulertBeregningsinformasjon in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.kalkulator.SimulertBeregningsinformasjon in PSELV)
@JsonInclude(NON_NULL)
data class SimulertBeregningInformasjonV2(
    val uttaksgrad: Double?,
    val belopMnd: Int?,
    val startMnd: Int?,
    val spt: Double?,
    val gp: Int?,
    val tp: Int?,
    val pt: Int?,
    val ttAnvKap19: Int?,
    val ttAnvKap20: Int?,
    val paE91: Int?,
    val paF92: Int?,
    val forholdstall: Double?,
    val delingstall: Double?,
    val pensjonsbeholdningForUttak: Int?,
    val pensjonsbeholdningEtterUttak: Int?,
    val basispensjon: Int?,
    val restbasispensjon: Int?,
    val inntektspensjon: Int?,
    val garantipensjon: Int?,
    val garantitillegg: Int?,
    val apKap19medGJR: Int?,
    val apKap19utenGJR: Int?,
    val pensjonKap19: Int?,
    val pensjonKap20: Int?,
    val pensjonKap19Vektet: Int?,
    val pensjonKap20Vektet: Int?,
    val noKap19: Int?,
    val noKap20: Int?,
    val gjtAP: Int?,
    val gjtAPKap19: Int?,
    val minstenivaTilleggIndividuelt: Int?,
    val minstenivaTilleggPensjonistpar: Int?,
    val minstePensjonsnivaSats: Double?,
    val skjermingstillegg: Int?
)

// Maps 1-to-1 with no.nav.pensjon.pen.domain.api.sak.Uttaksgrad in PEN
// (which is the same as no.nav.pensjon.pen.domain.api.sak.Uttaksgrad in PSELV)
@JsonInclude(NON_NULL)
data class UttakGradV2(
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val fomDato: LocalDate? = null,
    @JsonFormat(shape = STRING, pattern = "yyyy-MM-dd", timezone = "CET") val tomDato: LocalDate? = null,
    val uttaksgrad: Int? = null,
    val version: Int? = null
    // Not used in PSELV:
    // uttaksgradId
    // uttaksgradKopiert
    // changeStamp
)

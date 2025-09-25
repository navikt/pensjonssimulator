package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class AfpOffentligLivsvarigYtelseMedDelingstall (
    val pensjonsbeholdning: Int,
    val afpYtelsePerAar: Double,
    val delingstall: Double,
    val gjelderFraOgMed: LocalDate,
    val gjelderFraOgMedAlder: Alder,
)

data class AfpBeregningsgrunnlag(val pensjonsbeholdning: Int, val alderForDelingstall: AlderForDelingstall, val delingstall: Double)

data class InntektPeriode(@param:JsonProperty("fraOgMedDato") val fom: LocalDate, @param:JsonProperty("arligInntekt") val arligInntekt: Int)

data class SimulerAFPBeholdningGrunnlagRequest(val personId: Pid, @param:JsonProperty("uttaksDato") val fom: LocalDate, @param:JsonProperty("fremtidigInntektListe") val inntekter: List<InntektPeriode>)

data class PensjonsbeholdningMedDelingstallAlder(val pensjonsbeholdning: Int, val alderForDelingstall: AlderForDelingstall)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AFPGrunnlagBeholdningPeriode(@param:JsonProperty("belop") val pensjonsBeholdning: Int, @param:JsonProperty("fraOgMedDato") val fom: LocalDate)
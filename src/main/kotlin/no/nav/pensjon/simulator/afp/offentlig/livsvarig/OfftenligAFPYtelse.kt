package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class AfpBeregningsgrunnlag(val pensjonsbeholdning: Int, val alderForDelingstall: AlderForDelingstall, val delingstall: Double)

data class InntektPeriode(val fraOgMedDato: LocalDate, val arligInntekt: Int)

data class SimulerAFPBeholdningGrunnlagRequest(val personId: Pid, val uttaksDato: LocalDate, val fremtidigInntektListe: List<InntektPeriode>)

data class PensjonsbeholdningMedDelingstallAlder(val pensjonsbeholdning: Int, val alderForDelingstall: AlderForDelingstall)

@JsonIgnoreProperties(ignoreUnknown = true)
data class AFPGrunnlagBeholdningPeriode(@param:JsonProperty("belop") val pensjonsBeholdning: Int, @param:JsonProperty("fraOgMedDato") val fom: LocalDate)
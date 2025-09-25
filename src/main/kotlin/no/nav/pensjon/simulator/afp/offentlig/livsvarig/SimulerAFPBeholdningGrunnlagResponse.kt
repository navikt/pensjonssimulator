package no.nav.pensjon.simulator.afp.offentlig.livsvarig

import com.fasterxml.jackson.annotation.JsonIgnoreProperties
import com.fasterxml.jackson.annotation.JsonProperty

@JsonIgnoreProperties(ignoreUnknown = true)
data class SimulerAFPBeholdningGrunnlagResponse(@param:JsonProperty("afpBeholdningsgrunnlag") val pensjonsBeholdningsPeriodeListe: List<AFPGrunnlagBeholdningPeriode>)

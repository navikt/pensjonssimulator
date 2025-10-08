package no.nav.pensjon.simulator.afp.offentlig.fra2025.beholdninger.client.acl

import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

data class SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec(val personId: Pid, val uttaksDato: LocalDate, val fremtidigInntektListe: List<Inntektsperiode>)


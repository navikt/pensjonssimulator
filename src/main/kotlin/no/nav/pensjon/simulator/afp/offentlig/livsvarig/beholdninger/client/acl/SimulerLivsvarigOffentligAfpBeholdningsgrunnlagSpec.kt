package no.nav.pensjon.simulator.afp.offentlig.livsvarig.beholdninger.client.acl

import java.time.LocalDate

data class SimulerLivsvarigOffentligAfpBeholdningsgrunnlagSpec(
    val personId: String,
    val uttaksDato: LocalDate,
    val fremtidigInntektListe: List<Inntektsperiode>
)


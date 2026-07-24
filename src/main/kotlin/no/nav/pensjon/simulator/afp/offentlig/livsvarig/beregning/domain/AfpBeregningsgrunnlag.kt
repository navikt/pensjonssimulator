package no.nav.pensjon.simulator.afp.offentlig.livsvarig.beregning.domain

data class AfpBeregningsgrunnlag(val pensjonsbeholdning: Int, val alderForDelingstall: AlderForDelingstall, val delingstall: Double)

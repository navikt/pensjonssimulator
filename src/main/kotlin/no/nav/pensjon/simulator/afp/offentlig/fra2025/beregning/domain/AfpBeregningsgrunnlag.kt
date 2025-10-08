package no.nav.pensjon.simulator.afp.offentlig.fra2025.beregning.domain

data class AfpBeregningsgrunnlag(val pensjonsbeholdning: Int, val alderForDelingstall: AlderForDelingstall, val delingstall: Double)

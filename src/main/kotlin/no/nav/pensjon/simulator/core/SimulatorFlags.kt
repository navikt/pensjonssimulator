package no.nav.pensjon.simulator.core

data class SimulatorFlags(
    val inkluderLivsvarigOffentligAfp: Boolean,
    val inkluderPensjonBeholdninger: Boolean,
    val ignoreAvslag: Boolean,
    val outputSimulertBeregningInformasjonForAllKnekkpunkter: Boolean
)

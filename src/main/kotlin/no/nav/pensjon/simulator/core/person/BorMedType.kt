package no.nav.pensjon.simulator.core.person

// no.nav.domain.pensjon.kjerne.kodetabeller.BorMedTypeCode
enum class BorMedType {
    /**
     * Vurdert som enslig
     */
    GLAD_EKT,

    /**
     * Vurdert som enslig
     */
    GLAD_PART,

    /**
     * Ja
     */
    J_AVDOD,

    /**
     * Ja
     */
    J_BARN,

    /**
     * Vurdert som gift
     */
    J_EKTEF,

    /**
     * Ja
     */
    J_FBARN,

    /**
     * Vurdert som gift
     */
    J_PARTNER,

    /**
     * Ja
     */
    J_SOSKEN,

    /**
     * Nei
     */
    N_AVDOD,

    /**
     * Nei
     */
    N_BARN,

    /**
     * Nei
     */
    N_FBARN,

    /**
     * Vurdert som gift
     */
    N_GIFT,

    /**
     * Vurdert som gift
     */
    N_GIFT_P,

    /**
     * Vurderes som ikke samboer
     */
    N_SAMBOER,

    /**
     * Nei
     */
    N_SOSKEN,

    /**
     * Vurdert som samboer §1-5
     */
    SAMBOER1_5,

    /**
     * Vurdert som samboer §3-2
     */
    SAMBOER3_2
}

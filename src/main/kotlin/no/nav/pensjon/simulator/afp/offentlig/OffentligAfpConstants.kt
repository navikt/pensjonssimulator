package no.nav.pensjon.simulator.afp.offentlig

object OffentligAfpConstants {
    /**
     * Fødselsåret som markerer overgang fra "gamle" til "nye" regler for AFP i offentlig sektor.
     * "Gamle" regler gjelder før 2025.
     * "Nye" regler gjelder fra 2025 (offentlig AFP er da en livsvarig ytelse).
     * 2025 er det året da personer født 1963 oppnår nedre aldersgrense for pensjonering (som i 2025 er 62 år).
     */
    const val OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR = 1963
}

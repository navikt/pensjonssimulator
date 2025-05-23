package no.nav.pensjon.simulator.uttak

enum class Uttaksgrad(val prosentsats: Int) {
    NULL(0),
    TJUE_PROSENT(20),
    FOERTI_PROSENT(40),
    FEMTI_PROSENT(50),
    SEKSTI_PROSENT(60),
    AATTI_PROSENT(80),
    HUNDRE_PROSENT(100);

    companion object {
        @OptIn(ExperimentalStdlibApi::class)
        fun from(prosentsats: Int) =
            entries.singleOrNull { it.prosentsats == prosentsats }
                ?: throw IllegalArgumentException("Ugyldig prosentsats for uttaksgrad: $prosentsats")
    }
}

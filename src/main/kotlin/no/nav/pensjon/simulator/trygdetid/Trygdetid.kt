package no.nav.pensjon.simulator.trygdetid

data class Trygdetid(
    val kapittel19: Int,
    val kapittel20: Int
) {
    val erTilstrekkelig: Boolean
        get() = kapittel19 >= KAPITTEL19_MINIMUM_TRYGDETID_ANTALL_AAR ||
                kapittel20 >= MINIMUM_TRYGDETID_FOR_GARANTIPENSJON_ANTALL_AAR

    private companion object {
        /**
         * Folketrygdloven kapittel 19, https://lovdata.no/lov/1997-02-28-19/§19-2:
         * "Det er et vilkår for rett til alderspensjon at vedkommende har minst fem års trygdetid" (med noen unntak)
         */
        private const val KAPITTEL19_MINIMUM_TRYGDETID_ANTALL_AAR = 5

        /**
         * Folketrygdloven kapittel 20, https://lovdata.no/lov/1997-02-28-19/§20-10:
         * "Det er et vilkår for rett til garantipensjon at vedkommende har minst fem års trygdetid"
         */
        private const val MINIMUM_TRYGDETID_FOR_GARANTIPENSJON_ANTALL_AAR = 5
    }
}

package no.nav.pensjon.simulator.opptjening.client.popp.acl

import no.nav.pensjon.simulator.core.domain.regler.Uforeopptjening
import no.nav.pensjon.simulator.core.domain.regler.Yrkesskadeopptjening

data class PoppUfoereOpptjeningBeloep(
    val belop: Double? = null,
    val proRataBeregnetUp: Boolean? = null,
    val poengtall: Double? = null,
    val uforegrad: Int? = null,
    val antattInntekt: Double? = null,
    val antattInntektProRata: Double? = null,
    val andelProrata: Double? = null,
    val poengarTellerProRata: Int? = null,
    val poengarNevnerProRata: Int? = null,
    val antFremtidigArProRata: Int? = null,
    val poengAntattArligInntekt: Double? = null,
    val yrkesskadegrad: Int? = null,
    val antattInntektYrke: Double? = null
) {
    fun toUfoereopptjening() =
        Uforeopptjening().also {
            it.belop = belop ?: 0.0
            it.proRataBeregnetUP = proRataBeregnetUp == true
            it.poengtall = poengtall ?: 0.0
            it.ufg = uforegrad ?: 0
            it.antattInntekt = antattInntekt ?: 0.0
            it.antattInntekt_proRata = antattInntektProRata ?: 0.0
            it.andel_proRata = andelProrata ?: 0.0
            it.poengarTeller_proRata = poengarTellerProRata ?: 0
            it.poengarNevner_proRata = poengarNevnerProRata ?: 0
            it.antFremtidigeAr_proRata = antFremtidigArProRata ?: 0
            it.yrkesskadeopptjening = yrkesskadeopptjening()
        }

    private fun yrkesskadeopptjening() =
        Yrkesskadeopptjening().also {
            it.paa = poengAntattArligInntekt ?: 0.0
            it.yug = yrkesskadegrad ?: 0
            it.antattInntektYrke = antattInntektYrke ?: 0.0
        }
}
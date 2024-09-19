package no.nav.pensjon.simulator.core.krav

import no.nav.pensjon.simulator.core.SimuleringSpec
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import no.nav.pensjon.simulator.tech.time.DateUtil.maanederInnenforAaret
import no.nav.pensjon.simulator.tech.time.DateUtil.maanederInnenforRestenAvAaret

object KravUtil {

    fun kravlinjeType(type: KravlinjeTypePlus) =
        KravlinjeTypeCti(type.name).apply { hovedKravlinje = type.erHovedkravlinje }

    /**
     * Finner antall hele måneder utenlands i et gitt år.
     */
    fun utlandMaanederInnenforAaret(spec: SimuleringSpec, year: Int): Int =
        spec.utlandPeriodeListe.maxOfOrNull {
            maanederInnenforAaret(it.fom, it.tom, year)
        } ?: 0

    /**
     * Finner antall hele måneder utenlands i perioden fra første uttaksdato til siste dag i det samme året.
     */
    fun utlandMaanederInnenforRestenAvAaret(spec: SimuleringSpec): Int =
        spec.utlandPeriodeListe.maxOfOrNull {
            maanederInnenforRestenAvAaret(it.fom, it.tom, spec.foersteUttakDato!!)
        } ?: 0
}


package no.nav.pensjon.simulator.core.domain.reglerextend.beregning

import no.nav.pensjon.simulator.core.domain.regler.beregning.AfpTillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Familietillegg
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

fun AfpTillegg.copy() =
    AfpTillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun Familietillegg.copy() =
    Familietillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun copyYtelseskomponent(
    source: Ytelseskomponent,
    target: Ytelseskomponent
) {
    target.brutto = source.brutto
    target.netto = source.netto
    target.fradrag = source.fradrag
    target.bruttoPerAr = source.bruttoPerAr
    target.nettoPerAr = source.nettoPerAr
    target.fradragPerAr = source.fradragPerAr
    target.ytelsekomponentTypeEnum = source.ytelsekomponentTypeEnum
    target.formelKodeEnum = source.formelKodeEnum
    target.merknadListe = source.merknadListe.map { it.copy() }.toMutableList()
    target.reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
    target.fradragsTransaksjon = source.fradragsTransaksjon
    target.opphort = source.opphort
    target.sakTypeEnum = source.sakTypeEnum
    //--- Extra:
    target.brukt = source.brukt
    //target.unroundedNettoPerAr = source.unroundedNettoPerAr
}

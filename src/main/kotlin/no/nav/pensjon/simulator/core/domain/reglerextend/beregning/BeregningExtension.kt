package no.nav.pensjon.simulator.core.domain.reglerextend.beregning

import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BasisGrunnpensjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.trygdetid.AnvendtTrygdetid
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copyBarnetillegg
import no.nav.pensjon.simulator.core.domain.reglerextend.copy

fun AfpTillegg.copy() =
    AfpTillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun BarnetilleggFellesbarn.copy() =
    BarnetilleggFellesbarn().also {
        copyBarnetillegg(source = this, target = it)
    }

fun BarnetilleggSerkullsbarn.copy() =
    BarnetilleggSerkullsbarn().also {
        copyBarnetillegg(source = this, target = it)
    }

fun Familietillegg.copy() =
    Familietillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun FasteUtgifterTillegg.copy() =
    FasteUtgifterTillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

/*
fun Grunnpensjon.copy() =
    Grunnpensjon().also {
        it.pSats_gp = this.pSats_gp
        it.satsTypeEnum = this.satsTypeEnum
        it.ektefelleInntektOver2G = this.ektefelleInntektOver2G
        it.anvendtTrygdetid = this.anvendtTrygdetid?.let(::AnvendtTrygdetid)
        copyYtelseskomponent(source = this, target = it)
    }

fun Grunnpensjon.basis() =
    BasisGrunnpensjon().also {
        it.pSats_gp = this.pSats_gp
        it.satsTypeEnum = this.satsTypeEnum
        it.ektefelleInntektOver2G = this.ektefelleInntektOver2G
        it.anvendtTrygdetid = this.anvendtTrygdetid?.let(::AnvendtTrygdetid)
        copyYtelseskomponent(source = this, target = it)
        brutto = 0
        netto = 0
    }
*/

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

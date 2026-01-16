package no.nav.pensjon.simulator.core.domain.reglerextend.beregning

import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning.penobjekter.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copyBarnetillegg
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.util.copy

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

fun Beregning.copy() =
    Beregning().also {
        it.penPerson = this.penPerson?.let(::PenPerson)
        it.virkFom = this.virkFom?.copy()
        it.virkTom = this.virkTom?.copy()
        it.brutto = this.brutto
        it.netto = this.netto
        it.gp = this.gp?.let(::Grunnpensjon)
        it.gpKapittel3 = this.gpKapittel3?.let(::Grunnpensjon)
        it.gpAfpPensjonsregulert = this.gpAfpPensjonsregulert?.let(::Grunnpensjon)
        it.tp = this.tp?.let(::Tilleggspensjon)
        it.tpKapittel3 = this.tpKapittel3?.let(::Tilleggspensjon)
        it.st = this.st?.copy()
        it.stKapittel3 = this.stKapittel3?.copy()
        it.minstenivatilleggPensjonistpar = this.minstenivatilleggPensjonistpar?.copy()
        it.minstenivatilleggIndividuelt = this.minstenivatilleggIndividuelt?.copy()
        it.afpTillegg = this.afpTillegg?.copy()
        it.vt = this.vt?.copy()
        it.vtKapittel3 = this.vtKapittel3?.copy()
        it.p851_tillegg = this.p851_tillegg?.copy()
        it.et = this.et?.let(::Ektefelletillegg)
        it.tfb = this.tfb?.copy()
        it.tsb = this.tsb?.copy()
        it.familietillegg = this.familietillegg?.copy()
        it.tilleggFasteUtgifter = this.tilleggFasteUtgifter?.copy()
        it.garantitillegg_Art_27 = this.garantitillegg_Art_27?.copy()
        it.garantitillegg_Art_50 = this.garantitillegg_Art_50?.copy()
        it.hjelpeloshetsbidrag = this.hjelpeloshetsbidrag?.copy()
        it.krigOgGammelYrkesskade = this.krigOgGammelYrkesskade?.copy()
        it.konverteringsdataUT = this.konverteringsdataUT?.copy()
        it.mendel = this.mendel?.copy()
        it.tilleggTilHjelpIHuset = this.tilleggTilHjelpIHuset?.copy()
        it.g = this.g
        it.tt_anv = this.tt_anv
        it.beregningsMetodeEnum = this.beregningsMetodeEnum
        it.trygdetid = this.trygdetid?.copy()
        // NB: Not copying delberegningsListe
        it.beregningTypeEnum = this.beregningTypeEnum
        it.resultatTypeEnum = this.resultatTypeEnum
        it.ikkeTraverser = this.ikkeTraverser
        it.ektefelleInntektOver2g = this.ektefelleInntektOver2g
        it.p67beregning = this.p67beregning
        it.beregningArsakEnum = this.beregningArsakEnum
        it.minstepensjontypeEnum = this.minstepensjontypeEnum
        it.minstepensjonArsak = this.minstepensjonArsak
        it.totalVinner = this.totalVinner
        it.afpPensjonsgrad = this.afpPensjonsgrad
        it.fribelop = this.fribelop
        it.friinntekt = this.friinntekt
        it.beregnetFremtidigInntekt = this.beregnetFremtidigInntekt
        it.ektefelleMottarPensjon = this.ektefelleMottarPensjon
        it.uforeEkstra = this.uforeEkstra?.copy()
        it.benyttetSivilstandEnum = this.benyttetSivilstandEnum
        it.brukersSivilstandEnum = this.brukersSivilstandEnum
        it.gradert = this.gradert
        it.inntektBruktIAvkorting = this.inntektBruktIAvkorting
        it.redusertPgaInstOpphold = this.redusertPgaInstOpphold
        it.instOppholdTypeEnum = this.instOppholdTypeEnum
        it.ufg = this.ufg
        it.yug = this.yug
        it.brukOpptjeningFra65I66Aret = this.brukOpptjeningFra65I66Aret
        it.eosEkstra = this.eosEkstra?.copy()
        it.lonnsvekstInformasjon = this.lonnsvekstInformasjon?.copy()
        it.pubReguleringFratrekk = this.pubReguleringFratrekk
        it.ttBeregnetForGrunnlagsrolle = this.ttBeregnetForGrunnlagsrolle
        it.ungUforGarantiFrafalt = this.ungUforGarantiFrafalt
        it.uft = this.uft?.copy()
        it.yst = this.yst?.copy()
        it.merknadListe = this.merknadListe.map { o -> o.copy() }
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

fun KonverteringsdataUT.copy() =
    KonverteringsdataUT().also {
        it.tpUfor = this.tpUfor
        it.tpYrke = this.tpYrke
    }

fun Paragraf_8_5_1_tillegg.copy() =
    Paragraf_8_5_1_tillegg().also {
        copyYtelseskomponent(source = this, target = it)
    }

fun Sertillegg.copy() =
    Sertillegg().also {
        it.pSats_st = this.pSats_st
        copyYtelseskomponent(source = this, target = it)
    }

fun UforeEkstra.copy() =
    UforeEkstra().also {
        it.inntektkode1Enum = this.inntektkode1Enum
        it.inntektkode2Enum = this.inntektkode2Enum
        it.tak = this.tak
        it.inntektsgrenseForFriinntektsdato = this.inntektsgrenseForFriinntektsdato
        it.fpp = this.fpp
        it.fppGaranti = this.fppGaranti
        it.fppGarantiKodeEnum = this.fppGarantiKodeEnum
        it.redusertAntFppAr = this.redusertAntFppAr
        it.uforeperiode = this.uforeperiode?.let(::BeregningUforeperiode)
        it.uforeperiodeYSK = this.uforeperiodeYSK?.let(::BeregningUforeperiode)
    }

fun Ventetillegg.copy() =
    Ventetillegg().also {
        it.venteTillegg_GP = this.venteTillegg_GP
        it.venteTillegg_TP = this.venteTillegg_TP
        it.venteTilleggProsent = this.venteTilleggProsent
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
    target.merknadListe = source.merknadListe.map { o -> o.copy() }.toMutableList()
    target.reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
    target.fradragsTransaksjon = source.fradragsTransaksjon
    target.opphort = source.opphort
    target.sakTypeEnum = source.sakTypeEnum
    //--- Extra:
    target.brukt = source.brukt
    //target.unroundedNettoPerAr = source.unroundedNettoPerAr
}

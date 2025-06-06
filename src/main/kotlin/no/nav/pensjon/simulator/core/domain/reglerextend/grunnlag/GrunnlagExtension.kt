package no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import java.util.*

fun AfpHistorikk.copy() =
    AfpHistorikk().also {
        it.afpFpp = this.afpFpp
        it.virkFom = this.virkFom?.clone() as? Date
        it.virkTom = this.virkTom?.clone() as? Date
        it.afpPensjonsgrad = this.afpPensjonsgrad
        it.afpOrdningEnum = this.afpOrdningEnum
    }

fun AfpOpptjening.copy() =
    AfpOpptjening().also {
        copyBeholdning(source = this, target = it)
    }

fun AntallArMndDag.copy() =
    AntallArMndDag().also {
        it.antallAr = this.antallAr
        it.antallMnd = this.antallMnd
        it.antallDager = this.antallDager
    }

fun ArbeidsforholdEtterUforgrunnlag.copy() =
    ArbeidsforholdEtterUforgrunnlag().also {
        it.fomDato = this.fomDato?.clone() as? Date
        it.stillingsprosent = this.stillingsprosent
        it.varigTilrettelagtArbeid = this.varigTilrettelagtArbeid
    }

fun Arbeidsforholdsgrunnlag.copy() =
    Arbeidsforholdsgrunnlag().also {
        it.fomDato = this.fomDato?.clone() as? Date
        it.tomDato = this.tomDato?.clone() as? Date
        it.stillingsprosent = this.stillingsprosent
        it.arbeidsgiver = this.arbeidsgiver
        it.orgNummer = this.orgNummer
    }

fun BarnetilleggVilkar.copy() =
    BarnetilleggVilkar().also {
        it.btVilkarTypeEnum = this.btVilkarTypeEnum
        it.vurdertTil = this.vurdertTil
    }

fun BarnetilleggVurderingsperiode.copy() =
    BarnetilleggVurderingsperiode().also {
        it.fomDato = this.fomDato?.clone() as? Date
        it.tomDato = this.tomDato?.clone() as? Date
        it.btVilkarListe = this.btVilkarListe.map { it.copy() }
    }

fun Beholdninger.copy() =
    Beholdninger().also {
        it.beholdninger = this.beholdninger.map(::copyBeholdning)
    }

// PEN: Beholdninger.findBeholdningAvType
fun Beholdninger.beholdning(type: BeholdningtypeEnum) =
    beholdninger.firstOrNull { type == it.beholdningsTypeEnum }

fun EosEkstra.copy() =
    EosEkstra().also {
        it.proRataBeregningTypeEnum = this.proRataBeregningTypeEnum
        it.redusertAntFppAr = this.redusertAntFppAr
        it.spt_eos = this.spt_eos
        it.spt_pa_f92_eos = this.spt_pa_f92_eos
        it.spt_pa_e91_eos = this.spt_pa_e91_eos
        it.vilkar3_17Aok = this.vilkar3_17Aok
    }

fun Garantipensjonsbeholdning.copy() =
    Garantipensjonsbeholdning().also {
        it.justertGarantipensjonsniva = this.justertGarantipensjonsniva?.copy()
        it.pensjonsbeholdning = this.pensjonsbeholdning
        it.delingstall67 = this.delingstall67
        it.delingstallVedNormertPensjonsalder = this.delingstallVedNormertPensjonsalder
        it.satsTypeEnum = this.satsTypeEnum
        it.sats = this.sats
        it.garPN_tt_anv = this.garPN_tt_anv
        it.garPN_justert = this.garPN_justert
        copyBeholdning(source = this, target = it)
    }

fun Garantitilleggsbeholdning.copy() =
    Garantitilleggsbeholdning().also {
        it.garantitilleggInformasjon = this.garantitilleggInformasjon?.copy()
        copyBeholdning(source = this, target = it)
    }

fun GarantiTrygdetid.copy() =
    GarantiTrygdetid().also {
        it.trygdetid_garanti = this.trygdetid_garanti
        it.fomDato = this.fomDato?.clone() as? Date
        it.tomDato = this.tomDato?.clone() as? Date
    }

fun InstOpphFasteUtgifterperiode.copy() =
    InstOpphFasteUtgifterperiode().also {
        it.instOpphFasteUtgifterperiodeId = this.instOpphFasteUtgifterperiodeId
        it.fom = this.fom?.clone() as? Date
        it.tom = this.tom?.clone() as? Date
        it.fasteUtgifter = this.fasteUtgifter
    }

fun Pensjonsbeholdning.copy() =
    Pensjonsbeholdning().also {
        it.fom = this.fom?.clone() as? Date
        it.tom = this.tom?.clone() as? Date
        copyBeholdning(source = this, target = it)
    }

fun Unntak.copy() =
    Unntak().also {
        it.unntak = this.unntak
        it.unntakTypeEnum = this.unntakTypeEnum
        it.eksportUnntakEnum = this.eksportUnntakEnum
    }

fun Uttaksgrad.copy() =
    Uttaksgrad().also {
        it.fomDato = this.fomDato?.clone() as? Date
        it.tomDato = this.tomDato?.clone() as? Date
        it.uttaksgrad = this.uttaksgrad
    }

private fun copyBeholdning(source: Beholdning): Beholdning =
    when (source) {
        is AfpOpptjening -> source.copy()
        is Garantitilleggsbeholdning -> source.copy()
        is Garantipensjonsbeholdning -> source.copy()
        is Pensjonsbeholdning -> source.copy()
        else -> throw RuntimeException("Unsupported Beholdning type: ${source.javaClass.name}")
    }

private fun copyBeholdning(source: Beholdning, target: Beholdning) {
    target.ar = source.ar
    target.totalbelop = source.totalbelop
    target.reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
    target.opptjening = source.opptjening?.let(::Opptjening)
    target.beholdningsTypeEnum = source.beholdningsTypeEnum
    target.formelKodeEnum = source.formelKodeEnum
    target.merknadListe = source.merknadListe.map { it.copy() }.toMutableList()
    target.lonnsvekstInformasjon = source.lonnsvekstInformasjon?.copy()
}

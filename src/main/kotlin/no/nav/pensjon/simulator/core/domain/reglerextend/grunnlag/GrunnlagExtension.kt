package no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
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

fun BarnetilleggVilkar.copy() =
    BarnetilleggVilkar().also {
        it.btVilkarTypeEnum = this.btVilkarTypeEnum
        it.vurdertTil = this.vurdertTil
    }

fun Beholdninger.copy() =
    Beholdninger().also {
        it.beholdninger = this.beholdninger.map(::copyBeholdning)
    }

fun Garantipensjonsbeholdning.copy() =
    Garantipensjonsbeholdning().also {
        it.justertGarantipensjonsniva = this.justertGarantipensjonsniva?.copy()
        it.pensjonsbeholdning = this.pensjonsbeholdning
        it.delingstall67 = this.delingstall67
        it.satsTypeEnum = this.satsTypeEnum
        it.sats = this.sats
        it.garPN_tt_anv = this.garPN_tt_anv
        it.garPN_justert = this.garPN_justert
        // beholdningsTypeEnum is set in constructor
        copyBeholdning(source = this, target = it)
    }

fun Garantitilleggsbeholdning.copy() =
    Garantitilleggsbeholdning().also {
        it.garantitilleggInformasjon = this.garantitilleggInformasjon?.copy()
        copyBeholdning(source = this, target = it)
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
    target.lonnsvekstInformasjon = source.lonnsvekstInformasjon?.let(::LonnsvekstInformasjon)
}

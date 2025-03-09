package no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpHistorikk
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AfpOpptjening
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.AntallArMndDag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.BarnetilleggVilkar
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantipensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantitilleggsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Unntak
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BeholdningsTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.EksportUnntakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.FormelKodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InngangUnntakCti
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import java.util.*

fun AfpHistorikk.copy() =
    AfpHistorikk().also {
        it.afpFpp = this.afpFpp
        it.virkFom = this.virkFom?.clone() as? Date
        it.virkTom = this.virkTom?.clone() as? Date
        it.afpPensjonsgrad = this.afpPensjonsgrad
        it.afpOrdning = this.afpOrdning?.let(::AfpOrdningTypeCti)
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
        it.btVilkarType = this.btVilkarType
        it.btVilkarTypeEnum = this.btVilkarTypeEnum
        it.vurdertTil = this.vurdertTil
    }

fun Garantipensjonsbeholdning.copy() =
    Garantipensjonsbeholdning().also {
        //TODO delingstall67 etc
        copyBeholdning(source = this, target = it)
    }

fun Garantitilleggsbeholdning.copy() =
    Garantitilleggsbeholdning().also {
        //TODO garantitilleggInformasjon
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
        it.unntakType = this.unntakType?.let(::InngangUnntakCti)
        it.unntakTypeEnum = this.unntakTypeEnum
        it.eksportUnntak = this.eksportUnntak?.let(::EksportUnntakCti)
        it.eksportUnntakEnum = this.eksportUnntakEnum
    }

fun copyBeholdning(source: Beholdning, target: Beholdning) {
    target.ar = source.ar
    target.totalbelop = source.totalbelop
    target.reguleringsInformasjon = source.reguleringsInformasjon?.let(::ReguleringsInformasjon)
    target.opptjening = source.opptjening?.let(::Opptjening)
    target.beholdningsType = BeholdningsTypeCti(source.beholdningsType)
    target.beholdningsTypeEnum = source.beholdningsTypeEnum
    target.formelkode = source.formelkode?.let(::FormelKodeCti)
    target.formelKodeEnum = source.formelKodeEnum
    target.merknadListe = source.merknadListe.map { it.copy() }.toMutableList()
    target.lonnsvekstInformasjon = source.lonnsvekstInformasjon?.let(::LonnsvekstInformasjon)
}

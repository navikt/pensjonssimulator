package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.enum.BegrunnelseTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VilkarVurderingEnum
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findEarliestDateByDay
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import java.time.LocalDate
import java.util.*

// 2025-03-13
class VilkarsVedtak {
    /**
     * Kode som angir hvilket resultat pensjon-regler setter på vilkårsvedtaket. Se
     * K_VILKAR_RESUL_T for gyldige typer. Kun satt hvis pensjon-regler utfører
     * vilkårsprøving.
     */
    var anbefaltResultatEnum: VedtakResultatEnum? = null

    /**
     * Kode som angir det resultatet til vilkårsvedtaket. Settes av konsumenten
     * av tjenesten, eventuelt basert på anbefaltResultat. Se K_VILKAR_RESUL_T
     * for gyldige typer.
     */
    var vilkarsvedtakResultatEnum: VedtakResultatEnum? = null

    /**
     * Kode som angir hvilken type kravlinje vilkårsvedtaket relaterer seg til.
     * K_KRAVLINJE_T for gyldige typer.
     */
    var kravlinjeTypeEnum: KravlinjeTypeEnum? = null

    /**
     * Beskriver hvilken vurdering saksbehandler har lagt til grunn for
     * resultatvurderingen. Se K_VILKAR_VURD_T.
     */
    var anvendtVurderingEnum: VilkarVurderingEnum? = null

    /**
     * Dato vilkårsvedtaket har virkning fra.
     */
    var virkFom: Date? = null

    /**
     * Dato vilkårsvedtaket har virkning til. Denne skal ikke være satt, og det
     * betyr at vedtaket har uendelig gyldighet. Skulle den likevel være satt
     * vil pensjon-regler sjekke at virken på ytelsen det beregnes for er nnnnenfor
     * virkFom-virkTom. Er den utenfor blir vedtaket behandlet som ikke gyldig.
     */
    var virkTom: Date? = null

    /**
     * Dato Første innvilgede vilkårsvedtak personen har fått fra trygden.
     * Eks: har personen tidligere hatt UP og før nå AP vil det være datoen for Første UP-vedtaket.
     */
    var forsteVirk: Date? = null

    /**
     * Dato dette vilkårsvedtakets kravlinje fårst ble innvilget.
     * Eks: personen fikk innvilget gjenlevenderett fom dette virkningstidspunkt.
     */
    var kravlinjeForsteVirk: Date? = null

    /**
     * Kravlinje som er vilkårsprøvd.
     */
    var kravlinje: Kravlinje? = null

    /**
     * Id for personen
     */
    var penPerson: PenPerson? = null

    var vilkarsprovresultat: AbstraktVilkarsprovResultat? = null
    var begrunnelseEnum: BegrunnelseTypeEnum? = null
    var avslattKapittel19 = false
    var avslattGarantipensjon = false
    var vurderSkattefritakET = false

    var unntakHalvMinstepensjon = false
    var epsRettEgenPensjon = false

    /**
     * List av Beregningsvilkarperioder
     */
    var beregningsvilkarPeriodeListe: List<BeregningsvilkarPeriode> = mutableListOf()

    /**
     * Liste av merknader - forklaringer,unntak og avvisningsgrunner fra
     * regelmotoren.
     */
    var merknadListe: List<Merknad> = mutableListOf()

    // SIMDOM-ADD
    // kjerne.Vilkarsvedtak.setKravlinjeForsteVirk
    fun fastsettForstevirkKravlinje(
        vedtakListe: MutableList<VilkarsVedtak>,
        virkningListe: List<FoersteVirkningDato>
    ) {
        kravlinjeForsteVirk = findEarliestDateByDay(
            foersteVirkningDatoForDetteVedtak(virkningListe)?.toNorwegianDateAtNoon(),
            foersteVirkningFomBlantInnvilgedeVedtakAvRelevantType(vedtakListe)
        ) // NB: legacy constructs new Date
    }

    // kjerne.Vilkarsvedtak.findFirstInnvilgetDateForKravlinjeOnVedtak
    private fun foersteVirkningFomBlantInnvilgedeVedtakAvRelevantType(vedtakListe: List<VilkarsVedtak>): Date? =
         vedtakListe
            .filter { it.kravlinjeTypeEnum == kravlinjeTypeEnum && VedtakResultatEnum.INNV == it.vilkarsvedtakResultatEnum }
            .mapNotNull { it.virkFom }
            .minByOrNull { it.time } // legacy uses 'nullsLast' i.e. nulls > non-nulls. Hence, mapNotNull + minByOrNull ought to be equivalent

    // kjerne.Vilkarsvedtak.findFirstInnvilgetDateForKravlinjeOnSakForKravlinjePerson
    // NB: kjerne.Vilkarsvedtak gets kravhodeSakForsteVirkningsdatoer from vedtak field (not present in simdom.VilkarsVedtak)
    private fun foersteVirkningDatoForDetteVedtak(virkningListe: List<FoersteVirkningDato>): LocalDate? =
        virkningListe
            .filter(::virkerForDetteVedtak)
            .map { it.virkningDato }
            .firstOrNull()

    // kjerne.Vilkarsvedtak.hasSamePersonAndKravlinjetypeAsVilkarsvedtak
    private fun virkerForDetteVedtak(virkning: FoersteVirkningDato): Boolean =
        (erTillegg().not() || erTilleggOgRiktigPerson(virkning)) //TODO simplify
                && virkning.kravlinjeType == kravlinjeTypeEnum

    @JsonIgnore
    var gjelderPerson: PenPerson? = null

    // kjerne.Vilkarsvedtak.isEtOrBtAndCorrectPerson
    private fun erTilleggOgRiktigPerson(virkning: FoersteVirkningDato): Boolean =
        erTillegg() && gjelderPerson == virkning.annenPerson

    // kjerne.Vilkarsvedtak.isEktefelleOrBarnetillegg
    private fun erTillegg(): Boolean =
        listOf(KravlinjeTypeEnum.BT, KravlinjeTypeEnum.ET).contains(kravlinjeTypeEnum)

    @JsonIgnore
    var rawForsteVirk: Date? = null

    fun finishInit() {
        rawForsteVirk = forsteVirk
        forsteVirk = rawForsteVirk?.noon()
    }

    // end SIMDOM-ADD
}

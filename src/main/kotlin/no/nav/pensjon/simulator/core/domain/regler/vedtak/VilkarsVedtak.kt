package no.nav.pensjon.simulator.core.domain.regler.vedtak

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import no.nav.pensjon.simulator.core.krav.KravlinjeType
import no.nav.pensjon.simulator.core.domain.VedtakResultat
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsvilkarPeriode
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.BegrunnelseTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.KravlinjeTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarVurderingCti
import no.nav.pensjon.simulator.core.domain.regler.kode.VilkarsvedtakResultatCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.io.Serializable
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneId
import java.util.*

class VilkarsVedtak(
    /**
     * Kode som angir hvilket resultat PREG setter på vilkårsvedtaket. Se
     * K_VILKAR_RESUL_T for gyldige typer. Kun satt hvis PREG utfører
     * vilkårsprøving.
     */
    var anbefaltResultat: VilkarsvedtakResultatCti? = null,

    /**
     * Kode som angir det resultatet til vilkårsvedtaket. Settes av konsumenten
     * av tjenesten, eventuelt basert på anbefaltResultat. Se K_VILKAR_RESUL_T
     * for gyldige typer.
     */
    var vilkarsvedtakResultat: VilkarsvedtakResultatCti? = null,

    /**
     * Kode som angir hvilken type kravlinje vilkårsvedtaket relaterer seg til.
     * K_KRAVLINJE_T for gyldige typer.
     */
    var kravlinjeType: KravlinjeTypeCti? = null,

    /**
     * Beskriver hvilken vurdering saksbehandler har lagt til grunn for
     * resultatvurderingen. Se K_VILKAR_VURD_T.
     */
    private var anvendtVurdering: VilkarVurderingCti? = null,

    /**
     * Dato vilkårsvedtaket har virkning fra.
     */
    var virkFom: Date? = null,

    /**
     * Dato vilkårsvedtaket har virkning til. Denne skal ikke være satt, og det
     * betyr at vedtaket har uendelig gyldighet. Skulle den likevel være satt
     * vil PREG sjekke at virken på ytelsen det beregnes for er nnnnenfor
     * virkFom-virkTom. Er den utenfor blir vedtaket behandlet som ikke gyldig.
     */
    var virkTom: Date? = null,

    /**
     * Dato første innvilgede vilkårsvedtak personen har fått fra trygden.
     * Eks: har personen tidligere hatt UP og får nå AP vil det være datoen for første UP-vedtaket.
     */
    var forsteVirk: Date? = null,

    /**
     * Dato dette vilkårsvedtakets kravlinje først ble innvilget.
     * Eks: personen fikk innvilget gjenlevenderett fom dette virkningstidspunkt.
     */
    var kravlinjeForsteVirk: Date? = null,

    /**
     * Kravlinje som er vilkårsprøvd.
     */
    var kravlinje: Kravlinje? = null,

    var penPerson: PenPerson? = null,
    var vilkarsprovresultat: AbstraktVilkarsprovResultat? = null,
    var begrunnelse: BegrunnelseTypeCti? = null,
    var avslattKapittel19: Boolean = false,
    var avslattGarantipensjon: Boolean = false,
    @JsonIgnore var persongrunnlag: Persongrunnlag? = null,
    var vurderSkattefritakET: Boolean = false,
    var unntakHalvMinstepensjon: Boolean = false,
    var epsAvkallEgenPensjon: Boolean = false, // SIMDOM-ADD

    /**
     * Angir om EPS har teoretisk rett til egen alderspensjon.
     */
    var epsRettEgenPensjon: Boolean = false,

    var beregningsvilkarPeriodeListe: MutableList<BeregningsvilkarPeriode> = mutableListOf(),
    var merknadListe: MutableList<Merknad> = mutableListOf()

) : Comparable<VilkarsVedtak>, Serializable {

    // SIMDOM-ADD
    // kjerne.Vilkarsvedtak.setKravlinjeForsteVirk
    fun fastsettForstevirkKravlinje(
        vilkarsvedtak: MutableList<VilkarsVedtak>,
        kravhodeSakForsteVirkningsdatoer: List<FoersteVirkningDato>
    ) {
        val forsteVirkForKravlinjeOnVedtak: Date? = findFirstInnvilgetDateForKravlinjeOnVedtak(vilkarsvedtak)

        val forsteVirkForKravlinjeOnSak: LocalDate? =
            findFirstInnvilgetDateForKravlinjeOnSakForKravlinjePerson(kravhodeSakForsteVirkningsdatoer)

        kravlinjeForsteVirk = DateUtil.findEarliestDateByDay(
            fromLocalDate(forsteVirkForKravlinjeOnSak),
            forsteVirkForKravlinjeOnVedtak
        ) // NB: legacy constructs new Date
    }

    // kjerne.Vilkarsvedtak.findFirstInnvilgetDateForKravlinjeOnVedtak
    private fun findFirstInnvilgetDateForKravlinjeOnVedtak(vedtakList: List<VilkarsVedtak>): Date? {
        return vedtakList
            .filter { it.kravlinjeType == kravlinjeType && VedtakResultat.INNV.name == it.vilkarsvedtakResultat?.kode }
            .mapNotNull { it.virkFom }
            .minByOrNull { it.time } // legacy uses 'nullsLast' i.e. nulls > non-nulls. Hence, mapNotNull + minByOrNull ought to be equivalent
    }

    /* Code from legacy (assumed to be irrelevant):
    // kjerne.Vilkarsvedtak.setKravlinjeForsteVirk
    private fun setKravlinjeForsteVirk(date: Date?) {
        this.kravlinjeForsteVirk = date?.let { Date(it.time) }
    }

    var vedtak: Vedtak? = null // Not in simdom

    private fun findFirstInnvilgetDateForKravlinjeOnSakForKravlinjePerson(kravhodeSakForsteVirkningsdatoList: List<Date>): Date? {
        return Optional.ofNullable<Vedtak>(vedtak).map { v: Vedtak ->
            v.kravhode.sak.forsteVirkningsdatoListe
                .filter { hasSamePersonAndKravlinjetypeAsVilkarsvedtak(it) }
                .map { it.virkningsdato }.firstOrNull()
        }
            .orElse(null)
    }
    */

    // kjerne.Vilkarsvedtak.findFirstInnvilgetDateForKravlinjeOnSakForKravlinjePerson
    // NB: kjerne.Vilkarsvedtak gets kravhodeSakForsteVirkningsdatoer from vedtak field (not present in simdom.VilkarsVedtak)
    private fun findFirstInnvilgetDateForKravlinjeOnSakForKravlinjePerson(kravhodeSakForsteVirkningsdatoer: List<FoersteVirkningDato>): LocalDate? {
        return kravhodeSakForsteVirkningsdatoer
            .filter(::hasSamePersonAndKravlinjetypeAsVilkarsvedtak)
            .map { it.virkningDato }
            .firstOrNull()
    }

    // kjerne.Vilkarsvedtak.hasSamePersonAndKravlinjetypeAsVilkarsvedtak
    private fun hasSamePersonAndKravlinjetypeAsVilkarsvedtak(forsteVirkningsdato: FoersteVirkningDato): Boolean =
        (!isEktefelleOrBarnetillegg() || isEktefelleOrBarnetilleggAndCorrectPerson(forsteVirkningsdato))
                && forsteVirkningsdato.kravlinjeType?.name == kravlinjeType?.kode

    @JsonIgnore
    var gjelderPerson: PenPerson? = null

    // kjerne.Vilkarsvedtak.isEtOrBtAndCorrectPerson
    private fun isEktefelleOrBarnetilleggAndCorrectPerson(forsteVirkningsdato: FoersteVirkningDato): Boolean {
        return isEktefelleOrBarnetillegg() && gjelderPerson == forsteVirkningsdato.annenPerson
    }

    // kjerne.Vilkarsvedtak.isEktefelleOrBarnetillegg
    private fun isEktefelleOrBarnetillegg(): Boolean {
        return listOf(KravlinjeType.BT.name, KravlinjeType.ET.name).contains(kravlinjeType?.kode)
    }

    @JsonIgnore
    var rawForsteVirk: Date? = null

    fun finishInit() {
        rawForsteVirk = forsteVirk
        forsteVirk = rawForsteVirk?.noon()
    }

    // end SIMDOM-ADD

    constructor(v: VilkarsVedtak) : this() {
        if (v.anbefaltResultat != null) {
            this.anbefaltResultat = VilkarsvedtakResultatCti(v.anbefaltResultat)
        }
        if (v.vilkarsvedtakResultat != null) {
            this.vilkarsvedtakResultat = VilkarsvedtakResultatCti(v.vilkarsvedtakResultat)
        }
        if (v.kravlinjeType != null) {
            this.kravlinjeType = KravlinjeTypeCti(v.kravlinjeType!!)
        }
        if (v.anvendtVurdering != null) {
            this.anvendtVurdering = VilkarVurderingCti(v.anvendtVurdering)
        }
        if (v.virkFom != null) {
            this.virkFom = v.virkFom!!.clone() as Date
        }
        if (v.virkTom != null) {
            this.virkTom = v.virkTom!!.clone() as Date
        }
        if (v.forsteVirk != null) {
            this.forsteVirk = v.forsteVirk!!.clone() as Date
        }
        if (v.kravlinjeForsteVirk != null) {
            this.kravlinjeForsteVirk = v.kravlinjeForsteVirk!!.clone() as Date
        }
        if (v.kravlinje != null) {
            this.kravlinje = Kravlinje(v.kravlinje!!)
        }
        if (v.penPerson != null) {
            this.penPerson = PenPerson(v.penPerson!!)
        }
        if (v.begrunnelse != null) {
            this.begrunnelse = v.begrunnelse
        }
        if (v.vilkarsprovresultat != null) {
            if (v.vilkarsprovresultat is VilkarsprovAlderspensjonResultat) {
                this.vilkarsprovresultat =
                    VilkarsprovAlderspensjonResultat(v.vilkarsprovresultat as VilkarsprovAlderspensjonResultat)
            } else if (v.vilkarsprovresultat is VilkarsprovAlderspensjon67Resultat) {
                this.vilkarsprovresultat =
                    VilkarsprovAlderspensjon67Resultat(v.vilkarsprovresultat as VilkarsprovAlderspensjon67Resultat?)
            }
        }
        this.avslattKapittel19 = v.avslattKapittel19
        this.avslattGarantipensjon = v.avslattGarantipensjon
        this.vurderSkattefritakET = v.vurderSkattefritakET
        this.unntakHalvMinstepensjon = v.unntakHalvMinstepensjon

        val copyMerknadList = v.merknadListe.toMutableList()
        for (merknad in copyMerknadList) {
            this.merknadListe.add(Merknad(merknad))
        }
        for (bvp in v.beregningsvilkarPeriodeListe) {
            this.beregningsvilkarPeriodeListe.add(BeregningsvilkarPeriode(bvp))
        }
        this.epsRettEgenPensjon = v.epsRettEgenPensjon

        v.rawForsteVirk?.let { rawForsteVirk = it }
        epsAvkallEgenPensjon = v.epsAvkallEgenPensjon
    }

    constructor(
        anbefaltResultat: VilkarsvedtakResultatCti?,
        vilkarsvedtakResultat: VilkarsvedtakResultatCti?,
        kravlinjeType: KravlinjeTypeCti?,
        anvendtVurdering: VilkarVurderingCti?,
        virkFom: Date?,
        virkTom: Date?,
        forsteVirk: Date?,
        kravlinje: Kravlinje?,
        penPerson: PenPerson?,
        vilkarsprovresultat: VilkarsprovAlderspensjonResultat?,
        begrunnelse: BegrunnelseTypeCti?,
        avslattKapittel19: Boolean,
        merknadListe: MutableList<Merknad> = mutableListOf()
    ) : this() {
        this.anbefaltResultat = anbefaltResultat
        this.vilkarsvedtakResultat = vilkarsvedtakResultat
        this.kravlinjeType = kravlinjeType
        this.anvendtVurdering = anvendtVurdering
        this.virkFom = virkFom
        this.virkTom = virkTom
        this.forsteVirk = forsteVirk
        this.kravlinje = kravlinje
        this.penPerson = penPerson
        this.vilkarsprovresultat = vilkarsprovresultat
        this.begrunnelse = begrunnelse
        this.avslattKapittel19 = avslattKapittel19
        for (merknad in merknadListe) {
            this.merknadListe.add(merknad)
        }
    }

    override fun compareTo(other: VilkarsVedtak): Int {
        return DateCompareUtil.compareTo(virkFom, other.virkFom)
    }

    /**
     * @return siste periode fra beregningsvilkarsPeriodeListe, null hvis ingen elementer i listen
     */
    val sisteBeregningsvilkarPeriode: BeregningsvilkarPeriode?
        get() = if (beregningsvilkarPeriodeListe.isNotEmpty()) getSortedBeregningssvilkarPeriodeListe(true)[0] else null

    /**
     * Read only property for abstraktBeregningsvilkarListe as array.
     *
     * @return f�rste periode fra beregningsvilkarsPeriodeListe, null hvis ingen elementer i listen
     */
    val førsteBeregningsvilkarPeriode: BeregningsvilkarPeriode?
        get() = if (beregningsvilkarPeriodeListe.isNotEmpty()) getSortedBeregningssvilkarPeriodeListe(false)[0] else null

    fun getSortedBeregningssvilkarPeriodeListe(reverse: Boolean): MutableList<BeregningsvilkarPeriode> {
        return if (beregningsvilkarPeriodeListe.isNotEmpty()) {
            val sortedBvp = ArrayList(beregningsvilkarPeriodeListe)
            if (reverse) {
                Collections.sort(sortedBvp, Collections.reverseOrder())
            } else {
                sortedBvp.sort()
            }
            sortedBvp
        } else {
            mutableListOf()
        }
    }

    val sortedBeregningssvilkarPeriodeListe: MutableList<BeregningsvilkarPeriode>
        get() = getSortedBeregningssvilkarPeriodeListe(false)

    /**
     * @return the beregningsvilkarsPeriode som gjaldt p� oppgitt dato, tar utgangspunkt at det ikke kan finnes overlapp i perioder.
     */
    fun getGjeldendeBeregningsvilkarPeriodePaaDato(dato: Date?): BeregningsvilkarPeriode? {
        return beregningsvilkarPeriodeListe.finnGjeldendeBeregningsvilkaarPeriodePaaDato(dato)
    }

    /**
     * @param dato the cutoff date
     * @return the beregningsvilkarPeriodeListe that contains beregningsvilkarPeriode that are valid before or on the cutoff date, as a sorted array in ascending order.
     * The method is used by the ruleservices to determine which beregningsvilkarPeriode are relevant (past and present period, relative to virk, is relevant).
     */
    fun getSortedBeregningssvilkarPeriodeListeTomDato(dato: Date): MutableList<BeregningsvilkarPeriode> {
        val resultList = mutableListOf<BeregningsvilkarPeriode>()
        for (periode in beregningsvilkarPeriodeListe) {
            val periodeFom = periode.fomDato?.toInstant()?.atZone(ZoneId.systemDefault())?.toLocalDate()
            val dateToCompare = dato.toInstant().atZone(ZoneId.systemDefault()).toLocalDate()
            if (periodeFom != null) {
                if (periodeFom.isBefore(dateToCompare) || periodeFom == dateToCompare) {
                    resultList.add(periode)
                }
            }
        }
        resultList.sort()
        return resultList
    }

    private fun List<BeregningsvilkarPeriode>?.finnGjeldendeBeregningsvilkaarPeriodePaaDato(dato: Date?): BeregningsvilkarPeriode? {
        if (this != null) {
            for (bvp in this) {
                if (isDateInPeriod(dato.toLocalDate(), bvp.fomDato.toLocalDate(), bvp.tomDato.toLocalDate())) {
                    return bvp
                }
            }
        }
        return null
    }

    private fun Date?.toLocalDate(): LocalDate? {
        return if (this != null) Instant.ofEpochMilli(this.time)
            .atZone(ZoneId.systemDefault())
            .toLocalDate() else null
    }

    private fun isDateInPeriod(compDate: LocalDate?, fomDate: LocalDate?, tomDate: LocalDate?): Boolean {
        if (null == fomDate || null == compDate) {
            return false
        }
        var tomOK = false
        if (null != tomDate) {
            if (compDate.isBefore(tomDate) || compDate == tomDate) {
                tomOK = true
            }
        } else {
            tomOK = true
        }
        return (fomDate.isBefore(compDate) || compDate == fomDate) && tomOK
    }

    /**
     * @param virk
     * @return List of BeregningsvilkarPeriode i �ret for virk, sortert i kronologisk rekkef�lge.
     */
    fun findBeregningsvilkarperioderForAr(virk: LocalDate): MutableList<BeregningsvilkarPeriode> {
        val resultList = mutableListOf<BeregningsvilkarPeriode>()
        val fom = LocalDate.of(virk.year, 1, 1)
        val tom = LocalDate.of(virk.year, 12, 31)
        for (periode in beregningsvilkarPeriodeListe) {
            val periodeFom = LocalDate.ofInstant(periode.fomDato?.toInstant(), ZoneId.systemDefault())
            val periodeTom = if (periode.tomDato != null) LocalDate.ofInstant(
                periode.tomDato!!.toInstant(),
                ZoneId.systemDefault()
            ) else LocalDate.of(9999, 12, 31)
            if (periodeFom.isBefore(tom) && (periodeTom == null || periodeTom.isAfter(fom))) {
                resultList.add(periode)
            }
        }
        resultList.sort()
        return resultList
    }
}

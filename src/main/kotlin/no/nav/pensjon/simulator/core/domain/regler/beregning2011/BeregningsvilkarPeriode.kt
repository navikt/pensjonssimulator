package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.util.DateRange
import no.nav.pensjon.simulator.core.domain.regler.vedtak.*
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.io.Serializable
import java.time.LocalDate
import java.util.*

/**
 * @author Magnus Bakken (Accenture), PK-10597, PKPYTON-1063, PK-9695, PKPYTON-923
 * @author Swiddy de Louw (Capgemini), PK-8704,PKPYTON-563
 * Husk at når du legger til nye "smarte metoder" som f.eks set/getInntektEtterUforhet(), at dette må gjøres også i VilkarsVedtak som da
 * agerer på seneste fomDato i beregningsvilkarperiodeListe.
 * @author Steinar Hjellvik (Decisive) - PK-11391, PKPYTON-1447 Rettet feil i copy constructor. Den var ikke nullpointer safe ved kall til dypKopi metode.
 */

class BeregningsvilkarPeriode : Comparable<BeregningsvilkarPeriode>, Serializable, DateRange {
    /**
     * Fom dato for perioden de angitte beregningsvilkår og vilkår gjelder for
     */
    var fomDato: Date? = null

    /**
     * Tom dato for perioden de angitte beregningsvilkår og vilkår gjelder for
     */
    var tomDato: Date? = null

    /**
     * Liste av beregningsvilkår til bruk ved beregning av uføretrygd.
     */
    var beregningsvilkarListe: MutableList<AbstraktBeregningsvilkar> = mutableListOf()

    /**
     * Liste av vilkår til bruk ved beregning av uføretrygd.
     */
    var vilkarListe: MutableList<AbstraktVilkar> = mutableListOf()

    /**
     * PREG variabel for å markere perioden at beregningsgrunnlagOrdiner er konvertert.
     */
    @JsonIgnore
    var konvertertOrdiner: Boolean = false

    /**
     * PREG variabel for å markere perioden at beregningsgrunnlagYrkesskade er konvertert.
     */
    @JsonIgnore
    var konvertertYrkesskade: Boolean = false

    /**
     * PREG variabel som angir hvorvidt perioden er av betydning for uføretrygdopptjening. Hvis satt er ufgFom/ufgTom relevante.
     */
    @JsonIgnore
    var uforetrygdOpptjening: Boolean = false

    /**
     * PREG variabel som angir hvilket år denne periodens uføregrad gjelder fra mht. opptjening fra uføretrygd.
     */
    @JsonIgnore
    var ufgFom: Date? = null

    /**
     * PREG variabel som angir hvilket år denne periodens uføregrad gjelder til mht. opptjening fra uføretrygd.
     */
    @JsonIgnore
    var ufgTom: Date? = null

    /**
     * PREG variabel som angir om perioden representerer et opphør eller en gjenoppliving av uføreytelsen.
     */
    @JsonIgnore
    var opphorEllerGjenopplivingType: Int = 0

    /**
     * @return trygdetidberegningsvilkår på beregningsvilkårlisten, null hvis det ikke finnes.
     */
    /**
     * Legg til nytt trygdetidberegningsvilkår i listen av beregningsvilkår, erstatter eksisterende beregningsvilkartrygdetidvilkår om det finnes.
     *
     */
    var trygdetidBeregningsvilkar: TrygdetidBeregningsvilkar?
        get() = hentBeregningsvilkar(TrygdetidBeregningsvilkar::class.java)
        set(trygdetidBeregningsvilkar) = settBeregningsvilkar(
            TrygdetidBeregningsvilkar::class.java,
            trygdetidBeregningsvilkar
        )

    var uforegrad: Uforegrad?
        get() = hentBeregningsvilkar(Uforegrad::class.java)
        set(uforegrad) = settBeregningsvilkar(Uforegrad::class.java, uforegrad)

    var uforetidspunkt: Uforetidspunkt?
        get() = hentBeregningsvilkar(Uforetidspunkt::class.java)
        set(uforetidspunkt) = settBeregningsvilkar(Uforetidspunkt::class.java, uforetidspunkt)

    var yrkesskadegrad: Yrkesskadegrad?
        get() = hentBeregningsvilkar(Yrkesskadegrad::class.java)
        set(yrkesskadegrad) = settBeregningsvilkar(Yrkesskadegrad::class.java, yrkesskadegrad)

    var skadetidspunkt: Skadetidspunkt?
        get() = hentBeregningsvilkar(Skadetidspunkt::class.java)
        set(skadetidspunkt) = settBeregningsvilkar(Skadetidspunkt::class.java, skadetidspunkt)

    var inntektVedSkadetidspunktet: InntektVedSkadetidspunktet?
        get() = hentBeregningsvilkar(InntektVedSkadetidspunktet::class.java)
        set(inntektVedSkadetidspunktet) = settBeregningsvilkar(
            InntektVedSkadetidspunktet::class.java,
            inntektVedSkadetidspunktet
        )

    var inntektForUforhet: InntektForUforhet?
        get() = hentBeregningsvilkar(InntektForUforhet::class.java)
        set(inntektForUforhet) = settBeregningsvilkar(InntektForUforhet::class.java, inntektForUforhet)

    var inntektEtterUforhet: InntektEtterUforhet?
        get() = hentBeregningsvilkar(InntektEtterUforhet::class.java)
        set(inntektEtterUforhet) = settBeregningsvilkar(InntektEtterUforhet::class.java, inntektEtterUforhet)

    var ungUfor: UngUfor?
        get() = hentVilkar(UngUfor::class.java)
        set(ungUfor) = settVilkaar(UngUfor::class.java, ungUfor)

    var yrkesskade: Yrkesskade?
        get() = hentVilkar(Yrkesskade::class.java)
        set(yrkesskade) = settVilkaar(Yrkesskade::class.java, yrkesskade)

    var fortsattMedlemskap: FortsattMedlemskap?
        get() = hentVilkar(FortsattMedlemskap::class.java)
        set(fortsattMedlemskap) = settVilkaar(FortsattMedlemskap::class.java, fortsattMedlemskap)

    var forutgaendeMedlemskap: ForutgaendeMedlemskap?
        get() = hentVilkar(ForutgaendeMedlemskap::class.java)
        set(forutgaendeMedlemskap) = settVilkaar(ForutgaendeMedlemskap::class.java, forutgaendeMedlemskap)

    var medlemskapForUTEtterTrygdeavtaler: MedlemskapForUTEtterTrygdeavtaler?
        get() = hentVilkar(MedlemskapForUTEtterTrygdeavtaler::class.java)
        set(medlemskapForUTEtterTrygdeavtaler) = settVilkaar(
            MedlemskapForUTEtterTrygdeavtaler::class.java,
            medlemskapForUTEtterTrygdeavtaler
        )

    var rettTilEksportEtterTrygdeavtaler: RettTilEksportEtterTrygdeavtaler?
        get() = hentVilkar(RettTilEksportEtterTrygdeavtaler::class.java)
        set(rettTilEksportEtterTrygdeavtaler) = settVilkaar(
            RettTilEksportEtterTrygdeavtaler::class.java,
            rettTilEksportEtterTrygdeavtaler
        )

    var rettTilGjenlevendetillegg: RettTilGjenlevendetillegg?
        get() = hentVilkar(RettTilGjenlevendetillegg::class.java)
        set(rettTilGjenlevendetillegg) = settVilkaar(RettTilGjenlevendetillegg::class.java, rettTilGjenlevendetillegg)

    var alderspensjon2011VedDod: Alderspensjon2011VedDod?
        get() = hentBeregningsvilkar(Alderspensjon2011VedDod::class.java)
        set(alderspensjon2011VedDod) = settBeregningsvilkar(
            Alderspensjon2011VedDod::class.java,
            alderspensjon2011VedDod
        )

    var tidligereGjenlevendePensjon: TidligereGjenlevendePensjon?
        get() = hentBeregningsvilkar(TidligereGjenlevendePensjon::class.java)
        set(tidligereGjenlevendePensjon) = settBeregningsvilkar(
            TidligereGjenlevendePensjon::class.java,
            tidligereGjenlevendePensjon
        )

    init {
        beregningsvilkarListe = mutableListOf()
        vilkarListe = mutableListOf()
    }

    @JvmOverloads
    constructor(
        fomDato: Date? = null,
        tomDato: Date? = null,
        beregningsvilkarListe: MutableList<AbstraktBeregningsvilkar> = mutableListOf(),
        vilkarListe: MutableList<AbstraktVilkar> = mutableListOf()
    ) {
        this.fomDato = fomDato
        this.tomDato = tomDato
        this.beregningsvilkarListe = beregningsvilkarListe
        this.vilkarListe = vilkarListe
    }

    private fun <T : AbstraktBeregningsvilkar> hentBeregningsvilkar(classOfBeregningsvilkar: Class<T>): T? {
        val bvFiltered = beregningsvilkarListe.filterIsInstance(classOfBeregningsvilkar)
        return if (bvFiltered.isNotEmpty()) {
            bvFiltered[0]
        } else {
            null
        }
    }

    private fun <T : AbstraktBeregningsvilkar> settBeregningsvilkar(
        classOfBeregningsvilkar: Class<T>,
        beregningsvilkar: T?
    ) {
        val old = hentBeregningsvilkar(classOfBeregningsvilkar)
        if (old != null) {
            beregningsvilkarListe.remove(old)
        }
        if (beregningsvilkar != null) {
            beregningsvilkarListe.add(beregningsvilkar)
        }
    }

    private fun <T : AbstraktVilkar> hentVilkar(classOfVilkar: Class<T>): T? {
        val vFiltered = vilkarListe.filterIsInstance(classOfVilkar)
        return if (vFiltered.isNotEmpty()) {
            vFiltered[0]
        } else {
            null
        }
    }

    private fun <T : AbstraktVilkar> settVilkaar(vilkaarClass: Class<T>, vilkaar: T?) {
        hentVilkar(vilkaarClass)?.let(vilkarListe::remove)
        vilkaar?.let(vilkarListe::add)
    }

    override fun compareTo(other: BeregningsvilkarPeriode): Int =
        DateCompareUtil.compareTo(fomDato, other.fomDato)

    override fun range(): ClosedRange<LocalDate> {
        val fom = fomDato?.toNorwegianLocalDate() ?: LocalDate.MIN // SIMDOM-MOD LocalDateHelper.MIN
        val tom = tomDato?.toNorwegianLocalDate() ?: LocalDate.MAX //LocalDateHelper.MAX
        return fom..tom
    }
}

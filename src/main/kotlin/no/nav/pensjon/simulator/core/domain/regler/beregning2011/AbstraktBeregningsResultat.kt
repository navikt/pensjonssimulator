package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import com.fasterxml.jackson.annotation.JsonIgnore
import com.fasterxml.jackson.annotation.JsonSubTypes
import com.fasterxml.jackson.annotation.JsonTypeInfo
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.kode.BeregningArsakCti
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import java.io.Serializable
import java.util.*

@JsonSubTypes(
    JsonSubTypes.Type(value = BeregningsresultatUforetrygd::class),
    JsonSubTypes.Type(value = BeregningsResultatAlderspensjon2016::class),
    JsonSubTypes.Type(value = BeregningsResultatAlderspensjon2025::class),
    JsonSubTypes.Type(value = BeregningsResultatAfpPrivat::class),
    JsonSubTypes.Type(value = BeregningsResultatAlderspensjon2011::class)
)
@JsonTypeInfo(use = JsonTypeInfo.Id.NAME, include = JsonTypeInfo.As.PROPERTY, property = "type")
abstract class AbstraktBeregningsResultat : Serializable {
    // SIMDOM-ADD:
    @JsonIgnore var kravId: Long? = null
    @JsonIgnore var virkTom: Date? = null
    @JsonIgnore var epsMottarPensjon: Boolean = false // false in old PREG class
    @JsonIgnore private var beregningInformasjon: BeregningsInformasjon? = null

    open fun hentBeregningsinformasjon(): BeregningsInformasjon? = beregningInformasjon

    fun setBeregningsinformasjon(value: BeregningsInformasjon) {
        beregningInformasjon = value
    }
    // end SIMDOM-ADD

    var virkFom: Date? = null
    var pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null

    /**
     * Snittet av uttaksgradene i perioden fra (virk bakover i tid til 1 mai) og til (virk fremover i tid til 1 mai).
     */
    var uttaksgrad: Int = 0
    var brukersSivilstand: SivilstandTypeCti? = null
    var benyttetSivilstand: BorMedTypeCti? = null
    @JsonIgnore var beregningArsak: BeregningArsakCti? = null // SIMDOM-EDIT @JsonIgnore (since not present in kjerne/PEN AbstraktBeregningsresultat
    var lonnsvekstInformasjon: LonnsvekstInformasjon? = null
    var merknadListe: MutableList<Merknad> = mutableListOf()
    var gjennomsnittligUttaksgradSisteAr: Double = 0.0

    //var versjon = Version.imageVersion

    protected constructor() : super()

    protected constructor(r: AbstraktBeregningsResultat) : super() {
        if (r.virkFom != null) {
            virkFom = r.virkFom!!.clone() as Date
        }
        if (r.pensjonUnderUtbetaling != null) {
            pensjonUnderUtbetaling = PensjonUnderUtbetaling(r.pensjonUnderUtbetaling!!)
        }
        if (r.brukersSivilstand != null) {
            brukersSivilstand = SivilstandTypeCti(r.brukersSivilstand)
        }
        if (r.benyttetSivilstand != null) {
            benyttetSivilstand = BorMedTypeCti(r.benyttetSivilstand)
        }
        if (r.beregningArsak != null) {
            beregningArsak = BeregningArsakCti(r.beregningArsak)
        }
        if (r.lonnsvekstInformasjon != null) {
            lonnsvekstInformasjon = LonnsvekstInformasjon(r.lonnsvekstInformasjon!!)
        }
        uttaksgrad = r.uttaksgrad
        gjennomsnittligUttaksgradSisteAr = r.gjennomsnittligUttaksgradSisteAr
        for (merknad in r.merknadListe) {
            merknadListe.add(Merknad(merknad))
        }
        // SIMDOM-ADD:
        r.virkTom?.let { virkTom = it.clone() as Date }
        kravId = r.kravId
        epsMottarPensjon = r.epsMottarPensjon
        r.hentBeregningsinformasjon()?.let { setBeregningsinformasjon(BeregningsInformasjon(it)) }
        // end SIMDOM-ADD
    }

    constructor(
        virkFom: Date? = null,
        pensjonUnderUtbetaling: PensjonUnderUtbetaling? = null,
        uttaksgrad: Int = 0,
        brukersSivilstand: SivilstandTypeCti? = null,
        benyttetSivilstand: BorMedTypeCti? = null,
        beregningArsak: BeregningArsakCti? = null,
        lonnsvekstInformasjon: LonnsvekstInformasjon? = null,
        merknadListe: MutableList<Merknad> = mutableListOf(),
        gjennomsnittligUttaksgradSisteAr: Double = 0.0
    ) {
        this.virkFom = virkFom
        this.pensjonUnderUtbetaling = pensjonUnderUtbetaling
        this.uttaksgrad = uttaksgrad
        this.brukersSivilstand = brukersSivilstand
        this.benyttetSivilstand = benyttetSivilstand
        this.beregningArsak = beregningArsak
        this.lonnsvekstInformasjon = lonnsvekstInformasjon
        this.merknadListe = merknadListe
        this.gjennomsnittligUttaksgradSisteAr = gjennomsnittligUttaksgradSisteAr
    }
}

package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.domain.regler.util.DateCompareUtil
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.kode.BorMedTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagsrolleCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SivilstandTypeCti
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.io.Serializable
import java.util.*

/**
 * PersonDetalj inneholder persondetaljer som er relevante for fastsettelse av vedtak i Pensjonsløsningen,
 * dvs detaljer om den faktiske rollen en bruker har i et krav, sivilstand , pensjonsfaglig vurdert
 * sivilstand og barn for en definert periode.
 */
class PersonDetalj(

    /**
     * Rollen denne personen har i kontekst av kravet.
     */
    var grunnlagsrolle: GrunnlagsrolleCti? = null,
    /**
     * Fra-og-med dato for rollens gyldighet.
     */
    var rolleFomDato: Date? = null,
    /**
     * Til-og-med dato for rollens gyldighet.
     */
    var rolleTomDato: Date? = null,

    /**
     * Representerer personens sivilstand i henhold til TPS.
     */
    var sivilstandType: SivilstandTypeCti? = null,
    /**
     * Eventuell angivelse av hvilken annen person som sivilstandType relaterer seg til,
     * for eksempel ektefelle eller samboer.
     */
    var sivilstandRelatertPerson: PenPerson? = null,
    /**
     * Representerer om og hvordan personen bor sammen med en annen person med persongrunnlag på kravet.
     * Refereres konseptuelt som pensjonsfaglig vurdert sivilstand.
     */
    var borMed: BorMedTypeCti? = null,
    /**
     * Detaljer om barnet hvis rolle=BARN. Angir om barnet bor med en annen forelder.
     */
    var barnDetalj: BarnDetalj? = null,

    /**
     * Angir om det er opprettet barne- eller ektefelletillegg kravlinje for persongrunnlaget
     * med gitt rolle.
     */
    var tillegg: Boolean = false,
    /**
     * Angir om persondetaljen brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true,
    /**
     * Angir kilden til persondetaljen.
     */
    var grunnlagKilde: GrunnlagKildeCti? = null,

    /**
     * Angir om minstepensjonsnivå satstype særskilt skal vurderes.
     */
    //var serskiltSatsUtenET: Boolean = false,
    var serskiltSatsUtenET: Boolean? = null,

    /**
     * Angir om EPS (dette persongrunnlag) har gitt avkall på egen pensjon.
     */
    //var epsAvkallEgenPensjon: Boolean = false
    var epsAvkallEgenPensjon: Boolean? = null,

    ) : Comparable<PersonDetalj>, Serializable {

    // SIMDOM-ADD
    @JsonIgnore
    var virkFom: Date? = null
    @JsonIgnore
    var virkTom: Date? = null
    @JsonIgnore
    var legacyRolleFomDato: Date? = null
    @JsonIgnore
    var legacyRolleTomDato: Date? = null
    @JsonIgnore
    var rawRolleFomDato: Date? = null
    @JsonIgnore
    var rawRolleTomDato: Date? = null

    fun finishInit() {
        rawRolleFomDato = rolleFomDato
        rawRolleTomDato = rolleTomDato

        rolleFomDato = rawRolleFomDato?.noon()
        updateVirkFom()
        updateVirkTomAndVirkFom()

        rolleTomDato = rawRolleTomDato?.noon()

        if (rolleFomDato != null) {
            updateVirkFom()
            updateVirkTomAndVirkFom()
        }

        legacyRolleFomDato = rolleFomDato
        legacyRolleTomDato = rolleTomDato
        rolleFomDato =
            virkFom?.noon() // since rolleFomDato is set to virkFom.noon() in GrunnlagToReglerMapper.mapPersonDetaljToRegler
        rolleTomDato = virkTom?.noon()
    }
    // end SIMDOM-ADD

    constructor(personDetalj: PersonDetalj) : this() {
        if (personDetalj.grunnlagsrolle != null) {
            this.grunnlagsrolle = GrunnlagsrolleCti(personDetalj.grunnlagsrolle)
        }
        if (personDetalj.rolleFomDato != null) {
            this.rolleFomDato = personDetalj.rolleFomDato!!.clone() as Date
        }
        if (personDetalj.rolleTomDato != null) {
            this.rolleTomDato = personDetalj.rolleTomDato!!.clone() as Date
        }
        if (personDetalj.sivilstandType != null) {
            this.sivilstandType = SivilstandTypeCti(personDetalj.sivilstandType)
        }
        if (personDetalj.sivilstandRelatertPerson != null) {
            this.sivilstandRelatertPerson = PenPerson(personDetalj.sivilstandRelatertPerson!!)
        }
        if (personDetalj.borMed != null) {
            this.borMed = BorMedTypeCti(personDetalj.borMed)
        }
        if (personDetalj.barnDetalj != null) {
            this.barnDetalj = BarnDetalj(personDetalj.barnDetalj!!)
        }
        this.tillegg = personDetalj.tillegg
        this.bruk = personDetalj.bruk
        if (personDetalj.grunnlagKilde != null) {
            this.grunnlagKilde = GrunnlagKildeCti(personDetalj.grunnlagKilde)
        }
        this.serskiltSatsUtenET = personDetalj.serskiltSatsUtenET
        this.epsAvkallEgenPensjon = personDetalj.epsAvkallEgenPensjon

        // SIMDOM-ADD:
        if (personDetalj.virkFom != null) {
            this.virkFom = personDetalj.virkFom!!.clone() as Date
        }
        if (personDetalj.virkTom != null) {
            this.virkTom = personDetalj.virkTom!!.clone() as Date
        }
        if (personDetalj.legacyRolleFomDato != null) {
            this.legacyRolleFomDato = personDetalj.legacyRolleFomDato!!.clone() as Date
        }
        if (personDetalj.legacyRolleTomDato != null) {
            this.legacyRolleTomDato = personDetalj.legacyRolleTomDato!!.clone() as Date
        }
        if (personDetalj.rawRolleFomDato != null) {
            this.rawRolleFomDato = personDetalj.rawRolleFomDato!!.clone() as Date
        }
        if (personDetalj.rawRolleTomDato != null) {
            this.rawRolleTomDato = personDetalj.rawRolleTomDato!!.clone() as Date
        }
        // finishInit not called here, since it is assumed that all fields inited in personDetalj argument
        // end SIMDOM-ADD
    }

    constructor(
        grunnlagsrolle: GrunnlagsrolleCti,
        rolleFomDato: Date,
        rolleTomDato: Date,
        sivilstandType: SivilstandTypeCti,
        sivilstandRelatertPerson: PenPerson,
        borMed: BorMedTypeCti,
        barnDetalj: BarnDetalj,
        tillegg: Boolean,
        grunnlagKilde: GrunnlagKildeCti,
        serskiltSatsUtenET: Boolean
    ) : this() {
        this.grunnlagsrolle = grunnlagsrolle
        this.rolleFomDato = rolleFomDato
        this.rolleTomDato = rolleTomDato
        this.sivilstandType = sivilstandType
        this.sivilstandRelatertPerson = sivilstandRelatertPerson
        this.borMed = borMed
        this.barnDetalj = barnDetalj
        this.tillegg = tillegg
        this.bruk = true
        this.grunnlagKilde = grunnlagKilde
        this.serskiltSatsUtenET = serskiltSatsUtenET
    }

    override fun toString(): String {
        val TAB = "    "

        val retValue = StringBuilder()

        retValue.append("PersonDetalj ( ").append(super.toString()).append(TAB).append("grunnlagsrolle = ")
            .append(grunnlagsrolle).append(TAB).append("rolleFomDato = ")
            .append(rolleFomDato).append(TAB).append("rolleTomDato = ").append(rolleTomDato).append(TAB)
            .append("sivilstandType = ").append(sivilstandType).append(TAB)
            .append("borMed = ").append(borMed).append(TAB).append("barnDetalj = ").append(barnDetalj).append(TAB)
            .append("tillegg = ").append(tillegg).append(TAB)
            .append("bruk = ").append(bruk).append(TAB).append("grunnlagKilde = ").append(grunnlagKilde).append(TAB)
            .append(" )")

        return retValue.toString()
    }

    override fun compareTo(other: PersonDetalj): Int {
        return DateCompareUtil.compareTo(rolleFomDato, other.rolleFomDato)
    }

    // SIMDOM-ADD
    private fun updateVirkFom() {
        if (rolleTomDato != null && rolleFomDato != null && getLastDayOfMonth(rolleFomDato!!).after(rolleTomDato)) {
            virkFom = null
        } else {
            if (rolleFomDato != null) {
                val monthAfterRolleFom = getRelativeDateByMonth(rolleFomDato!!, 1)
                virkFom = getFirstDayOfMonth(monthAfterRolleFom)
            }
        }
    }

    private fun updateVirkTomAndVirkFom() {
        if (rolleTomDato == null) {
            virkTom = null
        } else {
            val rolleTomPlusOneDay = getRelativeDateByDays(rolleTomDato!!, 1)
            if (getMonth(rolleTomPlusOneDay) == getMonth(rolleFomDato!!)
                && getYear(rolleTomPlusOneDay) == getYear(rolleFomDato!!)
            ) {
                virkFom = null
                virkTom = null
            } else {
                virkTom = getLastDayOfMonth(rolleTomPlusOneDay)
            }
        }
    }

    /**
     * EPS = Ektefelle/partner/samboer
     */
    fun isEps() = hasGrunnlagsrolle(GrunnlagRolle.EKTEF, GrunnlagRolle.PARTNER, GrunnlagRolle.SAMBO)

    private fun hasGrunnlagsrolle(vararg roller: GrunnlagRolle) =
        grunnlagsrolle?.let { roller.any { x -> x.name == it.kode } } ?: false
}

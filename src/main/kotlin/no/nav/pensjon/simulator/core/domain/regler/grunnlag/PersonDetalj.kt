package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByDays
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getYear
import no.nav.pensjon.simulator.core.util.DateNoonExtension.noon
import java.util.*

/**
 * PersonDetalj inneholder persondetaljer som er relevante for fastsettelse av vedtak i Pensjonsløsningen,
 * dvs detaljer om den faktiske rollen en bruker har i et krav, sivilstand , pensjonsfaglig vurdert
 * sivilstand og barn for en definert periode.
 */
open class PersonDetalj {

    /**
     * Rollen denne personen har i kontekst av kravet.
     */
    var grunnlagsrolleEnum: GrunnlagsrolleEnum? = null

    /**
     * Fra-og-med dato for rollens gyldighet.
     */
    var rolleFomDato: Date? = null

    /**
     * Til-og-med dato for rollens gyldighet.
     */
    var rolleTomDato: Date? = null

    /**
     * Representerer personens sivilstand i henhold til TPS.
     */
    var sivilstandTypeEnum: SivilstandEnum? = null

    /**
     * Eventuell angivelse av hvilken annen person som sivilstandType relaterer seg til,
     * for eksempel ektefelle eller samboer.
     */
    var sivilstandRelatertPerson: PenPerson? = null

    /**
     * Representerer om og hvordan personen bor sammen med en annen person med persongrunnlag på kravet.
     * Refereres konseptuelt som pensjonsfaglig vurdert sivilstand.
     */
    var borMedEnum: BorMedTypeEnum? = null

    /**
     * Detaljer om barnet hvis rolle=BARN. Angir om barnet bor med en annen forelder.
     */
    var barnDetalj: BarnDetalj? = null

    /**
     * Angir om det er opprettet barne- eller ektefelletillegg kravlinje for persongrunnlaget
     * med gitt rolle.
     */
    var tillegg = false

    /**
     * Angir om persondetaljen brukes som grunnlag på kravet.
     */
    var bruk: Boolean = true

    /**
     * Angir kilden til persondetaljen.
     */
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null

    var serskiltSatsUtenET: Boolean? = null
    var epsAvkallEgenPensjon: Boolean? = null

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
        rolleFomDato = virkFom?.noon() // since rolleFomDato is set to virkFom.noon() in GrunnlagToReglerMapper.mapPersonDetaljToRegler
        rolleTomDato = virkTom?.noon()
    }

    constructor() {}

    constructor(source: PersonDetalj) : this() {
        if (source.grunnlagsrolleEnum != null) {
            this.grunnlagsrolleEnum = source.grunnlagsrolleEnum
        }

        if (source.rolleFomDato != null) {
            this.rolleFomDato = source.rolleFomDato!!.clone() as Date
        }

        if (source.rolleTomDato != null) {
            this.rolleTomDato = source.rolleTomDato!!.clone() as Date
        }

        if (source.sivilstandTypeEnum != null) {
            this.sivilstandTypeEnum = source.sivilstandTypeEnum
        }

        if (source.sivilstandRelatertPerson != null) {
            this.sivilstandRelatertPerson = PenPerson(source.sivilstandRelatertPerson!!)
        }

        if (source.borMedEnum != null) {
            this.borMedEnum = source.borMedEnum
        }

        if (source.barnDetalj != null) {
            this.barnDetalj = BarnDetalj(source.barnDetalj!!)
        }

        this.tillegg = source.tillegg
        this.bruk = source.bruk

        if (source.grunnlagKildeEnum != null) {
            this.grunnlagKildeEnum = source.grunnlagKildeEnum
        }

        this.serskiltSatsUtenET = source.serskiltSatsUtenET
        this.epsAvkallEgenPensjon = source.epsAvkallEgenPensjon

        // SIMDOM-ADD:
        if (source.virkFom != null) {
            this.virkFom = source.virkFom!!.clone() as Date
        }

        if (source.virkTom != null) {
            this.virkTom = source.virkTom!!.clone() as Date
        }

        if (source.legacyRolleFomDato != null) {
            this.legacyRolleFomDato = source.legacyRolleFomDato!!.clone() as Date
        }

        if (source.legacyRolleTomDato != null) {
            this.legacyRolleTomDato = source.legacyRolleTomDato!!.clone() as Date
        }

        if (source.rawRolleFomDato != null) {
            this.rawRolleFomDato = source.rawRolleFomDato!!.clone() as Date
        }

        if (source.rawRolleTomDato != null) {
            this.rawRolleTomDato = source.rawRolleTomDato!!.clone() as Date
        }
        // end SIMDOM-ADD
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
    fun isEps() = hasGrunnlagsrolle(GrunnlagsrolleEnum.EKTEF, GrunnlagsrolleEnum.PARTNER, GrunnlagsrolleEnum.SAMBO)

    private fun hasGrunnlagsrolle(vararg roller: GrunnlagsrolleEnum) =
        //grunnlagsrolle?.let { roller.any { x -> x.name == it.kode } } ?: false
        grunnlagsrolleEnum?.let { roller.any { x -> x == it } } ?: false

    // end SIMDOM-ADD
}

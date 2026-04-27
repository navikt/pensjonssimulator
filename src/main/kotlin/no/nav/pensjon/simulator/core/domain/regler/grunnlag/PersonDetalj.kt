package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.BorMedTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagkildeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SivilstandEnum
import no.nav.pensjon.simulator.core.domain.reglerextend.copy
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDayOfMonth
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonth
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.person.relasjon.Soesken
import java.time.LocalDate

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
    var rolleFomDatoLd: LocalDate? = null

    /**
     * Til-og-med dato for rollens gyldighet.
     */
    var rolleTomDatoLd: LocalDate? = null

    /**
     * Representerer personens sivilstand i henhold til TPS.
     */
    var sivilstandTypeEnum: SivilstandEnum? = null // NB: Not nullable in regler

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
    var bruk: Boolean? = null // NB: default true i pensjon-regler (nullable in PersonDetalj in PEN,
    // and default is 'false' in GrunnlagToReglerMapper.mapPersonDetaljToRegler in PEN

    /**
     * Angir kilden til persondetaljen.
     */
    var grunnlagKildeEnum: GrunnlagkildeEnum? = null

    var serskiltSatsUtenET: Boolean? = null
    var epsAvkallEgenPensjon: Boolean? = null

    //--- Extra fields:
    @JsonIgnore
    var penRolleFom: LocalDate? = null

    @JsonIgnore
    var penRolleTom: LocalDate? = null

    @JsonIgnore
    var virkFom: LocalDate? = null

    @JsonIgnore
    var virkTom: LocalDate? = null

    @JsonIgnore
    var soesken: Soesken? = null
    // end extra fields

    constructor()

    constructor(source: PersonDetalj) : this() {
        if (source.grunnlagsrolleEnum != null) {
            this.grunnlagsrolleEnum = source.grunnlagsrolleEnum
        }

        if (source.rolleFomDatoLd != null) {
            this.rolleFomDatoLd = source.rolleFomDatoLd
        }

        if (source.rolleTomDatoLd != null) {
            this.rolleTomDatoLd = source.rolleTomDatoLd
        }

        this.sivilstandTypeEnum = source.sivilstandTypeEnum

        this.sivilstandRelatertPerson = source.sivilstandRelatertPerson?.copy()

        if (source.borMedEnum != null) {
            this.borMedEnum = source.borMedEnum
        }

        this.barnDetalj = source.barnDetalj?.copy()

        this.tillegg = source.tillegg
        this.bruk = source.bruk

        if (source.grunnlagKildeEnum != null) {
            this.grunnlagKildeEnum = source.grunnlagKildeEnum
        }

        this.serskiltSatsUtenET = source.serskiltSatsUtenET
        this.epsAvkallEgenPensjon = source.epsAvkallEgenPensjon

        //--- Extra:
        virkFom = source.virkFom
        virkTom = source.virkTom
        penRolleFom = source.penRolleFom
        penRolleTom = source.penRolleTom
        // end extra
    }

    //--- Extra functions:
    /**
     * Equivalent to setRolleFomDato + setRolleTomDato
     * in PEN no.nav.domain.pensjon.kjerne.grunnlag.PersonDetalj
     */
    fun finishInit() {
        // Ref. PEN setRolleFomDato:
        updateVirkFom()
        updateVirkTomAndVirkFom()

        // Ref. PEN setRolleTomDato:
        if (penRolleFom != null) {
            updateVirkFom()
            updateVirkTomAndVirkFom()
        }

        // Set virk-periode as rolle-periode (ref. PEN GrunnlagToReglerMapper.mapPersonDetaljToRegler):
        rolleFomDatoLd = virkFom
        rolleTomDatoLd = virkTom
    }

    // PEN: updateVirkFom in no.nav.domain.pensjon.kjerne.grunnlag.PersonDetalj
    private fun updateVirkFom() {
        if (penRolleTom != null && penRolleFom != null && getLastDayOfMonth(penRolleFom!!).after(penRolleTom?.toNorwegianDateAtNoon())) {
            virkFom = null
        } else {
            penRolleFom?.let {
                val monthAfterRolleFom = it.plusMonths(1)
                virkFom = monthAfterRolleFom.withDayOfMonth(1)
            }
        }
    }

    // PEN: updateVirkTomAndVirkFom in no.nav.domain.pensjon.kjerne.grunnlag.PersonDetalj
    private fun updateVirkTomAndVirkFom() {
        if (penRolleTom == null) {
            virkTom = null
        } else {
            val rolleTomPlusOneDay = penRolleTom!!.plusDays(1)
            if (rolleTomPlusOneDay.monthValue == penRolleFom!!.monthValue
                && rolleTomPlusOneDay.year == penRolleFom!!.year
            ) {
                virkFom = null
                virkTom = null
            } else {
                virkTom = lastDayOfMonth(rolleTomPlusOneDay)
            }
        }
    }

    /**
     * EPS = Ektefelle/partner/samboer
     */
    fun isEps() = hasGrunnlagsrolle(GrunnlagsrolleEnum.EKTEF, GrunnlagsrolleEnum.PARTNER, GrunnlagsrolleEnum.SAMBO)

    // PEN: no.nav.domain.pensjon.kjerne.grunnlag.PersonDetalj.isGrunnlagsrolleSamboer
    fun isGrunnlagsrolleSamboer(): Boolean =
        GrunnlagsrolleEnum.SAMBO == grunnlagsrolleEnum

    // PEN: no.nav.domain.pensjon.kjerne.grunnlag.PersonDetalj.is3_2Samboer
    fun is3_2Samboer(): Boolean =
        BorMedTypeEnum.SAMBOER3_2 == borMedEnum

    private fun hasGrunnlagsrolle(vararg roller: GrunnlagsrolleEnum): Boolean =
        grunnlagsrolleEnum?.let { roller.any { x -> x == it } } == true
    // end extra functions
}

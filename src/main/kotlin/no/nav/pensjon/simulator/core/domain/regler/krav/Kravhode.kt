package no.nav.pensjon.simulator.core.domain.regler.krav

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpOffentligLivsvarigGrunnlag
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDato
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Periode
import java.time.LocalDate
import java.util.*

// 2025-04-05
/**
 * Kravhode utgjør, sammen med en liste av VilkarsVedtak, hele inndata for
 * de fleste regeltjenestene.
 */
open class Kravhode {
    /**
     * Liste av personer som inngår som datagrunnlag.
     * nøyaktig en person i listen må ha rollen "SOKER".
     */
    var persongrunnlagListe: MutableList<Persongrunnlag> = mutableListOf()

    /**
     * Liste av krav søkeren fremmer.
     */
    var kravlinjeListe: MutableList<Kravlinje> = mutableListOf()

    /**
     * Angir type AFPordning.
     */
    var afpOrdningEnum: AFPtypeEnum? = null

    /**
     * Angir om søker skal ha afptillegg.
     */
    var afptillegg = false

    /**
     * Angir om opptjeningen fra det 65 året skal brukes som opptjening i det 66 år fram til brukeren fyller 70 år.
     * Settes av Regelmotoren første gang.
     */
    var brukOpptjeningFra65I66Aret = false

    /**
     * Angir detaljering i kravet,brukes i barnepensjon.
     */
    var kravVelgTypeEnum: KravVelgtypeEnum? = null

    /**
     * Angir om personen som kravet gjelder har bodd eller arbeidet utenlands:
     */
    var boddEllerArbeidetIUtlandet = false

    /**
     * Flagg som angir om brukerens far har bodd eller arbeidet i utlandet.
     */
    var boddArbeidUtlandFar = false

    /**
     * Flagg som angir om brukerens mor har bodd eller arbeidet i utlandet.
     */
    var boddArbeidUtlandMor = false

    /**
     * Flagg som angir om brukerens avdøde E/P/S har bodd eller arbeidet i utlandet.
     */
    var boddArbeidUtlandAvdod = false
    var uttaksgradListe: MutableList<Uttaksgrad> = mutableListOf()
        set(value) {
            field = value
            sorterUttaksgradListe()
        }

    var regelverkTypeEnum: RegelverkTypeEnum? = null

    /**
     * Angir siste sakstype før overgang til AP.
     */
    var sisteSakstypeForAPEnum: SakTypeEnum? = null

    /*
	* Felt for å tre p_satsGP fra SisteAldersberegning2011.basispensjon.gp.p_satsGP  inn til BestemPsatsGPRS
	* i forbindelse med kall tilBER 3152 RevurderingOpptjening. Innført ifm PK15267/PEN6372
	 */
    var overstyrendeP_satsGP = 0.0

    /*
    * Angir om barnetilleggsgrunnlag er på nytt format (Gjelder per i dag uføretrygd).
     */
    var btVurderingsperiodeBenyttet = false

    /**
     * Sorterer på nyeste fomDato - denne blir uttaksgradListe.get(0)
     */
    private fun sorterUttaksgradListe() {
        Collections.sort(uttaksgradListe, Collections.reverseOrder())
    }

    //--- Extra:
    @JsonIgnore
    var kravId: Long? = null

    @JsonIgnore
    var kravFremsattDato: Date? = null

    @JsonIgnore
    var onsketVirkningsdato: LocalDate? = null

    @JsonIgnore
    var gjelder: KravGjelder? = null

    @JsonIgnore
    var sakId: Long? = null

    @JsonIgnore
    var sakType: SakTypeEnum? = null

    @JsonIgnore
    var sakPenPersonFnr: Pid? = null // sak.penPerson.fnr

    @JsonIgnore
    var sakForsteVirkningsdatoListe: List<FoersteVirkningDato> = emptyList() // PEN: sak.forsteVirkningsdatoList

    fun sakForsteVirkningsdato(): LocalDate? =
        sakForsteVirkningsdatoListe.mapNotNull { it.virkningDato }.minOfOrNull { it }

    fun hentPersongrunnlagForRolle(rolle: GrunnlagsrolleEnum, checkBruk: Boolean): Persongrunnlag? {
        persongrunnlagListe.forEach {
            for (detalj in it.personDetaljListe) {
                if ((!checkBruk || detalj.bruk == true) && rolle == detalj.grunnlagsrolleEnum) {
                    return it
                }
            }
        }

        return null
    }

    // PEN: KravHode.hentPersongrunnlagForSoker + finnPersongrunnlagForRolle
    fun hentPersongrunnlagForSoker(): Persongrunnlag =
        persongrunnlagListe.firstOrNull(::isSoekerAmongDetaljer)
            ?: throw IllegalStateException("Kravhode has no persongrunnlag for søker")

    fun findPersonGrunnlagIGrunnlagsRolle(rolle: GrunnlagsrolleEnum): Persongrunnlag? =
        findPersonGrunnlagIGrunnlagsRolle(rolle, false)

    // PEN: KravHode.getHarGjenlevenderettighet + hasNotAvsluttetKravlinjeOfType
    fun harGjenlevenderettighet(): Boolean =
        kravlinjeListe.any {
            KravlinjeTypeEnum.GJR == it.kravlinjeTypeEnum && !it.isKravlinjeAvbrutt()
        }

    private fun findPersonGrunnlagIGrunnlagsRolle(rolle: GrunnlagsrolleEnum, inUse: Boolean): Persongrunnlag? =
        persongrunnlagListe.firstOrNull { it.findPersonDetaljIPersongrunnlag(rolle, inUse) != null }

    private fun isSoekerAmongDetaljer(persongrunnlag: Persongrunnlag): Boolean =
        persongrunnlag.personDetaljListe.any {
            it.bruk == true && GrunnlagsrolleEnum.SOKER == it.grunnlagsrolleEnum
        }

    fun isUforetrygd() = hasKravlinjeOfType(KravlinjeTypeEnum.UT)

    private fun hasKravlinjeOfType(type: KravlinjeTypeEnum): Boolean =
        kravlinjeListe.any { type == it.kravlinjeTypeEnum }

    fun findPersonDetaljIBruk(rolle: GrunnlagsrolleEnum): PersonDetalj? {
        for (persongrunnlag in persongrunnlagListe) {
            for (detalj in persongrunnlag.personDetaljListe) {
                if (detalj.bruk == true && rolle == detalj.grunnlagsrolleEnum) {
                    return detalj
                }
            }
        }

        return null
    }

    fun findPersongrunnlag(person: PenPerson) =
        persongrunnlagListe.firstOrNull { it.penPerson!!.penPersonId == person.penPersonId }

    fun findHovedKravlinje(kravGjelder: KravGjelder?): Kravlinje? {
        var hovedKravlinje: Kravlinje? = null

        for (linje in kravlinjeListe) {
            val erNorge = linje.land == LandkodeEnum.NOR

            if (linje.erHovedkravlinje() && (erNorge || kunUtland(kravGjelder))) {
                if (hovedKravlinje == null || !linje.isKravlinjeAvbrutt()) {
                    hovedKravlinje = linje
                }

                if (erNorge) {
                    break
                }
            }
        }

        return hovedKravlinje
    }

    // PEN: KravHode.hentGjeldendeLivsvarigInnvilgetAfpOffentligGrunnlag
    fun gjeldendeInnvilgetLivsvarigOffentligAfpGrunnlag(): AfpOffentligLivsvarigGrunnlag? =
        hentPersongrunnlagForSoker().livsvarigOffentligAfpGrunnlagListe
            .firstOrNull {
                periodFallsWithin(
                    p1Start = it.uttaksdato,
                    p1End = it.virkTom,
                    p2Start = onsketVirkningsdato,
                    p2End = onsketVirkningsdato,
                    considerSameDayAsFallsWithin = true
                )
            }

    private companion object {

        // PEN: DateUtils.isDatePeriodFallsWithInWithPossiblyOpenEndings
        // -> LocalDateUtils.isDatePeriodFallsWithIn
        private fun periodFallsWithin(
            p1Start: LocalDate?,
            p1End: LocalDate?,
            p2Start: LocalDate?,
            p2End: LocalDate?,
            considerSameDayAsFallsWithin: Boolean
        ): Boolean {
            val p1: Periode = Periode.of(p1Start, p1End)
            val p2: Periode = Periode.of(p2Start, p2End)
            return p2.fitsIn(p1) && (considerSameDayAsFallsWithin || !p1.sameStart(p2) && !p1.sameEnd(p2))
        }

        private fun kunUtland(kravGjelder: KravGjelder?) =
            kravGjelder?.let {
                EnumSet.of(KravGjelder.F_BH_KUN_UTL, KravGjelder.SLUTTBEH_KUN_UTL).contains(it)
            } == true
    }
    // end extra
}

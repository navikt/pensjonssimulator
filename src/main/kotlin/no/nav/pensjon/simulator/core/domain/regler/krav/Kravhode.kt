package no.nav.pensjon.simulator.core.domain.regler.krav

import com.fasterxml.jackson.annotation.JsonIgnore
import no.nav.pensjon.simulator.core.domain.Land
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.krav.KravGjelder
import no.nav.pensjon.simulator.core.krav.KravlinjeType
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.KravVelgTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.RegelverkTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SakTypeCti
import no.nav.pensjon.simulator.person.Pid
import java.io.Serializable
import java.util.*

/**
 * Kravhode utgjør, sammen med en liste av VilkarsVedtak, hele inndata for
 * de fleste regeltjenestene.
 */

/** Fjernet sorterUttaksgradListe i setter for uttaksgradListe**/
class Kravhode : Serializable {

    // SIMDOM-ADD
    @JsonIgnore var kravId: Long? = null
    @JsonIgnore var kravFremsattDato: Date? = null
    @JsonIgnore var onsketVirkningsdato: Date? = null
    @JsonIgnore var gjelder: KravGjelder? = null
    @JsonIgnore var sakId: Long? = null
    @JsonIgnore var sakType: SakType? = null
    @JsonIgnore var sakPenPersonFnr: Pid? = null // sak.penPerson.fnr
    @JsonIgnore var sakForsteVirkningsdatoListe: List<Date> = emptyList() // sak.forsteVirkningsdatoList

    fun sakForsteVirkningsdato(): Date? = sakForsteVirkningsdatoListe.minOfOrNull { it }

    fun hentPersongrunnlagForRolle(grunnlagsrolle: GrunnlagRolle, checkBruk: Boolean): Persongrunnlag? {
        for (persongrunnlag in persongrunnlagListe) {
            for (detalj in persongrunnlag.personDetaljListe) {
                if (detalj.grunnlagsrolle!!.kode == grunnlagsrolle.name) {
                    if (checkBruk) {
                        if (detalj.bruk) {
                            return persongrunnlag
                        }
                    } else {
                        return persongrunnlag
                    }
                }
            }
        }

        return null
    }

    // KravHode.hentPersongrunnlagForSoker + finnPersongrunnlagForRolle
    fun hentPersongrunnlagForSoker(): Persongrunnlag =
        persongrunnlagListe.firstOrNull(::isSokerAmongDetaljer) ?: throw IllegalStateException("Kravhode has no persongrunnlag for søker")

    fun findPersonGrunnlagIGrunnlagsRolle(grunnlagsRolle: GrunnlagRolle): Persongrunnlag? =
        findPersonGrunnlagIGrunnlagsRolle(grunnlagsRolle, false)

    // KravHode.getHarGjenlevenderettighet + hasNotAvsluttetKravlinjeOfType
    fun harGjenlevenderettighet(): Boolean =
        kravlinjeListe.any {
            KravlinjeType.GJR.name == it.kravlinjeType?.kode &&
                !it.isKravlinjeAvbrutt()
        }

    private fun findPersonGrunnlagIGrunnlagsRolle(grunnlagsRolle: GrunnlagRolle, inUse: Boolean): Persongrunnlag? =
        persongrunnlagListe.firstOrNull { it.findPersonDetaljIPersongrunnlag(grunnlagsRolle, inUse) != null }

    private fun isSokerAmongDetaljer(persongrunnlag: Persongrunnlag): Boolean =
        persongrunnlag.personDetaljListe.any {
            it.bruk &&
                GrunnlagRolle.SOKER.name == it.grunnlagsrolle?.kode
        }
    //--- end SIMDOM-ADD

    /**
     * Liste av personer som inngår som datagrunnlag.
     * Nøyaktig en person i listen må ha rollen "SOKER".
     */
    var persongrunnlagListe: MutableList<Persongrunnlag> = mutableListOf()

    /**
     * Liste av krav søkeren fremmer.
     */
    var kravlinjeListe: MutableList<Kravlinje> = mutableListOf()

    /**
     * Angir type AFPordning.
     */
    var afpOrdning: AfpOrdningTypeCti? = null

    /**
     * Angir om søker skal ha afptillegg.
     */
    var afptillegg = false

    /*
     * Angir om opptjeningen fra det 65 året skal brukes som opptjening i det 66 år fram til brukeren fyller 70 år.
     * Settes av Regelmotoren første gang.
     */
    var brukOpptjeningFra65I66Aret = false

    /**
     * Angir detaljering i kravet,brukes i barnepensjon.
     */
    var kravVelgType: KravVelgTypeCti? = null

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

    var regelverkTypeCti: RegelverkTypeCti? = null

    /**
     * Angir siste sakstype før overgang til AP.
     */
    var sisteSakstypeForAP: SakTypeCti? = null

    /**
     * Angir om ektefellen mottar pensjon.
     * Innført ifm CR140475. BeregnYtelse blir kalt med flagget ektefelleMottarPensjon i Request-objektet.
     * Det flagget blir overført til epsMottarPensjon i startBeregnYtelse slik at det blir med inn
     * til initPREG(kravhode, ..). initPREG utvides til å kalle en ny funksjon settEpsMottarPensjon som
     * setter flagget tilknyttet.mottarPensjon lik ektefelleMottarPensjon i Request-objektet.
     * Dermed kan regelsettet TilknyttetHarPensjonRS i støttefunksjoner kun bytte innholdet til å bruke
     * tilknyttet.mottarPensjon istedet for å se etter PENF inntekt osv.
     */
    @JsonIgnore
    var epsMottarPensjon = false

    /*
     * Felt for å tre p_satsGP fra SisteAldersberegning2011.basispensjon.gp.p_satsGP  inn til BestemPsatsGPRS
     * i forbindelse med kall tilBER 3152 RevurderingOpptjening. Innført ifm PK15267/PEN6372
     */
    var overstyrendeP_satsGP = 0.0

    /*
     * Angir om barnetilleggsgrunnlag er på nytt format (Gjelder per i dag uføretrygd).
     */
    var btVurderingsperiodeBenyttet = false

    constructor()

    constructor(kravhode: Kravhode) : this() {
        for (persongrunnlag in kravhode.persongrunnlagListe) {
            this.persongrunnlagListe.add(Persongrunnlag(persongrunnlag))
        }
        for (kravlinje in kravhode.kravlinjeListe) {
            this.kravlinjeListe.add(Kravlinje(kravlinje))
        }
        if (kravhode.afpOrdning != null) {
            this.afpOrdning = AfpOrdningTypeCti(kravhode.afpOrdning)
        }
        this.afptillegg = kravhode.afptillegg
        this.brukOpptjeningFra65I66Aret = kravhode.brukOpptjeningFra65I66Aret
        if (kravhode.kravVelgType != null) {
            this.kravVelgType = KravVelgTypeCti(kravhode.kravVelgType!!)
        }
        this.boddEllerArbeidetIUtlandet = kravhode.boddEllerArbeidetIUtlandet
        this.boddArbeidUtlandFar = kravhode.boddArbeidUtlandFar
        this.boddArbeidUtlandMor = kravhode.boddArbeidUtlandFar
        this.boddArbeidUtlandAvdod = kravhode.boddArbeidUtlandAvdod
        for (uttaksgrad in kravhode.uttaksgradListe) {
            this.uttaksgradListe.add(Uttaksgrad(uttaksgrad))
        }
        sorterUttaksgradListe()
        this.regelverkTypeCti = kravhode.regelverkTypeCti
        this.sisteSakstypeForAP = kravhode.sisteSakstypeForAP
        this.epsMottarPensjon = kravhode.epsMottarPensjon
        this.overstyrendeP_satsGP = kravhode.overstyrendeP_satsGP
        this.btVurderingsperiodeBenyttet = kravhode.btVurderingsperiodeBenyttet
    }

    constructor(
        persongrunnlagListe: MutableList<Persongrunnlag> = mutableListOf(),
        kravlinjeListe: MutableList<Kravlinje> = mutableListOf(),
        afpOrdning: AfpOrdningTypeCti? = null,
        afptillegg: Boolean = false,
        brukOpptjeningFra65I66Aret: Boolean = false,
        kravVelgType: KravVelgTypeCti? = null,
        boddEllerArbeidetIUtlandet: Boolean = false,
        boddArbeidUtlandFar: Boolean = false,
        boddArbeidUtlandMor: Boolean = false,
        boddArbeidUtlandAvdod: Boolean = false,
        uttaksgradListe: MutableList<Uttaksgrad> = mutableListOf(),
        vurdereTrygdeavtale: Boolean = false,
        regelverkTypeCti: RegelverkTypeCti? = null,
        sisteSakstypeForAP: SakTypeCti? = null
    ) : this() {
        for (persongrunnlag in persongrunnlagListe) {
            this.persongrunnlagListe.add(persongrunnlag)
        }

        for (kravlinje in kravlinjeListe) {
            this.kravlinjeListe.add(kravlinje)
        }

        this.afpOrdning = afpOrdning
        this.afptillegg = afptillegg
        this.brukOpptjeningFra65I66Aret = brukOpptjeningFra65I66Aret
        this.kravVelgType = kravVelgType
        this.boddEllerArbeidetIUtlandet = boddEllerArbeidetIUtlandet
        this.boddArbeidUtlandFar = boddArbeidUtlandFar
        this.boddArbeidUtlandMor = boddArbeidUtlandMor
        this.boddArbeidUtlandAvdod = boddArbeidUtlandAvdod
        for (uttaksgrad in uttaksgradListe) {
            this.uttaksgradListe.add(uttaksgrad)
        }

        this.regelverkTypeCti = regelverkTypeCti
        this.sisteSakstypeForAP = sisteSakstypeForAP
        sorterUttaksgradListe()
    }

    /**
     * Sorterer på nyeste fomDato - denne blir uttaksgradListe.get(0)
     */
    private fun sorterUttaksgradListe() {
        uttaksgradListe.sortWith(Collections.reverseOrder())
    }

    fun sortertUttaksgradListe(): MutableList<Uttaksgrad> {
        sorterUttaksgradListe()
        return uttaksgradListe
    }

    //SIMDOM-ADD:
    fun isUforetrygd() = hasKravlinjeOfType(KravlinjeType.UT)

    private fun hasKravlinjeOfType(kravlinjeTypeCode: KravlinjeType) =
        kravlinjeListe.any { it.kravlinjeType!!.kode == kravlinjeTypeCode.name }

    fun findPersonDetaljIBruk(grunnlagsrolle: GrunnlagRolle): PersonDetalj? {
        for (persongrunnlag in persongrunnlagListe) {
            for (detalj in persongrunnlag.personDetaljListe) {
                if (detalj.grunnlagsrolle!!.kode == grunnlagsrolle.name && detalj.bruk) {
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

        for (kravlinje in kravlinjeListe) {
            val linje = kravlinje as Kravlinje
            val erNorge = linje.land == Land.NOR

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

    private companion object {

        private fun kunUtland(kravGjelder: KravGjelder?) =
            kravGjelder?.let { EnumSet.of(KravGjelder.F_BH_KUN_UTL, KravGjelder.SLUTTBEH_KUN_UTL).contains(it) } ?: false
    }
}

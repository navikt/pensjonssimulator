package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoRepopulator
import no.nav.pensjon.simulator.core.beholdning.BeholdningType
import no.nav.pensjon.simulator.core.domain.GrunnlagRolle
import no.nav.pensjon.simulator.core.krav.KravlinjeTypePlus
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.KravVelgTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getFirstDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getLastDateInYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getRelativeDateByYear
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersectsWithPossiblyOpenEndings
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.util.toLocalDate
import java.time.LocalDate
import java.util.*
import java.util.function.Predicate

// BehandlingsperiodeUtil
object BehandlingPeriodeUtil {

    // BehandlingsperiodeUtil.periodiserGrunnlag
    fun periodiserGrunnlag(
        virkningFom: LocalDate?,
        virkningTom: LocalDate?,
        originalKravhode: Kravhode,
        periodiserFomTomDatoUtenUnntak: Boolean,
        sakType: SakType?
    ): Kravhode {
        val persongrunnlagList = mutableListOf<Persongrunnlag>()
        var usePersongrunnlag: Boolean

        for (originalPersongrunnlag in originalKravhode.persongrunnlagListe) {
            usePersongrunnlag = false

            // Trygdetid should always be copied (and sent to preg) since this is the only way that preg can tell us exactly
            // which trygdetid that has been used by them.
            val newPersongrunnlag = Persongrunnlag(originalPersongrunnlag).apply {
                gjelderOmsorg = false
                gjelderUforetrygd = true
            }.also { it.finishInit() }

            originalPersongrunnlag.overgangsInfoUPtilUT?.let { newPersongrunnlag.overgangsInfoUPtilUT = it }
            newPersongrunnlag.personDetaljListe.removeIf { it.virkFom == null }

            val removables: MutableList<PersonDetalj> = mutableListOf()

            for (detalj in newPersongrunnlag.personDetaljListe) {
                val detaljVirkningFom = detalj.virkFom
                var detaljVirkningTom = detalj.virkTom

                if (GrunnlagRolle.AVDOD.name == detalj.grunnlagsrolle?.kode && detaljVirkningTom != null) {
                    detaljVirkningTom = null
                }

                if (detalj.bruk
                    && (detalj.grunnlagsrolle!!.kode == GrunnlagRolle.MOR.name
                            || detalj.grunnlagsrolle!!.kode == GrunnlagRolle.FAR.name
                            || dateIsValid(detaljVirkningFom, detaljVirkningTom, virkningFom, virkningTom))
                ) {
                    usePersongrunnlag = true
                } else {
                    removables.add(detalj)
                }
            }

            removables.forEach(newPersongrunnlag::removePersonDetalj)

            // Persongrunnlag - include only if there is a PersonDetalj attached that is in use for this period.
            if (usePersongrunnlag) {
                newPersongrunnlag.trygdetidPerioder.removeIf { !it.bruk }
                newPersongrunnlag.opptjeningsgrunnlagListe.removeIf { !it.bruk }

                newPersongrunnlag.inntektsgrunnlagListe.removeIf(
                    InntektsgrunnlagIsValidPredicate(
                        sakType,
                        fromLocalDate(virkningFom),
                        fromLocalDate(virkningTom),
                        periodiserFomTomDatoUtenUnntak
                    ).negate()
                )

                if (newPersongrunnlag.uforegrunnlag != null && !newPersongrunnlag.uforegrunnlag!!.bruk) {
                    newPersongrunnlag.deleteUforegrunnlag()
                }

                if (newPersongrunnlag.yrkesskadegrunnlag != null && !newPersongrunnlag.yrkesskadegrunnlag!!.bruk) {
                    newPersongrunnlag.deleteYrkesskadegrunnlag()
                }

                val dateValidator = DateValidator(fromLocalDate(virkningFom), fromLocalDate(virkningTom))
                newPersongrunnlag.utenlandsoppholdListe.removeIf { !dateValidator.areValid(it.fom!!, it.tom) }
                newPersongrunnlag.instOpphFasteUtgifterperiodeListe.removeIf {
                    !dateValidator.areValid(
                        it.fom!!,
                        it.tom
                    )
                }
                newPersongrunnlag.barnetilleggVurderingsperioder.removeIf {
                    !dateValidator.areValid(
                        it.fomDato!!,
                        it.tomDato
                    )
                }

                newPersongrunnlag.beholdninger
                    .removeIf {
                        BeholdningType.PEN_B.name == it.beholdningsType!!.kode && !dateValidator.areValid(
                            it.fom!!,
                            it.tom
                        )
                    }

                /* NB Tjenestepensjonsgrunnlag ikke brukt?
                newPersongrunnlag.tjenestepensjonsgrunnlagList.removeIf(
                    TjenestepensjonsgrunnlagPredicate(newPersongrunnlag, virkDatoFom, periodiserFomTomDatoUtenUnntak, virkDatoTom)
                )*/

                newPersongrunnlag.trygdetider.removeIf { !isDateInPeriod(virkningFom, it.virkFom, it.virkTom) }
                persongrunnlagList.add(newPersongrunnlag)
            }
        }

        val kravhode = copyKravhodeExceptPersongrunnlag(originalKravhode, persongrunnlagList).also {
            it.persongrunnlagListe.addAll(persongrunnlagList)
        }
        FoersteVirkningDatoRepopulator.mapFoersteVirkningDatoGrunnlagTransfer(kravhode)
        return kravhode
    }

    internal class DateValidator(virkFom: Date?, virkTom: Date?) {
        private val virkFom: Date?
        private val virkTom: Date?

        init {
            this.virkFom = if (virkFom == null) null else Date(virkFom.time)
            this.virkTom = if (virkTom == null) null else Date(virkTom.time)
        }

        //fun areValid(fom: LocalDate, tom: LocalDate?): Boolean =
        //    dateIsValid(fom, tom, virkFom, virkTom)

        fun areValid(fom: Date, tom: Date?): Boolean =
            dateIsValid(fom, tom, virkFom, virkTom)
    }

    internal class InntektsgrunnlagIsValidPredicate(
        private val sakType: SakType?,
        virkDatoFom: Date?,
        virkDatoTom: Date?,
        periodiserFomTomDatoUtenUnntak: Boolean
    ) : Predicate<Inntektsgrunnlag> {
        private val virkDatoFom: Date?
        private val virkDatoTom: Date?
        private val periodiserFomTomDatoUtenUnntak: Boolean

        init {
            this.virkDatoFom = if (virkDatoFom != null) Date(virkDatoFom.time) else null
            this.virkDatoTom = if (virkDatoTom != null) Date(virkDatoTom.time) else null
            this.periodiserFomTomDatoUtenUnntak = periodiserFomTomDatoUtenUnntak
        }

        override fun test(inntektsgrunnlag: Inntektsgrunnlag): Boolean =
            inntektsgrunnlagIsValid(
                sakType,
                inntektsgrunnlag,
                virkDatoFom.toLocalDate(),
                virkDatoTom.toLocalDate(),
                periodiserFomTomDatoUtenUnntak
            )
    }

    /* TODO not used?
    internal class TjenestepensjonsgrunnlagPredicate(
        private val newPersongrunnlag: Persongrunnlag,
        virkDatoFom: LocalDate?,
        periodiserFomTomDatoUtenUnntak: Boolean,
        virkDatoTom: LocalDate?
    ) : Predicate<Tjenestepensjonsgrunnlag> {
        private val virkDatoFom: LocalDate?
        private val periodiserFomTomDatoUtenUnntak: Boolean
        private val virkDatoTom: LocalDate?

        init {
            this.virkDatoFom = if (virkDatoFom != null) Date(virkDatoFom.time) else null
            this.periodiserFomTomDatoUtenUnntak = periodiserFomTomDatoUtenUnntak
            this.virkDatoTom = if (virkDatoTom != null) Date(virkDatoTom.time) else null
        }

        override fun test(tjenestepensjonsgrunnlag: Tjenestepensjonsgrunnlag): Boolean {
            // Discriminator to check if grunnlag changes. To allow changes in tjenestepensjonsgrunnlag to be discovered by TPEN469 - opprettPeriodeArsakerForBehandlingsperiode,
            // eps65BirthDay must be set to true if EPS is 65 or older (false otherwise) in the behandlingsperiode. EPS turning 65 is to be considered as a change in grunnlag.
            val eps65BirthDay = getRelativeDateByYear(newPersongrunnlag.fodselsdato, 65)
            val epsAgeAbove65 = isAfterByDay(virkDatoFom, getFirstDayOfNextMonth(eps65BirthDay), true)
            tjenestepensjonsgrunnlag.isEpsAgeAbove65 = epsAgeAbove65

            return (BooleanUtils.isNotTrue(periodiserFomTomDatoUtenUnntak)
                    || !dateIsValid(
                tjenestepensjonsgrunnlag.firstDayOfFomDatoMonth,
                tjenestepensjonsgrunnlag.lastDayOfTomDatoMonth,
                virkDatoFom,
                virkDatoTom
            )
                    || isAfterByDay(
                tjenestepensjonsgrunnlag.firstDayOfFomDatoMonth,
                eps65BirthDay,
                ALLOW_SAME_DAY_FALSE
            ))
        }
    }

    private fun dateIsValid(fom: LocalDate?, tom: LocalDate?, virkFom: Date?, virkTom: Date?) =
        intersectsWithPossiblyOpenEndings(
            o1Start = fom,
            o1End = tom,
            o2Start = toLocalDate1(virkFom),
            o2End = toLocalDate1(virkTom),
            considerContactByDayAsIntersection = true
        )

    private fun dateIsValid(fom: LocalDate?, tom: LocalDate?, virkFom: LocalDate?, virkTom: LocalDate?) =
        intersectsWithPossiblyOpenEndings(fom, tom, virkFom, virkTom, true)

    private fun getFirstDayOfNextMonth(date: LocalDate?) =
        date?.let { getRelativeDateByMonth(getFirstDayOfMonth(it), 1) }

    private val ALLOW_SAME_DAY_FALSE = false
    */

    private var INNTEKT_IS_RELEVANT_BEFORE_DATE = LocalDate.of(1968, 1, 1)

    // This method skips the Persongrunnlag and some other lists of objects (which ones? NB)
    private fun copyKravhodeExceptPersongrunnlag(kravhode: Kravhode, persongrunnlagListe: List<Persongrunnlag>) =
        Kravhode().also {
            it.kravFremsattDato = kravhode.kravFremsattDato
            it.onsketVirkningsdato = kravhode.onsketVirkningsdato
            it.gjelder = kravhode.gjelder
            it.sakId = kravhode.sakId
            it.sakType = kravhode.sakType

            it.persongrunnlagListe = mutableListOf() // NB persongrunnlag list contents not copied
            copyRelevantKravlinjer(kravhode, it, persongrunnlagListe)
            kravhode.afpOrdning?.let { value -> it.afpOrdning = AfpOrdningTypeCti(value) }
            it.afptillegg = kravhode.afptillegg
            it.brukOpptjeningFra65I66Aret = kravhode.brukOpptjeningFra65I66Aret
            kravhode.kravVelgType?.let { value -> it.kravVelgType = KravVelgTypeCti(value) }
            it.boddEllerArbeidetIUtlandet = kravhode.boddEllerArbeidetIUtlandet
            it.boddArbeidUtlandFar = kravhode.boddArbeidUtlandFar
            it.boddArbeidUtlandMor = kravhode.boddArbeidUtlandMor
            it.boddArbeidUtlandAvdod = kravhode.boddArbeidUtlandAvdod
            it.uttaksgradListe = mutableListOf()

            kravhode.uttaksgradListe.let { list ->
                val iterator = list.iterator()

                while (iterator.hasNext()) {
                    it.uttaksgradListe.add(Uttaksgrad(iterator.next()))
                }

                Collections.sort(it.uttaksgradListe, Collections.reverseOrder())
            }

            it.regelverkTypeCti = kravhode.regelverkTypeCti
            it.sisteSakstypeForAP = kravhode.sisteSakstypeForAP
            //it.isPREG_epsMottarPensjon = kravhode.isPREG_epsMottarPensjon
            it.overstyrendeP_satsGP = kravhode.overstyrendeP_satsGP
            it.btVurderingsperiodeBenyttet = kravhode.btVurderingsperiodeBenyttet
        }

    private fun copyRelevantKravlinjer(source: Kravhode, target: Kravhode, persongrunnlagList: List<Persongrunnlag>) {
        source.kravlinjeListe
            .filter { isRelevantPersongrunnlagFoundForKravlinje(it, persongrunnlagList) }
            .map(::Kravlinje)
            .forEach { target.kravlinjeListe.add(it) }
    }

    private fun isRelevantPersongrunnlagFoundForKravlinje(
        kravlinje: Kravlinje,
        persongrunnlagList: List<Persongrunnlag>
    ): Boolean {
        val relevantTypes =
            EnumSet.of(KravlinjeTypePlus.GJR, KravlinjeTypePlus.UT_GJT, KravlinjeTypePlus.ET, KravlinjeTypePlus.BT)
        val currentType = KravlinjeTypePlus.valueOf(kravlinje.kravlinjeType!!.kode)

        return if (relevantTypes.contains(currentType)) {
            persongrunnlagList
                .filter { it.penPerson!!.penPersonId == kravlinje.relatertPerson!!.penPersonId }
                .any { hasRelevantRolleForYtelse(kravlinje, it) }
        } else true
    }

    private fun hasRelevantRolleForYtelse(kravlinje: Kravlinje, persongrunnlag: Persongrunnlag) =
        when (KravlinjeTypePlus.valueOf(kravlinje.kravlinjeType!!.kode)) {
            KravlinjeTypePlus.GJR, KravlinjeTypePlus.UT_GJT -> persongrunnlag.isAvdod()
            KravlinjeTypePlus.ET -> persongrunnlag.isEps()
            KravlinjeTypePlus.BT -> persongrunnlag.isBarnOrFosterbarn()
            else -> throw RuntimeException("Unsupported kravlinjetype code " + kravlinje.kravlinjeType!!.kode)
        }

    private fun dateIsValid(fom: Date?, tom: Date?, virkFom: Date?, virkTom: Date?) =
        intersectsWithPossiblyOpenEndings(
            o1Start = fom,
            o1End = tom,
            o2Start = virkFom,
            o2End = virkTom,
            considerContactByDayAsIntersection = true
        )

    private fun dateIsValid(fom: Date?, tom: Date?, virkFom: LocalDate?, virkTom: LocalDate?) =
        intersectsWithPossiblyOpenEndings(
            o1Start = fom.toLocalDate(),
            o1End = tom.toLocalDate(),
            o2Start = virkFom,
            o2End = virkTom,
            considerContactByDayAsIntersection = true
        )

    private fun inntektsgrunnlagIsValid(
        sakType: SakType?,
        grunnlag: Inntektsgrunnlag,
        virkningFom: LocalDate?,
        virkningTom: LocalDate?,
        periodiserFomTomDatoUtenUnntak: Boolean
    ): Boolean {
        if (!grunnlag.bruk) {
            return false
        }

        // CR198751: If periodiserFomTomDatoUtenUnntak is true, only fom/tom dates shall be considered.
        if (periodiserFomTomDatoUtenUnntak) {
            return dateIsValid(grunnlag.fom, grunnlag.tom, virkningFom, virkningTom)
        }

        if (sakType == SakType.AFP || dateIsValid(grunnlag.fom, grunnlag.tom, virkningFom, virkningTom)) {
            return true
        }

        // ... or if they are type PGI and have been in use in this year and the two years before (CR201293)
        val sameDateTwoYearsBefore = getRelativeDateByYear(virkningFom!!, -2)
        val startofTwoYearBeforeVirkDatoFom = getFirstDateInYear(sameDateTwoYearsBefore)
        val endOfThisYear = if (virkningTom != null) getLastDateInYear(virkningTom) else null
        // If type PGI, the Inntektsgrunnlag just has to start sometime after startOfLastYear and before endOfThisYear to be included (CR 72810)
        if (InntektType.PGI.name == grunnlag.inntektType!!.kode && intersectsWithPossiblyOpenEndings(
                startofTwoYearBeforeVirkDatoFom,
                endOfThisYear,
                grunnlag.fom,
                grunnlag.tom,
                true
            )
        ) {
            return true
        }

        return isValidInntektType(grunnlag.inntektType!!) && isValidInntektFom(grunnlag.fom)
    }

    private fun isValidInntektFom(fom: Date?): Boolean =
        fom?.let { isBeforeDay(it, INNTEKT_IS_RELEVANT_BEFORE_DATE) } == true

    private fun isValidInntektType(type: InntektTypeCti) =
        type.kode.let { InntektType.ARBLIGN.name == it || InntektType.AI.name == it }
}

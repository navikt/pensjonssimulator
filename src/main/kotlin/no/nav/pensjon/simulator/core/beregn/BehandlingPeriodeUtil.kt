package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.PersonDetalj
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.reglerextend.grunnlag.copy
import no.nav.pensjon.simulator.core.inntekt.InntektsgrunnlagValidity
import no.nav.pensjon.simulator.core.legacy.util.DateUtil
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.core.virkning.FoersteVirkningDatoRepopulator
import no.nav.pensjon.simulator.tech.time.Interval
import java.time.LocalDate
import java.util.*

// PEN: BehandlingsperiodeUtil
object BehandlingPeriodeUtil {

    // BehandlingsperiodeUtil.periodiserGrunnlag
    fun periodiserGrunnlag(
        virkningFom: LocalDate?,
        virkningTom: LocalDate?,
        originalKravhode: Kravhode,
        periodiserFomTomDatoUtenUnntak: Boolean,
        sakType: SakTypeEnum?
    ): Kravhode {
        val virkningPeriode = Interval(virkningFom, virkningTom)
        val persongrunnlagList = mutableListOf<Persongrunnlag>()
        var usePersongrunnlag: Boolean

        for (originalPersongrunnlag in originalKravhode.persongrunnlagListe) {
            usePersongrunnlag = false

            // Trygdetid should always be copied (and sent to pensjon-regler), since this is the only way that
            // pensjon-regler can tell us exactly which trygdetid that has been used by them.
            val newPersongrunnlag =
                Persongrunnlag(originalPersongrunnlag).apply {
                    gjelderOmsorg = false
                    gjelderUforetrygd = true
                }.also { it.finishInit() }

            originalPersongrunnlag.overgangsInfoUPtilUT?.let { newPersongrunnlag.overgangsInfoUPtilUT = it }
            newPersongrunnlag.personDetaljListe.removeIf { it.virkFom == null }

            val removables: MutableList<PersonDetalj> = mutableListOf()

            for (detalj in newPersongrunnlag.personDetaljListe) {
                val detaljVirkningFom = detalj.virkFom
                var detaljVirkningTom = detalj.virkTom

                if (GrunnlagsrolleEnum.AVDOD == detalj.grunnlagsrolleEnum && detaljVirkningTom != null) {
                    detaljVirkningTom = null
                }

                if (detalj.bruk == true
                    && (detalj.grunnlagsrolleEnum == GrunnlagsrolleEnum.MOR
                            || detalj.grunnlagsrolleEnum == GrunnlagsrolleEnum.FAR
                            || DateUtil.dateIsValid(detaljVirkningFom, detaljVirkningTom, virkningFom, virkningTom))
                ) {
                    usePersongrunnlag = true
                } else {
                    removables.add(detalj)
                }
            }

            removables.forEach(newPersongrunnlag::removePersonDetalj)

            // Persongrunnlag - include only if there is a PersonDetalj attached that is in use for this period.
            if (usePersongrunnlag) {
                /* TODO: Include when regler supports omstillingsstønad:
                // Kopier relevant omstillingsstønadgrunnlag:
                if (originalPersongrunnlag.is3_2Samboer()) {
                    originalPersongrunnlag.omstillingsstonadGrunnlagList.forEach {
                        if (dateIsValid(it.getFomDato(), it.getTomDato(), virkDatoFom, virkDatoTom)) {
                            newPersongrunnlag.addOmstillingsstonadGrunnlag(it.copy())
                        }
                    }
                }
                */

                // NB: trygdetidperioder removed also if bruk = null:
                newPersongrunnlag.trygdetidPerioder.removeIf { it.bruk != true }
                newPersongrunnlag.trygdetidPerioderKapittel20.removeIf { it.bruk != true }
                newPersongrunnlag.opptjeningsgrunnlagListe.removeIf { !it.bruk }

                newPersongrunnlag.inntektsgrunnlagListe.removeIf(
                    InntektsgrunnlagValidity(
                        sakType,
                        virkningFom,
                        virkningTom,
                        periodiserFomTomDatoUtenUnntak
                    ).negate()
                )

                if (newPersongrunnlag.uforegrunnlag?.bruk == false) {
                    newPersongrunnlag.deleteUforegrunnlag()
                }

                if (newPersongrunnlag.yrkesskadegrunnlag?.bruk == false) {
                    newPersongrunnlag.deleteYrkesskadegrunnlag()
                }

                newPersongrunnlag.utenlandsoppholdListe.removeIf {
                    virkningPeriode.intersectsWith(it.fom!!, it.tom).not()
                }

                newPersongrunnlag.instOpphFasteUtgifterperiodeListe.removeIf {
                    virkningPeriode.intersectsWith(it.fom!!, it.tom).not()
                }

                newPersongrunnlag.barnetilleggVurderingsperioder.removeIf {
                    virkningPeriode.intersectsWith(it.fomDato!!, it.tomDato).not()
                }

                newPersongrunnlag.beholdninger.removeIf {
                    it.beholdningsTypeEnum == BeholdningtypeEnum.PEN_B &&
                            virkningPeriode.intersectsWith(it.fom!!, it.tom).not()
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
            it.afpOrdningEnum = kravhode.afpOrdningEnum
            it.afptillegg = kravhode.afptillegg
            it.brukOpptjeningFra65I66Aret = kravhode.brukOpptjeningFra65I66Aret
            it.kravVelgTypeEnum = kravhode.kravVelgTypeEnum
            it.boddEllerArbeidetIUtlandet = kravhode.boddEllerArbeidetIUtlandet
            it.boddArbeidUtlandFar = kravhode.boddArbeidUtlandFar
            it.boddArbeidUtlandMor = kravhode.boddArbeidUtlandMor
            it.boddArbeidUtlandAvdod = kravhode.boddArbeidUtlandAvdod
            it.uttaksgradListe = mutableListOf()

            kravhode.uttaksgradListe.let { list ->
                val iterator = list.iterator()

                while (iterator.hasNext()) {
                    it.uttaksgradListe.add(iterator.next().copy())
                }

                it.uttaksgradListe.sortByDescending { it.fomDato }
            }

            it.regelverkTypeEnum = kravhode.regelverkTypeEnum
            it.sisteSakstypeForAPEnum = kravhode.sisteSakstypeForAPEnum
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
            EnumSet.of(KravlinjeTypeEnum.GJR, KravlinjeTypeEnum.UT_GJT, KravlinjeTypeEnum.ET, KravlinjeTypeEnum.BT)

        return if (relevantTypes.contains(kravlinje.kravlinjeTypeEnum)) {
            persongrunnlagList
                .filter { it.penPerson!!.penPersonId == kravlinje.relatertPerson!!.penPersonId }
                .any { hasRelevantRolleForYtelse(kravlinje, it) }
        } else true
    }

    private fun hasRelevantRolleForYtelse(kravlinje: Kravlinje, persongrunnlag: Persongrunnlag) =
        when (kravlinje.kravlinjeTypeEnum) {
            KravlinjeTypeEnum.GJR, KravlinjeTypeEnum.UT_GJT -> persongrunnlag.isAvdod()
            KravlinjeTypeEnum.ET -> persongrunnlag.isEps()
            KravlinjeTypeEnum.BT -> persongrunnlag.isBarnOrFosterbarn()
            else -> throw RuntimeException("Unsupported kravlinjetype code " + kravlinje.kravlinjeTypeEnum)
        }
}

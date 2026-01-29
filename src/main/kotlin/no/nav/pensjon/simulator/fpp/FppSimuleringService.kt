package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.afp.offentlig.pre2025.AfpVilkaarsproever
import no.nav.pensjon.simulator.core.GeneralPensjonSimuleringService
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.Trygdetid
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.ExtraSimuleringSpec
import no.nav.pensjon.simulator.core.util.isBefore
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.fpp.FppSimuleringSpecValidator.validate
import no.nav.pensjon.simulator.fpp.FppSimuleringUtil.persongrunnlagForRolle
import no.nav.pensjon.simulator.g.GrunnbeloepService
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.relasjon.eps.EpsUtil.epsMottarPensjon
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class FppSimuleringService(
    private val generalSimuleringService: GeneralPensjonSimuleringService,
    private val vilkaarsproever: AfpVilkaarsproever,
    private val personService: PersonService,
    private val grunnbeloepService: GrunnbeloepService
) {
    // PEN: SimpleSimuleringService.simulerPensjonsberegning
    //   -> SimulerPensjonsberegningCommand.execute
    fun simulerPensjonsberegning(spec: Simulering): Simuleringsresultat {
        validate(spec)
        val tilstrekkeligTrygdetid: Boolean = simulerTrygdetid(spec)

        if (tilstrekkeligTrygdetid.not()) {
            return Simuleringsresultat().apply {
                statusEnum = VedtakResultatEnum.AVSL
                merknadListe.add(Merknad().apply { kode = "MinsteTrygdetid" })
            }
        }

        addUfoerehistorikk(spec)
        createVilkaarsvedtakListe(spec)

        if (AFP == spec.simuleringTypeEnum) {
            val simuleringsresultat: Simuleringsresultat = simulerVilkaarsproevingAvTidsbegrensetOffentligAfp(spec)
            val status: VedtakResultatEnum = simuleringsresultat.statusEnum ?: VedtakResultatEnum.INNV

            if (status == VedtakResultatEnum.AVSL || status == VedtakResultatEnum.VETIKKE) {
                return simuleringsresultat
            }
        }

        return simulerMedFeilhaandtering(spec, extraSpec = extraSimuleringSpec(spec)).apply {
            if (statusEnum == null) {
                statusEnum = VedtakResultatEnum.INNV
            }
        }
    }

    private fun addUfoerehistorikk(spec: Simulering) {
        val simuleringType = spec.simuleringTypeEnum

        if (simuleringType == ALDER || simuleringType == ALDER_M_GJEN || simuleringType == AFP) {
            addUfoerehistorikk(spec, rolle = GrunnlagsrolleEnum.SOKER)
        }

        if (simuleringType == ALDER_M_GJEN) {
            addUfoerehistorikk(spec, rolle = GrunnlagsrolleEnum.AVDOD)
        }

        if (simuleringType == BARN) {
            addUfoerehistorikk(spec, rolle = GrunnlagsrolleEnum.MOR)
            addUfoerehistorikk(spec, rolle = GrunnlagsrolleEnum.FAR)
        }
    }

    private fun addUfoerehistorikk(spec: Simulering, rolle: GrunnlagsrolleEnum) {
        val grunnlag: Persongrunnlag = persongrunnlagForRolle(
            grunnlagListe = spec.persongrunnlagListe,
            rolle
        ) ?: return

        relevantUfoerehistorikk(
            person = grunnlag.penPerson,
            virkningFom = spec.uttaksdato
        )?.let {
            grunnlag.uforeHistorikk = it
        }
    }

    private fun relevantUfoerehistorikk(person: PenPerson?, virkningFom: Date?): Uforehistorikk? {
        val historikk: Uforehistorikk = person?.pid?.let(personService::person)?.uforehistorikk ?: return null

        historikk.uforeperiodeListe = historikk.uforeperiodeListe
            .filter { isRelevant(periode = it, virkningFom) }
            .toMutableList()

        return historikk
    }

    private fun createVilkaarsvedtakListe(spec: Simulering) {
        val virkningFom: LocalDate = spec.uttaksdato!!.toNorwegianLocalDate().withDayOfYear(1)
        val legacyVirkningFom: Date = virkningFom.toNorwegianDateAtNoon()
        val grunnbeloep: Int = grunnbeloepService.grunnbeloep(dato = virkningFom)

        for (persongrunnlag in spec.persongrunnlagListe) {
            persongrunnlag.personDetaljListe.forEach {
                updateVilkaarsvedtak(
                    spec,
                    vedtak = innvilgetVedtak(virkningFom = legacyVirkningFom, person = persongrunnlag.penPerson),
                    persongrunnlag,
                    personDetalj = it,
                    grunnbeloep
                )
            }
        }
    }

    private fun simulerMedFeilhaandtering(coreSpec: Simulering, extraSpec: ExtraSimuleringSpec): Simuleringsresultat =
        try {
            simuler(coreSpec, extraSpec)
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: RegelmotorValideringException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }

    private fun simuler(coreSpec: Simulering, extraSpec: ExtraSimuleringSpec): Simuleringsresultat =
        when (coreSpec.simuleringTypeEnum) {
            ALDER -> generalSimuleringService.simulerPensjon(coreSpec, extraSpec, simuleringType = ALDER)
            ALDER_M_GJEN -> generalSimuleringService.simulerPensjon(coreSpec, extraSpec, simuleringType = ALDER_M_GJEN)
            AFP -> generalSimuleringService.simulerPensjon(coreSpec, extraSpec, simuleringType = AFP)
            GJENLEVENDE -> generalSimuleringService.simulerPensjon(coreSpec, extraSpec, simuleringType = GJENLEVENDE)
            BARN -> simulerBarnepensjon(coreSpec, extraSpec)
            else -> Simuleringsresultat()
        }

    private fun simulerBarnepensjon(coreSpec: Simulering, extraSpec: ExtraSimuleringSpec): Simuleringsresultat {
        var antallBarn = 1
        val uttaksaar: Int = coreSpec.uttaksdato!!.toNorwegianLocalDate().year
        var soekersPersongrunnlag: Persongrunnlag? = null

        for (persongrunnlag in coreSpec.persongrunnlagListe) {
            val soekersFoedselsaar: Int = persongrunnlag.fodselsdato!!.toNorwegianLocalDate().year

            for (personDetalj in persongrunnlag.personDetaljListe) {
                personDetalj.grunnlagsrolleEnum?.let {
                    if (it == GrunnlagsrolleEnum.SOSKEN) {
                        if (uttaksaar - soekersFoedselsaar < MAX_ALDER_SOESKEN) {
                            antallBarn++
                        } else {
                            if (personDetalj.barnDetalj?.underUtdanning == true) {
                                antallBarn++
                            }
                        }
                    } else if (it == GrunnlagsrolleEnum.SOKER) {
                        soekersPersongrunnlag = persongrunnlag
                    }
                }
            }
        }

        soekersPersongrunnlag!!.barnekull = Barnekull().apply { this.antallBarn = antallBarn }
        return generalSimuleringService.simulerPensjon(coreSpec, extraSpec, simuleringType = BARN)
    }

    private fun simulerVilkaarsproevingAvTidsbegrensetOffentligAfp(spec: Simulering): Simuleringsresultat =
        try {
            vilkaarsproever.vilkaarsproevTidsbegrensetOffentligAfp(spec)
        } catch (e: KanIkkeBeregnesException) {
            throw FeilISimuleringsgrunnlagetException(e)
        } catch (e: RegelmotorValideringException) {
            throw KonsistensenIGrunnlagetErFeilException(e)
        }

    private fun extraSimuleringSpec(coreSpec: Simulering): ExtraSimuleringSpec {
        val beregnForsoergingstillegg = coreSpec.vilkarsvedtakliste.any {
            it.kravlinjeTypeEnum == KravlinjeTypeEnum.ET || it.kravlinjeTypeEnum == KravlinjeTypeEnum.BT
        }

        return ExtraSimuleringSpec(
            beregnInstitusjonsopphold = false,
            beregnForsoergingstillegg,
            epsMottarPensjon = epsMottarPensjon(personListe = coreSpec.persongrunnlagListe)
        )
    }

    private companion object {
        private const val GRUNNLAG_FOR_BEREGNING_AV_TRYGDETID: Int = 51
        private const val MAX_ALDER_SOESKEN = 18
        private const val MAX_TRYGDETID = 40
        private const val MIN_TRYGDETID = 3
        private const val TRYGDETID_HVIS_FLYKTNING = MAX_TRYGDETID

        /**
         * Setter trygdetid på persongrunnlagene og avgjør om trygdetiden er tilstrekkelig.
         */
        private fun simulerTrygdetid(spec: Simulering): Boolean {
            var tilstrekkelig = true // i utgangspunktet
            var dummyPersonId = 0L

            for (persongrunnlag in spec.persongrunnlagListe) {
                val person: PenPerson = persongrunnlag.penPerson!!

                // NB: penPersonId is nullable in PEN but not here
                if (person.penPersonId == 0L) {
                    person.penPersonId = ++dummyPersonId
                }

                val trygdetidAntallAar = trygdetidAntallAar(persongrunnlag)
                persongrunnlag.trygdetid = Trygdetid().apply { tt = trygdetidAntallAar }
                persongrunnlag.trygdetider.add(Trygdetid().apply { tt = trygdetidAntallAar })

                if (trygdetidAntallAar < MIN_TRYGDETID &&
                    ALDER == spec.simuleringTypeEnum &&
                    isSoeker(persongrunnlag)
                ) {
                    tilstrekkelig = false
                }
            }

            return tilstrekkelig
        }

        private fun trygdetidAntallAar(persongrunnlag: Persongrunnlag): Int {
            if (persongrunnlag.flyktning == true)
                return TRYGDETID_HVIS_FLYKTNING

            val antallAar: Int = GRUNNLAG_FOR_BEREGNING_AV_TRYGDETID - persongrunnlag.antallArUtland

            return antallAar
                .coerceAtLeast(0) // NB: Ikke MIN_TRYGDETID
                .coerceAtMost(MAX_TRYGDETID)
        }

        private fun updateVilkaarsvedtak(
            spec: Simulering,
            vedtak: VilkarsVedtak,
            persongrunnlag: Persongrunnlag,
            personDetalj: PersonDetalj,
            grunnbeloep: Int
        ) {
            val rolle = personDetalj.grunnlagsrolleEnum
            val simuleringType = spec.simuleringTypeEnum

            val kravlinjeType: KravlinjeTypeEnum? =
                rolle?.let { kravlinjeType(simuleringType, rolle = it, persongrunnlag, personDetalj, grunnbeloep) }

            kravlinjeType?.let {
                vedtak.kravlinje = Kravlinje().apply {
                    kravlinjeTypeEnum = it
                    hovedKravlinje = it.erHovedkravlinje
                    relatertPerson = persongrunnlag.penPerson
                }

                vedtak.kravlinjeTypeEnum = it
                vedtak.forsteVirk = vedtak.virkFom
                spec.vilkarsvedtakliste.add(vedtak)
            }
        }

        private fun kravlinjeType(
            simuleringType: SimuleringTypeEnum?,
            rolle: GrunnlagsrolleEnum,
            persongrunnlag: Persongrunnlag,
            personDetalj: PersonDetalj,
            grunnbeloep: Int
        ): KravlinjeTypeEnum? =
            when {
                rolle == GrunnlagsrolleEnum.SOKER -> simuleringType?.let(::kravlinjeTypeForSoeker)

                rolle == GrunnlagsrolleEnum.AVDOD && ALDER_M_GJEN == simuleringType -> KravlinjeTypeEnum.GJR

                rolle == GrunnlagsrolleEnum.BARN && (ALDER == simuleringType || ALDER_M_GJEN == simuleringType)
                        && personDetalj.barnDetalj?.inntektOver1G != true -> KravlinjeTypeEnum.BT

                isEps(rolle) &&
                        BorMedTypeEnum.SAMBOER1_5 == personDetalj.borMedEnum &&
                        harRettTilEktefelleTillegg(simuleringType, persongrunnlag, grunnbeloep) -> KravlinjeTypeEnum.ET

                else -> null
            }

        private fun kravlinjeTypeForSoeker(simuleringType: SimuleringTypeEnum): KravlinjeTypeEnum? =
            when (simuleringType) {
                ALDER -> KravlinjeTypeEnum.AP
                ALDER_M_GJEN -> KravlinjeTypeEnum.AP
                AFP -> KravlinjeTypeEnum.AFP
                GJENLEVENDE -> KravlinjeTypeEnum.GJP
                BARN -> KravlinjeTypeEnum.BP
                else -> null
            }

        private fun isEps(rolle: GrunnlagsrolleEnum): Boolean =
            rolle == GrunnlagsrolleEnum.EKTEF
                    || rolle == GrunnlagsrolleEnum.PARTNER
                    || rolle == GrunnlagsrolleEnum.SAMBO

        private fun isSoeker(grunnlag: Persongrunnlag): Boolean =
            grunnlag.personDetaljListe.any { GrunnlagsrolleEnum.SOKER == it.grunnlagsrolleEnum }

        private fun harRettTilEktefelleTillegg(
            simuleringType: SimuleringTypeEnum?,
            persongrunnlag: Persongrunnlag,
            grunnbeloep: Int
        ): Boolean {
            if (ALDER != simuleringType) return false

            val pensjonsinntektFraFolketrygden: Int = inntektBeloep(persongrunnlag, type = InntekttypeEnum.PENF)
            val forventetPensjongivendeInntekt: Int = inntektBeloep(persongrunnlag, type = InntekttypeEnum.FPI)

            val opprettEktefelleTillegg =
                pensjonsinntektFraFolketrygden <= 0 && forventetPensjongivendeInntekt <= grunnbeloep

            /* This is in PEN but will never have any effect:
            if (opprettEktefelleTillegg) {
                if (simulering.simuleringTypeEnum != ALDER) { // <--- This is never true, due to guard check
                    if (persongrunnlag.over60ArKanIkkeForsorgesSelv == false) {
                        opprettEktefelleTillegg = false
                    }
                }
            }*/

            return opprettEktefelleTillegg
        }

        private fun inntektBeloep(grunnlag: Persongrunnlag, type: InntekttypeEnum): Int =
            grunnlag.inntektsgrunnlagListe.firstOrNull { it.inntektTypeEnum == type }?.belop ?: 0

        private fun innvilgetVedtak(virkningFom: Date, person: PenPerson?) =
            VilkarsVedtak().apply {
                vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
                virkFom = virkningFom
                virkTom = null
                gjelderPerson = person
                penPerson = person
            }

        private fun isRelevant(periode: Uforeperiode, virkningFom: Date?): Boolean =
            periode.isRealUforeperiode() &&
                    periode.virk?.isBefore(virkningFom) == true
    }
}
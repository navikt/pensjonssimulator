package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningPOPPTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.time.Month
import java.util.*

/**
 * pensjon-dokumentasjon.ansatt.dev.nav.no/pen/Fellestjenester/FPEN027_HentBeholdningerMedGrunnlag.html
 */
@Service
class OpptjeningMedBeholdningService(
    private val opptjeningService: OpptjeningService,
    private val personService: PersonService,
    private val time: Time
) {
    fun pensjonsopptjening(spec: OpptjeningMedBeholdningSpec): Pensjonsopptjening =
        getPensjonsopptjening(spec.medBeholdninger()).let {
            if (spec.hentBeholdninger) it else it.utenBeholdninger()
        }

    private fun getPensjonsopptjening(spec: OpptjeningMedBeholdningSpec): Pensjonsopptjening {
        validate(spec)
        val soekerPid = spec.pid
        val personSpec: OpptjeningMedBeholdningPersonSpec = spec.personSpecListe.first { it.pid == spec.pid }
        val kapittel20Innvirker = kapittel20Innvirker(soekerPid)
        val gjelderPrivatAfp = spec.sakType == SakTypeEnum.AFP_PRIVAT

        val beholdAarEtterSisteGyldigeOpptjeningsaar =
            aarEtterSisteGyldigeOpptjeningsaarSkalFjernes(
                spec.regelverkType,
                spec.sakType,
                personSpec.gjelderSoeker
            ).not()

        val opptjeningsgrunnlag: OpptjeningsgrunnlagSamling? =
            opptjeningService.hentOpptjeningsgrunnlagSamling(
                soekerPid,
                grunnlagstypeListe = opptjeningsgrunnlagTypeListe(kapittel20Innvirker, gjelderPrivatAfp)
            )

        return Pensjonsopptjening(
            beholdningListe =
                if (spec.hentBeholdninger && kapittel20Innvirker)
                    beholdningListe(soekerPid, spec).map { it.toInternalValue() }
                else
                    emptyList(),
            opptjeningsgrunnlagListe =
                if (spec.hentPensjonspoeng)
                    pensjonspoengListe(
                        opptjeningsgrunnlag,
                        soekerPid,
                        spec,
                        beholdAarEtterSisteGyldigeOpptjeningsaar,
                        personSpec.sisteGyldigeOpptjeningsaar
                    )
                else
                    emptyList(),
            inntektsgrunnlagListe =
                if (spec.hentOpptjeningsgrunnlag)
                    inntektsgrunnlagListe(
                        opptjeningsgrunnlag,
                        beholdAarEtterSisteGyldigeOpptjeningsaar,
                        personSpec.sisteGyldigeOpptjeningsaar
                    )
                else
                    emptyList(),
            dagpengegrunnlagListe =
                if (spec.hentOpptjeningsgrunnlag && kapittel20Innvirker)
                    dagpengegrunnlagListe(
                        opptjeningsgrunnlag,
                        beholdAarEtterSisteGyldigeOpptjeningsaar,
                        personSpec.sisteGyldigeOpptjeningsaar
                    )
                else
                    emptyList(),
            omsorgsgrunnlagListe =
                if (spec.hentOpptjeningsgrunnlag && (kapittel20Innvirker || gjelderPrivatAfp))
                    omsorgsgrunnlagListe(
                        opptjeningsgrunnlag,
                        beholdAarEtterSisteGyldigeOpptjeningsaar,
                        personSpec.sisteGyldigeOpptjeningsaar
                    )
                else
                    emptyList(),
            foerstegangstjeneste =
                if (spec.hentOpptjeningsgrunnlag && kapittel20Innvirker)
                    opptjeningsgrunnlag?.foerstegangstjeneste
                else
                    null
        )
    }

    private fun beholdningListe(soekerPid: Pid, spec: OpptjeningMedBeholdningSpec): List<Pensjonsbeholdning> =
        beholdningerMedGyldiggjortOpptjening(
            soekerPid,
            spec.soekerSpec, //NB: Ikke spec.personSpecListe.firstOrNull { it.pid == spec.pid }
            regelverkType = spec.regelverkType,
            sakType = spec.sakType,
            beholdningListe = opptjeningService.hentBeholdningListe(soekerPid)
        ).ifEmpty { dummyPensjonsbeholdning() }

    private fun inntektsgrunnlagListe(
        grunnlag: OpptjeningsgrunnlagSamling?,
        beholdAarEtterSisteGyldigeOpptjeningsaar: Boolean,
        sisteGyldigeOpptjeningsaar: Int
    ): List<Inntektsgrunnlag> =
        grunnlag?.inntektListe.orEmpty()
            .filter {
                it.erRelevant &&
                        (beholdAarEtterSisteGyldigeOpptjeningsaar || it.aar() <= sisteGyldigeOpptjeningsaar)
            }

    private fun dagpengegrunnlagListe(
        grunnlag: OpptjeningsgrunnlagSamling?,
        beholdAarEtterSisteGyldigeOpptjeningsaar: Boolean,
        sisteGyldigeOpptjeningsaar: Int
    ): List<Dagpengegrunnlag> =
        grunnlag?.dagpengeListe.orEmpty()
            .filter { beholdAarEtterSisteGyldigeOpptjeningsaar || it.ar <= sisteGyldigeOpptjeningsaar }

    private fun omsorgsgrunnlagListe(
        grunnlag: OpptjeningsgrunnlagSamling?,
        beholdAarEtterSisteGyldigeOpptjeningsaar: Boolean,
        sisteGyldigeOpptjeningsaar: Int
    ): List<Omsorgsgrunnlag> =
        grunnlag?.omsorgListe.orEmpty()
            .filter { beholdAarEtterSisteGyldigeOpptjeningsaar || it.ar <= sisteGyldigeOpptjeningsaar }

    private fun pensjonspoengListe(
        grunnlag: OpptjeningsgrunnlagSamling?,
        pid: Pid?,
        spec: OpptjeningMedBeholdningSpec,
        beholdAarEtterSisteGyldigeOpptjeningsaar: Boolean,
        sisteGyldigeOpptjeningsaar: Int
    ): List<Opptjeningsgrunnlag> =
        pid?.let(opptjeningService::hentPensjonspoeng).orEmpty()
            .filter { beholdAarEtterSisteGyldigeOpptjeningsaar || it.ar <= sisteGyldigeOpptjeningsaar }
            .onEach {
                if (shouldAddOpptjeningstyper(grunnlag = it, spec)) {
                    it.opptjeningTypeListe.addAll(
                        opptjeningstyper(aar = it.ar, inntektListe = grunnlag?.inntektListe.orEmpty())
                    )
                }
            }

    private fun opptjeningsgrunnlagTypeListe(
        kapittel20Innvirker: Boolean,
        gjelderPrivatAfp: Boolean
    ): List<OpptjeningsgrunnlagType> =
        when {
            kapittel20Innvirker -> emptyList()
            gjelderPrivatAfp -> listOf(OpptjeningsgrunnlagType.INNTEKT, OpptjeningsgrunnlagType.OMSORG)
            else -> listOf(OpptjeningsgrunnlagType.INNTEKT)
        }

    private fun kapittel20Innvirker(pid: Pid): Boolean =
        isAfterByDay(
            thisDate = personService.person(pid)?.foedselsdato,
            thatDate = foersteOvergangskullDato,
            allowSameDay = true
        )

    /**
     * Hvis bruker aldri har hatt opptjening, vil det ikke være registrert noen beholdning.
     * Bruker kan likevel ha rett på pensjon, og siden regelmotoren forventer å få inn en beholdning,
     * må det opprettes en ”dummy” beholdning med totalbeløp lik 0 kr.
     */
    private fun dummyPensjonsbeholdning(): List<Pensjonsbeholdning> =
        listOf(Pensjonsbeholdning.dummy(aar = time.today().year))

    companion object {

        private const val FOERSTE_OVERGANGSKULL_FOEDSELSAAR = 1954

        private val foersteOvergangskullDato =
            LocalDate.of(FOERSTE_OVERGANGSKULL_FOEDSELSAAR, Month.JANUARY, 1)

        private val kapittel20RegelverkTyper: EnumSet<RegelverkTypeEnum> =
            EnumSet.of(
                RegelverkTypeEnum.N_REG_G_N_OPPTJ,
                RegelverkTypeEnum.N_REG_N_OPPTJ
            )

        private val relevanteSakTyper: EnumSet<SakTypeEnum> =
            EnumSet.of(
                SakTypeEnum.AFP_PRIVAT,
                SakTypeEnum.ALDER
            )

        private fun validate(spec: OpptjeningMedBeholdningSpec) {
            if (spec.harUfoeretrygdKravlinje && spec.hentPensjonspoeng && spec.hentOpptjeningsgrunnlag.not()) {
                throw InvalidArgumentException(
                    "HentBeholdningerMedGrunnlagRequest demands that if hentPensjonspoeng is true, then hentGrunnlagForOpptjeninger must be true as well"
                )
            }
        }

        private fun aarEtterSisteGyldigeOpptjeningsaarSkalFjernes(
            regelverkType: RegelverkTypeEnum?,
            sakType: SakTypeEnum?,
            gjelderSoeker: Boolean
        ): Boolean =
            relevanteSakTyper.contains(sakType)
                    && gjelderRegelverk2011EllerSenere(regelverkType)
                    && gjelderSoeker

        /**
         * 'G_REG' = 'gammelt regelverk', dvs. det som gjaldt før 2011.
         */
        private fun gjelderRegelverk2011EllerSenere(regelverkType: RegelverkTypeEnum?): Boolean =
            regelverkType?.let { it != RegelverkTypeEnum.G_REG } == true

        private fun beholdningerMedGyldiggjortOpptjening(
            soekerPid: Pid,
            soekerSpec: OpptjeningMedBeholdningPersonSpec,
            regelverkType: RegelverkTypeEnum?,
            sakType: SakTypeEnum?,
            beholdningListe: List<Pensjonsbeholdning>
        ): List<Pensjonsbeholdning> =
            if (sakType != SakTypeEnum.ALDER
                || soekerSpec.pid != soekerPid
                || kapittel20RegelverkTyper.contains(regelverkType).not()
            )
                beholdningListe
            else
                beholdningListe.toMutableList().filter {
                    it.aar() <= soekerSpec.sisteGyldigeOpptjeningsaar + OPPTJENING_ETTERSLEP_ANTALL_AAR
                }

        private fun shouldAddOpptjeningstyper(
            grunnlag: Opptjeningsgrunnlag,
            spec: OpptjeningMedBeholdningSpec
        ): Boolean =
            grunnlag.opptjeningTypeListe.isEmpty()
                    && grunnlag.opptjeningTypeEnum == OpptjeningtypeEnum.PPI
                    && spec.hentOpptjeningsgrunnlag
                    && spec.harUfoeretrygdKravlinje

        private fun opptjeningstyper(aar: Int, inntektListe: List<Inntektsgrunnlag>): List<OpptjeningTypeMapping> =
            inntektListe.filter { it.aar() == aar }.mapNotNull(::opptjeningTypeMapping)

        private fun opptjeningTypeMapping(inntekt: Inntektsgrunnlag): OpptjeningTypeMapping? =
            inntekt.inntektTypeEnum?.let {
                OpptjeningTypeMapping().apply {
                    opptjeningPOPPTypeEnum = OpptjeningPOPPTypeEnum.valueOf(it.name)
                }
            }
    }
}
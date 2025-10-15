package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertDelytelse
import no.nav.pensjon.simulator.alderspensjon.convert.SimulatorOutputConverter
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * PEN: SimulerAlderspensjonResponseV3Converter
 */
@Component
class AlderspensjonResultMapperV3(
    private val personService: GeneralPersonService,
    private val normertPensjonsalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun map(
        simuleringResult: SimulatorOutput,
        pid: Pid,
        foersteUttakFom: LocalDate?,
        heltUttakFom: LocalDate?
    ): AlderspensjonResultV3 {
        val alderspensjon = simuleringResult.alderspensjon
        val foedselsdato = personService.foedselsdato(pid)
        val idag = time.today()
        val uttakListe: List<Uttaksgrad> = alderspensjon?.uttakGradListe.orEmpty()
        val harUttak = uttakListe.any { it.tasUt(dato = idag) }

        return AlderspensjonResultV3(
            pensjonsperioder = alderspensjon?.pensjonPeriodeListe.orEmpty()
                .map { pensjonsperiode(source = it, foedselsdato) },
            simuleringsdataListe = simuleringsdataListe(simuleringResult, foedselsdato, foersteUttakFom),
            pensjonsbeholdningsperioder = alderspensjon?.pensjonBeholdningListe.orEmpty()
                .map(::beholdningsperiode),
            alderspensjonFraFolketrygden = alderspensjonListe(
                beregningsinformasjonListe = alderspensjon?.simulertBeregningInformasjonListe.orEmpty(),
                foersteUttakFom,
                heltUttakFom
            ),
            harUttak,
            harTidligereUttak = harUttak.not() && harHattUttakFoer(uttakListe, dato = idag),
            afpPrivatBeholdningVedUttak = privatAfpBeholdningVedUttak(simuleringResult.privatAfpPeriodeListe),
            sisteGyldigeOpptjeningsAr = simuleringResult.sisteGyldigeOpptjeningAar
        )
    }

    private fun simuleringsdataListe(
        simuleringResult: SimulatorOutput,
        foedselsdato: LocalDate,
        foersteUttakFom: LocalDate?
    ): List<SimuleringsdataResultV3> {
        val normertPensjoneringsdato: LocalDate = normertPensjonsalderService.normertPensjoneringsdato(foedselsdato)

        val foersteSimuleringsdataDato: LocalDate? =
            if (isBeforeByDay(thisDate = foersteUttakFom, thatDate = normertPensjoneringsdato, allowSameDay = true))
                normertPensjoneringsdato
            else
                foersteUttakFom

        return simuleringResult.alderspensjon?.simulertBeregningInformasjonListe.orEmpty()
            .filter { isAfterByDay(thisDate = it.datoFom, thatDate = foersteSimuleringsdataDato, allowSameDay = true) }
            .map(::simuleringsdata)
    }

    private companion object {

        private fun alderspensjonListe(
            beregningsinformasjonListe: List<SimulertBeregningInformasjon>,
            foersteUttakFom: LocalDate?,
            heltUttakFom: LocalDate?
        ): List<AlderspensjonFraFolketrygdenResultV3> {
            val alderspensjonListe: MutableList<AlderspensjonFraFolketrygdenResultV3> = mutableListOf()
            val foersteUttakInfo = elementSomStarterPaaDato(beregningsinformasjonListe, foersteUttakFom)
            alderspensjonListe.add(alderspensjon(foersteUttakInfo))

            heltUttakFom?.let {
                val heltUttakInfo = elementSomStarterPaaDato(beregningsinformasjonListe, dato = it)
                alderspensjonListe.add(alderspensjon(heltUttakInfo))
            }

            return alderspensjonListe
        }

        private fun alderspensjon(source: SimulertBeregningInformasjon) =
            AlderspensjonFraFolketrygdenResultV3(
                datoFom = source.datoFom?.toString(),
                delytelser = SimulatorOutputConverter.delytelser(source).map(::delytelse),
                uttaksgrad = source.uttakGrad?.toInt()
            )

        private fun delytelse(source: SimulertDelytelse) =
            DelytelseResultV3(
                pensjonstype = source.type.name,
                belop = source.beloep
            )

        private fun beholdningsperiode(source: BeholdningPeriode) =
            PensjonsbeholdningPeriodeResultV3(
                pensjonsbeholdning = source.pensjonsbeholdning,
                garantipensjonsbeholdning = source.garantipensjonsbeholdning,
                garantitilleggsbeholdning = source.garantitilleggsbeholdning,
                datoFom = source.datoFom.toString(),
                garantipensjonsniva = source.garantipensjonsniva?.let(::garantipensjonsnivaa)
            )

        private fun pensjonsperiode(source: PensjonPeriode, foedselsdato: LocalDate) =
            PensjonsperiodeResultV3(
                arligUtbetaling = source.beloep,
                datoFom = source.alderAar?.let {
                    foersteDagMaanedenEtterBursdag(foedselsdato, alderAar = it).toString()
                }
            )

        private fun garantipensjonsnivaa(source: GarantipensjonNivaa) =
            GarantipensjonsnivaaResultV3(
                belop = source.beloep,
                satsType = source.satsType,
                sats = source.sats,
                tt_anv = source.anvendtTrygdetid
            )

        private fun simuleringsdata(source: SimulertBeregningInformasjon) =
            SimuleringsdataResultV3(
                poengArTom1991 = source.pa_f92,
                poengArFom1992 = source.pa_e91,
                sluttpoengtall = source.spt,
                anvendtTrygdetid = source.tt_anv_kap19,
                basisgp = source.basisGrunnpensjon,
                basistp = source.basisTilleggspensjon,
                basispt = source.basisPensjonstillegg,
                forholdstallUttak = source.forholdstall,
                delingstallUttak = source.delingstall,
                uforegradVedOmregning = source.ufoereGrad,
                datoFom = source.datoFom?.toString()
            )

        private fun privatAfpBeholdningVedUttak(afpPeriodeListe: List<PrivatAfpPeriode>): Int? =
            if (afpPeriodeListe.isEmpty()) null else afpPeriodeListe[0].afpOpptjening

        private fun harHattUttakFoer(uttakListe: List<Uttaksgrad>, dato: LocalDate): Boolean =
            uttakListe.none { it.tasUt(dato) } && uttakListe.any { it.tattUtFoer(dato) }

        private fun elementSomStarterPaaDato(
            beregningsinformasjonListe: List<SimulertBeregningInformasjon>,
            dato: LocalDate?
        ): SimulertBeregningInformasjon =
            beregningsinformasjonListe.firstOrNull { isSameDay(it.datoFom, dato) }
                ?: SimulertBeregningInformasjon()

        private fun foersteDagMaanedenEtterBursdag(foedselsdato: LocalDate, alderAar: Int): LocalDate =
            foedselsdato
                .plusYears(alderAar.toLong())
                .plusMonths(1)
                .withDayOfMonth(1)
    }
}
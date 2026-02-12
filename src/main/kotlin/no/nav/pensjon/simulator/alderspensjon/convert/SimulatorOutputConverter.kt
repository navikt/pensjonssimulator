package no.nav.pensjon.simulator.alderspensjon.convert

import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpOutput
import no.nav.pensjon.simulator.afp.offentlig.pre2025.AfpGrad.beregnAfpGrad
import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.opptjening.OpptjeningGrunnlag
import no.nav.pensjon.simulator.trygdetid.Trygdetid
import java.time.LocalDate
import java.util.*

/**
 * Converts from simulator output to pensjon in an intermediate format.
 */
object SimulatorOutputConverter {

    private const val ALDER_REPRESENTING_LOEPENDE_YTELSER = 0

    fun pensjon(
        source: SimulatorOutput,
        today: LocalDate,
        inntektVedFase1Uttak: Int? = null
    ): SimulertPensjon {
        val alderspensjon: SimulertAlderspensjon? = source.alderspensjon
        val periodeListe: List<PensjonPeriode> = alderspensjon?.pensjonPeriodeListe.orEmpty()

        return SimulertPensjon(
            alderspensjon = periodeListe.map { aarligAlderspensjon(it, alderspensjon) },
            alderspensjonFraFolketrygden = alderspensjon?.simulertBeregningInformasjonListe.orEmpty()
                .map(::alderspensjonFraFolketrygden),
            pre2025OffentligAfp = source.pre2025OffentligAfp?.beregning?.let {
                tidsbegrensetOffentligAfp(
                    beregning = it,
                    foedselsdato = source.foedselDato,
                    inntektVedAfpUttak = inntektVedFase1Uttak
                )
            },
            privatAfp = source.privatAfpPeriodeListe.map(::privatAfp),
            livsvarigOffentligAfp = source.livsvarigOffentligAfp.orEmpty().map(::livsvarigOffentligAfp),
            pensjonBeholdningPeriodeListe = alderspensjon?.pensjonBeholdningListe.orEmpty()
                .map(::beholdningPeriode),
            harUttak = alderspensjon?.uttakGradListe.orEmpty().any { harUttakToday(it, today) },
            primaerTrygdetid = foersteTrygdetid(periodeListe),
            opptjeningGrunnlagListe = source.persongrunnlag?.opptjeningsgrunnlagListe.orEmpty()
                .map(::opptjeningGrunnlag).sortedBy { it.aar }
        )
    }

    // SimulerAlderspensjonResponseV3Converter.getDelytelserFromSimulertBeregningsinformasjon
    fun delytelser(source: SimulertBeregningInformasjon): List<SimulertDelytelse> =
        mutableListOf<SimulertDelytelse>().apply {
            source.grunnpensjon?.let { add(ytelse(type = YtelseskomponentTypeEnum.GP, beloep = it)) }
            source.tilleggspensjon?.let { add(ytelse(type = YtelseskomponentTypeEnum.TP, beloep = it)) }
            source.pensjonstillegg?.let { add(ytelse(type = YtelseskomponentTypeEnum.PT, beloep = it)) }
            source.individueltMinstenivaaTillegg?.let {
                add(ytelse(type = YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV, beloep = it))
            }
            source.inntektspensjon?.let { add(ytelse(type = YtelseskomponentTypeEnum.IP, beloep = it)) }
            source.garantipensjon?.let { add(ytelse(type = YtelseskomponentTypeEnum.GAP, beloep = it)) }
            source.garantitillegg?.let { add(ytelse(type = YtelseskomponentTypeEnum.GAT, beloep = it)) }
            source.skjermingstillegg?.let { add(ytelse(type = YtelseskomponentTypeEnum.SKJERMT, beloep = it)) }
        }

    private fun foersteTrygdetid(periodeListe: List<PensjonPeriode>): Trygdetid =
        firstBeregninginfo(periodeListe)?.let { Trygdetid(it.tt_anv_kap19 ?: 0, it.tt_anv_kap20 ?: 0) }
            ?: Trygdetid(0, 0)

    private fun firstBeregninginfo(periodeListe: List<PensjonPeriode>): SimulertBeregningInformasjon? =
        periodeListe.firstOrNull()?.simulertBeregningInformasjonListe?.firstOrNull()

    private fun aarligAlderspensjon(
        source: PensjonPeriode,
        pensjon: SimulertAlderspensjon?
    ): SimulertAarligAlderspensjon {
        val info = source.latestBeregningInformasjon

        return SimulertAarligAlderspensjon(
            alderAar = source.alderAar ?: ALDER_REPRESENTING_LOEPENDE_YTELSER,
            beloep = source.beloep ?: 0,
            inntektspensjon = info?.inntektspensjon,
            garantipensjon = info?.garantipensjon,
            delingstall = info?.delingstall,
            pensjonBeholdningFoerUttak = beholdningFoerUttak(source.simulertBeregningInformasjonListe),
            andelsbroekKap19 = pensjon?.kapittel19Andel ?: 0.0,
            andelsbroekKap20 = pensjon?.kapittel20Andel ?: 0.0,
            sluttpoengtall = info?.spt,
            trygdetidKap19 = info?.tt_anv_kap19,
            trygdetidKap20 = info?.tt_anv_kap20,
            poengaarFoer92 = info?.pa_f92,
            poengaarEtter91 = info?.pa_e91,
            forholdstall = info?.forholdstall,
            grunnpensjon = info?.grunnpensjon,
            tilleggspensjon = info?.tilleggspensjon,
            pensjonstillegg = info?.pensjonstillegg,
            skjermingstillegg = info?.skjermingstillegg,
            kapittel19Gjenlevendetillegg = info?.gjtAPKap19
        )
    }

    private fun beholdningFoerUttak(list: List<SimulertBeregningInformasjon>): Int? =
        list.firstOrNull { it.pensjonBeholdningFoerUttak != null }?.pensjonBeholdningFoerUttak

    // SimulerAlderspensjonResponseV3Converter.convertSimulertBeregningsinfoToAlderspensjonFraFolketrygden
    private fun alderspensjonFraFolketrygden(source: SimulertBeregningInformasjon) =
        SimulertAlderspensjonFraFolketrygden(
            datoFom = source.datoFom ?: LocalDate.MIN,
            delytelseListe = delytelser(source),
            uttakGrad = source.uttakGrad?.toInt() ?: 0,
            maanedligBeloep = source.maanedligBeloep ?: 0
        )

    private fun ytelse(type: YtelseskomponentTypeEnum, beloep: Int) =
        SimulertDelytelse(type, beloep)

    private fun privatAfp(source: PrivatAfpPeriode) =
        SimulertPrivatAfp(
            alderAar = source.alderAar ?: 0,
            beloep = source.aarligBeloep ?: 0,
            kompensasjonstillegg = source.kompensasjonstillegg ?: 0,
            kronetillegg = source.kronetillegg ?: 0,
            livsvarig = source.livsvarig ?: 0,
            maanedligBeloep = source.maanedligBeloep ?: 0
        )

    /**
     * Ref. BeregningFormPopulator.createBeregningFormDataFromBeregning in pensjon-pselv
     */
    private fun tidsbegrensetOffentligAfp(
        beregning: Beregning,
        foedselsdato: LocalDate?,
        inntektVedAfpUttak: Int?
    ): SimulertPre2025OffentligAfp? =
        if (foedselsdato == null)
            null
        else
            beregning.virkFom?.let {
                val sluttpoengtall = beregning.tp?.spt
                val poengrekke = sluttpoengtall?.poengrekke

                SimulertPre2025OffentligAfp(
                    alderAar = alderAar(foedselsdato, it),
                    totaltAfpBeloep = beregning.netto,
                    tidligereArbeidsinntekt = poengrekke?.tpi ?: 0,
                    grunnbeloep = beregning.g,
                    sluttpoengtall = sluttpoengtall?.pt ?: 0.0,
                    trygdetid = beregning.tt_anv,
                    poengaarTom1991 = poengrekke?.pa_f92 ?: 0,
                    poengaarFom1992 = poengrekke?.pa_e91 ?: 0,
                    grunnpensjon = beregning.gp?.netto ?: 0,
                    tilleggspensjon = beregning.tp?.netto ?: 0,
                    afpTillegg = beregning.afpTillegg?.netto ?: 0,
                    saertillegg = beregning.st?.netto ?: 0,
                    afpGrad = beregnAfpGrad(
                        inntektVedAfpUttak ?: 0,
                        tidligereInntekt = poengrekke?.tpi ?: 0
                    ),
                    afpAvkortetTil70Prosent = beregning.gpAfpPensjonsregulert?.brukt == true
                )
            }

    private fun livsvarigOffentligAfp(source: LivsvarigOffentligAfpOutput) =
        SimulertLivsvarigOffentligAfp(source.alderAar, source.beloep, source.maanedligBeloep)

    private fun beholdningPeriode(source: BeholdningPeriode) =
        SimulertPensjonBeholdningPeriode(
            pensjonBeholdning = source.pensjonsbeholdning ?: 0.0,
            garantipensjonBeholdning = source.garantipensjonsbeholdning ?: 0.0,
            garantitilleggBeholdning = source.garantitilleggsbeholdning ?: 0.0,
            datoFom = source.datoFom,
            garantipensjonNivaa = source.garantipensjonsniva?.let(::garantipensjonNivaa)
                ?: nullGarantipensjonNivaa()
        )

    private fun garantipensjonNivaa(source: GarantipensjonNivaa) =
        SimulertGarantipensjonNivaa(
            beloep = source.beloep,
            satsType = source.satsType,
            sats = source.sats,
            anvendtTrygdetid = source.anvendtTrygdetid
        )

    private fun nullGarantipensjonNivaa() =
        SimulertGarantipensjonNivaa(
            beloep = 0.0,
            satsType = "",
            sats = 0.0,
            anvendtTrygdetid = 0
        )

    private fun opptjeningGrunnlag(source: Opptjeningsgrunnlag) =
        OpptjeningGrunnlag(
            aar = source.ar,
            pensjonsgivendeInntekt = source.pi
        )

    private fun alderAar(foedselsdato: LocalDate, dato: Date): Int =
        PensjonAlderDato(foedselsdato, dato = dato.toNorwegianLocalDate()).alder.aar

    private fun harUttakToday(grad: Uttaksgrad, today: LocalDate) =
        grad.uttaksgrad > 0 && coversToday(grad.fomDato, grad.tomDato, today)

    // SimulerAlderspensjonResponseV3Converter.isUttaksgradToday
    private fun coversToday(fom: Date?, tom: Date?, today: LocalDate): Boolean {
        if (isAfterByDay(today, fom, true)) {
            return tom == null || isBeforeByDay(today, tom, true)
        }

        return false
    }
}

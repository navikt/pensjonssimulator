package no.nav.pensjon.simulator.alderspensjon.convert

import no.nav.pensjon.simulator.core.afp.privat.SimulertPrivatAfpPeriode
import no.nav.pensjon.simulator.core.beholdning.OpptjeningGrunnlag
import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.core.beregn.GarantipensjonNivaa
import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.domain.regler.beregning.Beregning
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.out.OutputLivsvarigOffentligAfp
import no.nav.pensjon.simulator.core.result.PensjonPeriode
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.result.SimulertAlderspensjon
import no.nav.pensjon.simulator.core.result.SimulertBeregningInformasjon
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.alderspensjon.alternativ.*
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import java.time.LocalDate
import java.time.Period
import java.util.*

/**
 * Converts from simulator output to pensjon in an intermediate format.
 */
object SimulatorOutputConverter {

    /**
     * https://lovdata.no/dokument/NL/lov/1997-02-28-19/KAPITTEL_7-2#KAPITTEL_7-2
     * § 20-10.Garantipensjon – trygdetid
     */
    private const val MINIMUM_TRYGDETID_FOR_GARANTIPENSJON_ANTALL_AAR = 5

    private const val ALDER_REPRESENTING_LOPENDE_YTELSER = 0

    fun pensjon(source: SimulatorOutput): SimulertPensjon {
        val alderspensjon: SimulertAlderspensjon? = source.alderspensjon
        val pensjonsperioder: List<PensjonPeriode> = alderspensjon?.pensjonPeriodeListe.orEmpty()
        val trygdetid = anvendtKapittel20Trygdetid(pensjonsperioder)

        return SimulertPensjon(
            alderspensjon = pensjonsperioder.map(SimulatorOutputConverter::alderspensjon),
            alderspensjonFraFolketrygden = alderspensjon?.simulertBeregningInformasjonListe.orEmpty()
                .map(SimulatorOutputConverter::alderspensjonFraFolketrygden),
            privatAfp = source.privatAfpPeriodeListe.map(SimulatorOutputConverter::privatAfp),
            pre2025OffentligAfp = source.pre2025OffentligAfp?.beregning?.let {
                pre2025OffentligAfp(it, source.foedselDato)
            },
            livsvarigOffentligAfp = source.livsvarigOffentligAfp.orEmpty().map(SimulatorOutputConverter::livsvarigOffentligAfp),
            pensjonBeholdningPeriodeListe = alderspensjon?.pensjonBeholdningListe.orEmpty()
                .map(SimulatorOutputConverter::beholdningPeriode),
            harUttak = alderspensjon?.uttakGradListe.orEmpty().any(SimulatorOutputConverter::harUttakToday),
            harNokTrygdetidForGarantipensjon = trygdetid >= MINIMUM_TRYGDETID_FOR_GARANTIPENSJON_ANTALL_AAR,
            trygdetid = trygdetid,
            opptjeningGrunnlagListe = source.persongrunnlag?.opptjeningsgrunnlagListe.orEmpty()
                .map(SimulatorOutputConverter::opptjeningGrunnlag).sortedBy { it.aar }
        )
    }

    private fun anvendtKapittel20Trygdetid(perioder: List<PensjonPeriode>): Int =
        perioder.firstOrNull()?.simulertBeregningInformasjonListe?.firstOrNull()?.tt_anv_kap20 ?: 0

    private fun alderspensjon(source: PensjonPeriode): SimulertAarligAlderspensjon {
        val info = source.simulertBeregningInformasjonListe.firstOrNull()

        return SimulertAarligAlderspensjon(
            alderAar = source.alderAar ?: ALDER_REPRESENTING_LOPENDE_YTELSER,
            beloep = source.beloep ?: 0,
            inntektspensjon = info?.inntektspensjon,
            garantipensjon = info?.garantipensjon,
            delingstall = info?.delingstall,
            pensjonBeholdningFoerUttak = beholdningFoerUttak(source.simulertBeregningInformasjonListe)
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

    // SimulerAlderspensjonResponseV3Converter.getDelytelserFromSimulertBeregningsinformasjon
    private fun delytelser(source: SimulertBeregningInformasjon): List<SimulertDelytelse> {
        val ytelser: MutableList<SimulertDelytelse> = mutableListOf()
        source.grunnpensjon?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.GP) }
        source.tilleggspensjon?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.TP) }
        source.pensjonstillegg?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.PT) }
        source.individueltMinstenivaaTillegg?.let {
            addYtelse(
                it,
                ytelser,
                YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV
            )
        }
        source.inntektspensjon?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.IP) }
        source.garantipensjon?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.GAP) }
        source.garantitillegg?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.GAT) }
        source.skjermingstillegg?.let { addYtelse(it, ytelser, YtelseskomponentTypeEnum.SKJERMT) }
        return ytelser
    }

    private fun addYtelse(belop: Int, ytelser: MutableList<SimulertDelytelse>, type: YtelseskomponentTypeEnum) {
        ytelser.add(SimulertDelytelse(type, belop))
    }

    private fun privatAfp(source: SimulertPrivatAfpPeriode) =
        SimulertPrivatAfp(source.alderAar ?: 0, source.aarligBeloep ?: 0)

    /**
     * Ref. BeregningFormPopulator.createBeregningFormDataFromBeregning in pensjon-pselv
     */
    private fun pre2025OffentligAfp(
        beregning: Beregning,
        foedselDato: LocalDate?
    ): SimulertPre2025OffentligAfp? =
        if (foedselDato == null)
            null
        else
            beregning.virkFom?.let {
                SimulertPre2025OffentligAfp(
                    alderAar = alderAar(foedselDato, it),
                    beregning.netto
                )
            }

    private fun livsvarigOffentligAfp(source: OutputLivsvarigOffentligAfp) =
        SimulertLivsvarigOffentligAfp(source.alderAar, source.beloep)

    private fun beholdningPeriode(source: BeholdningPeriode) =
        SimulertPensjonBeholdningPeriode(
            pensjonBeholdning = source.pensjonsbeholdning ?: 0.0,
            garantipensjonBeholdning = source.garantipensjonsbeholdning ?: 0.0,
            garantitilleggBeholdning = source.garantitilleggsbeholdning ?: 0.0,
            datoFom = source.datoFom,
            garantipensjonNivaa = source.garantipensjonsniva?.let(SimulatorOutputConverter::garantipensjonNivaa)
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
        alderDato(foedselsdato, dato.toNorwegianLocalDate()).alder.aar

    private fun harUttakToday(grad: Uttaksgrad) = grad.uttaksgrad > 0 && coversToday(grad.fomDato, grad.tomDato)

    // SimulerAlderspensjonResponseV3Converter.isUttaksgradToday
    private fun coversToday(fom: Date?, tom: Date?): Boolean {
        val today = LocalDate.now() //TODO use DateProvider

        if (isAfterByDay(today, fom, true)) {
            return tom == null || isBeforeByDay(today, tom, true)
        }

        return false
    }

    /**
     * Beregner alder ved angitt dato.
     * Kun helt fylte år og helt fylte måneder telles med.
     * (Eksempel: En alder av 5 år, 11 måneder og 27 dager returneres som 5 år og 11 måneder.)
     * Bakgrunnen for dette er at det i pensjonssammenheng opereres med hele måneder;
     * det er den første dag i påfølgende måned som legges til grunn ved f.eks. uttak av pensjon.
     */
    private fun alderDato(foedselDato: LocalDate, dato: LocalDate): PensjonAlderDato =
        with(
            Period.between(
                foedselDato.plusMonths(1).withDayOfMonth(1),
                dato.withDayOfMonth(1)
            )
        ) {
            PensjonAlderDato(
                alder = Alder(aar = this.years, maaneder = this.months),
                dato = dato
            )
        }
}

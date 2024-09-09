package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.AvviksjusteringCti
import java.util.*

/**
 * Angir detaljer rund avkortingen av barnetillegg.
 */
class AvkortingsinformasjonBT : AbstraktAvkortingsinformasjon {

    /**
     * Angir liste over fremtidige perioder.
     */
    var barnetilleggPeriodeListe: MutableList<AbstraktBarnetilleggperiode> = mutableListOf()

    /**
     * Fribeløp for antall barn ved virk.
     */
    var fribelopVedVirk: Int = 0

    /**
     * Hva gjenstår å utbetale for resten av året uten hensyn til justeringsbeløp.
     */
    var restTilUtbetalingForJustering: Double = 0.0

    /**
     * Sum av alle avviksbeløp fra alle tidligere barnetilleggperioder i et år.
     */
    var avviksbelop: Double = 0.0

    /**
     * Nødvendig justering av avkortingsbeløp.
     */
    var justeringsbelopUbegrensetPerAr: Double = 0.0

    /**
     * Det justeringsbeløpet som er praktisk mulig å effektuere.
     */
    var justeringsbelopPerAr: Double = 0.0

    /**
     * Gitt at forventet inntekt ikke endres, hva blir forventet etteroppgjør.
     */
    var forventetEtteroppgjor: Double = 0.0

    /**
     * Angir en konklusjon for behovsprøvingen. Innenfor eller utenfor rammene for justering. Kodeverk K_AVVIKSJUSTERING_T
     */
    var avviksjusteringType: AvviksjusteringCti? = null

    /**
     * Flagg som angir om inntekt er periodisert pga kortere periode med barnetillegg enn uføretrygd.
     */
    var inntektPeriodisert: Boolean = false

    /**
     * Flagg som angir at fribeløp er periodisert pga barnetillegg i deler av året.
     */
    var fribelopPeriodisert: Boolean = false

    val sortertBarnetilleggperiodeliste: MutableList<AbstraktBarnetilleggperiode>
        get() {
            val sortedList = barnetilleggPeriodeListe
            sortedList.sort()
            return sortedList
        }

    val sortertTidligereBarnetilleggperiodeliste: MutableList<TidligereBarnetilleggperiode>
        get() {
            val sortedTidligereList = mutableListOf<TidligereBarnetilleggperiode>()
            for (btp in barnetilleggPeriodeListe) {
                if (btp is TidligereBarnetilleggperiode) {
                    sortedTidligereList.add(btp)
                }
            }
            Collections.sort(sortedTidligereList)
            return sortedTidligereList
        }

    val sortertFremtidigBarnetilleggperiodeliste: MutableList<FremtidigBarnetilleggperiode>
        get() {
            val sortedFremtidigList: MutableList<FremtidigBarnetilleggperiode> = mutableListOf()
            for (btp in barnetilleggPeriodeListe) {
                if (btp is FremtidigBarnetilleggperiode) {
                    sortedFremtidigList.add(btp)
                }
            }
            sortedFremtidigList.sort()
            return sortedFremtidigList
        }

    constructor() : super()

    constructor(avkortingsinformasjonBT: AvkortingsinformasjonBT) : super(avkortingsinformasjonBT) {

        fribelopVedVirk = avkortingsinformasjonBT.fribelopVedVirk
        restTilUtbetalingForJustering = avkortingsinformasjonBT.restTilUtbetalingForJustering
        avviksbelop = avkortingsinformasjonBT.avviksbelop
        justeringsbelopUbegrensetPerAr = avkortingsinformasjonBT.justeringsbelopUbegrensetPerAr
        justeringsbelopPerAr = avkortingsinformasjonBT.justeringsbelopPerAr
        forventetEtteroppgjor = avkortingsinformasjonBT.forventetEtteroppgjor
        inntektPeriodisert = avkortingsinformasjonBT.inntektPeriodisert
        fribelopPeriodisert = avkortingsinformasjonBT.fribelopPeriodisert

        if (avkortingsinformasjonBT.avviksjusteringType != null) {
            avviksjusteringType = AvviksjusteringCti(avkortingsinformasjonBT.avviksjusteringType)
        }

        for (btp in avkortingsinformasjonBT.barnetilleggPeriodeListe) {
            if (btp is TidligereBarnetilleggperiode) {
                barnetilleggPeriodeListe.add(TidligereBarnetilleggperiode(btp))
            }
            if (btp is FremtidigBarnetilleggperiode) {
                barnetilleggPeriodeListe.add(FremtidigBarnetilleggperiode(btp))
            }
        }
    }

    constructor(
            barnetilleggPeriodeListe: MutableList<AbstraktBarnetilleggperiode> = mutableListOf(),
            fribelopVedVirk: Int = 0,
            restTilUtbetalingForJustering: Double = 0.0,
            avviksbelop: Double = 0.0,
            justeringsbelopUbegrensetPerAr: Double = 0.0,
            justeringsbelopPerAr: Double = 0.0,
            forventetEtteroppgjor: Double = 0.0,
            avviksjusteringType: AvviksjusteringCti? = null,
            inntektPeriodisert: Boolean = false,
            fribelopPeriodisert: Boolean = false
    ) {
        this.barnetilleggPeriodeListe = barnetilleggPeriodeListe
        this.fribelopVedVirk = fribelopVedVirk
        this.restTilUtbetalingForJustering = restTilUtbetalingForJustering
        this.avviksbelop = avviksbelop
        this.justeringsbelopUbegrensetPerAr = justeringsbelopUbegrensetPerAr
        this.justeringsbelopPerAr = justeringsbelopPerAr
        this.forventetEtteroppgjor = forventetEtteroppgjor
        this.avviksjusteringType = avviksjusteringType
        this.inntektPeriodisert = inntektPeriodisert
        this.fribelopPeriodisert = fribelopPeriodisert
    }
}

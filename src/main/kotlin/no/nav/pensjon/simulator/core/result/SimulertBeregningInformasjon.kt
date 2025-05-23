package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import java.time.LocalDate

/**
 * Beregningsinformasjon for en beregning av alderspensjon.
 * Dette objektet vil kun finnes på pensjonsperiode dersom det har forekommet en uttaksgradendring
 * i løpet av pensjonsperioden, eller dersom alder er normalderen og bruker har privat AFP.
 * Objektet beskriver beregningen som kommer som følge av en av disse hendelsene.
 */
// no.nav.domain.pensjon.kjerne.simulering.SimulertBeregningsinformasjon
class SimulertBeregningInformasjon {
    //TODO make data class & use val
    var datoFom: LocalDate? = null

    /**
     * Beregnet årlig beløp
     */
        var aarligBeloep: Int? = null

    /**
     * Beregnet månedlig beløp
     */
    var maanedligBeloep: Int? = null

    /**
     * Antall måneder mellom måned bruker er født i og virk på beregningen (Tar verdier i intervallet 1-12, der 1 er mnd etter bruker fyller år)
     */
    var startMaaned: Int? = null

    /**
     * Angir om det er søkers eller avdødes beregning som ligger til grunn for pensjonsperioden.
     */
    var vinnendeBeregning: GrunnlagsrolleEnum? = null

    /**
     * Gjeldende uttaksgrad for denne beregningen.
     */
    var uttakGrad: Double? = null

    /**
     * Beregnet pensjon etter kap20, dersom bruker hadde hatt/har kun AP kap20
     */
    var kapittel20Pensjon: Int? = null

    /**
     * Pensjon beregnet fullt etter Kapittel 20 regler multiplisert med andel av pensjonen som beregnes etter kapittel 20
     * regler. For eksempel så vil andel for kapittel 20 for 1956-kullet være 0.3.
     */
    var vektetKapittel20Pensjon: Int? = null

    /**
     * Beregnet inntektspensjon.
     */
    var inntektspensjon: Int? = null

    /**
     * Beregnet inntektspensjon per måned.
     */
    var inntektspensjonPerMaaned: Int? = null

    /**
     * Beregnet garantipensjon.
     */
    var garantipensjon: Int? = null

    /**
     * Beregnet garantipensjon per måned.
     */
    var garantipensjonPerMaaned: Int? = null

    var garantipensjonssats: Double? = null

    /**
     * Beregnet garantitillegg.
     */
    var garantitillegg: Int? = null

    /**
     * Pensjonsbeholdning før beregning.
     */
    var pensjonBeholdningFoerUttak: Int? = null

    /**
     * Resterende pensjonsbeholdning som følge av beregningen.
     */
    var pensjonBeholdningEtterUttak: Int? = null

    /**
     * Beregnet pensjon etter kap19, dersom bruker hadde hatt/har kun AP kap19
     */
    var kapittel19Pensjon: Int? = null

    /**
     * Pensjon beregnet fullt etter Kapittel 19 regler multiplisert med andel av pensjonen som beregnes etter kapittel 19
     * regler. For eksempel så vil andel for kapittel 19 for 1956-kullet være 0.7.
     */
    var vektetKapittel19Pensjon: Int? = null

    /**
     * Beregnet basispensjonen
     */
    var basispensjon: Int? = null

    /**
     * Beregnet basisgrunnpensjon.
     */
    var basisGrunnpensjon: Double? = null

    /**
     * Beregnet basistilleggspensjon.
     */
    var basisTilleggspensjon: Double? = null

    /**
     * Beregnet basispensjonstillegg.
     */
    var basisPensjonstillegg: Double? = null

    /**
     * Det som er ”igjen” av basispensjon etter utbetalt pensjon. Påkrevd for alle født i 1962 eller tidligere
     */
    var restBasisPensjon: Int? = null

    /**
     * Beregnet grunnpensjon.
     */
    var grunnpensjon: Int? = null

    /**
     * Beregnet grunnpensjon per måned.
     */
    var grunnpensjonPerMaaned: Int? = null

    var grunnpensjonsats: Double? = null

    /**
     * Beregnet tilleggspensjon.
     */
    var tilleggspensjon: Int? = null

    /**
     * Beregnet tilleggspensjon per måned.
     */
    var tilleggspensjonPerMaaned: Int? = null

    /**
     * Beregnet pensjonstillegg.
     */
    var pensjonstillegg: Int? = null

    /**
     * Beregnet pensjonstillegg per måned.
     */
    var pensjonstilleggPerMaaned: Int? = null

    /**
     * Minstenivatillegg individuelt.
     */
    var individueltMinstenivaaTillegg: Int? = null

    /**
     * Minstenivatillegg pensjonistpar.
     */
    var pensjonistParMinstenivaaTillegg: Int? = null

    /**
     * Beregnet skjermingstillegg
     */
    var skjermingstillegg: Int? = null

    /**
     * Benyttet uføregrad ved beregning av skjermingstillegg
     */
    var ufoereGrad: Int? = null

    /**
     * Forholdstall (for gitt alderskull ved gitt alder) som er lagt til grunn for beregningen.
     */
    var forholdstall: Double? = null

    /**
     * Delingstall (for gitt alderskull ved gitt alder) som er lagt til grunn for beregningen.
     */
    var delingstall: Double? = null

    /**
     * Trygdetid anvendt i beregningen for kapittel 19.
     */
    var tt_anv_kap19: Int? = null

    /**
     * Sum av GP, TP og PenT for AP2011 medregnet GJR.
     */
    var apKap19medGJR: Int? = null

    /**
     * Sum av GP, TP og PenT for AP2011 uten GJR.
     */
    var apKap19utenGJR: Int? = null

    /**
     * Brutto per år for gjenlevendetillegg.
     */
    var gjtAP: Int? = null

    /**
     * Brutto per år for gjenlevendetillegg kap19.
     */
    var gjtAPKap19: Int? = null

    /**
     * Trygdetid anvendt i beregningen for kapittel 20.
     */
    var tt_anv_kap20: Int? = null

    /**
     * Antall poengår før 1992.
     */
    var pa_f92: Int? = null

    /**
     * Antall poengår fra og med 1992.
     */
    var pa_e91: Int? = null

    /**
     * Beregnet sluttpoengtall.
     */
    var spt: Double? = null

    /**
     * Endring i basispensjon fra forrige viste beregningsinfomasjon
     */
    var nOkap19: Int? = null

    /**
     * Endring i pensjonsbeholdning fra forrige viste beregningsinfomasjon
     */
    var nOkap20: Int? = null

    /**
     * Satsen brukt ved utregning av minste pensjonsnivå.
     */
    var minstePensjonsnivaSats: Double? = null
}

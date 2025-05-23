package no.nav.pensjon.simulator.core.legacy

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import java.util.*

/**
 * Beregningsinformasjon for en beregning av alderspensjon.
 * Dette objektet vil kun finnes på pensjonsperiode dersom det har forekommet en uttaksgradendring i løpet av pensjonsperioden,
 * eller dersom alder = 67 år og bruker har AFP privat.
 * Objektet beskriver beregningen som kommer som følge av en av disse hendelsene.
 */
// no.nav.domain.pensjon.kjerne.simulering.SimulertBeregningsinformasjon
data class OutputLegacyBeregningInformasjon(
    val datoFom: Date? = null,

    /**
     * Beregnet årlig beløp
     */
    val aarligBeloep: Int? = null,

    /**
     * Beregnet månedlig beløp
     */
    val maanedligBeloep: Int? = null,

    /**
     * Antall måneder mellom måned bruker er født i og virk på beregningen (Tar verdier i intervallet 1-12, der 1 er mnd etter bruker fyller år)
     */
    val startMnd: Int? = null,

    /**
     * Angir om det er søkers eller avdødes beregning som ligger til grunn for pensjonsperioden.
     */
    val vinnendeBeregning: GrunnlagsrolleEnum? = null,

    /**
     * Gjeldende uttaksgrad for denne beregningen.
     */
    val uttaksgrad: Double? = null,

    /**
     * Beregnet pensjon etter kap20, dersom bruker hadde hatt/har kun AP kap20
     */
    val pensjonKap20: Int? = null,

    /**
     * Pensjon beregnet fullt etter Kapittel 20 regler multiplisert med andel av pensjonen som beregnes etter kapittel 20
     * regler. For eksempel så vil andel for kapittel 20 for 1956-kullet være 0.3.
     */
    val pensjonKap20Vektet: Int? = null,

    /**
     * Beregnet inntektspensjon.
     */
    val inntektspensjon: Int? = null,

    /**
     * Beregnet garantipensjon.
     */
    val garantipensjon: Int? = null,

    /**
     * Beregnet garantitillegg.
     */
    val garantitillegg: Int? = null,

    /**
     * Pensjonsbeholdning før beregning.
     */
    val pensjonsbeholdningForUttak: Int? = null,

    /**
     * Resterende pensjonsbeholdning som følge av beregningen.
     */
    val pensjonsbeholdningEtterUttak: Int? = null,

    /**
     * Beregnet pensjon etter kap19, dersom bruker hadde hatt/har kun AP kap19
     */
    val pensjonKap19: Int? = null,

    /**
     * Pensjon beregnet fullt etter Kapittel 19 regler multiplisert med andel av pensjonen som beregnes etter kapittel 19
     * regler. For eksempel så vil andel for kapittel 19 for 1956-kullet være 0.7.
     */
    val pensjonKap19Vektet: Int? = null,

    /**
     * Beregnet basispensjonen
     */
    val basispensjon: Int? = null,

    /**
     * Beregnet basisgrunnpensjon.
     */
    val basisgp: Double? = null,

    /**
     * Beregnet basistilleggspensjon.
     */
    val basistp: Double? = null,

    /**
     * Beregnet basispensjonstillegg.
     */
    val basispt: Double? = null,

    /**
     * Det som er ”igjen” av basispensjon etter utbetalt pensjon. Påkrevd for alle født i 1962 eller tidligere
     */
    val restbasispensjon: Int? = null,

    /**
     * Beregnet grunnpensjon.
     */
    val gp: Int? = null,

    /**
     * Beregnet tilleggspensjon.
     */
    val tp: Int? = null,

    /**
     * Beregnet pensjonstillegg.
     */
    val pt: Int? = null,

    /**
     * Minstenivatillegg individuelt.
     */
    val minstenivaTilleggIndividuelt: Int? = null,

    /**
     * Minstenivatillegg pensjonistpar.
     */
    val minstenivaTilleggPensjonistpar: Int? = null,

    /**
     * Beregnet skjermingstillegg
     */
    val skjermt: Int? = null,

    /**
     * Benyttet uføregrad ved beregning av skjermingstillegg
     */
    val ufg: Int? = null,

    /**
     * Forholdstall (for gitt alderskull ved gitt alder) som er lagt til grunn for beregningen.
     */
    val forholdstall: Double? = null,

    /**
     * Delingstall (for gitt alderskull ved gitt alder) som er lagt til grunn for beregningen.
     */
    val delingstall: Double? = null,

    /**
     * Trygdetid anvendt i beregningen for kapittel 19.
     */
    val tt_anv_kap19: Int? = null,

    /**
     * Sum av GP, TP og PenT for AP2011 medregnet GJR.
     */
    val apKap19medGJR: Int? = null,

    /**
     * Sum av GP, TP og PenT for AP2011 uten GJR.
     */
    val apKap19utenGJR: Int? = null,

    /**
     * Brutto per år for gjenlevendetillegg.
     */
    val gjtAP: Int? = null,

    /**
     * Brutto per år for gjenlevendetillegg kap19.
     */
    val gjtAPKap19: Int? = null,

    /**
     * Trygdetid anvendt i beregningen for kapittel 20.
     */
    val tt_anv_kap20: Int? = null,

    /**
     * Antall poengår før 1992.
     */
    val pa_f92: Int? = null,

    /**
     * Antall poengår fra og med 1992.
     */
    val pa_e91: Int? = null,

    /**
     * Beregnet sluttpoengtall.
     */
    val spt: Double? = null,

    /**
     * Endring i basispensjon fra forrige viste beregningsinfomasjon
     */
    val nOkap19: Int? = null,

    /**
     * Endring i pensjonsbeholdning fra forrige viste beregningsinfomasjon
     */
    val nOkap20: Int? = null,

    /**
     * Satsen brukt ved utregning av minste pensjonsnivå.
     */
    val minstePensjonsnivaaSats: Double? = null
)

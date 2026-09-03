package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.YtelseskomponentTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Beholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantipensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Pensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.privatAfp
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.getMonthBetween
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import java.time.LocalDate
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AfpPrivatLivsvarig as PrivatAfp

/**
 * PEN: no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011ResultatMapper
 */
object SimulatorOutputMapper {

    fun mapToSimulatorOutput(
        simuleringSpec: SimuleringSpec,
        soekerGrunnlag: Persongrunnlag
    ) =
        SimulatorOutput().apply {
            this.epsHarInntektOver2G = simuleringSpec.epsHarInntektOver2G
            this.epsHarPensjon = simuleringSpec.epsHarPensjon
            this.sivilstand = soekerGrunnlag.personDetaljListe[0].sivilstandTypeEnum
                ?: throw RuntimeException("Undefined sivilstand")
        }

    fun simulertPrivatAfpPeriode(
        aarligBeloep: Int,
        resultat: BeregningsResultatAfpPrivat,
        alder: Int?
    ): PrivatAfpPeriode {
        val privatAfpUnderUtbetaling = resultat.pensjonUnderUtbetaling
        val ytelseKomponentListe = privatAfpUnderUtbetaling?.ytelseskomponenter.orEmpty()
        val privatAfp: PrivatAfp? = resultat.privatAfp()
        val afpKronetillegg = firstYtelseOfType(ytelseKomponentListe, YtelseskomponentTypeEnum.AFP_KRONETILLEGG)
        val afpKompensasjonstillegg = firstYtelseOfType(ytelseKomponentListe, YtelseskomponentTypeEnum.AFP_KOMP_TILLEGG)

        return PrivatAfpPeriode(
            alderAar = alder,
            aarligBeloep = aarligBeloep,
            maanedligBeloep = privatAfpUnderUtbetaling?.totalbelopNetto,
            livsvarig = privatAfp?.netto,
            kronetillegg = afpKronetillegg?.netto ?: 0,
            kompensasjonstillegg = afpKompensasjonstillegg?.netto ?: 0,
            afpForholdstall = privatAfp?.afpForholdstall,
            justeringBeloep = privatAfp?.justeringsbelop,
            afpOpptjening = resultat.afpPrivatBeregning?.afpOpptjening?.totalbelop?.toInt() ?: 0
        )
    }

    // SimuleringEtter2011ResultatMapper.mapToSimulertBeregningsinformasjon
    fun mapToSimulertBeregningsinformasjon(
        kravhode: Kravhode,
        beregningResultat: AbstraktBeregningsResultat,
        simulertAlderspensjon: SimulertAlderspensjon,
        foedselsdato: LocalDate,
        knekkpunkt: LocalDate
    ) =
        SimulertBeregningInformasjon().apply {
            val beregningsresultatKapittel19: BeregningsResultatAlderspensjon2011?
            val beregningsresultatKapittel20: BeregningsResultatAlderspensjon2025?
            val beregningsinfo: BeregningsInformasjon?

            if (erAp2011Beregning(kravhode)) {
                beregningsresultatKapittel19 = beregningResultat as? BeregningsResultatAlderspensjon2011
                beregningsresultatKapittel20 = null
                beregningsinfo = beregningsresultatKapittel19?.beregningsInformasjonKapittel19
            } else if (erAp2016Beregning(kravhode)) {
                val resultat2016 = beregningResultat as? BeregningsResultatAlderspensjon2016
                beregningsresultatKapittel19 = resultat2016?.beregningsResultat2011
                beregningsresultatKapittel20 = resultat2016?.beregningsResultat2025
                beregningsinfo = beregningsresultatKapittel19?.beregningsInformasjonKapittel19
            } else {
                beregningsresultatKapittel19 = null
                beregningsresultatKapittel20 = beregningResultat as? BeregningsResultatAlderspensjon2025
                beregningsinfo = beregningsresultatKapittel20?.beregningsInformasjonKapittel20
            }

            val beregningsinfoKapittel19: BeregningsInformasjon? =
                beregningsresultatKapittel19?.beregningsInformasjonKapittel19
            val pensjon: PensjonUnderUtbetaling? = beregningResultat.pensjonUnderUtbetaling
            val beregningKapittel20: AldersberegningKapittel20? = beregningsresultatKapittel20?.beregningKapittel20
            val beregningKapittel19: AldersberegningKapittel19? = beregningsresultatKapittel19?.beregningKapittel19

            if (beregningsresultatKapittel20 != null) {
                pensjon?.gjenlevendetilleggAP?.let {
                    this.apKap19medGJR = it.apKap19MedGJR
                    this.apKap19utenGJR = it.apKap19UtenGJR
                    this.gjtAP = it.bruttoPerAr.toInt()
                    this.gjtAPPerMaaned = it.brutto
                }

                val pensjonKapittel20: Int =
                    beregningsresultatKapittel20.pensjonUnderUtbetaling?.totalbelopNettoAr?.toInt() ?: 0
                this.kapittel20Pensjon = pensjonKapittel20
                this.vektetKapittel20Pensjon = (pensjonKapittel20 * simulertAlderspensjon.kapittel20Andel).toInt()
                this.pensjonBeholdningEtterUttak =
                    firstPensjonsbeholdning(beregningKapittel20?.beholdninger?.beholdninger.orEmpty())?.totalbelop?.toInt()
            }

            if (beregningsresultatKapittel19 != null) { // ref. jira.adeo.no/browse/PEB-442
                pensjon?.gjenlevendetilleggAPKap19?.let {
                    this.gjtAPKap19 = it.bruttoPerAr.toInt()
                    this.gjtAPKap19PerMaaned = it.brutto
                }
            }

            beregningsinfoKapittel19?.let {
                this.vinnendeBeregning =
                    if (it.gjenlevenderettAnvendt) GrunnlagsrolleEnum.AVDOD
                    else GrunnlagsrolleEnum.SOKER
            }

            firstYtelseOfType(pensjon?.ytelseskomponenter.orEmpty(), YtelseskomponentTypeEnum.SKJERMT)?.let {
                this.skjermingstillegg = it.bruttoPerAr.toInt()
                this.skjermingstilleggPerMaaned = it.brutto

                if (it is Skjermingstillegg) {
                    this.ufoereGrad = it.ufg
                }
            }

            beregningsresultatKapittel19?.let {
                val totalbelopNettoAr = it.pensjonUnderUtbetaling?.totalbelopNettoAr?.toInt() ?: 0
                this.kapittel19Pensjon = totalbelopNettoAr
                this.vektetKapittel19Pensjon = (totalbelopNettoAr * simulertAlderspensjon.kapittel19Andel).toInt()
            }

            beregningKapittel19?.basispensjon?.let {
                this.basispensjon = it.totalbelop.toInt()
                this.basisGrunnpensjon = it.gp?.bruttoPerAr ?: 0.0
                this.basisTilleggspensjon = it.tp?.bruttoPerAr ?: 0.0
                this.basisPensjonstillegg = it.pt?.bruttoPerAr ?: 0.0
                this.minstePensjonsnivaSats = it.pt?.minstepensjonsnivaSats ?: 0.0
            }

            beregningKapittel19?.restpensjon?.let {
                val basisgrunnpensjon = it.gp?.bruttoPerAr?.toInt() ?: 0
                val basistilleggspensjon = it.tp?.bruttoPerAr?.toInt() ?: 0
                this.restBasisPensjon = basisgrunnpensjon + basistilleggspensjon
            }

            this.tt_anv_kap19 = beregningKapittel19?.tt_anv

            beregningKapittel20?.let {
                this.delingstall = it.delingstall
                this.tt_anv_kap20 = it.tt_anv
                this.garantipensjonssats = (it.beholdningerForForsteuttak ?: it.beholdninger)
                    ?.beholdninger?.filterIsInstance<Garantipensjonsbeholdning>()
                    ?.map(Garantipensjonsbeholdning::sats)?.firstOrNull()
            }

            beregningsinfo?.spt?.let {
                this.pa_f92 = it.poengrekke?.pa_f92
                this.pa_e91 = it.poengrekke?.pa_e91
                this.spt = it.pt
            }

            this.datoFom = knekkpunkt
            this.startMaaned = getMonthsBetweenInRange1To12(foedselsdato, knekkpunkt)
            this.aarligBeloep = pensjon?.totalbelopNettoAr?.toInt() ?: 0
            this.maanedligBeloep = pensjon?.totalbelopNetto
            this.inntektspensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.IP)
            this.inntektspensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.IP)
            this.garantipensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.GAP)
            this.garantipensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.GAP)
            this.garantitillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.GAT)
            this.garantitilleggPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.GAT)
            this.grunnpensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.GP)
            this.grunnpensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.GP)
            this.grunnpensjonsats = pensjon?.grunnpensjon?.pSats_gp
            this.tilleggspensjon = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.TP)
            this.tilleggspensjonPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.TP)
            this.pensjonstillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.PT)
            this.pensjonstilleggPerMaaned = bruttoPerMaaned(pensjon, YtelseskomponentTypeEnum.PT)
            this.minstePensjonsnivaSats = pensjon?.pensjonstillegg?.minstepensjonsnivaSats
            this.individueltMinstenivaaTillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.MIN_NIVA_TILL_INDV)
            this.pensjonistParMinstenivaaTillegg = bruttoPerAar(pensjon, YtelseskomponentTypeEnum.MIN_NIVA_TILL_PPAR)
            this.forholdstall = beregningsinfo?.forholdstallUttak
            this.uttakGrad = beregningResultat.uttaksgrad.toDouble()
        }

    private fun erAp2011Beregning(kravhode: Kravhode): Boolean =
        kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_G_OPPTJ

    private fun erAp2016Beregning(kravhode: Kravhode): Boolean =
        kravhode.regelverkTypeEnum == RegelverkTypeEnum.N_REG_G_N_OPPTJ

    private fun getMonthsBetweenInRange1To12(firstDate: LocalDate, secondDate: LocalDate): Int {
        val monthsBetween = getMonthBetween(firstDate, secondDate) % MAANEDER_PER_AAR
        return if (monthsBetween == 0) MAANEDER_PER_AAR else monthsBetween
    }

    // Specific variant of ArligInformasjonListeUtils.findElementOfType
    // Duplicate in RegdomOpprettOutputHelper
    private fun firstPensjonsbeholdning(list: List<Beholdning>): Pensjonsbeholdning? =
        list.firstOrNull { BeholdningtypeEnum.PEN_B == it.beholdningsTypeEnum } as? Pensjonsbeholdning

    // Specific variant of ArligInformasjonListeUtils.findElementOfType
    private fun firstYtelseOfType(list: List<Ytelseskomponent>, type: YtelseskomponentTypeEnum): Ytelseskomponent? =
        list.firstOrNull { it.ytelsekomponentTypeEnum == type }

    private fun bruttoPerAar(pensjon: PensjonUnderUtbetaling?, ytelseType: YtelseskomponentTypeEnum) =
        firstYtelseOfType(pensjon?.ytelseskomponenter.orEmpty(), ytelseType)?.bruttoPerAr?.toInt()

    private fun bruttoPerMaaned(pensjon: PensjonUnderUtbetaling?, ytelseType: YtelseskomponentTypeEnum) =
        firstYtelseOfType(pensjon?.ytelseskomponenter.orEmpty(), ytelseType)?.brutto
}
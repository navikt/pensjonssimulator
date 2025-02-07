package no.nav.pensjon.simulator.core.beregn

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning.Ytelseskomponent
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.*
import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Garantipensjonsbeholdning
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.*
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsprovAlderspensjonResultat
import no.nav.pensjon.simulator.core.person.eps.EpsUtil.epsMottarPensjon
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

// PEN: Beregning part of
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.VilkarsprovOgBeregnAlderHelper
@Component
class AlderspensjonBeregner(private val context: SimulatorContext) {

    // VilkarsprovOgBeregnAlderHelper.beregnAP
    fun beregnAlderspensjon(
        kravhode: Kravhode,
        vedtakListe: MutableList<VilkarsVedtak>,
        virkningDato: LocalDate,
        forholdstallUtvalg: ForholdstallUtvalg,
        delingstallUtvalg: DelingstallUtvalg,
        sisteAldersberegning2011: SisteBeregning?,
        privatAfp: AfpLivsvarig?,
        garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
        simuleringSpec: SimuleringSpec,
        sakId: Long?,
        isFoersteUttak: Boolean,
        ignoreAvslag: Boolean
    ): AbstraktBeregningsResultat =
        if (isFoersteUttak) {
            val request = beregningCommonSpec(
                kravhode, vedtakListe, virkningDato, forholdstallUtvalg, delingstallUtvalg,
                privatAfp, garantitilleggBeholdningGrunnlag, simuleringSpec
            )

            beregnFoersteUttak(request, sakId, ignoreAvslag)
        } else { // revurdering av alderspensjon
            val request = revurderingCommonSpec(
                kravhode, vedtakListe, virkningDato, forholdstallUtvalg, delingstallUtvalg,
                sisteAldersberegning2011!!, privatAfp, garantitilleggBeholdningGrunnlag, simuleringSpec
            )

            beregnRevurdering(request, sakId)
        }

    // VilkarsprovOgBeregnAlderHelper.beregnForsteUttak
    //@Throws(PEN222BeregningstjenesteFeiletException::class)
    private fun beregnFoersteUttak(
        spec: AlderspensjonBeregningCommonSpec,
        sakId: Long?,
        ignoreAvslag: Boolean
    ): AbstraktBeregningsResultat {
        val regelverkType: RegelverkTypeEnum =
            spec.kravhode?.regelverkTypeEnum ?: throw RuntimeException("Undefined regelverkTypeEnum")

        // SIMDOM-ADD for 'simuler folketrygdbeholdning' (da avslag ignoreres):
        if (ignoreAvslag && spec.vilkarsvedtakListe.any { it.anbefaltResultatEnum != innvilgetResultat }) {
            spec.vilkarsvedtakListe.replaceAll(::innvilgetVedtak)
        }
        // end SIMDOM-ADD

        return when (regelverkType) {
            RegelverkTypeEnum.N_REG_G_OPPTJ ->
                context.beregnAlderspensjon2011FoersteUttak(beregning2011Request(spec), sakId)

            RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                context.beregnAlderspensjon2016FoersteUttak(beregning2016Request(spec), sakId)

            RegelverkTypeEnum.N_REG_N_OPPTJ ->
                // NB: No special handling for eksterne ordninger (tjenestepensjonsleverandører)
                context.beregnAlderspensjon2025FoersteUttak(beregning2025Request(spec), sakId)

            else -> throw RuntimeException("Unexpected regelverkType: $regelverkType")
        }
    }

    // VilkarsprovOgBeregnAlderHelper.beregnRevurdering
    private fun beregnRevurdering(
        spec: AlderspensjonRevurderingCommonSpec,
        sakId: Long?
    ): AbstraktBeregningsResultat {
        val regelverkType: RegelverkTypeEnum =
            spec.kravhode?.regelverkTypeEnum ?: throw RuntimeException("Undefined regelverkTypeEnum")

        return when (regelverkType) {
            RegelverkTypeEnum.N_REG_G_OPPTJ ->
                context.revurderAlderspensjon2011(revurdering2011Request(spec), sakId)

            RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                context.revurderAlderspensjon2016(revurdering2016Request(spec), sakId)

            RegelverkTypeEnum.N_REG_N_OPPTJ ->
                // NB: No special handling for eksterne ordninger (tjenestepensjonsleverandører)
                context.revurderAlderspensjon2025(revurdering2025Request(spec), sakId)

            else -> throw RuntimeException("Unexpected regelverkType: $regelverkType")
        }
    }

    private companion object {
        private const val EPS_PEN_PERSON_ID = -2L
        private val innvilgetResultat = VedtakResultatEnum.INNV

        // VilkarsprovOgBeregnAlderHelper.createInnvilgetVilkarsvedtak
        private fun innvilgetVedtak(kravlinje: Kravlinje, virkningFom: LocalDate): VilkarsVedtak {
            val vedtakResultat = VedtakResultatEnum.INNV

            return VilkarsVedtak().apply {
                this.anbefaltResultatEnum = vedtakResultat
                this.vilkarsvedtakResultatEnum = vedtakResultat
                this.virkFom = virkningFom.toNorwegianDateAtNoon()
                this.virkTom = null
                this.kravlinje = kravlinje
                this.kravlinjeTypeEnum = kravlinje.kravlinjeTypeEnum
                this.penPerson = kravlinje.relatertPerson
                this.forsteVirk = virkningFom.toNorwegianDateAtNoon()
            }.also {
                it.finishInit()
            }
        }

        // SIMDOM-ADD
        private fun innvilgetVedtak(source: VilkarsVedtak) =
            /* TODO check if copy needed:
        VilkarsVedtak(source).also {
            it.anbefaltResultatEnum = innvilgetResultat
            it.vilkarsvedtakResultatEnum = innvilgetResultat
            it.begrunnelseEnum = null
            it.merknadListe = mutableListOf()
        }*/
            source.apply {
                anbefaltResultatEnum = innvilgetResultat
                vilkarsvedtakResultatEnum = innvilgetResultat
                begrunnelseEnum = null
                merknadListe = mutableListOf()
            } //.also { log.warn { "MUTATED VilkarsVedtak - innvilget" } }

        // PEN: VilkarsprovOgBeregnAlderHelper.buildBeregnApRequest
        private fun beregningCommonSpec(
            kravhode: Kravhode,
            vedtakListe: MutableList<VilkarsVedtak>,
            virkningFom: LocalDate,
            forholdstallUtvalg: ForholdstallUtvalg,
            delingstallUtvalg: DelingstallUtvalg,
            livsvarigAfp: AfpLivsvarig?,
            garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
            simuleringSpec: SimuleringSpec
        ): AlderspensjonBeregningCommonSpec {
            val epsMottarPensjon = epsMottarPensjon(simuleringSpec)
            val paavirkendeYtelseInfo = if (epsMottarPensjon) paavirkendeYtelseInfo(virkningFom, kravhode) else null

            return AlderspensjonBeregningCommonSpec(
                kravhode = kravhode,
                vilkarsvedtakListe = vedtakListe,
                infoPavirkendeYtelse = paavirkendeYtelseInfo,
                virkFom = virkningFom,
                forholdstallUtvalg = forholdstallUtvalg,
                delingstallUtvalg = delingstallUtvalg,
                epsMottarPensjon = epsMottarPensjon,
                afpLivsvarig = livsvarigAfp,
                garantitilleggsbeholdningGrunnlag = garantitilleggBeholdningGrunnlag
            )
        }

        private fun beregning2011Request(spec: AlderspensjonBeregningCommonSpec) =
            BeregnAlderspensjon2011ForsteUttakRequest().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = spec.vilkarsvedtakListe
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                virkTom = null // set to null in legacy SimuleringEtter2011Context.beregnAlderspensjon2011ForsteUttak
                forholdstallUtvalg = spec.forholdstallUtvalg
                ektefellenMottarPensjon = spec.epsMottarPensjon
                afpLivsvarig = spec.afpLivsvarig
            }

        private fun beregning2016Request(spec: AlderspensjonBeregningCommonSpec) =
            BeregnAlderspensjon2016ForsteUttakRequest().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = spec.vilkarsvedtakListe
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                epsMottarPensjon = spec.epsMottarPensjon
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }

        private fun beregning2025Request(spec: AlderspensjonBeregningCommonSpec) =
            BeregnAlderspensjon2025ForsteUttakRequest().apply {
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                kravhode = spec.kravhode
                vilkarsvedtakListe = spec.vilkarsvedtakListe
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                epsMottarPensjon = spec.epsMottarPensjon
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }

        private fun revurderingCommonSpec(
            kravhode: Kravhode,
            vedtakListe: MutableList<VilkarsVedtak>,
            virkningFom: LocalDate,
            forholdstallUtvalg: ForholdstallUtvalg,
            delingstallUtvalg: DelingstallUtvalg,
            sisteAlderspensjonBeregning2011: SisteBeregning,
            livsvarigAfp: AfpLivsvarig?,
            garantitilleggBeholdningGrunnlag: GarantitilleggsbeholdningGrunnlag,
            simuleringSpec: SimuleringSpec
        ): AlderspensjonRevurderingCommonSpec {
            val epsMottarPensjon = epsMottarPensjon(simuleringSpec)
            val paavirkendeYtelseInfo = if (epsMottarPensjon) paavirkendeYtelseInfo(virkningFom, kravhode) else null

            return AlderspensjonRevurderingCommonSpec(
                kravhode = kravhode,
                vilkarsvedtakListe = vedtakListe,
                infoPavirkendeYtelse = paavirkendeYtelseInfo,
                epsMottarPensjon = epsMottarPensjon,
                forholdstallUtvalg = forholdstallUtvalg,
                delingstallUtvalg = delingstallUtvalg,
                virkFom = virkningFom,
                forrigeAldersberegning = sisteAlderspensjonBeregning2011,
                afpLivsvarig = livsvarigAfp,
                garantitilleggsbeholdningGrunnlag = garantitilleggBeholdningGrunnlag
            )
        }

        // VilkarsprovOgBeregnAlderHelper.createFerdigApKravlinje
        private fun alderspensjonKravlinje(gjelderPerson: PenPerson) =
            Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AP
                relatertPerson = gjelderPerson
                // NB apparently not used (no kravlinjeStatus field): setKravlinjeStatus(KravlinjeStatus.FERDIG)
            }

        // PEN: VilkarsprovOgBeregnAlderHelper.createInfoPavirkendeYtelse
        private fun paavirkendeYtelseInfo(virkningDato: LocalDate, kravhode: Kravhode): InfoPavirkendeYtelse {
            var epsGrunnlag = kravhode.findPersongrunnlag(PenPerson(EPS_PEN_PERSON_ID))
            val grunnlagRoller =
                EnumSet.of(GrunnlagsrolleEnum.EKTEF, GrunnlagsrolleEnum.SAMBO, GrunnlagsrolleEnum.PARTNER)

            // Hvis bruker har en "riktig" EPS på sitt persongrunnlag hentes denne
            if (epsGrunnlag == null) {
                var grunnlagsrolle: GrunnlagsrolleEnum? = null

                for (persongrunnlag in kravhode.persongrunnlagListe) {
                    for (persondetalj in persongrunnlag.personDetaljListe) {
                        val rolle = persondetalj.grunnlagsrolleEnum

                        if (grunnlagRoller.contains(rolle)) {
                            grunnlagsrolle = rolle
                        }
                    }
                }

                epsGrunnlag = grunnlagsrolle?.let { kravhode.hentPersongrunnlagForRolle(it, false) }
            }

            if (epsGrunnlag == null) {
                return InfoPavirkendeYtelse()
            }

            val kravlinje = alderspensjonKravlinje(epsGrunnlag.penPerson!!)
            val vedtak = innvilgetVedtak(kravlinje, virkningDato)
            return InfoPavirkendeYtelse().also { it.vilkarsvedtakEPSListe.add(vedtak) }
        }

        private fun revurdering2011Request(spec: AlderspensjonRevurderingCommonSpec) =
            RevurderingAlderspensjon2011Request().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = Vector(spec.vilkarsvedtakListe)
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                epsMottarPensjon = spec.epsMottarPensjon
                forholdstallUtvalg = spec.forholdstallUtvalg
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                virkTom = null
                forrigeAldersBeregning = spec.forrigeAldersberegning as? SisteAldersberegning2011
                afpLivsvarig = spec.afpLivsvarig
            }

        private fun revurdering2016Request(spec: AlderspensjonRevurderingCommonSpec) =
            RevurderingAlderspensjon2016Request().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = ArrayList(spec.vilkarsvedtakListe)
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                epsMottarPensjon = spec.epsMottarPensjon
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                forrigeAldersBeregning = spec.forrigeAldersberegning as? SisteAldersberegning2016
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }.also {
                it.vilkarsvedtakListe.forEach(::prepareVedtak2016ForReglerCall)
                it.forrigeAldersBeregning?.let(::clearFormelMaps)
            }

        private fun revurdering2025Request(spec: AlderspensjonRevurderingCommonSpec) =
            RevurderingAlderspensjon2025Request().apply {
                kravhode = spec.kravhode
                vilkarsvedtakListe = ArrayList(spec.vilkarsvedtakListe)
                infoPavirkendeYtelse = spec.infoPavirkendeYtelse
                epsMottarPensjon = spec.epsMottarPensjon
                forholdstallUtvalg = spec.forholdstallUtvalg
                delingstallUtvalg = spec.delingstallUtvalg
                virkFom = spec.virkFom?.toNorwegianDateAtNoon()
                sisteAldersBeregning2011 = spec.forrigeAldersberegning as? SisteAldersberegning2011 // NB: 2011
                afpLivsvarig = spec.afpLivsvarig
                garantitilleggsbeholdningGrunnlag = spec.garantitilleggsbeholdningGrunnlag
            }.also {
                it.vilkarsvedtakListe.forEach(::prepareVedtak2025ForReglerCall)
                it.sisteAldersBeregning2011?.let(::prepareBeregningForReglerCall)
            }

        private fun prepareBeregningForReglerCall(beregning: SisteAldersberegning2011) {
            with(beregning) {
                pensjonUnderUtbetaling?.ytelseskomponenter?.forEach(Ytelseskomponent::roundNettoPerAr)
                beholdninger?.beholdninger?.forEach(::prepareBeholdningForReglerCall)
            }
        }

        private fun prepareBeholdningForReglerCall(beholdning: Beholdning) {
            when (beholdning) {
                is Pensjonsbeholdning -> beholdning.opptjening?.finishInit()
                is Garantipensjonsbeholdning -> beholdning.clearPensjonsbeholdning()
            }
        }

        private fun prepareVedtak2016ForReglerCall(vedtak: VilkarsVedtak) {
            with(vedtak) {
                finishInit()
                val alderspensjonVilkaarResultat = vilkarsprovresultat as? VilkarsprovAlderspensjonResultat ?: return

                alderspensjonVilkaarResultat.vilkarsprovInformasjon?.pensjonVedUttak?.ytelseskomponenter?.forEach(
                    Ytelseskomponent::roundNettoPerAr
                )

                val beregningResultat =
                    alderspensjonVilkaarResultat.beregningVedUttak as? BeregningsResultatAlderspensjon2016 ?: return

                with(beregningResultat) {
                    pensjonUnderUtbetaling?.ytelseskomponenter?.forEach(Ytelseskomponent::roundNettoPerAr)
                }
            }
        }

        private fun prepareVedtak2025ForReglerCall(vedtak: VilkarsVedtak) {
            with(vedtak) {
                finishInit()
                val alderspensjonVilkaarResultat = vilkarsprovresultat as? VilkarsprovAlderspensjonResultat ?: return

                alderspensjonVilkaarResultat.vilkarsprovInformasjon?.pensjonVedUttak?.ytelseskomponenter?.forEach(
                    Ytelseskomponent::roundNettoPerAr
                )

                val beregningResultat =
                    alderspensjonVilkaarResultat.beregningVedUttak as? BeregningsResultatAlderspensjon2025 ?: return

                with(beregningResultat) {
                    pensjonUnderUtbetaling?.ytelseskomponenter?.forEach(Ytelseskomponent::roundNettoPerAr)
                    beregningKapittel20?.beholdninger?.beholdninger?.forEach(::prepareBeholdningForReglerCall)
                    beregningKapittel20?.beholdningerForForsteuttak?.beholdninger?.forEach(::prepareBeholdningForReglerCall)
                    beregningsInformasjonKapittel20?.clearDelingstall()
                }
            }
        }

        private fun clearFormelMaps(beregning: SisteAldersberegning2016) {
            beregning.basispensjon?.let(::clearFormelMap)
            beregning.restpensjon?.let(::clearFormelMap)
            /* TODO check if relevant:
        beregning.pensjonUnderUtbetaling?.let(::clearFormelMaps)
        beregning.pensjonUnderUtbetaling2011?.let(::clearFormelMaps)
        beregning.pensjonUnderUtbetaling2011UtenGJR?.let(::clearFormelMaps)
        beregning.pensjonUnderUtbetaling2025?.let(::clearFormelMaps)
        beregning.pensjonUnderUtbetaling2025AltKonv?.let(::clearFormelMaps)*/
        }
        /* TODO: it as? FormelProvider = null always?
    private fun clearFormelMaps(pensjon: PensjonUnderUtbetaling) {
        pensjon.ytelseskomponenter.mapNotNull { it as? FormelProvider }.forEach(::clearFormelMap)
    }*/

        private fun clearFormelMap(pensjon: Basispensjon) {
            pensjon.tp?.formelMap?.clear()
        }
    }
}

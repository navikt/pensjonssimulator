package no.nav.pensjon.simulator.core.vilkaar

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.beregn.Tuple2
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2011
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.SisteAldersberegning2016
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.LandkodeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.VedtakResultatEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2011Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2016Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovAlderpensjon2025Request
import no.nav.pensjon.simulator.core.domain.regler.to.VilkarsprovRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.asLegacyPrivatAfp
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isAfterByDay
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.ubetingetPensjoneringDato
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import org.springframework.stereotype.Component
import java.time.LocalDate

// PEN: vilkårsprøving part of
// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.VilkarsprovOgBeregnAlderHelper
@Component
class Vilkaarsproever(private val context: SimulatorContext) {

    // PEN: VilkarsprovOgBeregnAlderHelper.vilkarsprovKrav
    fun vilkaarsproevKrav(spec: VilkaarsproevingSpec): Tuple2<MutableList<VilkarsVedtak>, Kravhode> {
        val vedtakListe = vilkaarsproevAlderspensjon(spec)

        createGjenlevenderettVedtakIfGjenlevenderettKravlinjePresent(
            spec.virkningFom,
            spec.kravhode,
            vedtakListe
        )

        postprocessVedtakAndKravhode(
            vedtakListe,
            spec.kravhode,
            spec.soekerFoersteVirkning,
            spec.avdoedFoersteVirkning
        )

        // NB TODO:
        // not necesseary at all, but design dictates us to return kravhode as well:
        return Tuple2(vedtakListe, spec.kravhode)
    }

    fun innvilgetVedtak(kravlinje: Kravlinje?, virkningFom: LocalDate): VilkarsVedtak =
        manueltVedtak(kravlinje, virkningFom).also {
            it.forsteVirk = virkningFom.toNorwegianDateAtNoon()
            it.vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
        }

    // VilkarsprovOgBeregnAlderHelper.vilkarsprovAlderInRegelmotor
    private fun vilkaarsproevAlderspensjon(spec: VilkaarsproevingSpec): MutableList<VilkarsVedtak> {
        val soekerGrunnlag = spec.kravhode.hentPersongrunnlagForSoker()
        val vedtakListe: MutableList<VilkarsVedtak> = vilkaarsproevAlderspensjon(spec, soekerGrunnlag)

        if (spec.ignoreAvslag) {
            log.debug { "vilkaarsproevAlderspensjon ignoreAvslag = TRUE" }
        } else {
            AvslagHandler.handleAvslag(vedtakListe)
        }

        return vedtakListe
    }

    // Part of VilkarsprovOgBeregnAlderHelper.vilkarsprovAlderInRegelmotor
    private fun vilkaarsproevAlderspensjon(
        spec: VilkaarsproevingSpec,
        soekerGrunnlag: Persongrunnlag
    ): MutableList<VilkarsVedtak> =
        if (isAfterByDay(spec.virkningFom, ubetingetPensjoneringDato(soekerGrunnlag.fodselsdato!!), true))
            context.vilkaarsproevUbetingetAlderspensjon(ubetingetVilkaarsproevingRequest(spec), spec.sakId)
        else
            vilkaarsproevBetingetAlderspensjon(spec)

    // Part of VilkarsprovOgBeregnAlderHelper.vilkarsprovAlderInRegelmotor
    private fun vilkaarsproevBetingetAlderspensjon(spec: VilkaarsproevingSpec): MutableList<VilkarsVedtak> =
        when (spec.kravhode.regelverkTypeEnum) {
            RegelverkTypeEnum.N_REG_G_OPPTJ ->
                context.vilkaarsproevAlderspensjon2011(vilkaarsproeving2011Request(spec), spec.sakId)

            RegelverkTypeEnum.N_REG_G_N_OPPTJ ->
                context.vilkaarsproevAlderspensjon2016(vilkaarsproeving2016Request(spec), spec.sakId)

            RegelverkTypeEnum.N_REG_N_OPPTJ ->
                context.vilkaarsproevAlderspensjon2025(vilkaarsproeving2025Request(spec), spec.sakId)

            else -> mutableListOf()
        }

    private companion object {
        private val log = KotlinLogging.logger {}

        // VilkarsprovOgBeregnAlderHelper.postProcessVilkarsvedtakAndKravHode
        private fun postprocessVedtakAndKravhode(
            vedtakListe: List<VilkarsVedtak>,
            kravhode: Kravhode,
            soekerFoersteVirkning: LocalDate,
            avdoedFoersteVirkning: LocalDate?
        ) {
            vedtakListe.forEach {
                it.kravlinje!!.kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                it.kravlinje!!.land = LandkodeEnum.NOR
                it.vilkarsvedtakResultatEnum = it.anbefaltResultatEnum

                val localFoersteVirk: LocalDate =
                    if (avdoedFoersteVirkning != null && isGjenlevenderettighet(it.kravlinjeTypeEnum))
                        avdoedFoersteVirkning
                    else
                        soekerFoersteVirkning

                val foersteVirk = localFoersteVirk.toNorwegianDateAtNoon()
                it.forsteVirk = foersteVirk // NB: modified in VilkarsprovOgBeregnAlderHelper 2023-08-30
                // Not setting kravlinjeForsteVirk, ref. github.com/navikt/pensjon-pen/pull/14573
                it.finishInit()
            }

            kravhode.kravlinjeListe.forEach {
                it.kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                it.land = LandkodeEnum.NOR
            }
        }

        // VilkarsprovOgBeregnAlderHelper.createGJRVilkarsvedtakIfGJRKravlinjePresent
        private fun createGjenlevenderettVedtakIfGjenlevenderettKravlinjePresent(
            virkningDato: LocalDate,
            kravhode: Kravhode,
            vedtakListe: MutableList<VilkarsVedtak>
        ) {
            kravlinjeForGjenlevenderett(kravhode.kravlinjeListe)?.let {
                addGjenlevenderettVedtak(virkningDato, vedtakListe, it)
            }
        }

        // Extracted from VilkarsprovOgBeregnAlderHelper.createGJRVilkarsvedtakIfGJRKravlinjePresent
        private fun kravlinjeForGjenlevenderett(list: List<Kravlinje>): Kravlinje? =
            list.firstOrNull { KravlinjeTypeEnum.GJR == it.kravlinjeTypeEnum }

        private fun isGjenlevenderettighet(kravlinjeType: KravlinjeTypeEnum?): Boolean =
            KravlinjeTypeEnum.GJR == kravlinjeType

        private fun addGjenlevenderettVedtak(
            virkningDato: LocalDate,
            vedtakListe: MutableList<VilkarsVedtak>,
            kravlinje: Kravlinje
        ) {
            val vedtak = manueltVedtak(kravlinje, virkningDato).also {
                it.anbefaltResultatEnum = VedtakResultatEnum.INNV
            }

            vedtakListe.add(vedtak)
        }

        // SimpleFpenService.opprettManueltVilkarsvedtak
        private fun manueltVedtak(kravlinje: Kravlinje?, virkningFom: LocalDate) =
            VilkarsVedtak().apply {
                this.anbefaltResultatEnum = VedtakResultatEnum.VELG
                this.vilkarsvedtakResultatEnum = VedtakResultatEnum.VELG
                this.virkFom = virkningFom.toNorwegianDateAtNoon()
                this.virkTom = null
                this.kravlinje = kravlinje
                this.kravlinjeTypeEnum = kravlinje?.kravlinjeTypeEnum
                this.penPerson = kravlinje?.relatertPerson
            }.also {
                it.finishInit()
            }

        private fun ubetingetVilkaarsproevingRequest(spec: VilkaarsproevingSpec) =
            VilkarsprovRequest(
                kravhode = spec.kravhode,
                sisteBeregning = null,
                fom = spec.virkningFom.toNorwegianDateAtNoon(),
                tom = null
            )

        private fun vilkaarsproeving2011Request(spec: VilkaarsproevingSpec) =
            VilkarsprovAlderpensjon2011Request().apply {
                kravhode = spec.kravhode
                fom = spec.virkningFom.toNorwegianDateAtNoon()
                tom = null
                afpVirkFom = spec.afpFoersteVirkning?.toNorwegianDateAtNoon()
                afpLivsvarig = spec.privatAfp?.asLegacyPrivatAfp()
                afpPrivatLivsvarig = spec.privatAfp
                sisteBeregning = spec.sisteBeregning as? SisteAldersberegning2011
                utforVilkarsberegning = true
            }

        private fun vilkaarsproeving2016Request(spec: VilkaarsproevingSpec) =
            VilkarsprovAlderpensjon2016Request().apply {
                kravhode = spec.kravhode
                virkFom = spec.virkningFom.toNorwegianDateAtNoon()
                afpLivsvarig = spec.privatAfp?.asLegacyPrivatAfp()
                afpPrivatLivsvarig = spec.privatAfp
                afpVirkFom = spec.afpFoersteVirkning?.toNorwegianDateAtNoon()
                sisteBeregning = spec.sisteBeregning as? SisteAldersberegning2016
                utforVilkarsberegning = true
            }

        private fun vilkaarsproeving2025Request(spec: VilkaarsproevingSpec) =
            VilkarsprovAlderpensjon2025Request().apply {
                kravhode = spec.kravhode
                fom = spec.virkningFom.toNorwegianDateAtNoon()
                afpLivsvarig = spec.privatAfp?.asLegacyPrivatAfp()
                afpPrivatLivsvarig = spec.privatAfp
                afpVirkFom = spec.afpFoersteVirkning?.toNorwegianDateAtNoon()
                sisteBeregning = spec.sisteBeregning as? SisteAldersberegning2011 // NB: 2011
                utforVilkarsberegning = true
                afpOffentligLivsvarigGrunnlag = spec.livsvarigOffentligAfpGrunnlag
            }
    }
}

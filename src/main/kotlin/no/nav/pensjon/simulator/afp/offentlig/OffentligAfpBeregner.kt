package no.nav.pensjon.simulator.afp.offentlig

import no.nav.pensjon.simulator.afp.offentlig.OffentligAfpConstants.OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR
import no.nav.pensjon.simulator.afp.offentlig.fra2025.LivsvarigOffentligAfpService
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpEndringBeregner
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpFoerstegangBeregner
import no.nav.pensjon.simulator.afp.offentlig.pre2025.Pre2025OffentligAfpTerminator.terminatePre2025OffentligAfp
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * Beregner både "gammel" (pre-2025) og "ny" (livsvarig) AFP i offentlig sektor.
 */
@Component
class OffentligAfpBeregner(
    private val pre2025FoerstegangBeregner: Pre2025OffentligAfpFoerstegangBeregner,
    private val pre2025EndringBeregner: Pre2025OffentligAfpEndringBeregner,
    private val livsvarigBeregner: LivsvarigOffentligAfpService
) {
    fun beregnAfp(
        spec: SimuleringSpec,
        kravhode: Kravhode,
        ytelser: LoependeYtelser,
        foedselsdato: LocalDate?,
        pid: Pid?
    ): OffentligAfpResult =
        when {
            spec.gjelderPre2025OffentligAfp() -> {
                val result = pre2025FoerstegangBeregner.beregnAfp(
                    spec,
                    kravhode,
                    ytelser.forrigeAlderspensjonBeregningResultat
                )
                OffentligAfpResult(pre2025 = result, livsvarig = null, result.kravhode)
            }

            spec.gjelderEndringUtenLivsvarigOffentligAfp() -> //TODO Også sjekke mayHavePre2025OffentligAfp her?
                OffentligAfpResult(
                    pre2025 = spec.foersteUttakDato?.let { pre2025EndringBeregner.beregnAfp(kravhode, it) },
                    livsvarig = null,
                    kravhode
                )

            spec.kreverTermineringAvPre2025OffentligAfp() && mayHavePre2025OffentligAfp(foedselsdato) ->
                OffentligAfpResult(
                    pre2025 = terminatePre2025OffentligAfp(kravhode, spec.foersteUttakDato),
                    livsvarig = null,
                    kravhode
                )

            spec.gjelderLivsvarigOffentligAfp() ->
                OffentligAfpResult(
                    pre2025 = null,
                    livsvarig = foedselsdato?.let {
                        livsvarigBeregner.beregnAfp(
                            pid!!,
                            foedselsdato = it,
                            forventetAarligInntektBeloep = spec.forventetInntektBeloep,
                            fremtidigeInntekter = spec.fremtidigInntektListe,
                            brukFremtidigInntekt = spec.brukFremtidigInntekt,
                            virkningDato = spec.livsvarigOffentligAfp?.rettTilAfpFom ?: spec.foersteUttakDato!!
                        )
                    },
                    kravhode
                )

            else -> OffentligAfpResult(pre2025 = null, livsvarig = null, kravhode)
        }

    private companion object {
        private fun mayHavePre2025OffentligAfp(foedselsdato: LocalDate?): Boolean =
            foedselsdato?.let { it.year < OVERGANG_PRE2025_TIL_LIVSVARIG_OFFENTLIG_AFP_FOEDSEL_AAR } == true
    }
}

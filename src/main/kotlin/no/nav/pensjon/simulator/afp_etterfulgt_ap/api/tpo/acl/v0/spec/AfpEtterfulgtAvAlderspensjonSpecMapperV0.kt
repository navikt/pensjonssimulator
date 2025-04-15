package no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec

import no.nav.pensjon.simulator.afp_etterfulgt_ap.api.tpo.acl.v0.spec.AfpEtterfulgtAvAlderspensjonSivilstandSpecV0.Companion.fromExternalValue
import no.nav.pensjon.simulator.core.afp.AfpOrdningType
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.krav.UttakGradKode
import no.nav.pensjon.simulator.core.spec.Pre2025OffentligAfpSpec
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

object AfpEtterfulgtAvAlderspensjonSpecMapperV0 {

    fun fromDto(
        source: AfpEtterfulgtAvAlderspensjonSpecV0.AfpEtterfulgtAvAlderspensjonValidatedSpecV0,
        inntektSisteMaaned: Int,
        hentSisteInntektFraPOPP: (Pid) -> Int
    ): SimuleringSpec {

        val pid = Pid(source.personId)
        val uttakDato = LocalDate.parse(source.uttakFraOgMedDato)
        val sivilstatus = fromExternalValue(source.sivilstandVedPensjonering).internalValue
        val forventetInntektBeloep: Int = source.fremtidigAarligInntektTilAfpUttak ?: hentSisteInntektFraPOPP(pid)
        val epsHarPensjon = source.epsPensjon
        val epsHarInntektOver2G = source.eps2G
        val inntektUnderGradertUttakBeloep = source.fremtidigAarligInntektUnderAfpUttak
        val afpInntektMaanedFoerUttak = inntektSisteMaaned
        val utlandAntallAar = source.aarIUtlandetEtter16

        return SimuleringSpec(
            type = SimuleringType.AFP_ETTERF_ALDER,
            pid = pid,
            sivilstatus = sivilstatus,
            epsHarPensjon = epsHarPensjon,
            epsHarInntektOver2G = epsHarInntektOver2G,
            foersteUttakDato = uttakDato,
            heltUttakDato = null, //alltid 67 år ved AFP etterfulgt av AP
            fremtidigInntektListe = mutableListOf(),
            brukFremtidigInntekt = false,
            inntektEtterHeltUttakBeloep = 0,
            inntektOver1GAntallAar = 0, // only for anonym
            inntektUnderGradertUttakBeloep = inntektUnderGradertUttakBeloep,
            inntektEtterHeltUttakAntallAar = null, //TODO mangler sluttdato
            forventetInntektBeloep = forventetInntektBeloep,
            utlandAntallAar = utlandAntallAar,
            simulerForTp = false, //simuler for tjenestepensjon
            erAnonym = false,
            ignoreAvslag = false,
            isHentPensjonsbeholdninger = false,
            isOutputSimulertBeregningsinformasjonForAllKnekkpunkter = true,
            onlyVilkaarsproeving = false,
            utlandPeriodeListe = mutableListOf(),
            epsKanOverskrives = false,
            foedselAar = 0, // only for anonym
            flyktning = false,
            rettTilOffentligAfpFom = null,
            pre2025OffentligAfp = Pre2025OffentligAfpSpec(
                afpOrdning = AfpOrdningType.AFPSTAT, // ingen praktisk betydning i regelmotoren
                inntektMaanedenFoerAfpUttakBeloep = afpInntektMaanedFoerUttak,
                // NB: For pre-2025 offentlig AFP brukes 'gradert uttak'-perioden som AFP-periode:
                inntektUnderAfpUttakBeloep = inntektUnderGradertUttakBeloep
            ),
            foedselDato = null, // only for anonym
            avdoed = null,
            isTpOrigSimulering = true,
            uttakGrad = UttakGradKode.P_100,
        )
    }
}

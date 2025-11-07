package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning

import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.StillingsprosentSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.afpprivat.AfpPrivatAggregator.aggregate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste.InntektListeAggregator.aggregate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.inntektliste.InntektListeSpecMapper.createSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.offentligafp.OffentligAfpAggregator.aggregate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder.SimuleringperioderListeAggregator.aggregate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simuleringsperioder.SimuleringsperioderSpecMapper.createSpec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.aggregate.simulertberegningsinformasjon.SimulerBeregningsinformasjonAggregator.aggregate
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.map.pensjonsbeholdninger.PensjonsbeholdningerMapper.map
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.map.sivilstand.SivilstandMapper.map
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import java.time.LocalDate

object TjenestepensjonSimuleringPre2025SpecAggregator {

    fun aggregateSpec(
        simuleringResultat: SimulatorOutput,
        simuleringSpec: SimuleringSpec,
        stillingsprosentSpec: StillingsprosentSpec,
        sisteGyldigeOpptjeningsaar: Int
    ): TjenestepensjonSimuleringPre2025Spec {
        val afpEtterfAlder = simuleringSpec.type == SimuleringTypeEnum.AFP_ETTERF_ALDER
        val foedselsdato: LocalDate = simuleringResultat.foedselDato!! //ikke anonym simulering

        val offentligAfp = aggregate(simuleringResultat.pre2025OffentligAfp, afpEtterfAlder)
        val privatAfp = aggregate(simuleringResultat.privatAfpPeriodeListe, afpEtterfAlder)
        val inntekter = aggregate(
            spec = createSpec(simuleringSpec, foedselsdato),
            sisteGyldigeOpptjeningsaar = sisteGyldigeOpptjeningsaar
        )
        val simuleringsperioder =
            aggregate(createSpec(simuleringSpec, offentligAfp, stillingsprosentSpec, foedselsdato))
        val simuleringsdata = aggregate(
            foedselsdato,
            simuleringSpec.foersteUttakDato,
            simuleringResultat.alderspensjon?.simulertBeregningInformasjonListe
        )

        return TjenestepensjonSimuleringPre2025Spec(
            pid = simuleringSpec.pid!!, //ikke anonym simulering
            foedselsdato = foedselsdato,
            sisteTpOrdningsTpNummer = "TPNR",
            simulertPrivatAfp = privatAfp,
            simulertOffentligAfp = offentligAfp,
            sivilstand = map(simuleringSpec.sivilstatus),
            inntekter = inntekter,
            pensjonsbeholdningsperioder = map(simuleringResultat.alderspensjon?.pensjonBeholdningListe),
            simuleringsperioder = simuleringsperioder,
            simuleringsdata = simuleringsdata,
            tpForhold = emptyList()
        )
    }
}

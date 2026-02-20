package no.nav.pensjon.simulator.tjenestepensjon.pre2025.apberegning.map.pensjonsbeholdninger

import no.nav.pensjon.simulator.core.beregn.BeholdningPeriode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pensjonsbeholdningsperiode

object PensjonsbeholdningerMapper {

    fun map(pensjonBeholdningListe: List<BeholdningPeriode>?): List<Pensjonsbeholdningsperiode> {
        return pensjonBeholdningListe?.map {
            Pensjonsbeholdningsperiode(
                fom = it.datoFom,
                pensjonsbeholdning = it.pensjonsbeholdning, //TODO kan 0.0 brukes?
                garantipensjonsbeholdning = it.garantipensjonsbeholdning, //TODO kan 0.0 brukes?
                garantitilleggsbeholdning = it.garantitilleggsbeholdning //TODO kan 0.0 brukes?
            )
        } ?: emptyList()
    }
}
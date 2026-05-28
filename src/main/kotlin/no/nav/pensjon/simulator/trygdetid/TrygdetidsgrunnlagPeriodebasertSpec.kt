package no.nav.pensjon.simulator.trygdetid

import no.nav.pensjon.simulator.core.domain.regler.enum.RegelverkTypeEnum
import no.nav.pensjon.simulator.core.spec.SimuleringSpec

// PEN: no.nav.service.pensjon.simulering.abstractsimulerapfra2011.SettTrygdetidsgrunnlagRequest
/**
 * Spesifikasjon (parametre) for beregning av trygdetid når utenlandsopphold er angitt som en liste av perioder.
 */
data class TrygdetidsgrunnlagPeriodebasertSpec(
    val simuleringSpec: SimuleringSpec,
    val brukSoekersUtenlandsperioder: Boolean,
    val regelverkType: RegelverkTypeEnum,
    val erFoerstegangsberegning: Boolean,
)
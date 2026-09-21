package no.nav.pensjon.simulator.opptjening

import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.Opptjening
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.LonnsvekstInformasjon
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.ReguleringsInformasjon
import no.nav.pensjon.simulator.core.domain.regler.enum.FormelKodeEnum
import java.time.LocalDate

data class Pensjonsbeholdning(
    val aar: Int,
    val fom: LocalDate? = null,
    val tom: LocalDate? = null,
    val totalbeloep: Double,
    val opptjening: Opptjening? = null,
    val loennsvekstinformasjon: LonnsvekstInformasjon? = null,
    val reguleringsinformasjon: ReguleringsInformasjon? = null,
    val formelkode: FormelKodeEnum? = null,
    val merknadListe: List<Merknad> = emptyList()
)
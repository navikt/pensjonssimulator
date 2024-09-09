package no.nav.pensjon.simulator.core.virkning

import no.nav.pensjon.simulator.core.krav.KravlinjeType
import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import java.time.LocalDate

data class FoersteVirkningDatoCombo(
    val person: PenPerson,
    val foersteVirkningDatoListe: List<FoersteVirkningDato>,
    val foersteVirkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
)

// vedtak.kravhode.sak.forsteVirkningsdatoListe
data class FoersteVirkningDato(
    val sakType: SakType?,
    val kravlinjeType: KravlinjeType?,
    val virkningDato: LocalDate?,
    val annenPerson: PenPerson?
)

package no.nav.pensjon.simulator.core.virkning

import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import java.time.LocalDate

// Corresponds to VirkningsdatoDtoForSimulator in PEN
data class FoersteVirkningDatoCombo(
    val foersteVirkningDatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
)

// vedtak.kravhode.sak.forsteVirkningsdatoListe
data class FoersteVirkningDato(
    val sakType: SakType?,
    val kravlinjeType: KravlinjeTypeEnum?,
    val virkningDato: LocalDate?,
    val annenPerson: PenPerson?
)

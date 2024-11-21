package no.nav.pensjon.simulator.sak.client.pen.acl

import no.nav.pensjon.simulator.core.domain.SakType
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.enum.KravlinjeTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForsteVirkningsdatoGrunnlag
import java.util.*

data class PenPersonVirkningDatoResult(
    val person: PenPerson,
    val forsteVirkningsdatoListe: List<PenFoersteVirkningDato>,
    val forsteVirkningsdatoGrunnlagListe: List<ForsteVirkningsdatoGrunnlag>
)

data class PenFoersteVirkningDato(
    val sakType: SakType?,
    val kravlinjeType: KravlinjeTypeEnum?,
    val virkningsdato: Date?,
    val annenPerson: PenPerson?
)

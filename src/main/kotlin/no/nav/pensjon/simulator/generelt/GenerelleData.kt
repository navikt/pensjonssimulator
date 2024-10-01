package no.nav.pensjon.simulator.generelt

import no.nav.pensjon.simulator.core.afp.privat.PrivatAfpSatser
import no.nav.pensjon.simulator.core.domain.regler.VeietSatsResultat
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.DelingstallUtvalg
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.ForholdstallUtvalg
import java.time.LocalDate

data class GenerelleData(
    val foedselDato: LocalDate,
    val privatAfpSatser: PrivatAfpSatser,
    val delingstallUtvalg: DelingstallUtvalg,
    val forholdstallUtvalg: ForholdstallUtvalg,
    val satsResultatListe: List<VeietSatsResultat>
)

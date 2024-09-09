package no.nav.pensjon.simulator.core.domain.regler.simulering

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.AfpOrdningTypeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.SimuleringTypeCti
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import java.util.*

/**
 * Simulering
 */
class Simulering(
    var simuleringType: SimuleringTypeCti? = null,
    var afpOrdning: AfpOrdningTypeCti? = null,
    var uttaksdato: Date? = null,
    var persongrunnlagListe: MutableList<Persongrunnlag> = mutableListOf(),
    var vilkarsvedtakliste: MutableList<VilkarsVedtak> = mutableListOf()
)

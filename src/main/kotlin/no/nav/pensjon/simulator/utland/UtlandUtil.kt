package no.nav.pensjon.simulator.utland

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.InngangOgEksportGrunnlag

object UtlandUtil {

    fun defaultInngangOgEksportGrunnlag() =
        InngangOgEksportGrunnlag().apply { fortsattMedlemFT = true }
}
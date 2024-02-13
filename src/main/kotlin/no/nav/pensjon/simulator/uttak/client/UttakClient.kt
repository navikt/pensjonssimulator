package no.nav.pensjon.simulator.uttak.client

import no.nav.pensjon.simulator.uttak.TidligstMuligUttak
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakSpec

interface UttakClient {
    fun finnTidligstMuligUttak(spec: TidligstMuligUttakSpec): TidligstMuligUttak
}

package no.nav.pensjon.simulator.krav.client

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.person.Pid

interface KravClient {

    fun fetchKravhode(kravhodeId: Long): Kravhode

    fun fetchUttaksgrader(pid: Pid): List<Uttaksgrad>
}

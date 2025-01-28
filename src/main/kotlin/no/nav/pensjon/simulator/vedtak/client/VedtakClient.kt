package no.nav.pensjon.simulator.vedtak.client

import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.vedtak.VedtakStatus
import java.time.LocalDate

interface VedtakClient {
    fun tidligsteKapittel20VedtakGjelderFom(pid: Pid, sakType: SakTypeEnum): LocalDate?

    fun fetchVedtakStatus(pid: Pid, uttakFom: LocalDate?): VedtakStatus
}

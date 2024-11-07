package no.nav.pensjon.simulator.vedtak

import no.nav.pensjon.simulator.core.domain.regler.enum.SakTypeEnum
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.vedtak.client.VedtakClient
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class VedtakService(private val client: VedtakClient) {

    fun tidligsteKapittel20VedtakGjelderFom(pid: Pid, sakType: SakTypeEnum): LocalDate? =
        client.tidligsteKapittel20VedtakGjelderFom(pid, sakType)
}

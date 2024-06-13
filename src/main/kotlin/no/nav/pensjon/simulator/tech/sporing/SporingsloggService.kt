package no.nav.pensjon.simulator.tech.sporing

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.sporing.client.SporingsloggClient
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SporingsloggService(
    private val client: SporingsloggClient,
    private val organisasjonsnummerProvider: OrganisasjonsnummerProvider
) {
    fun log(pid: Pid, leverteData: String) {
        client.log(
            Sporing(
                pid,
                mottaker = organisasjonsnummerProvider.provideOrganisasjonsnummer(),
                tema = "PEK",
                behandlingGrunnlag = "B353",
                uthentingTidspunkt = LocalDateTime.now(),
                leverteData
            )
        )
    }
}

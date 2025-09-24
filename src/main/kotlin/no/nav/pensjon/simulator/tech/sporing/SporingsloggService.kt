package no.nav.pensjon.simulator.tech.sporing

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import no.nav.pensjon.simulator.generelt.organisasjon.Organisasjonsnummer
import no.nav.pensjon.simulator.generelt.organisasjon.OrganisasjonsnummerProvider
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Organisasjoner
import no.nav.pensjon.simulator.tech.security.SecurityCoroutineContext
import no.nav.pensjon.simulator.tech.sporing.client.SporingsloggClient
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SporingsloggService(
    private val client: SporingsloggClient,
    private val organisasjonsnummerProvider: OrganisasjonsnummerProvider
) {
    /**
     * Log using fire-and-forget async call
     */
    fun log(pid: Pid, dataForespoersel: String, leverteData: String) {
        CoroutineScope(Dispatchers.Default).launch(SecurityCoroutineContext()) {
            val organisasjonsnummer = organisasjonsnummerProvider.provideOrganisasjonsnummer()

            client.log(
                Sporing(
                    pid,
                    mottaker = organisasjonsnummer,
                    tema = "PEK",
                    behandlingGrunnlag = "B353",
                    uthentingTidspunkt = LocalDateTime.now(),
                    dataForespoersel,
                    leverteData
                )
            )
        }
    }

    /**
     * Log utgående request using fire-and-forget async call
     */
    fun logUtgaaendeRequest(organisasjonsnummer: Organisasjonsnummer, pid: Pid, leverteData: String) {
        CoroutineScope(Dispatchers.Default).launch(SecurityCoroutineContext()) {
            client.log(
                Sporing(
                    pid,
                    mottaker = organisasjonsnummer,
                    tema = "PEK",
                    behandlingGrunnlag = "B353",
                    uthentingTidspunkt = LocalDateTime.now(),
                    "",
                    leverteData
                )
            )
        }
    }
}

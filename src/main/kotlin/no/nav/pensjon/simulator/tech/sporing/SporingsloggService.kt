package no.nav.pensjon.simulator.tech.sporing

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.metric.Metrics
import no.nav.pensjon.simulator.tech.sporing.client.SporingsloggClient
import org.springframework.stereotype.Service
import java.time.LocalDateTime

@Service
class SporingsloggService(
    private val client: SporingsloggClient,
    private val organisasjonsnummerProvider: OrganisasjonsnummerProvider
) {
    fun log(pid: Pid, leverteData: String) {
        val organisasjonsnummer = organisasjonsnummerProvider.provideOrganisasjonsnummer()

        client.log(
            Sporing(
                pid,
                mottaker = organisasjonsnummer,
                tema = "PEK",
                behandlingGrunnlag = "B353",
                uthentingTidspunkt = LocalDateTime.now(),
                leverteData
            )
        )

        countCall(organisasjonsnummer)
    }

    private fun countCall(organisasjonsnummer: Organisasjonsnummer) {
        Metrics.countIngressCall(organisasjonsnummer.value)
    }
}

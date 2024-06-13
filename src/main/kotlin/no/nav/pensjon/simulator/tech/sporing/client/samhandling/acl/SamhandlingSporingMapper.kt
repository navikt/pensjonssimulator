package no.nav.pensjon.simulator.tech.sporing.client.samhandling.acl

import no.nav.pensjon.simulator.tech.sporing.Sporing
import java.time.format.DateTimeFormatter

object SamhandlingSporingMapper {
    fun toDto(source: Sporing) =
        SamhandlingSporing(
            person = source.pid.value,
            mottaker = source.mottaker.value,
            tema = source.tema,
            behandlingsGrunnlag = source.behandlingGrunnlag,
            uthentingsTidspunkt = DateTimeFormatter.ISO_LOCAL_DATE_TIME.format(source.uthentingTidspunkt),
            leverteData = source.leverteData
        )
}

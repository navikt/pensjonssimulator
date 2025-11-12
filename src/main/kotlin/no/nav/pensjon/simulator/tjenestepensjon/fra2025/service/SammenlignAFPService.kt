package no.nav.pensjon.simulator.tjenestepensjon.fra2025.service

import mu.KotlinLogging
import no.nav.pensjon.simulator.tjenestepensjon.fra2025.domain.Utbetalingsperiode
import org.springframework.scheduling.annotation.Async
import org.springframework.stereotype.Service

@Service
open class SammenlignAFPService(
    //private val afp: AFPOffentligLivsvarigSimuleringService
) {
    private val log = KotlinLogging.logger {}

    @Async("loggingExecutor") //TODO slå på når AFP er på plass i pensjonssimulator
    open fun sammenlignOgLoggAfp(request: OffentligTjenestepensjonFra2025SimuleringSpec, utbetalingsperiode: List<Utbetalingsperiode>) {
//        val fremtidigInntekt = SPKMapper.mapToRequest(request).fremtidigInntektListe
//
//        val simuleringRequest = SimulerAFPOffentligLivsvarigRequest(
//            fnr = request.pid,
//            fom = request.uttaksdato,
//            fodselsdato = request.foedselsdato,
//            fremtidigeInntekter = fremtidigInntekt.map { FremtidigInntekt(it.aarligInntekt, it.fraOgMedDato ) }
//        )
//
//        val afpLokal = afp.simuler(simuleringRequest)
//
//        val afpFraTpOrdning = utbetalingsperiode.filter { it.ytelseType == "OAFP" }
//
//        val afpHverPeriode = afpFraTpOrdning.mapIndexed { i, it -> "Årlig AFP fra utbelaingsperiode $i: ${it.maanedligBelop * 12}" }.joinToString("\n")
//
//        log.info {
//            "$afpHverPeriode \n" +
//                    "AFP fra Nav: ${afpLokal.first().afpYtelsePerAar} \n"
//        }
//
//        log.info { "Request til Tp ordning AFP: ${redact(request.toString())}" +
//                "\nRequest for Nav AFP: ${simuleringRequest.fremtidigeInntekter}, ${simuleringRequest.fom}" +
//                "\nAFP fra Tp ordning: $afpFraTpOrdning" +
//                "\nAFP fra Nav $afpLokal" }
    }
}


package no.nav.pensjon.simulator.inntekt

import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.person.Pid
import org.springframework.stereotype.Component

/**
 * Håndterer inntekt som er ferdig lignet fra Skatteetaten.
 */
@Component
class LignetInntektService(
    private val inntektService: InntektService,
    private val generelleDataHolder: GenerelleDataHolder
) {
    /**
     * Behandler den sist lignede inntekt.
     * Hvis fjorårets lignede inntekt foreligger før 'siste gyldige opptjeningsår' er oppdatert i PEN,
     * så legges denne inntekten til inntektlisten.
     * Dette for å unngå at en estimert inntekt brukes istedenfor den reelle (lignede) inntekten i perioden
     * fra lignet inntekt foreligger (typisk i juni) til 'siste gyldige opptjeningsår' oppdateres i PEN
     * (typisk i november).
     * Funksjonen returner årstallet for neste inntekt som skal legges til inntektlisten.
     */
    fun behandleSistLignedeInntekt(pid: Pid, inntektListe: MutableList<AarligInntekt>): Int {
        val sistLignedeInntekt = inntektService.hentSisteLignetInntekt(pid)
        val sisteGyldigeOpptjeningsaar = generelleDataHolder.getSisteGyldigeOpptjeningsaar()
        val sistLignedeInntektAar = sistLignedeInntekt.fom.year
        val brukSisteLignedeInntekt = sistLignedeInntektAar == sisteGyldigeOpptjeningsaar + 1

        if (brukSisteLignedeInntekt) {
            inntektListe.add(
                AarligInntekt(
                    inntektAar = sistLignedeInntektAar,
                    beloep = sistLignedeInntekt.aarligBeloep
                )
            )

            return sistLignedeInntektAar + 1
        } else {
            return sisteGyldigeOpptjeningsaar + 1
        }
    }
}
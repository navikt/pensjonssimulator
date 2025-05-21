package no.nav.pensjon.simulator.normalder

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.normalder.client.NormertPensjonsalderClient
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import org.springframework.stereotype.Service
import java.time.LocalDate

/**
 * Simulator utilities related to "normalder".
 * The term "normalder" is defined in "NOU 2022: 7 - Et forbedret pensjonssystem"
 * (https://www.regjeringen.no/no/dokumenter/nou-2022-7/id2918654/?ch=10#kap9-1):
 * "aldersgrensen for ubetinget rett til alderspensjon som i dag (2024) er 67 år,
 *  kalles 'normert pensjoneringsalder', med 'normalderen' som kortform"
 */
@Service
class NormertPensjonsalderService(
    private val normalderClient: NormertPensjonsalderClient,
    private val personService: GeneralPersonService
) {
    fun normalder(foedselsdato: LocalDate): Alder =
        aldersgrenser(foedselsdato).normalder

    /**
     * Datoen da pensjonsuttak starter dersom det starter ved normert pensjonsalder.
     * Det er 1. dag i måneden etter at personen oppnår normalderen.
     */
    fun normertPensjoneringsdato(foedselsdato: LocalDate): LocalDate =
        PensjonAlderDato(foedselsdato, alder = normalder(foedselsdato)).dato

    /**
     * Datoen da personen oppnår normert pensjonsalder.
     */
    fun normalderOppnaasDato(foedselsdato: LocalDate): LocalDate =
        foedselsdato.let { normalder(it).oppnaasDato(it) }

    /**
     * Høyeste alder for pensjonsopptjening (måneder tas ikke med).
     * I 2025 er dette 75 år.
     * TODO: Er dette gyldig ved økte aldersgrenser (normert)?
     */
    fun maxOpptjeningAar(foedselsdato: LocalDate): Int =
        PensjonAlderDato(foedselsdato, alder = oevreAlder(foedselsdato)).alder.aar

    fun nedreAlder(foedselsdato: LocalDate): Alder =
        aldersgrenser(foedselsdato).nedreAlder

    fun oevreAlder(foedselsdato: LocalDate): Alder =
        aldersgrenser(foedselsdato).oevreAlder

    fun aldersgrenser(foedselsdato: LocalDate): Aldersgrenser =
        normalderClient.fetchNormalderListe().first { it.aarskull == foedselsdato.year }

    fun normalder(pid: Pid): Alder =
        normalder(personService.foedselsdato(pid))

    fun normalderDato(foedselsdato: LocalDate): LocalDate =
        foedselsdato.let {
            uttakDato(foedselsdato = it, uttakAlder = normalder(it))
        }
}

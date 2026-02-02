package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.uttak.TidligstMuligUttak
import no.nav.pensjon.simulator.validity.Problem

object UttakResultMapperV1 {

    fun resultV1(source: TidligstMuligUttak) =
        TidligstMuligUttakResultV1(
            tidligstMuligeUttakstidspunktListe = tidligstMuligUttakListeV1(source),
            feil = source.problem?.let(::feilV1)
        )

    private fun tidligstMuligUttakListeV1(source: TidligstMuligUttak): List<TidligstMuligUttakV1> =
        source.uttaksdato?.let {
            listOf(TidligstMuligUttakV1(source.uttaksgrad.prosentsats, it))
        }.orEmpty()


    private fun feilV1(source: Problem) =
        TidligstMuligUttakFeilV1(
            type = TidligstMuligUttakFeilTypeV1.fromInternalValue(source.type),
            beskrivelse = source.beskrivelse
        )
}

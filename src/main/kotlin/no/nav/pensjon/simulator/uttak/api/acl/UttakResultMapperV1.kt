package no.nav.pensjon.simulator.uttak.api.acl

import no.nav.pensjon.simulator.uttak.TidligstMuligUttak
import no.nav.pensjon.simulator.uttak.TidligstMuligUttakFeil

object UttakResultMapperV1 {

    fun resultV1(source: TidligstMuligUttak) =
        TidligstMuligUttakResultV1(
            tidligstMuligeUttakstidspunktListe = tidligstMuligUttakListeV1(source),
            feil = source.feil?.let(::feilV1)
        )

    private fun tidligstMuligUttakListeV1(source: TidligstMuligUttak): List<TidligstMuligUttakV1> =
        source.uttakDato?.let {
            listOf(TidligstMuligUttakV1(source.uttaksgrad.prosentsats, it))
        }.orEmpty()


    private fun feilV1(source: TidligstMuligUttakFeil) =
        TidligstMuligUttakFeilV1(
            type = TidligstMuligUttakFeilTypeV1.fromInternalValue(source.type),
            beskrivelse = source.beskrivelse
        )
}

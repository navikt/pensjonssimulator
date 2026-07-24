package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.ytelse.AlderspensjonYtelserFlags
import no.nav.pensjon.simulator.ytelse.EndringAlderspensjonYtelserFlags
import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec
import no.nav.pensjon.simulator.ytelse.TidsbegrensetOffentligAfpYtelserFlags

object PenLoependeYtelserSpecMapper {

    fun toDto(source: LoependeYtelserSpec) =
        PenLoependeYtelserSpec(
            pid = source.pid?.value,
            foersteUttakDato = source.foersteUttakDato,
            avdoed = source.avdoed?.let(::avdoed),
            alderspensjonFlags = source.alderspensjonFlags?.let(::alderspensjonFlags),
            endringAlderspensjonFlags = source.endringAlderspensjonFlags?.let(::endringAlderspensjonFlags),
            pre2025OffentligAfpYtelserFlags = source.tidsbegrensetOffentligAfpYtelserFlags?.let(::afpFlags),
        )

    private fun avdoed(source: Avdoed) =
        PenAvdoedYtelserSpec(
            pid = source.pid.value,
            doedDato = source.doedDato
        )

    private fun alderspensjonFlags(source: AlderspensjonYtelserFlags) =
        PenAlderspensjonYtelserFlags(
            inkluderPrivatAfp = source.inkluderPrivatAfp
        )

    private fun endringAlderspensjonFlags(source: EndringAlderspensjonYtelserFlags) =
        PenEndringAlderspensjonYtelserFlags(
            inkluderPrivatAfp = source.inkluderPrivatAfp
        )

    private fun afpFlags(source: TidsbegrensetOffentligAfpYtelserFlags) =
        PenTidsbegrensetOffentligAfpYtelserFlags(
            gjelderFpp = source.gjelderFpp,
            sivilstatusUdefinert = source.sivilstatusUdefinert
        )
}

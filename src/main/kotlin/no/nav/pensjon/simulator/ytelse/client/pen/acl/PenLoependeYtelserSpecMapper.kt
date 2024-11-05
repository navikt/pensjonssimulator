package no.nav.pensjon.simulator.ytelse.client.pen.acl

import no.nav.pensjon.simulator.ytelse.LoependeYtelserSpec

object PenLoependeYtelserSpecMapper {
    fun toDto(source: LoependeYtelserSpec) =
        PenLoependeYtelserSpec(
            pid = source.pid.value,
            foersteUttakDato = source.foersteUttakDato,
            inkluderPrivatAfp = source.inkluderPrivatAfp,
            avdoedPid = source.avdoedPid?.value,
            doedDato = source.doedDato
        )
}

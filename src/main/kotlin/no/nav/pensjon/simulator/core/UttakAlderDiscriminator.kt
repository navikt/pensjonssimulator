package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.core.exception.ForLavtTidligUttakException
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.trygd.ForKortTrygdetidException
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

interface UttakAlderDiscriminator {
    @Throws(
        ForKortTrygdetidException::class,
        ForLavtTidligUttakException::class
    )
    fun simuler(spec: SimuleringSpec): SimulatorOutput

    fun fetchFoedselsdato(pid: Pid): LocalDate
}

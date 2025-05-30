package no.nav.pensjon.simulator.core

import no.nav.pensjon.simulator.core.exception.UtilstrekkeligOpptjeningException
import no.nav.pensjon.simulator.core.exception.UtilstrekkeligTrygdetidException
import no.nav.pensjon.simulator.core.result.SimulatorOutput
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.person.Pid
import java.time.LocalDate

interface UttakAlderDiscriminator {

    @Throws(
        UtilstrekkeligOpptjeningException::class,
        UtilstrekkeligTrygdetidException::class
    )
    fun simuler(initialSpec: SimuleringSpec): SimulatorOutput

    fun fetchFoedselsdato(pid: Pid): LocalDate
}

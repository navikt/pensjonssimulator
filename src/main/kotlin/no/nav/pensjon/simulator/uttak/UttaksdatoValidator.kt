package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.exception.BadSpecException
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate

@Component
class UttaksdatoValidator(
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun verifyUttakFom(uttakFom: LocalDate, foedselsdato: LocalDate) {
        if (uttakFom.dayOfMonth != 1) {
            throw BadSpecException("uttakFom must be the first day in a month")
        }

        val alder = Alder.from(foedselsdato, dato = uttakFom)
        val aldersgrenser = normalderService.aldersgrenser(foedselsdato)

        if (alder.lessThan(aldersgrenser.nedreAlder)) {
            throw BadSpecException("uttakFom cannot be earlier than first month after user turns ${aldersgrenser.nedreAlder}")
        }

        if (alder.greaterThan(aldersgrenser.oevreAlder)) {
            throw BadSpecException("uttakFom cannot be later than first month after user turns ${aldersgrenser.oevreAlder}")
        }

        if (isBeforeByDay(uttakFom, time.today(), allowSameDay = false)) {
            throw BadSpecException("uttakFom must be after today")
        }
    }
}

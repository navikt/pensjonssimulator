package no.nav.pensjon.simulator.alderspensjon.api.samhandler.acl.v3

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.core.exception.InvalidArgumentException
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.Aldersgrenser
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.person.GeneralPersonService
import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * PEN: SimulerAlderspensjonRequestV3Converter.verify
 */
@Component
class AlderspensjonSpecValidatorV3(
    private val personService: GeneralPersonService,
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun validate(spec: AlderspensjonSpecV3) {
        val foedselsdato: LocalDate = personService.foedselsdato(Pid(spec.fnr))
        val aldersgrenser = normalderService.aldersgrenser(foedselsdato)
        val foersteUttak: UttaksperiodeSpecV3 = spec.forsteUttak
        val heltUttak: UttaksperiodeSpecV3? = spec.heltUttak
        spec.fremtidigInntektListe.orEmpty().forEach(::validateInntekt)
        validateUttak(uttaksperiode = foersteUttak, fieldName = "forsteUttak", foedselsdato, aldersgrenser)
        validateUttakskombinasjon(foersteUttak, heltUttak)
        heltUttak?.let { validateUttak(uttaksperiode = it, fieldName = "heltUttak", foedselsdato, aldersgrenser) }
        validateEps(spec)
    }

    private fun validateUttak(
        uttaksperiode: UttaksperiodeSpecV3,
        fieldName: String,
        foedselsdato: LocalDate,
        aldersgrenser: Aldersgrenser
    ) {
        val fom: LocalDate = uttaksperiode.datoFom.toNorwegianLocalDate()

        if (fom.dayOfMonth != 1)
            throw InvalidArgumentException(message = "$fieldName.datoFom må være første dag i måneden")

        val alder = Alder.from(foedselsdato, dato = fom)
        val nedreAlder = aldersgrenser.nedreAlder
        val oevreAlder = aldersgrenser.oevreAlder

        if (alder lessThan nedreAlder)
            throw InvalidArgumentException(message = "$fieldName.datoFom kan ikke være før måneden etter at personen oppnår $nedreAlder")

        if (alder greaterThan oevreAlder)
            throw InvalidArgumentException(message = "$fieldName.datoFom kan ikke være etter måneden etter at personen oppnår $oevreAlder")

        if (fom.isAfter(time.today()).not())
            throw InvalidArgumentException(message = "$fieldName.datoFom må være etter dagens dato")
    }

    private companion object {
        private const val HEL_UTTAKSGRAD = 100

        private val epsSivilstatuser: EnumSet<SivilstatusSpecV3> =
            EnumSet.of(SivilstatusSpecV3.GIFT, SivilstatusSpecV3.REPA, SivilstatusSpecV3.SAMB)

        private fun validateEps(spec: AlderspensjonSpecV3) {
            val sivilstatus = spec.sivilstandVedPensjonering

            if (epsSivilstatuser.contains(sivilstatus)) {
                if (spec.epsPensjon == null)
                    throw InvalidArgumentException(message = "epsPensjon må være angitt når sivilstandVedPensjonering er $sivilstatus")

                if (spec.eps2G == null)
                    throw InvalidArgumentException(message = "eps2G må være angitt når sivilstandVedPensjonering er $sivilstatus")
            }
        }

        private fun validateInntekt(inntekt: InntektSpecV3) {
            if (inntekt.fomDato.toNorwegianLocalDate().dayOfMonth != 1)
                throw InvalidArgumentException(message = "inntekt i fremtidigInntektListe må ha fomDato som er den første i måneden")
        }

        private fun validateUttakskombinasjon(foersteUttak: UttaksperiodeSpecV3, heltUttak: UttaksperiodeSpecV3?) {
            if (foersteUttak.grad == HEL_UTTAKSGRAD && heltUttak != null)
                throw InvalidArgumentException(message = "heltUttak må utelates når forsteUttak.grad is $HEL_UTTAKSGRAD")

            if (foersteUttak.grad < HEL_UTTAKSGRAD && heltUttak == null)
                throw InvalidArgumentException(message = "heltUttak må være angitt når forsteUttak.grad < $HEL_UTTAKSGRAD")
        }
    }
}

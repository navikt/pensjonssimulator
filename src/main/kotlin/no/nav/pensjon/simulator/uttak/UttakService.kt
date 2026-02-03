package no.nav.pensjon.simulator.uttak

import no.nav.pensjon.simulator.alder.Alder
import no.nav.pensjon.simulator.alder.PensjonAlderDato
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimuleringFacade
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertPensjonEllerAlternativ
import no.nav.pensjon.simulator.alderspensjon.alternativ.SimulertUttakAlder
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.isBeforeOrOn
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.time.format.DateTimeParseException

@Component
class UttakService(
    private val simuleringFacade: SimuleringFacade,
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun finnTidligstMuligUttak(spec: SimuleringSpec): TidligstMuligUttak =
        try {
            val tidligstUttakAlder: Alder = normalderService.nedreAlder(spec.foedselDato!!)
            val tidligstUttak = fremtidigPensjonAlderDato(spec.foedselDato, tidligstUttakAlder)
            val newSpec = spec.withFoersteUttakDato(tidligstUttak.dato)

            val result: SimulertPensjonEllerAlternativ =
                simuleringFacade.simulerAlderspensjon(newSpec, inkluderPensjonHvisUbetinget = true)

            // TMU (tidligst mulig uttak) er enten:
            // - Den lavest mulige fremtidige alder for helt uttak (hvis vilkårsprøvingen av denne gir OK), eller
            // - Den alternative alder for gradert/helt uttak som returneres av simuleringen (vilkårsprøvingen av denne har gitt OK)
            val tmuAlder: SimulertUttakAlder =
                result.alternativ?.gradertUttakAlder
                    ?: result.alternativ?.heltUttakAlder
                    ?: SimulertUttakAlder(tidligstUttak.alder, tidligstUttak.dato)

            TidligstMuligUttak(
                uttaksdato = tmuAlder.uttakDato,
                uttaksgrad = Uttaksgrad.from(newSpec.uttakGrad.value.toInt()),
                problem = null
            )
        } catch (e: BadRequestException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: BadSpecException) {
            problem(e)
        } catch (e: DateTimeParseException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: EgressException) {
            problem(e, type = ProblemType.SERVERFEIL)
        } catch (e: FeilISimuleringsgrunnlagetException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: ImplementationUnrecoverableException) {
            problem(e, type = ProblemType.SERVERFEIL)
        } catch (e: InvalidArgumentException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: InvalidEnumValueException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: KanIkkeBeregnesException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: KonsistensenIGrunnlagetErFeilException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: PersonForGammelException) {
            problem(e, type = ProblemType.PERSON_FOR_HOEY_ALDER)
        } catch (e: PersonForUngException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: RegelmotorValideringException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: UtilstrekkeligOpptjeningException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_TRYGDETID)
        }

    private fun fremtidigPensjonAlderDato(foedselsdato: LocalDate, alder: Alder): PensjonAlderDato {
        val alderDato = PensjonAlderDato(foedselsdato, alder)

        // Sjekk at dato er i fremtid:
        return time.today().let {
            if (alderDato.dato.isBeforeOrOn(it))
                PensjonAlderDato(foedselsdato, dato = it.withDayOfMonth(1).plusMonths(1))
            else
                alderDato
        }
    }

    private companion object {
        private fun problem(e: BadSpecException) =
            problem(e, type = e.problemType)

        private fun problem(e: RuntimeException, type: ProblemType) =
            TidligstMuligUttak(
                uttaksdato = null,
                uttaksgrad = Uttaksgrad.NULL,
                problem = Problem(type, beskrivelse = e.message ?: "Ukjent feil - ${e.javaClass.simpleName}")
            )
    }
}

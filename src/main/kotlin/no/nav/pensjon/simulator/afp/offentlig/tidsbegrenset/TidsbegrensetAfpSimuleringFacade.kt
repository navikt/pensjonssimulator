package no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset

import no.nav.pensjon.simulator.afp.offentlig.tidsbegrenset.serviceberegn.ServiceberegningAfpResult
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.*
import no.nav.pensjon.simulator.fpp.FppSimuleringResult
import no.nav.pensjon.simulator.tech.validation.InvalidEnumValueException
import no.nav.pensjon.simulator.tech.web.BadRequestException
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.validity.BadSpecException
import no.nav.pensjon.simulator.validity.InternDataInkonsistensException
import no.nav.pensjon.simulator.validity.Problem
import no.nav.pensjon.simulator.validity.ProblemType
import org.springframework.stereotype.Service
import java.time.format.DateTimeParseException

@Service
class TidsbegrensetAfpSimuleringFacade(
    private val specCreator: TidsbegrensetAfpSpecCreator,
    private val simulator: TidsbegrensetAfpSimuleringService
) {
    fun simulerAfpForFpp(spec: TidsbegrensetAfpSpec): FppSimuleringResult =
        fppResult(simuler(spec))

    fun simulerAfpForServiceberegning(spec: TidsbegrensetAfpSpec): ServiceberegningAfpResult =
        serviceberegningResult(simuler(spec))

    private fun simuler(spec: TidsbegrensetAfpSpec): TidsbegrensetAfpSimuleringResult =
        try {
            simulator.simulerPensjonsberegning(simuleringSpec(spec))
        } catch (e: UtilstrekkeligOpptjeningException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_OPPTJENING)
        } catch (e: UtilstrekkeligTrygdetidException) {
            problem(e, type = ProblemType.UTILSTREKKELIG_TRYGDETID)
        } catch (e: BadRequestException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: BadSpecException) {
            problem(e, type = e.problemType)
        } catch (e: DateTimeParseException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: EgressException) {
            problem(e, type = if (e.isClientError) ProblemType.ANNEN_SERVERFEIL else ProblemType.TREDJEPARTSFEIL)
        } catch (e: InternDataInkonsistensException) {
            problem(e, type = ProblemType.INTERN_DATA_INKONSISTENS)
        } catch (e: InvalidArgumentException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: InvalidEnumValueException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: PersonForGammelException) {
            problem(e, type = ProblemType.PERSON_FOR_HOEY_ALDER)
        } catch (e: PersonForUngException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: RegelmotorValideringException) {
            problem(e, type = ProblemType.ANNEN_KLIENTFEIL)
        } catch (e: TidsbegrensetOffentligAfpAvslaattException) {
            problem(e, type = e.aarsak?.problemType ?: ProblemType.ANNEN_KLIENTFEIL)
        }

    private fun simuleringSpec(spec: TidsbegrensetAfpSpec): Simulering =
        specCreator.createSpec(
            uttakFom = spec.uttakFom,
            personinfo = spec.personopplysninger,
            opptjeningListe = spec.opptjeningListe
        )

    private companion object {
        private fun fppResult(result: TidsbegrensetAfpSimuleringResult) =
            FppSimuleringResult(
                afpOrdning = result.afpOrdning,
                beregnetAfp = result.beregnetAfp,
                problem = result.problem
            )

        private fun serviceberegningResult(result: TidsbegrensetAfpSimuleringResult) =
            ServiceberegningAfpResult(
                beregnetAfp = result.beregnetAfp,
                opptjeningListe = result.opptjeningListe,
                problem = result.problem
            )

        private fun problem(e: RuntimeException, type: ProblemType) =
            TidsbegrensetAfpSimuleringResult(
                afpOrdning = null,
                beregnetAfp = null,
                opptjeningListe = emptyList(),
                problem = Problem(type, beskrivelse = e.javaClass.simpleName)
            )
    }
}
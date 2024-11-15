package no.nav.pensjon.simulator.core.person.eps

import mu.KotlinLogging
import no.nav.pensjon.simulator.core.beregn.InntektType
import no.nav.pensjon.simulator.core.domain.GrunnlagKilde
import no.nav.pensjon.simulator.core.domain.SimuleringType
import no.nav.pensjon.simulator.core.domain.SivilstatusType
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Inntektsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.kode.GrunnlagKildeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.InntektTypeCti
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.fromLocalDate
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeDay
import no.nav.pensjon.simulator.core.person.PersongrunnlagMapper
import no.nav.pensjon.simulator.core.spec.SimuleringSpec
import no.nav.pensjon.simulator.core.util.toLocalDate
import no.nav.pensjon.simulator.person.PersonService
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

/**
 * Functionality related to 'ektefelle/partner/samboer' (EPS).
 */
@Service
class EpsService(
    private val personService: PersonService,
    private val persongrunnlagMapper: PersongrunnlagMapper
) {
    private val log = KotlinLogging.logger {}

    // OpprettKravHodeHelper.opprettPersongrunnlagForEPS
    fun addPersongrunnlagForEpsToKravhode(spec: SimuleringSpec, kravhode: Kravhode, grunnbeloep: Int) {
        if (EnumSet.of(SimuleringType.ALDER_M_GJEN, SimuleringType.ENDR_ALDER_M_GJEN).contains(spec.type)) {
            //TODO createPersongrunnlagInCaseOfGjenlevenderett(simulering, kravhode)
            with("Simulering for gjenlevende is not supported") {
                log.error { this }
                throw RuntimeException(this)
            }
        } else if (erEps(spec.sivilstatus)) {
            kravhode.persongrunnlagListe.add(persongrunnlagBasedOnSivilstatus(spec, grunnbeloep))
        }
    }

    // OpprettKravHodeHelper.createPersongrunnlagBasedOnSivilstatus
    private fun persongrunnlagBasedOnSivilstatus(spec: SimuleringSpec, grunnbeloep: Int): Persongrunnlag {
        val grunnlag = persongrunnlagMapper.mapToEpsPersongrunnlag(
            sivilstatus = spec.sivilstatus,
            foedselsdato = foedselDato(spec)
        )

        if (spec.epsHarInntektOver2G) {
            val today = LocalDate.now()

            val foersteUttakDato: LocalDate =
                spec.foersteUttakDato?.let { if (isBeforeDay(it, today)) it else today } ?: today

            grunnlag.inntektsgrunnlagListe.add(
                epsInntektsgrunnlag(grunnbeloep, foersteUttakAar = foersteUttakDato.year)
            )
        }

        return grunnlag
    }

    private fun foedselDato(spec: SimuleringSpec): LocalDate =
        spec.pid?.let(personService::person)?.fodselsdato.toLocalDate() ?: foersteDag(spec.foedselAar)

    companion object {
        const val EPS_GRUNNBELOEP_MULTIPLIER = 3 // greater than 2 (due to 2G income limit for EPS)

        // OpprettKravHodeHelper.createInntektsgrunnlagForBrukerOrEps (special EPS variant)
        private fun epsInntektsgrunnlag(grunnbeloep: Int, foersteUttakAar: Int) =
            Inntektsgrunnlag().apply {
                belop = EPS_GRUNNBELOEP_MULTIPLIER * grunnbeloep
                fom = fromLocalDate(foersteDag(foersteUttakAar))
                tom = null
                grunnlagKilde = GrunnlagKildeCti(GrunnlagKilde.BRUKER.name)
                inntektType = InntektTypeCti(InntektType.FPI.name)
                bruk = true
            }

        private fun erEps(sivilstatus: SivilstatusType) =
            EnumSet.of(SivilstatusType.GIFT, SivilstatusType.REPA, SivilstatusType.SAMB).contains(sivilstatus)
    }
}

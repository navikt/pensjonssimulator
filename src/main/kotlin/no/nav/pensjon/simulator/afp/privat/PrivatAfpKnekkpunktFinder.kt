package no.nav.pensjon.simulator.afp.privat

import no.nav.pensjon.simulator.afp.privat.PrivatAfpBeregner.Companion.AFP_ALDER
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Opptjeningsgrunnlag
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.findLatestDateByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isSameDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.lastDayOfMonthUserTurnsGivenAge
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.PensjonTidUtil.OPPTJENING_ETTERSLEP_ANTALL_AAR
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.time.Time
import org.springframework.stereotype.Service
import java.time.LocalDate
import java.util.*

@Service
class PrivatAfpKnekkpunktFinder(
    private val normalderService: NormertPensjonsalderService,
    private val time: Time
) {
    fun findKnekkpunktDatoer(
        foersteUttakDato: LocalDate?,
        soekerGrunnlag: Persongrunnlag,
        privatAfpFoersteVirkning: LocalDate?,
        gjelderOmsorg: Boolean
    ): SortedSet<LocalDate> {
        // A knekkpunkt should be created for the year the user turns 63 years of age if and only if the user had
        // opptjening the year he turned 61 years old, and hence we need to find that opptjening.
        val relevantOpptjeningAar =
            yearUserTurnsGivenAge(
                foedselsdato = soekerGrunnlag.fodselsdato!!,
                age = AFP_ALDER - OPPTJENING_ETTERSLEP_ANTALL_AAR
            )

        val relevantOpptjeningGrunnlag =
            soekerGrunnlag.opptjeningsgrunnlagListe.firstOrNull { it.ar == relevantOpptjeningAar }

        return findPrivatAfpPerioder(
            foersteUttakDato,
            foedselsdato = soekerGrunnlag.fodselsdato?.toNorwegianLocalDate(),
            opptjeningGrunnlag = relevantOpptjeningGrunnlag,
            privatAfpFoersteVirkning,
            gjelderOmsorg
        )
    }

    private fun findPrivatAfpPerioder(
        foersteUttakDato: LocalDate?,
        foedselsdato: LocalDate?,
        opptjeningGrunnlag: Opptjeningsgrunnlag?,
        privatAfpFoersteVirkning: LocalDate?,
        gjelderOmsorg: Boolean
    ): SortedSet<LocalDate> {
        val knekkpunktDatoer: SortedSet<LocalDate> = TreeSet()

        // Første uttaksdato, but only if equal to første virk for privat AFP
        if (isSameDay(foersteUttakDato, privatAfpFoersteVirkning)) {
            knekkpunktDatoer.add(foersteUttakDato!!)
        }

        // Jan 1st the year the user turns 63 years old, but only if bruker has opptjening the year he/she turns 61 years of age
        opptjeningGrunnlag?.let {
            if (gjelderOmsorg || it.pi > 0) {
                knekkpunktDatoer.add(LocalDate.of(yearUserTurnsGivenAge(foedselsdato!!, AFP_ALDER), 1, 1))
            }
        }

        val sisteDagIMaanedenForNormalder: LocalDate = sisteDagIMaanedenForNormalder(foedselsdato!!)
        knekkpunktDatoer.add(sisteDagIMaanedenForNormalder.plusDays(1))

        // Returns only unique values that must be in future and not before første virk privat AFP
        return knekkpunktDatoer.tailSet(findLatestDateByDay(time.today(), privatAfpFoersteVirkning))
    }

    // no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.SimuleringEtter2011Utils.lastDayOfMonthUserTurns67
    private fun sisteDagIMaanedenForNormalder(foedselsdato: LocalDate): LocalDate =
        lastDayOfMonthUserTurnsGivenAge(
            foedselsdato.toNorwegianDateAtNoon(),
            alder = normalderService.normalder(foedselsdato)
        ).toNorwegianLocalDate()
}

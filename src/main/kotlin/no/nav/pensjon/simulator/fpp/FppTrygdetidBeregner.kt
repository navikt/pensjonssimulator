package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Utenlandsopphold
import no.nav.pensjon.simulator.core.spec.UtlandPeriodeConverter.TRYGDETID_MINSTEALDER_AAR
import no.nav.pensjon.simulator.tech.time.DateUtil.MAANEDER_PER_AAR
import no.nav.pensjon.simulator.trygdetid.TrygdetidUtil.FULL_TRYGDETID_ANTALL_AAR
import java.time.LocalDate
import java.time.Period

/**
 * Opptjeningsperiode for brukeren er f.o.m fylte 16 år t.o.m. 31.12. året for fylte 66 år.
 * Utenlandsopphold fratrekkes denne perioden; dette gir trygdetiden.
 * Trygdetiden avrundes til hele år.
 */
object FppTrygdetidBeregner {

    private val maxOpptjeningsperiode = Period.of(OPPTJENING_MAX_ANTALL_AAR, 0, 0)

    /**
     * Opptjeningsperiode = 67 - 16 - (periode 1.1. til fødselsdato) - (periode utenlands)
     * Trygdetid = Opptjeningsperiode avrundet til helt antall år og begrenset oppover til 40 år og nedover til 0
     * Avrunding: 6 måneder eller mer avrundes oppover til helt år, mens mindre avrundes nedover
     */
    fun trygdetidAntallAar(
        foedselsdato: LocalDate,
        utenlandsoppholdListe: List<Utenlandsopphold>,
        flyktning: Boolean
    ): Int =
        if (flyktning)
            FULL_TRYGDETID_ANTALL_AAR
        else
            antallAar(periode = nettoOpptjeningsperiode(foedselsdato, utenlandsoppholdListe))
                .coerceAtLeast(0) // NB: Ikke MINIMUM_TRYGDETID_ANTALL_AAR
                .coerceAtMost(FULL_TRYGDETID_ANTALL_AAR)

    private fun nettoOpptjeningsperiode(
        foedselsdato: LocalDate,
        utenlandsoppholdListe: List<Utenlandsopphold>
    ): Period {
        val periodeTilFoedselsdato = Period.between(LocalDate.of(foedselsdato.year, 1, 1), foedselsdato)
        val nettoPeriode = maxOpptjeningsperiode - periodeTilFoedselsdato - samletOpphold(utenlandsoppholdListe)
        return nettoPeriode.normalized()
    }

    private fun samletOpphold(utenlandsoppholdListe: List<Utenlandsopphold>): Period {
        var periode = Period.of(0, 0, 0)

        utenlandsoppholdListe.forEach {
            periode += Period.between(it.fomLd, it.tomLd).plusDays(1) // +1 siden 'til og med'
        }

        return periode
    }

    private fun antallAar(periode: Period): Int =
        if (periode.months + maanedKorreksjon(periode) < AVRUNDINGSGRENSE)
            periode.years
        else
            periode.years + 1

    /**
     * Siden 'Period' kan inneholde negative dagverdier, må antall måneder korrigeres i henhold til dette.
     * Her antas det at en måned inneholder 30 dager.
     */
    private fun maanedKorreksjon(periode: Period): Int =
        when {
            periode.days < 0 -> periode.days / DAGER_PER_MAANED - 1
            periode.days > 30 -> -periode.days / DAGER_PER_MAANED
            else -> 0
        }

    private const val OPPTJENING_MAX_ANTALL_AAR: Int = 67 - TRYGDETID_MINSTEALDER_AAR
    private const val DAGER_PER_MAANED: Int = 30
    private const val AVRUNDINGSGRENSE: Int = MAANEDER_PER_AAR / 2
}
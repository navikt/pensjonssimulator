package no.nav.pensjon.simulator.fpp

import no.nav.pensjon.simulator.core.domain.regler.enum.GrunnlagsrolleEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum.AFP
import no.nav.pensjon.simulator.core.domain.regler.enum.SimuleringTypeEnum.ALDER
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simulering
import no.nav.pensjon.simulator.core.exception.ImplementationUnrecoverableException
import no.nav.pensjon.simulator.core.exception.PersonForUngException
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.fpp.FppSimuleringUtil.persongrunnlagForRolle
import java.time.LocalDate

object FppSimuleringSpecValidator {
    private const val AFP_MIN_AGE = 62
    private const val AP_MIN_AGE = 67 //TODO normert aldersgrense

    fun validate(spec: Simulering) {
        val simuleringType: SimuleringTypeEnum = spec.simuleringTypeEnum
            ?: throw ImplementationUnrecoverableException("simulering.simuleringType")

        if (spec.uttaksdato == null)
            throw ImplementationUnrecoverableException("simulering.uttaksdato")

        if (AFP == simuleringType && spec.afpOrdningEnum == null)
            throw ImplementationUnrecoverableException("simulering.afpordning")

        if (spec.persongrunnlagListe.isEmpty())
            throw ImplementationUnrecoverableException("simulering.persongrunnlagListe")

        if (spec.persongrunnlagListe.any { it.personDetaljListe.isEmpty() })
            throw ImplementationUnrecoverableException("simulering.persongrunnlagListe.persondetaljliste")

        val soeker: Persongrunnlag =
            persongrunnlagForRolle(grunnlagListe = spec.persongrunnlagListe, rolle = GrunnlagsrolleEnum.SOKER)
                ?: throw ImplementationUnrecoverableException("simulering.persongrunnlagListe.soeker")

        val soekersFoedselsdato: LocalDate = soeker.fodselsdato?.toNorwegianLocalDate()
            ?: throw ImplementationUnrecoverableException("simulering.persongrunnlagListe.soeker.fodselsdato")

        val uttaksdato: LocalDate = spec.uttaksdato!!.toNorwegianLocalDate()

        if (simuleringType == ALDER) {
            //TODO normert aldersgrense
            if (forUng(soekersFoedselsdato, uttaksdato, minimumAlderAar = AP_MIN_AGE))
                throw PersonForUngException("Alderspensjon $AP_MIN_AGE")
        }

        if (simuleringType == AFP) {
            //TODO normert aldersgrense
            if (forUng(soekersFoedselsdato, uttaksdato, minimumAlderAar = AFP_MIN_AGE))
                throw PersonForUngException("AFP $AFP_MIN_AGE")
        }
    }

    private fun forUng(foedselsdato: LocalDate, dato: LocalDate, minimumAlderAar: Int): Boolean {
        val uttaksalderAar = dato.year - foedselsdato.year

        return uttaksalderAar < minimumAlderAar ||
                uttaksalderAar == minimumAlderAar && dato.monthValue <= foedselsdato.monthValue
    }
}

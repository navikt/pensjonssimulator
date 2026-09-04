package no.nav.pensjon.simulator.core.result

import no.nav.pensjon.simulator.core.domain.regler.beregning.Poengtall
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2016
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAlderspensjon2025
import no.nav.pensjon.simulator.core.domain.regler.enum.BeholdningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.DagpengetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.OpptjeningtypeEnum
import no.nav.pensjon.simulator.core.domain.regler.enum.UforetypeEnum
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.*
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.LOCAL_ETERNITY
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.intersectsWithPossiblyOpenEndings
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isBeforeByDay
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.isDateInPeriod
import no.nav.pensjon.simulator.tech.time.DateUtil.foersteDag
import no.nav.pensjon.simulator.tech.time.DateUtil.sisteDag
import java.time.LocalDate

object SimulertOpptjeningMapper {

    /**
     * The logic behind useNullAsDefaultPensjonspoeng is found in PEN:
     * SimuleringEtter2011ResultatMapper.mapToSimulertOpptjening
     */
    fun simulertOpptjening(
        aar: Int,
        soekerGrunnlag: Persongrunnlag,
        forrigeAlderspensjonsresultat: AbstraktBeregningsResultat?,
        resultatListe: List<AbstraktBeregningsResultat>,
        poengtallListe: List<Poengtall>
    ): SimulertOpptjening {
        val opptjeningsgrunnlagListe = soekerGrunnlag.opptjeningsgrunnlagListe
        val dagpengegrunnlagListe = soekerGrunnlag.dagpengegrunnlagListe

        return SimulertOpptjening(
            pensjonsgivendeInntekt = pensjonsgivendeInntektForAar(opptjeningsgrunnlagListe, aar)?.pi ?: 0,
            kalenderAar = aar,
            pensjonsgivendeInntektPensjonspoeng = forAar(poengtallListe, aar)?.pp ?: 0.0,
            omsorgPensjonspoeng = omsorgspoengForAar(opptjeningsgrunnlagListe, aar),
            pensjonBeholdning = pensjonsbeholdning(
                aar,
                soekerGrunnlag,
                forrigeAlderspensjonsresultat,
                resultatListe
            )?.totalbelop?.toInt(),
            omsorg = harOmsorgsgrunnlagForAar(soekerGrunnlag.omsorgsgrunnlagListe, aar),
            dagpenger = forAarOgType(liste = dagpengegrunnlagListe, aar, type = DagpengetypeEnum.DP),
            dagpengerFiskere = forAarOgType(liste = dagpengegrunnlagListe, aar, type = DagpengetypeEnum.DP_FF),
            foerstegangstjeneste = soekerGrunnlag.forstegangstjenestegrunnlag?.let {
                harTjenestegjortAaret(it.periodeListe, aar)
            },
            harUfoere = soekerGrunnlag.uforeHistorikk?.let {
                tidligsteOverlappendeMedAaret(relevantePerioder(it.uforeperiodeListe), aar) != null
            },
            harOffentligAfp = harOffentligAfp(soekerGrunnlag.afpHistorikkListe, aar),
        )
    }

    //TODO: Sjekk hvilken AFP-type denne funksjonen dekker
    private fun harOffentligAfp(afpHistorikkListe: List<AfpHistorikk>, aar: Int) =
        afpHistorikkListe.isNotEmpty()
                && overlapperMedAaret(historikk = afpHistorikkListe[0], aar)

    /**
     * Argmentet 'grunnlag' inneholder faktisk opptjening, samt estimert framtidig opptjening.
     * Argumentet 'resultatListe' inneholder beregnet framtidig opptjening, basert på grunnlaget og pensjonsuttak.
     * Det er dermed sistnevnte som inneholder de 'riktigste' verdiene (da de tar hensyn til uttak).
     * Merk at det er beholdningen på årets siste dag som brukes, da denne tar i betraktning endringer som har
     * skjedd i løpet av året (f.eks. regulering og pensjonsuttak).
     */
    private fun pensjonsbeholdning(
        aar: Int,
        grunnlag: Persongrunnlag,
        forrigeAlderspensjonsresultat: AbstraktBeregningsResultat?,
        resultatListe: List<AbstraktBeregningsResultat>
    ): Pensjonsbeholdning? {
        val dato = sisteDag(aar)

        return (gjeldendeForDato(resultatListe, dato)
            ?: gjeldendeForDato(forrigeAlderspensjonsresultat, dato))
            ?.let(::alderspensjonsresultat2025)?.beregningKapittel20?.beholdninger?.beholdninger
            ?.let(::sistePensjonsbeholdning)
            ?: pensjonsbeholdningForAar(grunnlag.beholdninger, aar)
    }

    /**
     * NB: Det er typisk to beholdninger per år (før og etter regulering);
     * her plukkes den første i listen, men man vet ikke om den er før eller etter regulering
     */
    private fun pensjonsbeholdningForAar(liste: List<Pensjonsbeholdning>, aar: Int): Pensjonsbeholdning? =
        liste.firstOrNull { it.ar == aar && it.beholdningsTypeEnum == BeholdningtypeEnum.PEN_B }

    private fun sistePensjonsbeholdning(liste: List<Beholdning>): Pensjonsbeholdning? =
        liste.filter { it.beholdningsTypeEnum == BeholdningtypeEnum.PEN_B }
            .maxByOrNull { it.ar } as? Pensjonsbeholdning

    private fun pensjonsgivendeInntektForAar(liste: List<Opptjeningsgrunnlag>, aar: Int): Opptjeningsgrunnlag? =
        liste.firstOrNull { it.ar == aar && it.opptjeningTypeEnum == OpptjeningtypeEnum.PPI }

    private fun omsorgspoengForAar(liste: List<Opptjeningsgrunnlag>, aar: Int): Double {
        var omsorgspoeng = 0.0
        var priority = Int.MAX_VALUE

        val omsorgTypes = arrayOf(
            OpptjeningtypeEnum.OSFE,
            OpptjeningtypeEnum.OBO7H,
            OpptjeningtypeEnum.OBU7,
            OpptjeningtypeEnum.OBO6H,
            OpptjeningtypeEnum.OBU6
        )

        val prioritisedOmsorgTypeList = ArrayList(listOf(*omsorgTypes))
        var tempPriority: Int

        for (grunnlag in liste) {
            if (grunnlag.ar == aar) {
                tempPriority = prioritisedOmsorgTypeList.indexOf(grunnlag.opptjeningTypeEnum)

                if (tempPriority != -1 && tempPriority < priority) {
                    priority = tempPriority
                    omsorgspoeng = grunnlag.pp
                }
            }
        }

        return omsorgspoeng
    }

    private fun alderspensjonsresultat2025(resultat: AbstraktBeregningsResultat): BeregningsResultatAlderspensjon2025? =
        resultat as? BeregningsResultatAlderspensjon2025
            ?: (resultat as? BeregningsResultatAlderspensjon2016)?.beregningsResultat2025

    private fun gjeldendeForDato(list: List<AbstraktBeregningsResultat>, dato: LocalDate): AbstraktBeregningsResultat? =
        list.firstOrNull { gjelderDato(it, dato) }

    private fun gjeldendeForDato(resultat: AbstraktBeregningsResultat?, dato: LocalDate): AbstraktBeregningsResultat? =
        resultat?.let { if (gjelderDato(it, dato)) it else null }

    private fun gjelderDato(resultat: AbstraktBeregningsResultat, dato: LocalDate): Boolean =
        isDateInPeriod(dato, resultat.virkFomLd, resultat.virkTomLd)

    private fun forAarOgType(liste: List<Dagpengegrunnlag>, aar: Int, type: DagpengetypeEnum): Boolean =
        liste.any { it.ar == aar && it.dagpengetypeEnum == type }

    private fun harOmsorgsgrunnlagForAar(liste: List<Omsorgsgrunnlag>, aar: Int): Boolean =
        forAar(liste, aar) != null

    private fun harTjenestegjortAaret(liste: List<ForstegangstjenestePeriode>, aar: Int): Boolean =
        forAar(liste, aar) != null

    private fun forAar(liste: List<ForstegangstjenestePeriode>, aar: Int): ForstegangstjenestePeriode? =
        liste.firstOrNull { it.fomDatoLd?.year == aar }

    private fun forAar(liste: List<Omsorgsgrunnlag>, aar: Int): Omsorgsgrunnlag? =
        liste.firstOrNull { it.ar == aar }

    private fun forAar(liste: List<Poengtall>, aar: Int): Poengtall? =
        liste.firstOrNull { it.ar == aar }

    private fun overlapperMedAaret(historikk: AfpHistorikk, aar: Int): Boolean =
        intersectsWithPossiblyOpenEndings(
            o1Start = foersteDag(aar),
            o1End = sisteDag(aar),
            o2Start = historikk.virkFomLd,
            o2End = historikk.virkTomLd,
            considerContactByDayAsIntersection = true
        )

    private fun relevantePerioder(liste: List<Uforeperiode>): List<Uforeperiode> =
        liste.filter { it.uforeTypeEnum != UforetypeEnum.VIRK_IKKE_UFOR }

    private fun tidligsteOverlappendeMedAaret(liste: List<Uforeperiode>, aar: Int): Uforeperiode? =
        tidligsteOverlappende(liste, foersteDag(aar), sisteDag(aar))

    private fun tidligsteOverlappende(liste: List<Uforeperiode>, fom: LocalDate, tom: LocalDate): Uforeperiode? {
        var result: Uforeperiode? = null
        var earliestDate: LocalDate? = LOCAL_ETERNITY

        for (element in liste) {
            if (intersectsWithPossiblyOpenEndings(fom, tom, element.ufgFomLd, element.ufgTomLd, true)) {
                if (isBeforeByDay(element.ufgFomLd, earliestDate, false)) {
                    earliestDate = element.ufgFomLd
                    result = element
                }
            }
        }

        return result
    }
}
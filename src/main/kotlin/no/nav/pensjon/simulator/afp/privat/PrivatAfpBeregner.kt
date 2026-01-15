package no.nav.pensjon.simulator.afp.privat

import no.nav.pensjon.simulator.core.SimulatorContext
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.PenPerson
import no.nav.pensjon.simulator.core.domain.regler.beregning2011.BeregningsResultatAfpPrivat
import no.nav.pensjon.simulator.core.domain.regler.enum.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Persongrunnlag
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravhode
import no.nav.pensjon.simulator.core.domain.regler.krav.Kravlinje
import no.nav.pensjon.simulator.core.domain.regler.to.BeregnAfpPrivatRequest
import no.nav.pensjon.simulator.core.domain.regler.vedtak.VilkarsVedtak
import no.nav.pensjon.simulator.core.domain.reglerextend.beregning2011.copy
import no.nav.pensjon.simulator.core.exception.RegelmotorValideringException
import no.nav.pensjon.simulator.core.krav.KravlinjeStatus
import no.nav.pensjon.simulator.core.legacy.util.DateUtil.yearUserTurnsGivenAge
import no.nav.pensjon.simulator.core.util.toNorwegianDateAtNoon
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.generelt.GenerelleDataHolder
import no.nav.pensjon.simulator.tech.time.Time
import no.nav.pensjon.simulator.validity.BadSpecException
import org.springframework.stereotype.Component
import java.time.LocalDate
import java.util.*

/**
 * PEN: no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.BeregnAfpPrivatHelper
 */
@Component
class PrivatAfpBeregner(
    private val context: SimulatorContext,
    private val generelleDataHolder: GenerelleDataHolder,
    private val knekkpunktFinder: PrivatAfpKnekkpunktFinder,
    private val time: Time
) {
    fun beregnPrivatAfp(spec: PrivatAfpSpec): PrivatAfpResult {
        val foersteVirkning: LocalDate = spec.virkningFom
        val foersteUttakDato: LocalDate? = spec.foersteUttakDato
        val forrigeBeregningResultat: BeregningsResultatAfpPrivat? = spec.forrigePrivatAfpBeregningResult
        val afpKravhode: Kravhode = periodiserGrunnlag(privatAfpKravhode(spec.kravhode))
        val soekerGrunnlag: Persongrunnlag = afpKravhode.hentPersongrunnlagForSoker()

        val knekkpunktDatoer: SortedSet<LocalDate> =
            knekkpunktFinder.findKnekkpunktDatoer(
                foersteUttakDato,
                soekerGrunnlag,
                privatAfpFoersteVirkning = foersteVirkning,
                gjelderOmsorg = spec.gjelderOmsorg
            )

        if (knekkpunktDatoer.isEmpty()) {
            // Ingen knekkpunkter; beregningsresultat fra gjeldende ytelse benyttes videre;
            // not sure if copying needed here (copying was done in PEN legacy code):
            return privatAfpResult(beregningResultat = forrigeBeregningResultat?.copy())
        }

        val afpKravlinje = newKravlinje(soekerGrunnlag.penPerson!!)
        afpKravhode.kravlinjeListe = mutableListOf(afpKravlinje)
        afpKravhode.afpOrdningEnum = AFPtypeEnum.LONHO // any value will do
        val foedselsdato: LocalDate = soekerGrunnlag.fodselsdato!!.toNorwegianLocalDate()
        val satser: PrivatAfpSatser = generelleDataHolder.getPrivatAfpSatser(foersteVirkning, foedselsdato)
        val afpBeholdningDato = foersteUttakDato?.let { calculateAfpBeholdningDato(it, foedselsdato) }
        soekerGrunnlag.replaceBeholdninger(context.beregnOpptjening(afpBeholdningDato, soekerGrunnlag))
        val tidligsteKnekkpunktDato = knekkpunktDatoer.first()

        val afpVedtak = newInnvilgetVedtak(
            soeker = soekerGrunnlag.penPerson!!,
            kravlinje = afpKravlinje,
            foersteVirkningFom = foersteVirkning,
            virkningFom = tidligsteKnekkpunktDato
        )

        val beregningResultatListe = beregnPrivatAfpForHvertKnekkpunkt(
            forrigeBeregningResultat,
            foersteVirkning,
            afpKravhode,
            afpVedtak,
            satser,
            knekkpunktDatoer,
            spec.sakId
        )

        val gjeldendeBeregningResultat = forrigeBeregningResultat?.copy()?.apply {
            virkTom = tidligsteKnekkpunktDato?.minusDays(1)?.toNorwegianDateAtNoon()
        }

        return PrivatAfpResult(beregningResultatListe, gjeldendeBeregningResultat)
    }

    private fun beregnPrivatAfpForHvertKnekkpunkt(
        initialForrigePrivatAfpBeregningResultat: BeregningsResultatAfpPrivat?,
        afpFoersteVirkning: LocalDate?,
        afpKravhode: Kravhode,
        afpVedtak: VilkarsVedtak,
        satser: PrivatAfpSatser,
        knekkpunktDatoer: SortedSet<LocalDate>,
        sakId: Long?
    ): MutableList<BeregningsResultatAfpPrivat> {
        var afpBeregningResultat: BeregningsResultatAfpPrivat? = null
        var forrigeAfpBeregningResultat = initialForrigePrivatAfpBeregningResultat
        val afpBeregningResultatListe: MutableList<BeregningsResultatAfpPrivat> = mutableListOf()

        for (knekkpunktDato in knekkpunktDatoer) {
            // Set TOM date on the previous beregningsresultat to the day before the current knekkpunkt
            if (afpBeregningResultat != null) {
                afpBeregningResultat.virkTom = knekkpunktDato.minusDays(1).toNorwegianDateAtNoon()
            }

            afpBeregningResultat = beregnPrivatAfp(
                forrigeAfpBeregningResultat,
                afpFoersteVirkning,
                afpKravhode,
                afpVedtak,
                satser,
                knekkpunktDato,
                sakId
            )

            forrigeAfpBeregningResultat = afpBeregningResultat
            afpBeregningResultatListe.add(afpBeregningResultat)
        }

        return afpBeregningResultatListe
    }

    private fun beregnPrivatAfp(
        forrigeAfpBeregningResultat: BeregningsResultatAfpPrivat?,
        afpFoersteVirkning: LocalDate?,
        afpKravhode: Kravhode,
        afpVedtak: VilkarsVedtak,
        satser: PrivatAfpSatser,
        knekkpunktDato: LocalDate,
        sakId: Long?
    ): BeregningsResultatAfpPrivat {
        val spec = BeregnAfpPrivatRequest().apply {
            kravhode = afpKravhode
            vilkarsvedtakListe = arrayListOf(afpVedtak)
            virkFom = knekkpunktDato.toNorwegianDateAtNoon()
            ft = satser.forholdstall
            justeringsbelop = satser.justeringsbeloep
            referansebelop = satser.referansebeloep
            ftKompensasjonstillegg = satser.kompensasjonstilleggForholdstall
            sisteAfpPrivatBeregning = forrigeAfpBeregningResultat
            virkFomAfpPrivatUttak = afpFoersteVirkning?.toNorwegianDateAtNoon()
        }

        try {
            return context.beregnPrivatAfp(spec, sakId)
        } catch (e: RegelmotorValideringException) {
            handleException(e, afpVedtak)
        }
    }

    private fun handleException(e: RegelmotorValideringException, vedtak: VilkarsVedtak): Nothing {
        throw if (indikererTrygdetidFeil(e.merknadListe))
            framtidig(dato = vedtak.virkFom)?.let {
                BadSpecException(message = "Personen har et vedtak med virkning f.o.m. $it;" +
                        " uttaksdato må være etter denne datoen")
            } ?: e
        else
            e
    }

    private fun framtidig(dato: Date?): LocalDate? =
        dato?.toNorwegianLocalDate()?.let {
            if (it > time.today()) it else null
        }

    companion object {
        const val AFP_ALDER = 63 //TODO normert?

        /**
         * Feilmeldingskoder fra pensjon-regler.
         */
        private val feilmeldingerSomSammenIndikererTrygdetidFeil = listOf(
            "TRYGDETID_KontrollerTrygdetidLogiskSammenhengBeregningRS.TrygdetidenErIkkeGyldigIBeregningsperioden",
            "TRYGDETID_KontrollerTrygdetidLogiskSammenhengBeregningRS.TrygdetidKapittel20ErIkkeGyldigIBeregningsperioden"
        )

        private fun calculateAfpBeholdningDato(foersteUttakDato: LocalDate, foedselsdato: LocalDate): LocalDate {
            val aarSoekerOppnaarAfpAlder: Int = yearUserTurnsGivenAge(foedselsdato, AFP_ALDER)

            return if (foersteUttakDato.year < aarSoekerOppnaarAfpAlder)
                LocalDate.of(aarSoekerOppnaarAfpAlder, 1, 1)
            else
                foersteUttakDato
        }

        private fun newKravlinje(person: PenPerson) =
            Kravlinje().apply {
                kravlinjeTypeEnum = KravlinjeTypeEnum.AFP_PRIVAT
                hovedKravlinje = KravlinjeTypeEnum.AFP_PRIVAT.erHovedkravlinje
                relatertPerson = person
                kravlinjeStatus = KravlinjeStatus.VILKARSPROVD
                land = LandkodeEnum.NOR
            }

        /**
         * Creates a kravhode specific for privat AFP, based on existing kravhode.
         * NB: Need to copy persongrunnlag (which will be modified in AFP-specific way).
         */
        private fun privatAfpKravhode(kravhode: Kravhode) =
            Kravhode().apply {
                sakType = SakTypeEnum.AFP_PRIVAT
                regelverkTypeEnum = kravhode.regelverkTypeEnum
                persongrunnlagListe.add(
                    Persongrunnlag(
                        source = kravhode.hentPersongrunnlagForSoker(),
                        excludeForsteVirkningsdatoGrunnlag = true,
                        excludeTrygdetidPerioder = true
                    ).also {
                        it.finishInit()
                    }
                )
            }

        private fun privatAfpResult(beregningResultat: BeregningsResultatAfpPrivat?) =
            PrivatAfpResult(
                afpPrivatBeregningsresultatListe = beregningResultat?.let(::listOf).orEmpty().toMutableList(),
                gjeldendeBeregningsresultatAfpPrivat = beregningResultat
            )

        private fun newInnvilgetVedtak(
            soeker: PenPerson,
            kravlinje: Kravlinje,
            foersteVirkningFom: LocalDate?,
            virkningFom: LocalDate
        ) =
            VilkarsVedtak().apply {
                this.forsteVirk = foersteVirkningFom?.toNorwegianDateAtNoon()
                this.penPerson = soeker
                this.kravlinje = kravlinje
                this.kravlinjeTypeEnum = kravlinje.kravlinjeTypeEnum
                this.vilkarsvedtakResultatEnum = VedtakResultatEnum.INNV
                this.virkFom = virkningFom.toNorwegianDateAtNoon()
            }.also {
                it.finishInit()
            }

        private fun periodiserGrunnlag(kravhode: Kravhode): Kravhode {
            kravhode.persongrunnlagListe.forEach {
                if (it.personDetaljListe.isNotEmpty()) {
                    for (personDetalj in it.personDetaljListe) {
                        if (personDetalj.bruk != true) {
                            it.personDetaljListe.remove(personDetalj)
                        }
                    }
                }
            }

            return kravhode
        }

        private fun indikererTrygdetidFeil(merknadListe: List<Merknad>): Boolean =
            feilmeldingerSomSammenIndikererTrygdetidFeil.all {
                anyMatchingKode(merknadListe, kode = it)
            }

        private fun anyMatchingKode(merknadListe: List<Merknad>, kode: String): Boolean =
            merknadListe.any { it.kode == kode }
    }
}

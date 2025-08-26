package no.nav.pensjon.simulator.alderspensjon.api.nav.viapen.acl.v2.result

import no.nav.pensjon.simulator.afp.privat.PrivatAfpPeriode
import no.nav.pensjon.simulator.core.domain.regler.Merknad
import no.nav.pensjon.simulator.core.domain.regler.beregning.*
import no.nav.pensjon.simulator.core.domain.regler.grunnlag.Uttaksgrad
import no.nav.pensjon.simulator.core.domain.regler.simulering.Simuleringsresultat
import no.nav.pensjon.simulator.core.result.*
import no.nav.pensjon.simulator.core.util.toNorwegianDate
import no.nav.pensjon.simulator.core.util.toNorwegianLocalDate
import no.nav.pensjon.simulator.core.util.toNorwegianNoon
import java.util.concurrent.atomic.AtomicLong

/**
 * Anti-corruption layer (ACL).
 * Maps simuleringsresultat from domain to DTO for 'Nav V2' service.
 */
object NavSimuleringResultMapperV2 {

    fun toSimuleringResultV2(source: SimulatorOutput) =
        NavSimuleringResultV2(
            ap = source.alderspensjon?.let(::alderspensjon),
            afpPrivat = source.privatAfpPeriodeListe.map(::privatAfpPeriode),
            afpOffentlig = source.pre2025OffentligAfp?.let(::simuleringResultat),
            opptjeningListe = source.opptjeningListe.map(::opptjening),
            grunnbelop = source.grunnbeloep,
            sivilstand = source.sivilstand,
            epsPensjon = source.epsHarPensjon,
            eps2G = source.epsHarInntektOver2G
        )

    private fun alderspensjon(source: SimulertAlderspensjon) =
        SimulertAlderspensjonV2(
            pensjonsperiodeListe = source.pensjonPeriodeListe.map(::pensjonPeriode),
            uttaksgradListe = source.uttakGradListe.map(::uttaksgrad),
            andelKap19 = source.kapittel19Andel,
            andelKap20 = source.kapittel20Andel
        )

    private fun pensjonPeriode(source: PensjonPeriode) =
        PensjonPeriodeV2(
            belop = source.beloep,
            alder = source.alderAar,
            simulertBeregningsinformasjonListe = source.simulertBeregningInformasjonListe.map(::beregningInformasjon)
        )

    private fun privatAfpPeriode(source: PrivatAfpPeriode) =
        SimulertPrivatAfpPeriodeV2(
            alder = source.alderAar?.let { if (it == 0) null else it },
            belopArlig = source.aarligBeloep,
            belopMnd = source.maanedligBeloep,
            livsvarig = source.livsvarig,
            kronetillegg = source.kronetillegg,
            komptillegg = source.kompensasjonstillegg
        )

    private fun simuleringResultat(source: Simuleringsresultat): SimuleringResultatV2 {
        val beregningerPerId: MutableMap<Long, NavBeregningResultV2> = mutableMapOf()

        source.beregning?.let {
            setIdOnDelberegninger(it, AtomicLong(1))
            collectDelberegninger(it, beregningerPerId)
        }

        return SimuleringResultatV2(
            status = source.statusEnum,
            virk = source.virk?.toNorwegianNoon(),
            beregning = source.beregning?.let(::beregning),
            delberegninger = beregningerPerId,
            merknader = source.merknadListe.map(::merknad)
        )
    }

    // no.nav.pensjon.pen_app.provider.api.pselv.setIdOnDelberegninger in PEN
    private fun setIdOnDelberegninger(beregning: Beregning, counter: AtomicLong) {
        beregning.id = counter.getAndIncrement()
        beregning.delberegningsListe.forEach { setIdOnDelberegninger(it, counter) }
    }

    private fun setIdOnDelberegninger(relasjon: BeregningRelasjon, counter: AtomicLong) {
        relasjon.beregning?.let { setIdOnDelberegninger(it, counter) }
    }

    // no.nav.pensjon.pen_app.provider.api.pselv.collectDelberegninger in PEN
    private fun collectDelberegninger(
        beregning: Beregning,
        collector: MutableMap<Long, NavBeregningResultV2>
    ) {
        beregning.delberegningsListe.forEach {
            collectDelberegninger(it, beregning, collector)
        }
    }

    private fun collectDelberegninger(
        relasjon: BeregningRelasjon,
        beregning: Beregning,
        collector: MutableMap<Long, NavBeregningResultV2>
    ) {
        relasjon.beregning?.let {
            collector[beregning.id] = beregning(it)
            collectDelberegninger(it, collector)
        }
    }

    /**
     * NB: In PEN 'ar' is not mapped from regler to PEN (ref. CommonToPen.mapMerknadListeToPen in PEN),
     * but is assigned in addMerknad in no.nav.domain.pensjon.kjerne.beregning.Poengtall
     */
    private fun merknad(source: Merknad, aar: Int? = null) =
        MerknadV2(
            ar = aar,
            argumentListeString = source.argumentListe.joinToString(separator = "¤"),
            kode = source.kode
        )

    private fun opptjening(source: SimulertOpptjening) =
        SimulertOpptjeningV2(
            arstall = source.kalenderAar,
            pensjonsgivendeInntekt = source.pensjonsgivendeInntekt,
            pensjonsbeholdning = source.pensjonBeholdning,
            pensjonspoengOmsorg = source.omsorgPensjonspoeng,
            pensjonspoengPi = source.pensjonsgivendeInntektPensjonspoeng,
            dagpenger = source.dagpenger,
            dagpengerFiskere = source.dagpengerFiskere,
            forstegangstjeneste = source.foerstegangstjeneste,
            omsorg = source.omsorg,
            harUfore = source.harUfoere,
            harAfpOffentlig = source.harOffentligAfp
        )

    // NB: Bare brukt for gammel offentlig AFP
    private fun beregning(source: Beregning) =
        NavBeregningResultV2(
            merknadliste = source.merknadListe.map(::merknad),
            virkDatoFom = source.virkFom?.toNorwegianLocalDate(),
            virkDatoTom = source.virkTom?.toNorwegianLocalDate(),
            brutto = source.brutto,
            netto = source.netto,
            g = source.g,
            resultatKilde = null, //ResultatKildeEnum.AUTO, // ref. BeregnPensjonUtil.setResultatKilde in PEN
            resultatType = source.resultatTypeEnum,
            afpPensjonsgrad = source.afpPensjonsgrad,
            ytelseskomponenter = source.getBrukteYtelseskomponenter().map(::ytelseKomponent),
            delberegningListe = source.delberegningsListe.map(::delberegning),
            minstepensjonType = source.minstepensjontypeEnum,
            ttAnv = source.tt_anv,
            yug = source.yug,
            ufg = source.ufg
        )

    private fun ytelseKomponent(source: Ytelseskomponent): NavYtelseKomponentV2 {
        val tilleggspensjon = source as? Tilleggspensjon

        return NavYtelseKomponentV2(
            ytelseskomponentType = source.ytelsekomponentTypeEnum,

            // Return distinct merknader,
            // ref. PEN Set<Merknad> merknadSet in no.nav.domain.pensjon.Merknader:
            merknader = source.merknadListe.distinctBy { it.kode }.map(::merknad),

            bruttoPerAr = source.bruttoPerAr,
            netto = source.netto,
            erBrukt = source.brukt,
            opphort = source.opphort,
            formelKode = source.formelKodeEnum,
            spt = tilleggspensjon?.spt?.let(::sluttpoengtall),
            ypt = tilleggspensjon?.ypt?.let(::sluttpoengtall)
        )
    }

    private fun sluttpoengtall(source: Sluttpoengtall) =
        SluttpoengtallV2(
            pt = source.pt,
            poengrekke = source.poengrekke?.let(::poengrekke),
            poengtillegg = source.poengTillegg
        )

    private fun poengrekke(source: Poengrekke) =
        PoengrekkeV2(
            pa = source.pa,
            paF92 = source.pa_f92,
            paE91 = source.pa_e91,
            tpi = source.tpi,
            paa = source.paa,
            poengtallListe = source.poengtallListe.map(::poengtall),
        )

    private fun poengtall(source: Poengtall) =
        PoengtallV2(
            pp = source.pp,
            pia = source.pia,
            pi = source.pi,
            ar = source.ar,
            bruktIBeregning = source.bruktIBeregning,
            gv = source.gv,
            poengtallType = source.poengtallTypeEnum,
            maksUforegrad = source.maksUforegrad,
            merknadListe = source.merknadListe.map {
                // Ref. PEN: addMerknad in no.nav.domain.pensjon.kjerne.beregning.Poengtall:
                merknad(it, source.ar)
            }
            // Not used in PSELV:
            // uforear
        )

    private fun delberegning(source: BeregningRelasjon) =
        BeregningRelasjonV2(
            delBeregning = source.beregning?.id,
            bruk = source.bruk
        )

    private fun beregningInformasjon(source: SimulertBeregningInformasjon) =
        SimulertBeregningInformasjonV2(
            uttaksgrad = source.uttakGrad,
            belopMnd = source.maanedligBeloep,
            startMnd = source.startMaaned,
            spt = source.spt,
            gp = source.grunnpensjon,
            tp = source.tilleggspensjon,
            pt = source.pensjonstillegg,
            ttAnvKap19 = source.tt_anv_kap19,
            ttAnvKap20 = source.tt_anv_kap20,
            paE91 = source.pa_e91,
            paF92 = source.pa_f92,
            forholdstall = source.forholdstall,
            delingstall = source.delingstall,
            pensjonsbeholdningForUttak = source.pensjonBeholdningFoerUttak,
            pensjonsbeholdningEtterUttak = source.pensjonBeholdningEtterUttak,
            basispensjon = source.basispensjon,
            restbasispensjon = source.restBasisPensjon,
            inntektspensjon = source.inntektspensjon,
            garantipensjon = source.garantipensjon,
            garantitillegg = source.garantitillegg,
            apKap19medGJR = source.apKap19medGJR,
            apKap19utenGJR = source.apKap19utenGJR,
            pensjonKap19 = source.kapittel19Pensjon,
            pensjonKap20 = source.kapittel20Pensjon,
            pensjonKap19Vektet = source.vektetKapittel19Pensjon,
            pensjonKap20Vektet = source.vektetKapittel20Pensjon,
            noKap19 = source.nOkap19,
            noKap20 = source.nOkap20,
            gjtAP = source.gjtAP,
            gjtAPKap19 = source.gjtAPKap19,
            minstenivaTilleggIndividuelt = source.individueltMinstenivaaTillegg,
            minstenivaTilleggPensjonistpar = source.pensjonistParMinstenivaaTillegg,
            minstePensjonsnivaSats = source.minstePensjonsnivaSats,
            skjermingstillegg = source.skjermingstillegg
        )

    private fun uttaksgrad(source: Uttaksgrad) =
        UttakGradV2(
            fomDato = source.fomDato?.toNorwegianDate(),
            tomDato = source.tomDato?.toNorwegianDate(),
            uttaksgrad = source.uttaksgrad
            // Not used in PSELV:
            // uttaksgradId
            // uttaksgradKopiert
            // version
        )
}

package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.*

object SimulerOffentligTjenestepensjonMapperV1 {

    fun fromDto(dto: SimulerOffentligTjenestepensjonSpecV1) =
        TjenestepensjonSimuleringPre2025Spec(
            pid = Pid(dto.fnr),
            foedselsdato = dto.fodselsdato,
            sisteTpOrdningsTpNummer = dto.sisteTpnr ?: "",
            simulertOffentligAfp = dto.simulertAFPOffentlig?.let(::simulertOffentligAfp),
            simulertPrivatAfp = dto.simulertAFPPrivat?.let(::simulertPrivatAfp),
            sivilstand = dto.sivilstandkode.internalValue,
            inntekter = dto.inntektListe.map(::inntekt),
            pensjonsbeholdningsperioder = dto.pensjonsbeholdningsperiodeListe.map(::pensjonsbeholdningsperiode),
            simuleringsperioder = dto.simuleringsperiodeListe.map(::simuleringsperiode),
            simuleringsdata = dto.simuleringsdataListe.map(::simuleringsdata),
            tpForhold = dto.tpForholdListe.orEmpty().map(::tpForhold)
        )

    private fun simulertOffentligAfp(dto: SimulertAFPOffentligV1) =
        SimulertOffentligAfp(
            brutto = dto.simulertAFPOffentligBrutto,
            tidligerePensjonsgivendeInntekt = dto.tpi
        )

    private fun simulertPrivatAfp(dto: SimulertAFPPrivatV1) =
        SimulertPrivatAfp(
            totalAfpBeholdning = dto.afpOpptjeningTotalbelop,
            kompensasjonstillegg = dto.kompensasjonstillegg
        )

    private fun inntekt(dto: InntektV1) =
        Inntekt(
            fom = dto.datoFom,
            beloep = dto.inntekt,
        )

    private fun pensjonsbeholdningsperiode(dto: PensjonsbeholdningsperiodeV1) =
        Pensjonsbeholdningsperiode(
            fom = dto.datoFom,
            pensjonsbeholdning = dto.pensjonsbeholdning,
            garantipensjonsbeholdning = dto.garantipensjonsbeholdning,
            garantitilleggsbeholdning = dto.garantitilleggsbeholdning,
        )

    private fun simuleringsperiode(dto: SimuleringsperiodeV1) =
        Simuleringsperiode(
            fom = dto.datoFom,
            folketrygdUttaksgrad = dto.folketrygdUttaksgrad,
            stillingsprosentOffentlig = dto.stillingsprosentOffentlig,
            simulerAFPOffentligEtterfulgtAvAlder = dto.simulerAFPOffentligEtterfulgtAvAlder,
        )

    private fun simuleringsdata(dto: SimuleringsdataV1) =
        Simuleringsdata(
            fom = dto.datoFom,
            andvendtTrygdetid = dto.andvendtTrygdetid,
            poengAarTom1991 = dto.poengArTom1991,
            poengAarFom1992 = dto.poengArFom1992,
            ufoeregradVedOmregning = dto.uforegradVedOmregning,
            basisGrunnpensjon = dto.basisgp,
            basisPensjonstillegg = dto.basispt,
            basisTilleggspensjon = dto.basistp,
            delingstallUttak = dto.delingstallUttak,
            forholdstallUttak = dto.forholdstallUttak,
            sluttpoengtall = dto.sluttpoengtall
        )

    private fun tpForhold(dto: TpForholdV1) =
        TpForhold(
            tpNr = dto.tpnr,
            opptjeningsperioder = dto.opptjeningsperiodeListe.map(::opptjeningsperiode)
        )

    private fun opptjeningsperiode(dto: OpptjeningsperiodeV1) =
        Opptjeningsperiode(
            fom = dto.datoFom,
            tom = dto.datoTom,
            stillingsprosent = dto.stillingsprosent,
            aldersgrense = dto.aldersgrense,
            faktiskHovedloenn = dto.faktiskHovedlonn,
            stillingsuavhengigTilleggsloenn = dto.stillingsuavhengigTilleggslonn
        )
}
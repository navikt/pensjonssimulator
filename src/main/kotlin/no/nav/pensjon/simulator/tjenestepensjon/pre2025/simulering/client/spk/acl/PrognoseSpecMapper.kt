package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.client.spk.acl

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Inntekt
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Opptjeningsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Pensjonsbeholdningsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Simuleringsdata
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.Simuleringsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertOffentligAfp
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.SimulertPrivatAfp
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.TpForhold

object PrognoseSpecMapper {

    fun toDto(spec: TjenestepensjonSimuleringPre2025Spec) =
        HentPrognoseRequestDto(
            fnr = spec.pid.value,
            fodselsdato = spec.foedselsdato,
            sisteTpnr = spec.sisteTpOrdningsTpNummer,
            sprak = "norsk",
            simulertAFPOffentlig = spec.simulertOffentligAfp?.let(::simulertOffentligAfp),
            simulertAFPPrivat = spec.simulertPrivatAfp?.let(::simulertPrivatAfp),
            sivilstandkode = SivilstandCodeEnumDto.fromInternalValue(spec.sivilstand),
            inntektListe = spec.inntekter.map(::inntekt),
            pensjonsbeholdningsperiodeListe = spec.pensjonsbeholdningsperioder.map(::pensjonsbeholdningsperiode),
            simuleringsperiodeListe = spec.simuleringsperioder.map(::simuleringsperiode),
            simuleringsdataListe = spec.simuleringsdata.map(::simuleringsdata),
            tpForholdListe = spec.tpForhold.map(::tjenestepensjonsforhold)
        )

    private fun simulertOffentligAfp(spec: SimulertOffentligAfp) =
        SimulertAFPOffentligDto(
            simulertAFPOffentligBrutto = spec.brutto,
            tpi = spec.tidligerePensjonsgivendeInntekt
        )

    private fun simulertPrivatAfp(spec: SimulertPrivatAfp) =
        SimulertAFPPrivatDto(
            afpOpptjeningTotalbelop = spec.totalAfpBeholdning,
            kompensasjonstillegg = spec.kompensasjonstillegg
        )

    private fun inntekt(inntekt: Inntekt) =
        InntektDto(
            datoFom = inntekt.fom,
            inntekt = inntekt.beloep
        )

    private fun pensjonsbeholdningsperiode(spec: Pensjonsbeholdningsperiode) =
        PensjonsbeholdningsperiodeDto(
            datoFom = spec.fom,
            pensjonsbeholdning = spec.pensjonsbeholdning?.toInt() ?: 0,
            garantipensjonsbeholdning = spec.garantipensjonsbeholdning?.toInt() ?: 0,
            garantitilleggsbeholdning = spec.garantitilleggsbeholdning?.toInt() ?: 0
        )

    private fun simuleringsperiode(spec: Simuleringsperiode) =
        SimuleringsperiodeDto(
            datoFom = spec.fom,
            folketrygdUttaksgrad = spec.folketrygdUttaksgrad,
            stillingsprosentOffentlig = spec.stillingsprosentOffentlig,
            simulerAFPOffentligEtterfulgtAvAlder = spec.simulerAFPOffentligEtterfulgtAvAlder
        )

    private fun simuleringsdata(spec: Simuleringsdata) =
        SimuleringsdataDto(
            datoFom = spec.fom,
            andvendtTrygdetid = spec.andvendtTrygdetid,
            poengArTom1991 = spec.poengAarTom1991,
            poengArFom1992 = spec.poengAarFom1992,
            uforegradVedOmregning = spec.ufoeregradVedOmregning,
            basisgp = spec.basisGrunnpensjon,
            basispt = spec.basisPensjonstillegg,
            basistp = spec.basisTilleggspensjon,
            delingstallUttak = spec.delingstallUttak,
            forholdstallUttak = spec.forholdstallUttak,
            sluttpoengtall = spec.sluttpoengtall
        )

    private fun tjenestepensjonsforhold(spec: TpForhold) =
        TpForholdDto(
            tpnr = spec.tpNr,
            opptjeningsperiodeListe = spec.opptjeningsperioder.map(::opptjeningsperiode)
        )

    private fun opptjeningsperiode(spec: Opptjeningsperiode) =
        OpptjeningsperiodeDto(
            datoFom = spec.fom,
            datoTom = spec.tom,
            stillingsprosent = spec.stillingsprosent.toDouble(),
            aldersgrense = spec.aldersgrense,
            faktiskHovedlonn = spec.faktiskHovedloenn,
            stillingsuavhengigTilleggslonn = spec.stillingsuavhengigTilleggsloenn
        )
}
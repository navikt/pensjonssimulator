package no.nav.pensjon.simulator.tjenestepensjon.pre2025.simulering.acl

import no.nav.pensjon.simulator.tjenestepensjon.pre2025.SimulerOffentligTjenestepensjonResult
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.Utbetalingsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.YtelseCode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Pensjonsbeholdningsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Simuleringsdata
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.Simuleringsperiode
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TjenestepensjonSimuleringPre2025Spec
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.TpForhold

object HentPrognoseMapper {

    fun toDto(spec: TjenestepensjonSimuleringPre2025Spec): HentPrognoseRequestDto {
        return HentPrognoseRequestDto(
            fnr = spec.pid.value,
            fodselsdato = spec.foedselsdato,
            sisteTpnr = spec.sisteTpOrdningsTpNummer,
            sprak = "norsk",
            simulertAFPOffentlig = spec.simulertOffentligAfp?.let {
                SimulertAFPOffentligDto(
                    it.brutto,
                    it.tidligerePensjonsgivendeInntekt
                )
            },
            simulertAFPPrivat = spec.simulertPrivatAfp?.let {
                SimulertAFPPrivatDto(
                    it.totalAfpBeholdning,
                    it.kompensasjonstillegg
                )
            },
            sivilstandkode = spec.sivilstand.remoteDtoSivilstand,
            inntektListe = spec.inntekter.map { InntektDto(it.fom, it.beloep) },
            pensjonsbeholdningsperiodeListe = spec.pensjonsbeholdningsperioder.map { toPensjonsbeholdningsperiode(it) },
            simuleringsperiodeListe = spec.simuleringsperioder.map { toSimuleringsperiode(it) },
            simuleringsdataListe = spec.simuleringsdata.map { toSimuleringsdata(it) },
            tpForholdListe = spec.tpForhold.map { toTpForhold(it) },
        )
    }

    private fun toPensjonsbeholdningsperiode(spec: Pensjonsbeholdningsperiode) = PensjonsbeholdningsperiodeDto(
        datoFom = spec.fom,
        pensjonsbeholdning = spec.pensjonsbeholdning?.toInt() ?: 0,
        garantipensjonsbeholdning = spec.garantipensjonsbeholdning?.toInt() ?: 0,
        garantitilleggsbeholdning = spec.garantitilleggsbeholdning?.toInt() ?: 0,
    )

    private fun toSimuleringsperiode(spec: Simuleringsperiode) =
        SimuleringsperiodeDto(
            datoFom = spec.fom,
            folketrygdUttaksgrad = spec.folketrygdUttaksgrad,
            stillingsprosentOffentlig = spec.stillingsprosentOffentlig,
            simulerAFPOffentligEtterfulgtAvAlder = spec.simulerAFPOffentligEtterfulgtAvAlder,
        )

    private fun toSimuleringsdata(spec: Simuleringsdata) = SimuleringsdataDto(
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
        sluttpoengtall = spec.sluttpoengtall,
    )

    private fun toTpForhold(spec: TpForhold) = TpForholdDto(spec.tpNr, spec.opptjeningsperioder.map {
        OpptjeningsperiodeDto(
            datoFom = it.fom,
            datoTom = it.tom,
            stillingsprosent = it.stillingsprosent.toDouble(),
            aldersgrense = it.aldersgrense,
            faktiskHovedlonn = it.faktiskHovedloenn,
            stillingsuavhengigTilleggslonn = it.stillingsuavhengigTilleggsloenn,
        )
    })

    fun fromDto(dto: HentPrognoseResponseDto) =
        SimulerOffentligTjenestepensjonResult(
            tpnr = dto.tpnr,
            navnOrdning = dto.navnOrdning,
            inkluderteOrdningerListe = dto.inkluderteOrdningerListe,
            leverandorUrl = dto.leverandorUrl,
            utbetalingsperiodeListe = dto.utbetalingsperiodeListe.filterNotNull().map {
                Utbetalingsperiode(
                    uttaksgrad = it.uttaksgrad,
                    arligUtbetaling = it.arligUtbetaling,
                    datoFom = it.datoFom,
                    datoTom = it.datoTom,
                    ytelsekode = YtelseCode.valueOf(it.ytelsekode)
                )
            },
            brukerErIkkeMedlemAvTPOrdning = dto.brukerErIkkeMedlemAvTPOrdning,
            brukerErMedlemAvTPOrdningSomIkkeStoettes = dto.brukerErMedlemAvTPOrdningSomIkkeStoettes,
        )
}
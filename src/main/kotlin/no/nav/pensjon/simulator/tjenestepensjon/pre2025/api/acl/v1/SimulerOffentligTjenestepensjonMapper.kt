package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.*

object SimulerOffentligTjenestepensjonMapper {

    fun fromDto(specV1: SimulerOffentligTjenestepensjonSpecV1): TjenestepensjonSimuleringPre2025Spec {
        return TjenestepensjonSimuleringPre2025Spec(
            pid = Pid(specV1.fnr),
            foedselsdato = specV1.fodselsdato,
            sisteTpOrdningsTpNummer = specV1.sisteTpnr ?: "",
            simulertAfpOffentlig = specV1.simulertAFPOffentlig?.let {
                SimulertAfpOffentlig(
                    brutto = it.simulertAFPOffentligBrutto,
                    tidligerePensjonsgivendeInntekt = it.tpi
                )
            },
            simulertAFPPrivat = specV1.simulertAFPPrivat?.let {
                SimulertAFPPrivat(
                    totalAfpBeholdning = it.afpOpptjeningTotalbelop,
                    kompensasjonstillegg = it.kompensasjonstillegg
                )
            },
            sivilstand = SivilstandKode.fromDto(specV1.sivilstandkode),
            inntekter = specV1.inntektListe.map {
                Inntekt(
                    fom = it.datoFom,
                    beloep = it.inntekt,
                )
            },
            pensjonsbeholdningsperioder = specV1.pensjonsbeholdningsperiodeListe.map {
                Pensjonsbeholdningsperiode(
                    fom = it.datoFom,
                    pensjonsbeholdning = it.pensjonsbeholdning,
                    garantipensjonsbeholdning = it.garantipensjonsbeholdning,
                    garantitilleggsbeholdning = it.garantitilleggsbeholdning,
                )
            },
            simuleringsperioder = specV1.simuleringsperiodeListe.map {
                Simuleringsperiode(
                    fom = it.datoFom,
                    folketrygdUttaksgrad = it.folketrygdUttaksgrad,
                    stillingsprosentOffentlig = it.stillingsprosentOffentlig,
                    simulerAFPOffentligEtterfulgtAvAlder = it.simulerAFPOffentligEtterfulgtAvAlder,
                )
            },
            simuleringsdata = specV1.simuleringsdataListe.map {
                Simuleringsdata(
                    fom = it.datoFom,
                    andvendtTrygdetid = it.andvendtTrygdetid,
                    poengAarTom1991 = it.poengArTom1991,
                    poengAarFom1992 = it.poengArFom1992,
                    ufoeregradVedOmregning = it.uforegradVedOmregning,
                    basisGrunnpensjon = it.basisgp,
                    basisPensjonstillegg = it.basispt,
                    basisTilleggspensjon = it.basistp,
                    delingstallUttak = it.delingstallUttak,
                    forholdstallUttak = it.forholdstallUttak,
                    sluttpoengtall = it.sluttpoengtall
                )
            },
            tpForhold = specV1.tpForholdListe?.map {
                TpForhold(
                    tpNr = it.tpnr,
                    opptjeningsperioder = it.opptjeningsperiodeListe.map { op ->
                        Opptjeningsperiode(
                            fom = op.datoFom,
                            tom = op.datoTom,
                            stillingsprosent = op.stillingsprosent,
                            aldersgrense = op.aldersgrense,
                            faktiskHovedloenn = op.faktiskHovedlonn,
                            stillingsuavhengigTilleggsloenn = op.stillingsuavhengigTilleggslonn,
                        )
                    },
                )
            } ?: emptyList(),
        )
    }
}
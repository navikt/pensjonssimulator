package no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.acl.v1

import no.nav.pensjon.simulator.person.Pid
import no.nav.pensjon.simulator.tjenestepensjon.pre2025.api.*

object SimulerOffentligTjenestepensjonSpecMapperV1 {

    fun fromDto(spec: SimulerOffentligTjenestepensjonSpecV1) =
        TjenestepensjonSimuleringPre2025Spec(
            pid = Pid(spec.fnr),
            foedselsdato = spec.fodselsdato,
            sisteTpOrdningsTpNummer = spec.sisteTpnr ?: "",
            simulertOffentligAfp = spec.simulertAFPOffentlig?.let {
                SimulertOffentligAfp(
                    brutto = it.simulertAFPOffentligBrutto,
                    tidligerePensjonsgivendeInntekt = it.tpi
                )
            },
            simulertPrivatAfp = spec.simulertAFPPrivat?.let {
                SimulertPrivatAfp(
                    totalAfpBeholdning = it.afpOpptjeningTotalbelop,
                    kompensasjonstillegg = it.kompensasjonstillegg
                )
            },
            sivilstand = SivilstandKode.entries.firstOrNull { it.remoteDtoSivilstand.name == spec.sivilstandkode.name }
                ?: throw IllegalArgumentException("Unknown dtoSivilstand: ${spec.sivilstandkode}"),
            inntekter = spec.inntektListe.map {
                Inntekt(
                    fom = it.datoFom,
                    beloep = it.inntekt,
                )
            },
            pensjonsbeholdningsperioder = spec.pensjonsbeholdningsperiodeListe.map {
                Pensjonsbeholdningsperiode(
                    fom = it.datoFom,
                    pensjonsbeholdning = it.pensjonsbeholdning,
                    garantipensjonsbeholdning = it.garantipensjonsbeholdning,
                    garantitilleggsbeholdning = it.garantitilleggsbeholdning,
                )
            },
            simuleringsperioder = spec.simuleringsperiodeListe.map {
                Simuleringsperiode(
                    fom = it.datoFom,
                    folketrygdUttaksgrad = it.folketrygdUttaksgrad,
                    stillingsprosentOffentlig = it.stillingsprosentOffentlig,
                    simulerAFPOffentligEtterfulgtAvAlder = it.simulerAFPOffentligEtterfulgtAvAlder,
                )
            },
            simuleringsdata = spec.simuleringsdataListe.map {
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
            tpForhold = spec.tpForholdListe?.map {
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
            } ?: emptyList()
        )
}

package no.nav.pensjon.simulator.core.spec

import no.nav.pensjon.simulator.core.domain.Avdoed
import no.nav.pensjon.simulator.core.ytelse.LoependeYtelser
import no.nav.pensjon.simulator.krav.KravService
import no.nav.pensjon.simulator.normalder.NormertPensjonsalderService
import no.nav.pensjon.simulator.tech.web.EgressException
import no.nav.pensjon.simulator.uttak.UttakUtil.uttakDato
import no.nav.pensjon.simulator.ytelse.InformasjonOmAvdoed
import org.springframework.stereotype.Component
import java.time.LocalDate

/**
 * "Spec" er inndata fra bruker som spesifiserer grunnlaget for simuleringen.
 * Normalt brukes spesifikasjonen som den er, men i noen tilfeller må dataene endres p.g.a.
 * - at de strider mot lover og regler
 * - at de ikke er i samsvar med eksisterende vedtak eller andre registrerte data
 */
@Component
class SpecOverrider(
    private val kravService: KravService,
    private val normalderService: NormertPensjonsalderService
) {
    fun behandleSpec(
        spec: SimuleringSpec,
        ytelser: LoependeYtelser,
        foedselsdato: LocalDate?
    ): SimuleringSpec =
        behandleHeltUttakDato(
            spec = behandleAvdoed(
                spec = behandleUtenlandsopphold(spec, ytelser), info = ytelser.avdoed
            ), foedselsdato
        )

    /**
     * Når alderspensjon kombineres med tidsbegrenset AFP i offentlig sektor,
     * må alder for helt uttak av alderspensjon være normert pensjonsalder (pr. 2026 er det 67 år).
     */
    private fun behandleHeltUttakDato(spec: SimuleringSpec, foedselsdato: LocalDate?): SimuleringSpec =
        foedselsdato?.let {
            if (spec.gjelderTidsbegrensetOffentligAfp())
                spec.withHeltUttakDato(uttaksdatoVedNormalder(foedselsdato = it))
            else
                spec
        } ?: spec

    /**
     * Hvis spesifikasjonen ikke inneholder informasjon om avdød,
     * så skal slik informasjon hentes fra eventuell eksisterende ytelse.
     * (Dette brukes ved beregning av gjenlevendetillegg.)
     */
    private fun behandleAvdoed(spec: SimuleringSpec, info: InformasjonOmAvdoed?) =
        if (spec.avdoed == null)
            info?.pid?.let {
                spec.withAvdoed(
                    Avdoed(
                        pid = it,
                        antallAarUtenlands = info.antallAarUtenlands ?: 0,
                        inntektFoerDoed = 0, // hypotese: inntekten er irrelevant ved endring
                        doedDato = info.doedsdato ?: throw EgressException("Mangler dødsdato i ytelse"),
                        erMedlemAvFolketrygden = info.harTilstrekkeligMedlemskapIFolketrygden == true,
                        harInntektOver1G = info.aarligPensjonsgivendeInntektErMinst1G == true
                    )
                )
            } ?: spec
        else
            spec

    /**
     * Dersom eksisterende vedtak er uten utenlandsopphold (og dermed har full trygdetid),
     * så ignoreres etterfølgende utenlandsopphold.
     * Dette for å unngå å få feil trygdetid ved simulering av endring av alderspensjon.
     */
    private fun behandleUtenlandsopphold(spec: SimuleringSpec, ytelser: LoependeYtelser): SimuleringSpec =
        if ((spec.utlandAntallAar > 0 || spec.utlandPeriodeListe.isNotEmpty()) && harUtenlandsopphold(ytelser) == false)
            spec.utenUtenlandsopphold()
        else
            spec

    private fun harUtenlandsopphold(ytelser: LoependeYtelser): Boolean? =
        ytelser.forrigeAlderspensjonBeregningResultat?.kravId
            ?.let(kravService::fetchKravhode)
            ?.boddEllerArbeidetIUtlandet

    private fun uttaksdatoVedNormalder(foedselsdato: LocalDate): LocalDate =
        uttakDato(
            foedselsdato,
            uttakAlder = normalderService.normalder(foedselsdato)
        )
}
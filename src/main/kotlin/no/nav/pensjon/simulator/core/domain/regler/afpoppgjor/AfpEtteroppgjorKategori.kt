package no.nav.pensjon.simulator.core.domain.regler.afpoppgjor

import no.nav.pensjon.simulator.core.domain.regler.BatchStatus
import no.nav.pensjon.simulator.core.domain.regler.kode.AFPetteroppgjorBehandlingskodeCti
import no.nav.pensjon.simulator.core.domain.regler.kode.AFPetteroppgjorGruppeCti

class AfpEtteroppgjorKategori(
    /**
     * Nøkkelfelt brukt av PEN tjenestene.
     * PREG skal ta denne inn og levere den tilbake i alle tjenestene sine.
     */
    var vedtakId: Long = 0,

    /**
     * Id til personsobjektet fra PEN
     */
    var penPersonId: Long = 0,

    /**
     * Beregnet inntekt etter opphør
     */
    var beregnetIEO: Int = 0,

    /**
     * Beregnet inntekt før uttak
     */
    var beregnetIFU: Int = 0,

    /**
     * Beregnet FPI i AFP-perioden
     */
    var beregnetFPI: Int = 0,

    /**
     * Beregnet inntekt i AFP-perioden
     */
    var beregnetIIAP: Int = 0,

    /**
     * Beregnet avvik mellom arbeidsinntekt (PGI) og inntekten pensjonen er beregnet etter.
     */
    var inntektsAvvik: Int = 0,

    /**
     * Settes til SPK hvis personen skal overføres til SPK (med i SPK filen).
     * Personen har AFP_STAT og fylte 65 år forrige år.
     * Settes til ANDRE ellers.
     */
    var behandlingskode: AFPetteroppgjorBehandlingskodeCti? = null,

    /**
     * Gruppen dette oppgjøret er kategorisert som; Gruppe 1 - 4
     */
    var gruppe: AFPetteroppgjorGruppeCti? = null,

    /**
     * Status fra PREG per element
     */
    var status: BatchStatus? = null
)

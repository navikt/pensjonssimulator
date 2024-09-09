package no.nav.pensjon.simulator.core.domain.regler.grunnlag

import no.nav.pensjon.simulator.core.domain.regler.kode.DagpengeTypeCti
import java.io.Serializable

class Dagpengegrunnlag(

    var ar: Int = 0,
    var dagpengeType: DagpengeTypeCti? = null,
    var uavkortetDagpengegrunnlag: Int = 0,
    var utbetalteDagpenger: Int = 0,
    var ferietillegg: Int = 0,
    var barnetillegg: Int = 0
) : Serializable {

    constructor(d: Dagpengegrunnlag) : this() {
        this.ar = d.ar
        if (d.dagpengeType != null) {
            this.dagpengeType = DagpengeTypeCti(d.dagpengeType)
        }
        this.uavkortetDagpengegrunnlag = d.uavkortetDagpengegrunnlag
        this.utbetalteDagpenger = d.utbetalteDagpenger
        this.ferietillegg = d.ferietillegg
        this.barnetillegg = d.barnetillegg
    }
}

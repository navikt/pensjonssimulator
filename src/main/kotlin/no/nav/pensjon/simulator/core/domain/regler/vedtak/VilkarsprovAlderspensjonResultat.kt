package no.nav.pensjon.simulator.core.domain.regler.vedtak

import no.nav.pensjon.simulator.core.domain.regler.beregning2011.AbstraktBeregningsResultat
import java.lang.reflect.InvocationTargetException

class VilkarsprovAlderspensjonResultat : AbstraktVilkarsprovResultat {
    var beregningVedUttak: AbstraktBeregningsResultat? = null
    var vilkarsprovInformasjon: VilkarsprovInformasjon? = null

    constructor()

    constructor(
        beregningVedUttak: AbstraktBeregningsResultat? = null,
        vilkarsprovInformasjon: VilkarsprovInformasjon? = null
    ) : super() {
        this.beregningVedUttak = beregningVedUttak
        this.vilkarsprovInformasjon = vilkarsprovInformasjon
    }

    constructor(r: VilkarsprovAlderspensjonResultat) : this() {
        if (r.beregningVedUttak != null) {
            val clazz = r.beregningVedUttak!!.javaClass
            try {
                val constructor = clazz.getConstructor(clazz)
                this.beregningVedUttak = constructor.newInstance(r.beregningVedUttak)
                // System.out.println("VilkarsprovAlderspensjonResultat copy constructor "
                // + this.beregningVedUttak);
            } catch (e: InvocationTargetException) {
                // Vil kastes hvis copy constructor f.eks. ledet til
                // nullpointerexception.
                val cause = e.cause
                if (cause is RuntimeException) {
                    throw cause
                } else {
                    throw RuntimeException(e)
                }
            } catch (e: Exception) {
                // Vil kastes hvis f.eks. copy constructor ikke finnes.
                throw RuntimeException(e)
            }
            if (r.vilkarsprovInformasjon != null) {
                val vilkarsprovInfoClazz = r.vilkarsprovInformasjon!!.javaClass
                try {
                    val vilkarsprovInfoConstructor = vilkarsprovInfoClazz.getConstructor(vilkarsprovInfoClazz)
                    this.vilkarsprovInformasjon = vilkarsprovInfoConstructor.newInstance(r.vilkarsprovInformasjon)
                } catch (e: InvocationTargetException) {
                    val cause = e.cause
                    if (cause is RuntimeException) {
                        throw cause
                    } else {
                        throw RuntimeException(e)
                    }
                } catch (e: Exception) {
                    throw RuntimeException(e)
                }
            }
            // this.vilkarsprovInformasjon = new VilkarsprovInformasjon(r.vilkarsprovInformasjon);
        }
    }

}

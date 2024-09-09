package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.kode.JusteringsTypeCti
import java.io.Serializable
import java.lang.reflect.InvocationTargetException

class JusteringsInformasjon : Serializable {
    var totalJusteringsfaktor: Double = 0.0
    var justeringsTypeCti: JusteringsTypeCti? = null
    var elementer: MutableList<IJustering> = mutableListOf()

    constructor()

    constructor(ji: JusteringsInformasjon) : this() {
        totalJusteringsfaktor = ji.totalJusteringsfaktor
        if (ji.justeringsTypeCti != null) {
            justeringsTypeCti = JusteringsTypeCti(ji.justeringsTypeCti)
        }
        for (ij in ji.elementer) {
            val clazz = ij.javaClass
            try {
                val constructor = clazz.getConstructor(clazz)
                elementer.add(constructor.newInstance(ij) as IJustering)
            } catch (e: InvocationTargetException) {
                //Vil kastes hvis copy constructor f.eks. ledet til nullpointerexception.
                val cause = e.cause
                if (cause is RuntimeException) {
                    throw cause
                } else {
                    throw RuntimeException(e)
                }
            } catch (e: Exception) {
                //Vil kastes hvis f.eks. copy constructor ikke finnes for komponenten.
                throw RuntimeException(e)
            }

        }
    }

    constructor(
            totalJusteringsfaktor: Double = 0.0,
            justeringsTypeCti: JusteringsTypeCti? = null,
            elementer: MutableList<IJustering> = mutableListOf()
    ) {
        this.totalJusteringsfaktor = totalJusteringsfaktor
        this.justeringsTypeCti = justeringsTypeCti
        this.elementer = elementer
    }
}

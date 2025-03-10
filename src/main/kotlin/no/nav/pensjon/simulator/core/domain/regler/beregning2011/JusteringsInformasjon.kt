package no.nav.pensjon.simulator.core.domain.regler.beregning2011

import no.nav.pensjon.simulator.core.domain.regler.enum.JusteringsTypeEnum
import java.io.Serializable
import java.lang.reflect.InvocationTargetException

// 2025-03-10
class JusteringsInformasjon : Serializable {
    var totalJusteringsfaktor: Double = 0.0
    var justeringsTypeEnum: JusteringsTypeEnum? = null
    var elementer: MutableList<IJustering> = mutableListOf()

    constructor()

    constructor(source: JusteringsInformasjon) : this() {
        totalJusteringsfaktor = source.totalJusteringsfaktor
            justeringsTypeEnum = source.justeringsTypeEnum

        for (element in source.elementer) {
            val clazz = element.javaClass
            try {
                val constructor = clazz.getConstructor(clazz)
                elementer.add(constructor.newInstance(element) as IJustering)
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
}

package no.nav.pensjon.simulator.core.legacy.util

import java.io.*

// no.nav.service.pensjon.simulering.support.command.abstractsimulerapfra2011.BeregnAfpPrivatHelper.copy
object CopyUtil {

    @Throws(ClassNotFoundException::class, IOException::class)
    fun copy(originalObject: Any?): Any {
        try {
            val bytes = ByteArrayOutputStream()
            val output = ObjectOutputStream(bytes)
            output.writeObject(originalObject)
            output.flush()
            output.close()

            /*
             * Make an input stream from the byte array and read a copy of the object back in.
             */
            val input = ObjectInputStream(ByteArrayInputStream(bytes.toByteArray()))
            return input.readObject()
        } catch (e: IOException) {
            throw IOException("The object and all its subobjects MUST be serializable.")
        } catch (e: ClassNotFoundException) {
            throw ClassNotFoundException("The object or one of its subclasses cannot be found.", e)
        }
    }
}

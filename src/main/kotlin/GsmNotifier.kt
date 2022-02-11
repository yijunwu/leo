import java.util.*

object GsmNotifier {

    @Throws(RuntimeException::class)
    fun fail(format: String, vararg args: Any?) {
        println(" FAILED")
        println("------------------------------------------------------------")
        System.out.printf(format, *args)
        println()
        println("------------------------------------------------------------")
        throw RuntimeException()
    }

    private var m_Done = false

    @Volatile
    private var m_T0: Long = 0

    @Volatile
    private var m_T1: Long = 0


    @Volatile
    var m_Tab = 0
    var m_Progress = 0

    @Throws(java.lang.Exception::class)


    fun begin(name: String?) {
        System.out.printf("%-46s", name)
        m_Tab = 46
        m_T0 = System.currentTimeMillis()
        m_Progress = 0
    }

    fun finishedOK(format: String, vararg args: Any?) {
        for (i in 0 until m_Tab) print(".")
        System.out.printf(" OK $format", *args)
        println()
    }

    private fun generateRandomMessage(m_TxCount: Int): ByteArray {
        return when (m_TxCount) {
            0 -> "ATE1\n"
            1 -> "AT+COLP=1\n"
            2 -> "ATD10086;\n"
            else -> null
        }?.toByteArray(charset = Charsets.US_ASCII) ?: run {

            val rnd = Random()
            val n: Int = 4 + (rnd.nextInt() and 63)
            val buffer = ByteArray(n + 2)
            //System.out.print("Sending: " + new String(buffer));
            var s = 0
            var i: Int
            i = 0
            while (i < n) {
                val b = (32 + (rnd.nextInt() and 63)).toByte()
                buffer[i] = b
                s += b.toInt()
                i++
            }
            buffer[i++] = (32 + (s and 63)).toByte()
            buffer[i] = '\n'.code.toByte()
            buffer
        }
    }

    fun notify(number: String, content: String): String {

        ATCommandDriver.outQueue.add("ATE1\n")
        Thread.sleep(2_000)
        ATCommandDriver.outQueue.add("AT+COLP=1\n")
        Thread.sleep(2_000)
        ATCommandDriver.outQueue.add("ATD$number;\n")
        try {
            ATCommandDriver.start()
            Thread.sleep(20_000)
        } catch (e: Throwable) {
            e.printStackTrace()
        } finally {
            try {
                ATCommandDriver.closePort()
            } catch (e: Throwable) {
                e.printStackTrace()
            }
        }
        return ""
    }

}
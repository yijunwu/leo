import purejavacomm.CommPortIdentifier
import purejavacomm.NoSuchPortException
import purejavacomm.SerialPort
import purejavacomm.SerialPortEvent
import purejavacomm.SerialPortEventListener
import java.io.IOException
import java.io.InputStream
import java.io.OutputStream
import java.util.*
import java.util.concurrent.LinkedBlockingQueue

object ATCommandDriver {

    val outQueue: Queue<String> = LinkedBlockingQueue()

    val inQueue: Queue<String> = LinkedBlockingQueue()

    const val APPLICATION_NAME = "PureJavaCommTestSuite"

    private val m_TestPortName = "COM3"

    private var m_Port: SerialPort? = null

    private lateinit var m_In: InputStream

    private lateinit var m_Out: OutputStream

    @Volatile
    private var rnd: Random = Random()

    @Volatile
    private var m_BytesReceived = 0

    @Volatile
    private var m_TotalReceived = 0

    @Volatile
    private var m_TxCount = 0

    @Volatile
    private var m_RxCount = 0

    @Volatile
    private var m_ErrorCount = 0
    private const val N = 1000

    private val m_ReceiveBuffer = ByteArray(10000)

    @Throws(Exception::class)
    fun drain(ins: InputStream) {
        Thread.sleep(100)
        var n: Int
        while (ins.available().also { n = it } > 0) {
            for (i in 0 until n) ins.read()
            Thread.sleep(100)
        }
    }

    @Throws(NoSuchPortException::class)
    private fun openPort(portName: String) {
        val portId: CommPortIdentifier = CommPortIdentifier.getPortIdentifier(portName)
        m_Port = portId.open(APPLICATION_NAME, 1000) as SerialPort
        m_Out = m_Port!!.getOutputStream()
        m_In = m_Port!!.getInputStream()
        drain(m_In)
    }

    fun closePort() {
        if (m_Port != null) {
            try {
                m_Out.flush()
                m_Port!!.close()
            } catch (e: IOException) {
                e.printStackTrace()
            } finally {
                m_Port = null
            }
        }
    }

    private fun processBuffer(buffer: ByteArray, n: Int) {
        for (i in 0 until n) {
            val b = buffer[i]
            if (n > buffer.size) {
                m_ErrorCount++
                return
            }
            m_ReceiveBuffer[m_BytesReceived++] = b
            if (b == '\n'.code.toByte()) {
                //System.out.print("Received: " + new String(linebuf, 0, inp));
                var s = 0
                var j: Int
                j = 0
                while (j < m_BytesReceived - 2) {
                    s += m_ReceiveBuffer.get(j)
                    j++
                }
                val cb = (32 + (s and 63)).toByte()
                if (cb != m_ReceiveBuffer.get(j) && m_RxCount > 0) {
                    println("check sum failure")
                    m_ErrorCount++
                }
                m_RxCount++
                m_BytesReceived = 0
            }
        }
    }

    fun run(speed: Int) {
        try {
            var m_Done = false
            rnd = Random()
            m_BytesReceived = 0
            m_TotalReceived = 0
            m_TxCount = 0
            m_RxCount = 0
            m_ErrorCount = 0
            openPort(m_TestPortName)
            m_Port!!.notifyOnDataAvailable(true)
            m_Port!!.notifyOnOutputEmpty(true)
            m_Port!!.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN + SerialPort.FLOWCONTROL_XONXOFF_OUT)
            m_Port!!.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
            val stop = booleanArrayOf(false)
            val m_T0 = System.currentTimeMillis()

            m_Port!!.addEventListener(eventListener)
            while (!m_Done) {
                try {
                    Thread.sleep(100)
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                }
            }
            val m_T1 = System.currentTimeMillis()
            if (m_ErrorCount > 0) fail("checksum sum failure in %d out %d messages", m_ErrorCount, N)
            val cs: Int = m_Port!!.getDataBits() + 2
            val actual: Double = m_TotalReceived * cs * 1000.0 / (m_T1 - m_T0)
            val requested: Int = m_Port!!.getBaudRate()
            finishedOK("average speed %1.0f b/sec at baud rate %d", actual, requested)
        } catch (e: Throwable) {
            println(e)
        } finally {
            closePort()
        }
    }

    fun start() {
        openPort(m_TestPortName)
        Thread {
            var m_Done = false
            rnd = Random()
            m_BytesReceived = 0
            m_TotalReceived = 0
            m_TxCount = 0
            m_RxCount = 0
            m_ErrorCount = 0
            openPort(m_TestPortName)

        }.start()
    }

    val eventListener: SerialPortEventListener = SerialPortEventListener { event ->
        try {
            if (event.eventType == SerialPortEvent.DATA_AVAILABLE) {
                val buffer = ByteArray(m_In.available())
                val n: Int = m_In.read(buffer)
                m_TotalReceived += n
                processBuffer(buffer, n)
                m_RxCount ++
            }
            if (event.eventType == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                val buffer = outQueue.remove().toByteArray(Charsets.US_ASCII)
                m_Out.write(buffer, 0, buffer.size)
                m_TxCount++
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    fun loop(speed: Int) {
        try {
            m_Port!!.notifyOnDataAvailable(true)
            m_Port!!.notifyOnOutputEmpty(true)
            m_Port!!.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN + SerialPort.FLOWCONTROL_XONXOFF_OUT)
            m_Port!!.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
            val m_T0 = System.currentTimeMillis()
            m_Port!!.addEventListener(eventListener)
            while (m_RxCount < N) {
                try {
                    Thread.sleep(100)
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                }
            }
            val m_T1 = System.currentTimeMillis()
            if (m_ErrorCount > 0) throw RuntimeException("checksum sum failure in $m_ErrorCount out $N messages")
            val cs: Int = m_Port!!.getDataBits() + 2
            val actual: Double = m_TotalReceived * cs * 1000.0 / (m_T1 - m_T0)
            val requested: Int = m_Port!!.getBaudRate()
            //finishedOK("average speed %1.0f b/sec at baud rate %d", actual, requested)
        } catch (e: Throwable) {
            println(e)
        } finally {
            closePort()
        }
    }

}
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
    private const val N = 100

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
                m_RxCount++
                m_BytesReceived = 0
            }
        }
    }

    fun start() {
        Thread {
            rnd = Random()
            m_BytesReceived = 0
            m_TotalReceived = 0
            m_TxCount = 0
            m_RxCount = 0
            m_ErrorCount = 0
            openPort(m_TestPortName)
            loop(115200)
        }.start()
    }

    private val eventListener: SerialPortEventListener = SerialPortEventListener { event ->
        try {
            if (event.eventType == SerialPortEvent.DATA_AVAILABLE) {
                val buffer = ByteArray(m_In.available())
                val n: Int = m_In.read(buffer)
                m_TotalReceived += n
                processBuffer(buffer, n)
                println(buffer.toString(Charsets.US_ASCII))
                m_RxCount ++
                sendNext()
            }
            if (event.eventType == SerialPortEvent.OUTPUT_BUFFER_EMPTY) {
                //sendNext()
            }
        } catch (e: java.lang.Exception) {
            e.printStackTrace()
        }
    }

    private fun sendNext() {
        val buffer = outQueue.poll()?.toByteArray(Charsets.US_ASCII)
        if (buffer != null) {
            m_Out.write(buffer, 0, buffer.size)
            m_TxCount++
        }
    }

    fun loop(speed: Int) {
        try {
            m_Port!!.notifyOnDataAvailable(true)
            m_Port!!.notifyOnOutputEmpty(true)
            m_Port!!.setFlowControlMode(SerialPort.FLOWCONTROL_XONXOFF_IN + SerialPort.FLOWCONTROL_XONXOFF_OUT)
            m_Port!!.setSerialPortParams(speed, SerialPort.DATABITS_8, SerialPort.STOPBITS_1, SerialPort.PARITY_NONE)
            val m_T0 = System.currentTimeMillis()
            var m_T1 = System.currentTimeMillis()
            m_Port!!.addEventListener(eventListener)
            sendNext()
            while (m_TxCount < N && m_T1 < m_T0 + 400_000) {
                try {
                    Thread.sleep(100)
                    m_T1 = System.currentTimeMillis()
                } catch (ex: java.lang.Exception) {
                    ex.printStackTrace()
                }
            }

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
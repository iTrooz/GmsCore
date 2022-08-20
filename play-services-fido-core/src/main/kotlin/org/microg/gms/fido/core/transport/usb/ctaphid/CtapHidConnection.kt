package org.microg.gms.fido.core.transport.usb.ctaphid

import android.content.Context
import android.hardware.usb.UsbConstants.USB_DIR_IN
import android.hardware.usb.UsbConstants.USB_DIR_OUT
import android.hardware.usb.UsbDevice
import android.hardware.usb.UsbDeviceConnection
import android.hardware.usb.UsbInterface
import android.hardware.usb.UsbRequest
import android.util.Base64
import android.util.Log
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import kotlinx.coroutines.withTimeout
import org.microg.gms.fido.core.protocol.msgs.Ctap1Command
import org.microg.gms.fido.core.protocol.msgs.Ctap1Request
import org.microg.gms.fido.core.protocol.msgs.Ctap1Response
import org.microg.gms.fido.core.transport.usb.endpoints
import org.microg.gms.fido.core.transport.usb.usbManager
import org.microg.gms.utils.toBase64
import java.nio.ByteBuffer
import kotlin.experimental.and

class CtapHidConnection(
    val context: Context,
    val device: UsbDevice,
    val iface: UsbInterface,
) {
    private var connection: UsbDeviceConnection? = null
    private val inEndpoint = iface.endpoints.first { it.direction == USB_DIR_IN }
    private val outEndpoint = iface.endpoints.first { it.direction == USB_DIR_OUT }
    private var channelIdentifier = 0xffffffff.toInt()
    private var capabilities: Byte = 0

    val hasCtap1Support: Boolean
        get() = capabilities and CtapHidInitResponse.CAPABILITY_NMSG == 0.toByte()

    val hasCtap2Support: Boolean
        get() = capabilities and CtapHidInitResponse.CAPABILITY_CBOR > 0

    val hasWinkSupport: Boolean
        get() = capabilities and CtapHidInitResponse.CAPABILITY_WINK > 0

    suspend fun open(): Boolean {
        Log.d(TAG, "Opening connection")
        connection = context.usbManager?.openDevice(device)
        if (connection?.claimInterface(iface, true) != true) {
            Log.d(TAG, "Failed claiming interface")
            close()
            return false
        }
        val initRequest = CtapHidInitRequest()
        sendRequest(initRequest)
        val initResponse = readResponse()
        if (initResponse !is CtapHidInitResponse || !initResponse.nonce.contentEquals(initRequest.nonce)) {
            Log.d(TAG, "Failed init procedure")
            close()
            return false
        }
        channelIdentifier = initResponse.channelId
        capabilities = initResponse.capabilities
        return true
    }

    suspend fun close() {
        connection?.close()
        connection = null
        channelIdentifier = 0xffffffff.toInt()
        capabilities = 0
    }

    suspend fun sendRequest(request: CtapHidRequest) {
        val connection = connection ?: throw IllegalStateException("Not opened")
        val packets = request.encodePackets(channelIdentifier, outEndpoint.maxPacketSize)
        Log.d(TAG, "Sending $request in ${packets.size} packets")
        val outRequest = UsbRequest()
        outRequest.initialize(connection, outEndpoint)
        for (packet in packets) {
            if (outRequest.queue(ByteBuffer.wrap(packet.bytes), packet.bytes.size)) {
                withContext(Dispatchers.IO) { connection.requestWait() }
                Log.d(TAG, "Sent packet ${packet.bytes.toBase64(Base64.NO_WRAP)}")
            } else {
                throw RuntimeException("Failed queuing packet")
            }
        }
    }

    suspend fun readResponse(timeout: Long = 1000): CtapHidResponse = withTimeout(timeout) {
        val connection = connection ?: throw IllegalStateException("Not opened")
        val inRequest = UsbRequest()
        inRequest.initialize(connection, inEndpoint)
        val packets = mutableListOf<CtapHidPacket>()
        val buffer = ByteBuffer.allocate(inEndpoint.maxPacketSize)
        var initializationPacket: CtapHidInitializationPacket? = null
        while (true) {
            buffer.clear()
            if (inRequest.queue(buffer, inEndpoint.maxPacketSize)) {
                Log.d(TAG, "Reading ${inEndpoint.maxPacketSize} bytes from usb")
                withContext(Dispatchers.IO) { connection.requestWait() }
                Log.d(TAG, "Received packet ${buffer.array().toBase64(Base64.NO_WRAP)}")
                if (initializationPacket == null) {
                    initializationPacket = CtapHidInitializationPacket.decode(buffer.array())
                    packets.add(initializationPacket)
                } else {
                    val continuationPacket = CtapHidContinuationPacket.decode(buffer.array())
                    if (continuationPacket.channelIdentifier == initializationPacket.channelIdentifier) {
                        packets.add(continuationPacket)
                    } else {
                        // Dropping unexpected packet
                    }
                }
                if (packets.sumOf { it.data.size } >= initializationPacket.payloadLength) {
                    if (initializationPacket.channelIdentifier != channelIdentifier) {
                        packets.clear()
                        initializationPacket = null
                    } else {
                        val message = CtapHidMessage.decode(packets)
                        if (message.commandId == CtapHidKeepAliveMessage.COMMAND_ID) {
                            packets.clear()
                            initializationPacket = null
                        } else {
                            val response = CtapHidResponse.parse(message)
                            Log.d(TAG, "Received $response in ${packets.size} packets")
                            return@withTimeout response
                        }
                    }
                }
            } else {
                throw RuntimeException("Failed queuing packet")
            }
        }
        throw RuntimeException("Interrupted")
    }

    suspend fun <Q : Ctap1Request, S : Ctap1Response> runCommand(command: Ctap1Command<Q, S>): S {
        require(hasCtap1Support)
        sendRequest(CtapHidMessageRequest(command.request))
        val response = readResponse()
        if (response is CtapHidMessageResponse) {
            if (response.statusCode == 0x9000.toShort()) {
                return command.decodeResponse(response.statusCode, response.payload)
            }
            throw CtapHidMessageStatusException(response.statusCode)
        }
        throw RuntimeException("Unexpected response: $response")
    }

    suspend fun <R> open(block: suspend (CtapHidConnection) -> R): R {
        if (!open()) throw RuntimeException("Could not open device")
        var exception: Throwable? = null
        try {
            return block(this)
        } catch (e: Throwable) {
            exception = e
            throw e
        } finally {
            when (exception) {
                null -> close()
                else -> try {
                    close()
                } catch (closeException: Throwable) {
                    // cause.addSuppressed(closeException) // ignored here
                }
            }
        }
    }

    companion object {
        const val TAG = "FidoCtapHidConnection"
    }
}

class CtapHidMessageStatusException(val status: Short) : Exception("Received status ${status.toString(16)}")

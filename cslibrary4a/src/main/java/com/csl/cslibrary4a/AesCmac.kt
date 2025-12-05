package com.csl.cslibrary4a

import java.security.InvalidKeyException
import java.security.Key
import java.security.NoSuchAlgorithmException
import javax.crypto.BadPaddingException
import javax.crypto.Cipher
import javax.crypto.IllegalBlockSizeException
import javax.crypto.NoSuchPaddingException
import javax.crypto.ShortBufferException
import javax.crypto.spec.IvParameterSpec
import javax.crypto.spec.SecretKeySpec

class AesCmac @JvmOverloads constructor(length: Int = BLOCK_SIZE) {
    private var macLength = 0
    private var aesCipher: Cipher? = null

    private var buffer: ByteArray = TODO()
    private var bufferCount = 0

    private var k1: ByteArray
    private var k2: ByteArray

    init {
        if (length > BLOCK_SIZE) {
            throw NoSuchAlgorithmException("AES CMAC maximum length is " + BLOCK_SIZE)
        }

        try {
            macLength = length
            aesCipher = Cipher.getInstance("AES/CBC/NOPADDING")
            buffer = ByteArray(BLOCK_SIZE)
        } catch (nspe: NoSuchPaddingException) {
            nspe.printStackTrace()
        }
    }

    private fun doubleSubKey(k: ByteArray): ByteArray {
        val ret = ByteArray(k.size)

        val firstBitSet = ((k[0].toInt() and 0x80) != 0)
        for (i in k.indices) {
            ret[i] = (k[i].toInt() shl 1).toByte()
            if (i + 1 < k.size && ((k[i + 1].toInt() and 0x80) != 0)) {
                ret[i] = (ret[i].toInt() or 0x01).toByte()
            }
        }
        if (firstBitSet) {
            ret[ret.size - 1] = (ret[ret.size - 1].toInt() xor CONSTANT.toInt()).toByte()
        }
        return ret
    }

    @Throws(Exception::class)
    fun init(key: Key?) {
        if (key !is SecretKeySpec) {
            throw InvalidKeyException("Key is not of required type SecretKey.")
        }
        if (key.getAlgorithm() != "AES") {
            throw InvalidKeyException("Key is not an AES key.")
        }
        val iv = byteArrayOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0)
        aesCipher!!.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))

        // First calculate k0 from zero bytes
        val k0 = ByteArray(BLOCK_SIZE)
        try {
            aesCipher!!.update(k0, 0, k0.size, k0, 0)
        } catch (sbe: ShortBufferException) {
        }

        // Calculate values for k1 and k2
        k1 = doubleSubKey(k0)
        k2 = doubleSubKey(k1)
        aesCipher!!.init(Cipher.ENCRYPT_MODE, key, IvParameterSpec(iv))
        bufferCount = 0
    }

    fun updateByte(b: Byte) {
        updateBlock(byteArrayOf(b))
    }

    fun updateBlock(data: ByteArray) {
        var currentOffset = 0

        if (data.size < BLOCK_SIZE - bufferCount) {
            System.arraycopy(data, 0, buffer, bufferCount, data.size)
            bufferCount += data.size
            return
        } else if (bufferCount > 0) {
            System.arraycopy(data, 0, buffer, bufferCount, BLOCK_SIZE - bufferCount)
            try {
                aesCipher!!.update(buffer, 0, BLOCK_SIZE, buffer, 0)
            } catch (sbe: ShortBufferException) {
            }
            currentOffset += BLOCK_SIZE - bufferCount
            bufferCount = 0
        }

        // Transform all the full blocks in data
        while (currentOffset + BLOCK_SIZE < data.size) {
            try {
                aesCipher!!.update(data, currentOffset, BLOCK_SIZE, buffer, 0)
            } catch (sbe: ShortBufferException) {
            }
            currentOffset += BLOCK_SIZE
        }

        // Save the leftover bytes to buffer
        if (currentOffset != data.size) {
            System.arraycopy(data, currentOffset, buffer, 0, data.size - currentOffset)
            bufferCount = data.size - currentOffset
        }
    }

    fun doFinal(): ByteArray {
        var subKey = k1
        if (bufferCount < BLOCK_SIZE) {
            // Add padding and XOR with k2 instead
            buffer[bufferCount] = 0x80.toByte()
            for (i in bufferCount + 1..<BLOCK_SIZE) buffer[i] = 0x00.toByte()
            subKey = k2
        }
        for (i in 0..<BLOCK_SIZE) {
            buffer[i] = (buffer[i].toInt() xor subKey[i].toInt()).toByte()
        }

        // Calculate the final CMAC calue
        try {
            aesCipher!!.doFinal(buffer, 0, BLOCK_SIZE, buffer, 0)
        } // These should never happen because we pad manually
        catch (sbe: ShortBufferException) {
        } catch (ibse: IllegalBlockSizeException) {
        } catch (ibse: BadPaddingException) {
        }
        bufferCount = 0

        val mac = ByteArray(macLength)
        System.arraycopy(buffer, 0, mac, 0, macLength)
        return mac
    }

    fun calculateHash(data: ByteArray): ByteArray {
        updateBlock(data)
        return doFinal()
    }

    companion object {
        private val CONSTANT = 0x87.toByte()
        private const val BLOCK_SIZE = 16
    }
}
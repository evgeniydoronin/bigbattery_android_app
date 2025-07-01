package com.zetarapower.monitor.logic

const val BITS_OF_BYTE = 8
const val POLYNOMIAL = 0xA001
const val INITIAL_VALUE = 0xFFFF
const val FF  = 0xFF


/**
 *
 *  crc16
 */
fun ByteArray.crc16Verify(): Boolean{
    if (this.size < 3)  return false
    var res = INITIAL_VALUE
    for (index in 0..this.size-3) {
        res = res xor (this[index].toInt() and FF)
        for (i in 0 until BITS_OF_BYTE) {
            res = if (res and 0x0001 == 1) res shr 1 xor POLYNOMIAL else res shr 1
        }
    }
    val lowByte: Byte = (res  shr 8 and FF).toByte()
    val highByte: Byte = (res and FF).toByte()
    return highByte == this[this.size - 2] && lowByte == this[this.size - 1]
}

/**
 *
 *  crc16
 */
fun ByteArray.generateCRC16(): ByteArray{
    if (this.size < 3)  return this
    var res = INITIAL_VALUE
    for (index in 0 until this.size) {
        res = res xor (this[index].toInt() and FF)
        for (i in 0 until BITS_OF_BYTE) {
            res = if (res and 0x0001 == 1) res shr 1 xor POLYNOMIAL else res shr 1
        }
    }
    val lowByte: Byte = (res  shr 8 and FF).toByte()
    val highByte: Byte = (res and FF).toByte()

    var crc16Array = ByteArray(this.size+2)
    System.arraycopy(this, 0, crc16Array, 0, this.size)
    crc16Array[crc16Array.size - 2] = highByte
    crc16Array[crc16Array.size - 1] = lowByte
    return crc16Array
}

private  val HEXARRAY = "0123456789ABCDEF".toCharArray()
fun ByteArray.toHexString(): String {
    val hexChars = CharArray(this.size*2)
    for (i in this.indices) {
        val v = this[i].toInt() and 0xFF
        hexChars[i*2] = HEXARRAY[v ushr 4]
        hexChars[i*2+1] = HEXARRAY[v and 0x0F]
    }
    return String(hexChars)
}

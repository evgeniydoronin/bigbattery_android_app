package com.zetarapower.monitor.logic

/**
 *
 */
object BMSProtocalV2 {


    // "100470080B5030312D4752570000005030322D534C4B0000005030332D4459000000005030342D4D47520000005030352D5643540000005030362D4C55580000005030372D534D410000005030382D494E480000005030392D534F4C0000005031302D41464F0000005031312D535455000000350B"
    //

    //  地址码
    const val ADDRESS_CODE_BMS = 0x01
    const val ADDRESS_CODE_SETTINGS = 0x10

    // BMS data
    val OPERATION_GETBMS_HEX = "01030000002705d0"

    // ids
    val OPERATION_GETIDS = "1002007165"
    const val FUN_CODE_IDS_GET = 0x02
    const val FUN_CODE_IDS_SET = 0x07

    // RS485
    val OPERATION_GETRS485 = "10030070F5"
    const val FUN_CODE_RS485_GET = 0x03
    const val FUN_CODE_RS485_SET = 0x05

    // CAN
    val OPERATION_GETCAN = "10040072C5"
    const val FUN_CODE_CAN_GET = 0x04
    const val FUN_CODE_CAN_SET = 0x06


    const val NORMAL_CELLNUM = 16           // 正常电池串数
    const val BLEDATA_OFFSET = 3            // NORMAL_FUNCTION_CODE 数据的偏移

    const val FUNCTION_INDEX = 1            // 功能码偏移
    const val NORMAL_FUNCTION_CODE = 0x03   // 正常数据功能码
    const val SPLIT_FUNCTION_CODE  = 0x04   // 分片数据功能码


    private const val TOTAL_VOLTAGE_INDEX = 0   // 电池
    private const val CURRENT_INDEX = 2         // 电流

    // 18*2
    private  const val TEMP_PCB_INDEX = 36

    // 33*2 = 66
    private  const val TEMP_CELL_INDEX = 66

    // 20*2
    private const val TEMP_MAX_INDEX = 40
    // 23*2
    private const val SOHINDEX = 46
    // 24*2
    private const val SOCINDEX = 48
    // 25*2
    private const val STATUS_INDEX = 51 // 只读低字节
    // 36*2
    private const val CELLNUM_INDEX = 72

    // cell 16(2-17) 2*2   4+2*16 = 36
    private const val CELL_VOLTAGE_INDEX = 4



    /**
     *
     */
    fun getFunctionCode(data: ByteArray): Int{
        if (data.size <= FUNCTION_INDEX){
            return -1
        }
        return data[FUNCTION_INDEX].toInt()
    }



    /**
     *
     */
    fun getCellNum(data:ByteArray, offset: Int = 0): Int {
        if (data.size <= CELLNUM_INDEX+1+offset){
            return -1
        }
        return get2ByteIntValue(data, CELLNUM_INDEX+offset)
    }

    /**
     *
     */

    //01034e
    //053100000cfb0cfa0cfb0cf9000000000000000000000000000000000000000000000000000c00aa00aa0fa000140064006400040000000000220000000055d4a800aaaa0000000000040fa00000
    //d191
    //去掉前后的bms实体数据
    fun convertToBMSModel(data: ByteArray): BMSData? {
        var cellCount = getCellNum(data, 0)
        if (cellCount < 0) return null
        if (cellCount > 500) cellCount = NORMAL_CELLNUM  // special case

        // 电压
        val voltage = get2ByteFloatValue(data, TOTAL_VOLTAGE_INDEX, 100) // v

        var current = get2ByteFloatValue(data, CURRENT_INDEX, 10) // A
        if (current > 3276.8f) current = ((current - 6553.6f) * 10).toInt() / 10.0f;

        //
        val soc  = get2ByteIntValue(data, SOCINDEX)
        val soh = get2ByteIntValue(data, SOHINDEX)

        // 温度
        val temp =  get2ByteShortValue(data, TEMP_MAX_INDEX)
        val tempPcb = data[TEMP_PCB_INDEX+1]

        //  cell 温度
        var cellTempArray = ByteArray(4)
        for (i in 0 until 4) {
            cellTempArray[i] = data[TEMP_CELL_INDEX+i]
        }

        var status = data[STATUS_INDEX].toInt()
        // cell电压
        var cellVoltages = FloatArray(cellCount)
        var firstFrameCellCount = if (cellCount > NORMAL_CELLNUM) NORMAL_CELLNUM else cellCount

        for (i in 0 until firstFrameCellCount) {
            cellVoltages[i] = get2ByteFloatValue(data, CELL_VOLTAGE_INDEX+i*2, 1000)
        }

        return BMSData(voltage, current, cellVoltages,
            cellCount = cellCount,soc=soc, soh=soh, tempPCB = tempPcb , cellTempArray = cellTempArray,
            tempEnv = temp, status = status)
    }


    /**
     *
     */
    fun getVoltageFromOtherFrame(data: ByteArray, offset: Int, cellNum: Int) : FloatArray{
        var cellVoltages = FloatArray(cellNum)
        for (i in 0 until cellNum) {
            cellVoltages[i] = get2ByteFloatValue(data, offset + i * 2, 1000)
        }
        return cellVoltages
    }



    private fun get2ByteFloatValue(data: ByteArray, index: Int, unit:Int = 1): Float{
        var first: Int = data[index].toInt() and 0xFF
        var second: Int = data[index+1].toInt() and 0xFF
        return (first*0x100+second).toFloat()/unit
    }

    private fun get2ByteIntValue(data: ByteArray, index: Int): Int{
        var first: Int = data[index].toInt() and 0xFF
        var second: Int = data[index+1].toInt() and 0xFF
        return (first*0x100+second)
    }

    private fun get2ByteShortValue(data: ByteArray, index: Int): Short{
        var first: Int = data[index].toInt() and 0xFF
        var second: Int = data[index+1].toInt() and 0xFF
        return (first*0x100+second).toShort()
    }
}

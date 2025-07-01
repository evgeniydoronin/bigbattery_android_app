package com.zetarapower.monitor.logic

/**
 * Created by juqiu.lt on 10/27/22.
 */
class BMSSplitter(private val bmsDataReadyCallback: BMSDataReadyCallback)  {

    var mBmsData : BMSData? = null

    /**
     *
     */
    fun addBLEData(bleData: ByteArray) {
        if (!bleData.crc16Verify()){
            return
        }
        if (BMSProtocalV2.getFunctionCode(bleData) == BMSProtocalV2.NORMAL_FUNCTION_CODE) { // 第一帧
            var cellNum = BMSProtocalV2.getCellNum(bleData, BMSProtocalV2.BLEDATA_OFFSET)
            if (cellNum < 0) {
                resetBmsData()
                return
            }
            var bmsArray = ByteArray(bleData.size-5)
            System.arraycopy(bleData, BMSProtocalV2.BLEDATA_OFFSET, bmsArray, 0, bmsArray.size) // get the real data
            var bmsData: BMSData? = BMSProtocalV2.convertToBMSModel(bmsArray)
            if (bmsData == null){
                resetBmsData()
                return
            }
            if (bmsData.cellCount <= BMSProtocalV2.NORMAL_CELLNUM) {   // 16串以下直接处理
                resetBmsData()
                bmsDataReadyCallback.bmsDataReady(bmsData)
            }else if (bmsData.cellCount > BMSProtocalV2.NORMAL_CELLNUM) {
                mBmsData = bmsData
            }
        }else if (BMSProtocalV2.getFunctionCode(bleData) == BMSProtocalV2.SPLIT_FUNCTION_CODE) { // 其他帧 01 04 01 20
            if (mBmsData != null){
                var frameNo  =  bleData[2].toInt()
                var cellNumLeft = mBmsData!!.cellCount - frameNo*BMSProtocalV2.NORMAL_CELLNUM
                if (cellNumLeft > BMSProtocalV2.NORMAL_CELLNUM) cellNumLeft = BMSProtocalV2.NORMAL_CELLNUM
                if (cellNumLeft > 0 ){
                   var cellVotageArray = BMSProtocalV2.getVoltageFromOtherFrame(bleData, 4, cellNumLeft)
                   for (i in frameNo*BMSProtocalV2.NORMAL_CELLNUM until frameNo*BMSProtocalV2.NORMAL_CELLNUM+cellNumLeft){
                       mBmsData!!.cellVoltages[i] = cellVotageArray[i-frameNo*BMSProtocalV2.NORMAL_CELLNUM]
                   }
                }
                var totalFrame = (mBmsData!!.cellCount+BMSProtocalV2.NORMAL_CELLNUM-1)/BMSProtocalV2.NORMAL_CELLNUM
                if (frameNo == totalFrame -1){
                    var bmsData = mBmsData
                    resetBmsData()
                    bmsDataReadyCallback.bmsDataReady(bmsData)
                }
            }
        }
    }


    private fun resetBmsData(){
        this.mBmsData = null
    }
}
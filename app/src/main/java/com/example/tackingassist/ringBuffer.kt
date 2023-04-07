package com.sverreskort.android.tackingassist

import android.util.Log
import kotlin.math.sin

// TODO test when GPS stops

class RingBuffer (var bufferSize :Int) {
    // constructor
    private val buffer = Array(bufferSize){0.0f}

    //Member variables
    private var dataSize :Int = 0
    private var NowPointer :Int = -1


    // Index   0   1   2   3   4   5   6   7   8   9
    // Data    101 102 103 104 105 106  na  na  na  na
    //                             L_NowPointer
    // bufferSize=10
    // NowPointer=5 buffer[5]
    // dataSize=6

    // Index   0   1   2   3   4   5   6   7   8   9
    // Data    111 112 113 104 105 106 107 108 109 110
    //                 L_NowPointer
    // bufferSize=10
    // NowPointer=2 buffer[2]
    // dataSize=10

    // Class functions
    fun push(newData: Float) {
        Log.d("ringBuffer", "Add-data = $newData")
        NowPointer+=1
        if (NowPointer>=bufferSize){
            NowPointer=0
        }
        buffer[NowPointer] = newData

        if(dataSize<bufferSize)
            dataSize++
    }
    fun getDataSize() : Int {
        Log.d("ringBuffer", "dataSize = $dataSize")
        return dataSize
    }
    fun getMax(): Float {
        var max = Float.MIN_VALUE
        for (i in buffer) {
            max = max.coerceAtLeast(i)
        }
        return max
    }
    fun getMin(): Float {
        var min = Float.MAX_VALUE
        for (i in buffer) {
            min = min.coerceAtMost(i)
        }
        return min
    }
    fun printToLog() {
        Log.d("ringBuffer", "printToLog()")
        var i = 0
        var printstring: String
        while( i < bufferSize ) {
            if (i == NowPointer) {
                printstring = "Print: [${i}] = ${buffer[i]}  - NowPointer"
            }
            else {
                printstring = "Print: [${i}] = ${buffer[i]}"
            }
            Log.d("ringBuffer", printstring)
            i++
        }
        printstring = "Print: Min = ${getMin()}"
        Log.d("ringBuffer", printstring)
        printstring = "Print: Max = ${getMax()}"
        Log.d("ringBuffer", printstring)
        printstring = "Print: DataSize = ${getDataSize()}"
        Log.d("ringBuffer", printstring)
    }
    // Make some demodata and fill the array with data from 0.5 to 4.5
    fun fillDemoData(){
        var i = 0
        while( i < bufferSize ) {
            var demodata = Math.PI*2.0
            demodata = sin( demodata * i / bufferSize)
            demodata = (demodata*2.0) + 2.5
            this.push( demodata.toFloat() )

            i++
        }
    }
    fun getData(sinceNow : Int):Float{
        if (sinceNow>dataSize || sinceNow<0){return 0f}

        var i = NowPointer-sinceNow

        if (i<0){
            i = dataSize + (i)
        }

        if (i>dataSize || i<0){return 0.0f}

        return buffer[i]
    }
}
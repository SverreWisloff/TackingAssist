package com.sverreskort.android.tackingassist

import android.util.Log
import kotlinx.coroutines.selects.whileSelect
import kotlin.math.sin

// TODO test when GPS stops

data class gpsDynamics(var time: Long, var speed: Float, var speedSmoothed: Float, var heading: Float)

class RingBuffer (var bufferSize :Int) {
    // constructor
    private val bufferGps = Array(bufferSize) { gpsDynamics(0, 0f, 0f, 0f) }

    //TODO: Let the buffer hold at data class (speed, smoothed speed, heading, ...)

    //Member variables
    private var dataSize: Int = 0
    private var NowPointer: Int = -1


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
    fun push(newData: gpsDynamics) {
        Log.d("ringBuffer", "Add-data = $newData")
        NowPointer += 1
        if (NowPointer >= bufferSize) {
            NowPointer = 0
        }
        bufferGps[NowPointer] = newData

        if (dataSize < bufferSize)
            dataSize++

        updateSmoothingOnLatestData()
    }

    //triangular smoothing: y = (x + 2x +3x + 2x + x) / 9
    //The function calculate smoothed numbers, and store them in this.speedSmoothed.
    //typically run after push()
    fun updateSmoothingOnLatestData(){
        //           4         3         2         1         0
        //y( 0) =                     (  x(-2) + 2*x(-1) + 3*x( 0) ) / 6
        //y(-1) =           (  x(-3) + 2*x(-2) + 3*x(-1) + 2*x( 0) ) / 8
        //y(-2) = (  x(-4) + 2*x(-3) + 3*x(-2) + 2*x(-1) +   x( 0) ) / 9
        if (dataSize<=3) {
            // 0,1,2
            this.bufferGps[0].speedSmoothed = bufferGps[0].speed
            if (dataSize>0) {
                // 1,2
                this.bufferGps[1].speedSmoothed = bufferGps[1].speed
            }
            if (dataSize>1)
                // 2
                this.bufferGps[2].speedSmoothed = bufferGps[2].speed
            return
        }

        val x0 = this.getGpsData(0).speed
        val x1 = this.getGpsData(1).speed
        val x2 = this.getGpsData(2).speed
        val x3 = this.getGpsData(3).speed
        val x4 = this.getGpsData(4).speed

        val y0 =                 (x2 + 2f*x1 + 3f*x0) / 6f
        val y1 =         (x3 + 2f*x2 + 3f*x1 + 2f*x0) / 8f
        val y2 = (x4 + 2f*x3 + 3f*x2 + 2f*x1 +    x0) / 9f

        val Index0 = getIndexSinceNow(0)
        val Index1 = getIndexSinceNow(1)
        val Index2 = getIndexSinceNow(2)

        this.bufferGps[Index0].speedSmoothed = y0
        this.bufferGps[Index1].speedSmoothed = y1
        this.bufferGps[Index2].speedSmoothed = y2
    }

    fun getDataSize(): Int {
        Log.d("ringBuffer", "dataSize = $dataSize")
        return dataSize
    }

    fun getMaxSpeed(): Float {
        var max = Float.MIN_VALUE
        var i = 0
        while (i < bufferGps.size) {
            if (bufferGps[i].speed > max)
                max = bufferGps[i].speed
            i++
        }
        return max
    }

    fun getMinSpeed(): Float {
        var min = Float.MAX_VALUE
        var i = 0
        while (i < bufferGps.size) {
            if (bufferGps[i].speed < min)
                min = bufferGps[i].speed
            i++
        }
        return min
    }

    fun printToLog() {
        Log.d("ringBuffer", "printToLog()")
        var i = 0
        var printstring: String
        while (i < bufferSize) {
            if (i == NowPointer) {
                printstring = "Print: [${i}] = ${bufferGps[i].speed}  - NowPointer"
            } else {
                printstring = "Print: [${i}] = ${bufferGps[i].speed}"
            }
            Log.d("ringBuffer", printstring)
            i++
        }
        printstring = "Print: Min = ${getMinSpeed()}"
        Log.d("ringBuffer", printstring)
        printstring = "Print: Max = ${getMaxSpeed()}"
        Log.d("ringBuffer", printstring)
        printstring = "Print: DataSize = ${getDataSize()}"
        Log.d("ringBuffer", printstring)
    }

    // Make some demodata and fill the array with data from 0.5 to 4.5
    fun fillDemoData() {
        var i = 0
        while (i < bufferSize) {
            var demodata = Math.PI * 2.0
            demodata = sin(demodata * i / bufferSize)
            demodata = (demodata * 2.0) + 2.5
            val data = gpsDynamics(0, demodata.toFloat(), 0f, 0f)
            this.push(data)

            i++
        }
    }

    // Return Array-index that host data for 'sinceNow' epochs
    // Return -1 when error
    //
    // Index   0   1   2   3   4   5   6   7   8   9
    // Data    111 112 113 104 105 106 107 108 109 110
    //                 L_NowPointer
    // bufferSize=10
    // NowPointer=2 buffer[2]
    // dataSize=10
    fun getIndexSinceNow(sinceNow: Int) : Int {
        if (sinceNow > dataSize || sinceNow < 0) {
            return -1
        }

        var i : Int = NowPointer - sinceNow

        if (i < 0) {
            i = dataSize + (i)
        }

        if (i > dataSize || i < 0) {
            return -1
        }

        return i
    }

    fun getSpeed(sinceNow: Int): Float {
        val i = getIndexSinceNow(sinceNow)

        if (i<0) {
            return 0.0f //error
        }
        return bufferGps[i].speed
    }

    fun getGpsData(sinceNow: Int): gpsDynamics {
        val i = getIndexSinceNow(sinceNow)

        if (i<0) {
            return gpsDynamics(0, 0f, 0f, 0f) //error
        }
        return bufferGps[i]
    }

}
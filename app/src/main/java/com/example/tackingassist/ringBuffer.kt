package com.example.tackingassist

import android.util.Log

class ringBuffer (var bufferSize :Int) {
    // constructor
    val buffer = Array(bufferSize){0.0f}

    //Member variables
    var dataSize :Int = 0
    var NowPointer :Int = -1


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
        NowPointer+=1;
        if (NowPointer>=bufferSize){
            NowPointer=0;
        }
        buffer[NowPointer] = newData;
    }
    fun dataSize() : Int {
        Log.d("ringBuffer", "dataSize = $dataSize")
        return dataSize
    }
    fun printToLog() {
        Log.d("ringBuffer", "printToLog()")
        var i = 0
        while( i < bufferSize ) {
            var printstring: String = ""
            if (i==NowPointer)
                printstring = "Print: [${i}] = ${buffer[i]}  - NowPointer"
            else
                printstring = "Print: [${i}] = ${buffer[i]}"
            Log.d("ringBuffer", printstring)
            i++
        }
    }
    // Make some demodata and fill the array with data from 1 to 3
    fun fillDemoData(){
        var i = 0
        while( i < bufferSize ) {
            var demodata = Math.PI*2.0;
            demodata = Math.sin( demodata * i / bufferSize);
            demodata = demodata + 2;
            this.push( demodata.toFloat() );

            i++
        }
    }
}
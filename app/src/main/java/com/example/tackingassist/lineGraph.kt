package com.sverreskort.android.tackingassist

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import kotlin.math.floor


class lineGraphView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

    private val debug = true
    private val lineWidth = 18f
    private val fontsize = 60.0f
    private val pathGraph = Path()
    private var size = 0
    private var canvasWidth = 0              // Width of the graph.
    private var canvasHeight = 0
    private val inset = 5f                  // Marg=strokeWidth
    private val leftMargin = 180f           // Venstrmarg. Her vises hjelpe-tall
    private val rightMargin = 18f          // Høyremarg. Her vises nåværende fart
    private var plotWidth = 0f
    private var plotHeight = 0f
    private var graphMinValue = 0.0f
    private var graphMaxValue = 0.0f
    private val greenspeed = Color.parseColor("#4CAF50")         // TODO - get color from colors.xml

    // Paint styles used for rendering are initialized here. This
    // is a performance optimization, since onDraw() is called
    // for every screen refresh.
    private val paintGraph = Paint().apply {
        color = greenspeed
        isAntiAlias = true  // Smooths out edges of what is drawn without affecting shape.
        //isDither = true  // Dithering affects how colors with higher-precision than the device are down-sampled.
        style = Paint.Style.STROKE // default: FILL
        //strokeJoin = Paint.Join.ROUND // default: MITER
        //strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = lineWidth
    }


    private val paintHelplines = Paint().apply {
        color = Color.DKGRAY
        style = Paint.Style.STROKE
        strokeWidth = 5f
        //textAlign = Paint.Align.CENTER
        textSize = fontsize
        typeface = Typeface.create("", Typeface.NORMAL)
    }


    // TODO Later Delete this?
    init {
        paintGraph.isAntiAlias = true
        isClickable = false
    }

    /**
     * This is called during layout when the size of this view has changed. If
     * the view was just added to the view hierarchy, it is called with the old
     * values of 0. The code determines the drawing bounds for the custom view.
     *
     * @param width     Current width of this view.
     * @param height    Current height of this view.
     * @param oldWidth  Old width of this view.
     * @param oldHeight Old height of this view.
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        // Calculate the radius from the smaller of the width and height.
        canvasWidth  = width
        canvasHeight = height
        plotWidth    = canvasWidth - leftMargin - rightMargin - inset - inset
        plotHeight   = canvasHeight - inset - inset
    }

    /**
     * Renders view content: A line-plot
     *
     * @param canvas The canvas on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)

        drawHelplines(canvas)
        drawGraph(canvas)
        Log.d("lineGraphView", "onDraw")
    }

    //   ================================   == I
    //  ||     |                |       ||
    //  || 13kn|   _____        |       ||
    //  ||     |__/     \___    |       ||   H
    //  ||     |            \___| 12.1kn||
    //  || 11kn|                |       ||
    //  ||     |                |       ||
    //   ================================   == I
    //
    //  ||  A  |      B         |  C    ||
    //  I                                I
    //
    // inset        = I
    // leftMargin   = A
    // plotWidth    = B     (=canvasWidth-leftMargin-rightMargin-inset-inset)
    // rightMargin  = C
    // canvasWidth  = I+A+B+C+I
    // plotHeight   = H     (=canvasHeight-inset-inset)
    // canvasHeight = I+H+I

    private fun drawHelplines(canvas: Canvas) {

        // Fill background color
        canvas.drawColor(Color.BLACK)

        // Draw a frame around the canvas.
        //canvas.drawRect( inset, inset , graphWidth-rightMargin, graphHeight-inset, paintHelplines)

        //Draw a help line right under top
        val helplineTop =   floor(graphMaxValue).toLong()
        val helplineTopPix = canvasHeight.toFloat() - ((helplineTop - graphMinValue )/(graphMaxValue - graphMinValue) * canvasHeight.toFloat())
        val helplineTopStr = "${helplineTop} kn"
        canvas.drawLine(leftMargin+inset, helplineTopPix, leftMargin+inset+plotWidth, helplineTopPix, paintHelplines)
        canvas.drawText( helplineTopStr, inset, helplineTopPix+fontsize, paintHelplines)

        // TODO - if ony one hilplne - dont draw it twice ....
        //Draw a help line right over lowest datapont
        val helplineMin =   floor(graphMinValue).toLong() + 1
        val helplineMinPix = canvasHeight.toFloat() - ((helplineMin - graphMinValue )/(graphMaxValue - graphMinValue) * canvasHeight.toFloat())
        val helplineMinStr = "${helplineMin} kn"
        canvas.drawLine(leftMargin+inset, helplineMinPix, leftMargin+inset+plotWidth, helplineMinPix, paintHelplines)
        canvas.drawText( helplineMinStr, inset, helplineMinPix, paintHelplines)

        //TODO - does Log.d run when app is compiled under release?
        if (debug) {
            val printstring = "helplineMin=${helplineMin} helplineMinPix=${helplineMinPix} helplineTop=${helplineTop} helplineTopPix=${helplineTopPix}"
            Log.d("drawHelplines", printstring)
        }
    }
    private fun drawGraph(canvas: Canvas) {
        canvas.drawPath(pathGraph, paintGraph)
    }

    // Import data to plot, and prepare for a path that is drawn
    fun importData(dataBuffer: RingBuffer) {
        graphMaxValue = dataBuffer.getMax()
        graphMinValue = dataBuffer.getMin()
        val dataPoints = dataBuffer.getDataSize()
        var printstring: String

        if (dataPoints<1)
            return

        //Vertical scale small enough that minimum two knots is in sight
        if ( (graphMaxValue-graphMinValue) < 1.8f) {
            graphMinValue = ( (graphMaxValue+graphMinValue) / 2.0f ) - 1.0f
            graphMaxValue = ( (graphMaxValue+graphMinValue) / 2.0f ) + 1.0f
            if (debug) {
                val printstring = "graphMinValue=${graphMinValue} graphMaxValue=${graphMaxValue} "
                Log.d("importData", printstring)
            }
        }

        pathGraph.reset() //Clear any lines and curves from the path, making it empty.

        var sinceNow = dataPoints-1

        while (sinceNow>=0) {
            val Data = dataBuffer.getData(sinceNow);

            // x = Time
//            val x : Float = canvasWidth-rightMargin-inset - (((sinceNow.toFloat()+1f )/ dataPoints.toFloat()) * (canvasWidth-rightMargin-inset)) + inset
            val x : Float = (plotWidth+leftMargin+inset) - (((sinceNow.toFloat()+1f ) / dataPoints.toFloat()) * (plotWidth))

            // y = Speed
            val y : Float = canvasHeight.toFloat() - ((Data - graphMinValue )/(graphMaxValue - graphMinValue) * (canvasHeight.toFloat()-inset-inset) )

            if (sinceNow==(dataPoints-1 ))
                pathGraph.moveTo(x,y)
            else
                pathGraph.lineTo(x,y)

            printstring = "importData x=${x}  y=${y} (i=${sinceNow} dataPoints=${dataPoints} Data=${Data} canvasWidth=${canvasWidth} canvasHeight=${canvasHeight} graphMinValue=${graphMinValue} graphMaxValue=${graphMaxValue} )"
            Log.d("lineGraphView", printstring)

            sinceNow--
        }


    }
}

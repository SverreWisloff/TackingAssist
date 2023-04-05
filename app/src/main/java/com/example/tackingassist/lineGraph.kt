package com.sverreskort.android.tackingassist

import android.content.Context
import android.graphics.*
import android.icu.util.UniversalTimeScale.toLong
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.sverreskort.android.tackingassist.RingBuffer
import kotlin.math.floor
import kotlin.math.min


class lineGraphView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

    private val debug = true
    private val fontsize = 60.0f
    private val pathGraph = Path()
    private var size = 0
    private var graphWidth = 0              // Width of the graph.
    private var graphHeight = 0
    private var graphMinValue = 0.0f
    private var graphMaxValue = 0.0f
    private val inset = 5f                  // Marg=strokeWidth
    private val rightMargin = 180f           // Høyremarg. Her vises hjelpe-tall

    // Paint styles used for rendering are initialized here. This
    // is a performance optimization, since onDraw() is called
    // for every screen refresh.
    private val paintGraph = Paint().apply {
        color = Color.GREEN
        isAntiAlias = true  // Smooths out edges of what is drawn without affecting shape.
        //isDither = true  // Dithering affects how colors with higher-precision than the device are down-sampled.
        style = Paint.Style.STROKE // default: FILL
        //strokeJoin = Paint.Join.ROUND // default: MITER
        //strokeCap = Paint.Cap.ROUND // default: BUTT
        strokeWidth = 15f
    }
    private val paintHelplines = Paint().apply {
        color = Color.MAGENTA
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
     * @param width    Current width of this view.
     * @param height    Current height of this view.
     * @param oldWidth Old width of this view.
     * @param oldHeight Old height of this view.
     */
    override fun onSizeChanged(width: Int, height: Int, oldWidth: Int, oldHeight: Int) {
        // Calculate the radius from the smaller of the width and height.
        graphWidth = width
        graphHeight = height

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

    private fun drawHelplines(canvas: Canvas) {

        // Fill background color
        canvas.drawColor(Color.DKGRAY)

        // Draw a frame around the canvas.
        //canvas.drawRect( inset, inset , graphWidth-rightMargin, graphHeight-inset, paintHelplines)

        //Draw a help line right over lowest datapont
        val helplineMin =   floor(graphMinValue).toLong() + 1
        val helplineMinpix = graphHeight.toFloat() - ((helplineMin - graphMinValue )/(graphMaxValue - graphMinValue) * graphHeight.toFloat())
        val helplineMinstr = "${helplineMin} kn"
        canvas.drawLine(0f, helplineMinpix, graphWidth.toFloat()-rightMargin, helplineMinpix, paintHelplines)
        canvas.drawText( helplineMinstr, graphWidth.toFloat()-rightMargin+20, helplineMinpix, paintHelplines)

        //Draw a help line right under top
        val helplineTop =   floor(graphMaxValue).toLong()
        val helplineToppix = graphHeight.toFloat() - ((helplineTop - graphMinValue )/(graphMaxValue - graphMinValue) * graphHeight.toFloat())
        val helplineTopstr = "${helplineTop} kn"
        canvas.drawLine(0f, helplineToppix, graphWidth.toFloat()-rightMargin, helplineToppix, paintHelplines)
        canvas.drawText( helplineTopstr, graphWidth.toFloat()-rightMargin+20, helplineToppix+fontsize, paintHelplines)

        //TODO - does Log.d run when app is compiled under release?
        if (debug) {
            val printstring = "helplineMid=${helplineMin} helplineMidpix=${helplineMinpix} helplineTop=${helplineTop} helplineToppix=${helplineToppix}"
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

        pathGraph.reset() //Clear any lines and curves from the path, making it empty.

        var sinceNow = 0
        pathGraph.moveTo(inset,graphHeight/2f)

        while (sinceNow<dataPoints) {
            val Data = dataBuffer.getData(sinceNow);

            // x = Time
            val x : Float = (((sinceNow.toFloat()+1f )/ dataPoints.toFloat()) * (graphWidth-rightMargin-inset)) + inset

            // y = Speed
            val y : Float = graphHeight.toFloat() - ((Data - graphMinValue )/(graphMaxValue - graphMinValue) * (graphHeight.toFloat()-inset-inset) )

            pathGraph.lineTo(x,y)

            printstring = "importData x=${x}  y=${y} (i=${sinceNow} dataPoints=${dataPoints} Data=${Data} graphWidth=${graphWidth} graphHeight=${graphHeight} graphMinValue=${graphMinValue} graphMaxValue=${graphMaxValue} )"
            Log.d("lineGraphView", printstring)

            sinceNow++
        }


    }
}

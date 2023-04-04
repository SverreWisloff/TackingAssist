package com.sverreskort.android.tackingassist

import android.content.Context
import android.graphics.*
import android.util.AttributeSet
import android.util.Log
import android.view.View
import com.sverreskort.android.tackingassist.RingBuffer
import kotlin.math.min

//class lineGraph(context: Context) : View(context) {


class lineGraphView @JvmOverloads constructor(
        context: Context,
        attrs: AttributeSet? = null,
        defStyleAttr: Int = 0
    ) : View(context, attrs, defStyleAttr) {

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
        strokeWidth = 4f // default: Hairline-width (really thin)
        //textAlign = Paint.Align.CENTER
        //textSize = 55.0f
        //typeface = Typeface.create("", Typeface.BOLD)
    }
    private val paintOrdinates = Paint().apply {
        color = Color.MAGENTA
        style = Paint.Style.STROKE
        strokeWidth = 2f
    }

    private val pathGraph = Path()
    private var size = 0
    private var graphWidth = 0.0f                  // Width of the graph.
    private var graphHeight = 0.0f
    private var graphMinValue = 0.0f
    private var graphMaxValue = 0.0f
    private lateinit var frame: Rect

// TODO Later Delete this?
    init {
        paintGraph.isAntiAlias = true
        isClickable = false

        //TODO Testing conde, must be deleted
        pathGraph.moveTo( 0f,20f)
        pathGraph.lineTo( 20f,22f)
        pathGraph.lineTo( 40f,30f)
        pathGraph.lineTo( 60f,39f)
        pathGraph.lineTo( 80f,30f)
        pathGraph.lineTo( 100f,35f)
        Log.d("lineGraphView", "Init")
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
        //graphWidth = width.toFloat()
        //graphHeight = height.toFloat()

        // Calculate a rectangular frame around the picture.
        val inset = 5
        frame = Rect(inset, inset, width - inset, height - inset)
    }

    /**
     * Renders view content: A line-plot
     *
     * @param canvas The canvas on which the background will be drawn.
     */
    override fun onDraw(canvas: Canvas) {
        // call the super method to keep any drawing from the parent side.
        super.onDraw(canvas)

        drawOrdinates(canvas)
        drawGraph(canvas)
        Log.d("lineGraphView", "onDraw")
    }

    private fun drawOrdinates(canvas: Canvas) {
        // Draw a frame around the canvas.
        canvas.drawRect(frame, paintOrdinates)
    }
    private fun drawGraph(canvas: Canvas) {
        canvas.drawPath(pathGraph, paintGraph)
    }

    // Import data to plot, and prepare for a path that is drawn
    fun importData(dataBuffer: RingBuffer) {
        // TODO populate pathGraph not finished
        graphMaxValue = dataBuffer.getMax()
        graphMinValue = dataBuffer.getMin()
        val dataPoints = dataBuffer.getDataSize()

        pathGraph.reset() //Clear any lines and curves from the path, making it empty.

        var sinceNow = 0
        pathGraph.moveTo(0f,0f)

        while (sinceNow<dataPoints) {
            val Data = dataBuffer.getData(sinceNow);

            val x : Float = sinceNow/dataPoints + 0.1f
            val y : Float = (graphMaxValue - graphMinValue) * Data * graphHeight

            pathGraph.lineTo(x,y)

            sinceNow++
        }


    }
}

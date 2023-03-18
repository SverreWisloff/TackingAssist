/*
 * Copyright 2019 Google LLC
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     https://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.sverreskort.android.tackingassist

import android.content.Context
import android.location.Location
import android.util.Log
import androidx.core.content.edit
import com.sverreskort.tackingassist.R

/**
 * Degrees between 0 and 360
 */
fun reduseDeg(deg: Float) : Float {
    var redused = deg
    if (redused<0.0f)   {   redused += 360f }
    if (redused<0.0f) 	{	redused += 360f }
    if (redused>360.0f)	{	redused -= 360f }
    if (redused>360.0f)	{	redused -= 360f }
    return redused
}

/**
 * function that print  drawable hash-marks for the compass
 * printed to Log
 * Only run in debug to het the hash-marks, ant copy them to drawable/outline_compass2.xml
 */
fun drawHashMarks(canvassSize: Int) : String {
    val pathData: String
    var sX: Double
    var sY: Double
    var eX: Double
    var eY: Double
    val outerRad = canvassSize / 2.0
    var innerRad = outerRad - 12.0
    val builder = StringBuilder()
    val builder2 = StringBuilder()

    // draw 10-deg tick marks.
    // Format: M180,180 L190,190 M10,10 L16,16
    var i = 0.0
    while (i < 2.0 * Math.PI) {
        //compute coordinates to hash-marks
        sY = outerRad + innerRad * Math.sin(i)
        sX = outerRad + innerRad * Math.cos(i)
        eY = outerRad + outerRad * Math.sin(i)
        eX = outerRad + outerRad * Math.cos(i)
        //format Moveto - Lineto string
        val formattedString = String.format("M%.1f,%.1fL%.1f,%.1f", sX , sY , eX , eY )
        builder.append(formattedString)
        i += (Math.PI / 18.0)
    }
    Log.d("drawHashMarks-10deg", builder.toString())

    // draw 2-deg tick marks.
    innerRad = outerRad - 6
    i = 0.0
    while (i < 2.0 * Math.PI) {
        sY = outerRad + innerRad * Math.sin(i)
        sX = outerRad + innerRad * Math.cos(i)
        eY = outerRad + outerRad * Math.sin(i)
        eX = outerRad + outerRad * Math.cos(i)
        val formattedString = String.format("M%.1f,%.1fL%.1f,%.1f", sX , sY , eX , eY )
        builder2.append(formattedString)
        i += (Math.PI / 90.0)
    }
    Log.d("drawHashMarks-2deg", builder2.toString())
    pathData = builder.toString() + builder2.toString()
    return pathData
}


/**
 * Returns the `location` object as a human readable string.
 */
fun Location?.toText(): String {
    return if (this != null) {
        "($latitude, $longitude)"
    } else {
        "Unknown location"
    }
}

/**
 * Provides access to SharedPreferences for location to Activities and Services.
 */
internal object SharedPreferenceUtil {

    const val KEY_FOREGROUND_ENABLED = "tracking_foreground_location"

    /**
     * Returns true if requesting location updates, otherwise returns false.
     *
     * @param context The [Context].
     */
    fun getLocationTrackingPref(context: Context): Boolean =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key), Context.MODE_PRIVATE)
            .getBoolean(KEY_FOREGROUND_ENABLED, false)

    /**
     * Stores the location updates state in SharedPreferences.
     * @param requestingLocationUpdates The location updates state.
     */
    fun saveLocationTrackingPref(context: Context, requestingLocationUpdates: Boolean) =
        context.getSharedPreferences(
            context.getString(R.string.preference_file_key),
            Context.MODE_PRIVATE).edit {
            putBoolean(KEY_FOREGROUND_ENABLED, requestingLocationUpdates)
        }
}

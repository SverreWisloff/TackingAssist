# Android studio - Kotin - NOTES
_Notes for a functioning alzheimer_

## Google Play Console
https://play.google.com/console/u/0/developers/8035817924766649761/app-list

#### Keystore
C:\Users\sw\Dropbox\00 SVERRE\Android\keystore\
C:\Users\sw\Dropbox\00 SVERRE\Android\keystores.jks

#### Release:
1. Build.gradle: update "versionCode" & "versionName"
2. Build > Rebuild project
3. Build > Generate Signed Bundle/APK
4. C:\Users\sw\AndroidStudioProjects\TackingAssist\app\release\

## Update launch icon:
1. update ic_launcher_foreground.xml
2. Start "Image Asset Studio"
3. Configure Imate Asset
4. Velg: Launcher icons
5. Navn: ic_launcher
6. Velg foreground layer: \drawable\ic_launcher_foreground.xml
7. Velg background layer: \drawable\ic_launcher_background.xml

## GitHub integrastion:
1. Generate token: https://stackoverflow.com/questions/64869735/cant-log-in-to-github-on-android-studio
2. Select repo(all), read:org (under admin:org), gist, workflow
3. How to Upload Android Studio Project to GitHub? https://dev.to/vtsen/how-to-upload-android-studio-project-to-github-4d2
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 

## Android Studio
Prosjekter ligger her:
+ C:\Users\sw\AndroidStudioProjects

## Diverse tips
Debug:
+ Run > Toggle Line Breakpoint or Control-F8
+ Run > Step Over	 or type F8
+ Run > Step Into	 or type F7
+ Run > Step Out	 or type Shift-F8
+ Run > Resume Program	 or click the Resume Resume Icon icon

Reformat your code
+ Code > Reformat Code	 or Ctrl+Alt+L

Formatere string / sprintf
```kotlin
val myStr = String.format("The PI value is %.2f", PI)
Format string: String.format("%.2f kt", boatSpeed)
```

Kommentarer i xml:
1)
``` xml
/// <text><![CDATA[Hello World!]]></text>
```

2)
``` xml
<!--
	<text>
		<![CDATA[Hello World!]]>
	</text>
-->
```


## Location - GPS
#### Android Run Tracking App
- [ ] https://www.kodeco.com/28767779-how-to-make-an-android-run-tracking-app
#### Codelab: Receive location updates in Android with Kotlin
- [ ] https://codelabs.developers.google.com/codelabs/while-in-use-location#3

## Graphics
#### Codelab: Add vector drawable assets
- [ ] https://developer.android.com/codelabs/basic-android-kotlin-training-polished-user-experience#3
- [ ] https://developer.android.com/codelabs/basic-android-kotlin-training-create-dice-roller-app-with-button#4
#### VectorDrawable pathData
- [ ] https://medium.com/@ali.muzaffar/understanding-vectordrawable-pathdata-commands-in-android-d56a6054610e
- [ ] https://www.w3.org/TR/SVG/paths.html
- [ ] https://developer.android.com/studio/write/vector-asset-studio
```
moveto: M (abs.coord)
lineto: L (abs.coord)
Ex: M180,180 L190,190 M10,10 L16,16
```
#### Codelab: Smooth rotatin og graphic
- [ ] https://developer.android.com/codelabs/advanced-android-kotlin-training-property-animation#3
#### Draw lines
- [ ] https://www.geeksforgeeks.org/how-to-draw-a-line-in-android-with-kotlin/
- [ ] https://www.tutorialspoint.com/how-to-draw-a-line-in-android-using-kotlin
#### Kotlin Line Chart
- [ ] https://medium.com/@yilmazvolkan/kotlinlinecharts-c2a730226ff1

## Sensors
#### Creating a Barometer Application for Android
- [ ] https://www.ssaurel.com/blog/creating-a-barometer-application-for-android/
#### Advanced Android 03.1:Getting sensor data (JAVA)
- [ ] https://developer.android.com/codelabs/advanced-android-training-sensor-data?index=..%2F..advanced-android-training#0
#### Handling Sensors in Android (Kotlin)
- [ ] https://nhkarthick.medium.com/handling-sensors-in-android-kotlin-d728ddc20394
- [ ] https://medium.com/@chris_42047/exploring-device-sensors-in-android-with-kotlin-db3e5ffd361a
#### Sensors Tutorial for Android - Compass image
- [ ] https://www.kodeco.com/10838302-sensors-tutorial-for-android-getting-started
- [ ] https://developer.android.com/reference/android/hardware/SensorManager
#### Compute the device's orientation.
- [ ] https://developer.android.com/guide/topics/sensors/sensors_position#:~:text=Compute%20the%20device%27s%20orientation.




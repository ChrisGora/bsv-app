BRISTOL STREET VIEW APP

// ---------------------------------------------------------------------------------------------------------------------
INSTALLING THE APK
// ---------------------------------------------------------------------------------------------------------------------

1) Build the apk

        ./gradlew assemble

    The generated apk will be in

        bristolstreetview/app/build/outputs/apk/release/app-release-unsigned.apk

2) Copy the apk to the device and install it.
   Make sure installing unsigned apks is allowed in developer's settings.


// ---------------------------------------------------------------------------------------------------------------------
COLLECTING DATA
// ---------------------------------------------------------------------------------------------------------------------

1. Connect the camera to the phone via WiFi
        https://theta360.com/uk/support/manual/v/content/prepare/prepare_06.html

2. Make sure any other WiFi networks have their auto-connect option set to OFF

3. Open the app and select AUTOMATIC MODE > START LOCAION

4. This will start listening for location updates and collecting data

5. Keep an eye on distance walked since last photo and the exact time when the last photo was saved.


// ---------------------------------------------------------------------------------------------------------------------
TROUBLESHOOTING
// ---------------------------------------------------------------------------------------------------------------------

    1) Make sure the both the camera and the WiFi indicator are ON (if flashing the camera has disconnected)
    2) STOP LOCATION, wait a few seconds, then START LOCATION again.
    3) Close the app and re-open it.
    4) Close the app, turn off WiFi, turn it back on, connect camera, re-open the app.
    5) DO NOT reconnect the camera while the app is open.
    6) If the WiFi connection drops for any reason you HAVE to close the app, reconnect, open the app.
    7) If all else fails try to power cycle both the camera and the phone.
    8) To help the GPS signal, try providing a WiFi hotspot and open Google Maps.
        Once the correct location is shown there, reconnect to the camera and re-open the app.



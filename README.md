# BRISTOL STREET VIEW APP

[READ THE PROJECT SUMMARY!](https://bitbucket.org/kg17815/bristolstreetview/src/master/Bristol%20Street%20View%20PROJECT%20SUMMARY.pdf)

This is the first part of work that I did for my Summer 2018 Internship at the University of Bristol's Visual Information Lab. The main aim of the project was to create a platform to collect and store 1000s of 360 degree images of Bristol.

The repository contains an Android app, which connects to a 360 camera and takes a picture every 20 meters. The photos and the metadata are both stored on the phone to be processed by a computer after the trip.

For the second part of the project, have a look at the" Bristol Street View Desktop Client" repo.

## INSTALLING THE APK

Simply copy the apk to the device and install it. Make sure installing untrusted apks is allowed in developer's settings.

If you want to build the apk yourself, Android SDK must be installed. This can be done using Android Studio.

Gradle will look for the file 'local.properties' in the main project directory. This file must contain the line:

`sdk.dir=[PATH TO YOUR ANDROID SDK]`

Then execute:

`./gradlew assemble`

The generated apk will be in

`bristolstreetview/app/build/outputs/apk/release/app-release-unsigned.apk`

## COLLECTING DATA

1. Connect the camera to the phone via WiFi
        https://theta360.com/uk/support/manual/v/content/prepare/prepare_06.html

2. Make sure any other WiFi networks have their auto-connect option set to OFF

3. Open the app and select AUTOMATIC MODE > START LOCATION

4. This will start listening for location updates and collecting data

5. Keep an eye on distance walked since last photo and the exact time when the last photo was saved.

## TROUBLESHOOTING

* Make sure the both the camera and the WiFi indicator are ON (if flashing the camera has disconnected)
* STOP LOCATION, wait a few seconds, then START LOCATION again.
* Close the app and re-open it.
* Close the app, turn off WiFi, turn it back on, connect camera, re-open the app.
* DO NOT reconnect the camera while the app is open.
* If the WiFi connection drops for any reason you HAVE to close the app, reconnect, open the app.
* If all else fails try to power cycle both the camera and the phone.
* To help the GPS signal, try providing a WiFi hotspot and open Google Maps. Once the correct location is shown there, reconnect to the camera and re-open the app.



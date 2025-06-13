# UofT Mobile Android App

<a href='https://play.google.com/store/apps/details?id=ca.utoronto.megaapp'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width=300/></a>

This app is built using a combination of clean architecture principles, tailored to work effectively within the Android development environment. We've also followed Google's recommended app architecture guidelines and used MVVM. The app follows single activity with multiple composable.

<p float="left">    
<img src="https://github.com/RoneyThomas/UofTMobileAndroid/blob/master/docs/AppScreenShot1.jpg?raw=true" alt="app screenshot" width="150" />
<img src="https://github.com/RoneyThomas/UofTMobileAndroid/blob/master/docs/AppScreenShot2.jpg?raw=true" alt="app screenshot" width="150" />
<img src="https://github.com/RoneyThomas/UofTMobileAndroid/blob/master/docs/AppScreenShot3.jpg?raw=true" alt="app screenshot" width="150" />
</p>    

## Future improvements

1. If you check the json response, you will notice for some apps(e.g Quercus) the link is an app link. On Android before opening the app link you need to check if the app is installed on the device. Or else redirect the user to Google Play for the app listing, for that you need package namespace. Something to keep in mind is whenever there is a new addition of a new app with app link, make sure you are adding the package namespace in HomeScreen.kt, you will find a section with comment on where to add.
2. Support for dynamic theme and dark theme. To do that use [Material theme generator](https://material-foundation.github.io/material-theme-builder/). All the assets in the drawables also need to get updated for light and dark theme or use SVG icon with tint.
3. Currently the app uses OkHttp for network request and at the time of writing the coroutine support was experimental, once it's stable you can switch to it and move away from Livedata to Flow for the whole project.


# **Third Party Notices**

## **The following sets forth attribution notices for third party software that may be contained in portions of this product.**

### **Apache License 2.0**

The following components are licensed under the Apache License 2.0 reproduced below:

* **AOSP**, Copyright 2021 The Android Open Source Project
* **AndroidX**
* **OkHttp**, Copyright 2019 Square, Inc.
* **kotlinx.serialization**
* **RSS-Parser**, Copyright 2016-2023 Marco Gomiero
* **Coil**, Copyright 2023 Coil Contributors  
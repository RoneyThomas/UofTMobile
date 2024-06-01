# UofT Mobile Android App

<a href='https://play.google.com/store/apps/details?id=ca.utoronto.megaapp'><img alt='Get it on Google Play' src='https://play.google.com/intl/en_us/badges/static/images/badges/en_badge_web_generic.png' width=300/></a>

This app is built using a combination of clean architecture principles, tailored to work effectively within the Android development environment. We've also followed Google's recommended app architecture guidelines and used MVVM. The app follows single activity with multiple composables.

<p float="left">
<img src="https://github.com/UofTMADLab/UofTMobileAndroid/blob/feature/final-fixes/docs/AppScreenShot1.webp?raw=true" alt="app screenshot" width="150" />  
<img src="https://github.com/UofTMADLab/UofTMobileAndroid/blob/feature/final-fixes/docs/AppScreenShot2.webp?raw=true" alt="app screenshot" width="150" />  
<img src="https://github.com/UofTMADLab/UofTMobileAndroid/blob/feature/final-fixes/docs/AppScreenShot3.webp?raw=true" alt="app screenshot" width="150" />  
</p>

## Future improvements

1. Support for dynamic theme and dark theme. To do that use [Material theme generator](https://material-foundation.github.io/material-theme-builder/). All the assets in the drawables also need to get updated for light and dark theme or use SVG icon with tint.
2. Currently the app uses OkHttp for network request and at the time of writing the coroutine support was experimental, once it's stable you can switch to it and move away from Livedata to Flow.
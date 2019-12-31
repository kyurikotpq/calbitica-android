# calbitica-android

## Take Notes:
* Reference Links: https://github.com/alamkanak/Android-Week-View/issues/541
    * Example: change "targetSdkVersion" 28 to 27 from app level build.gradle(from one of the comment in the link)
* minSdkVersion 24 -> For firebase getOrDefault function(Able to retrieve the key of the checking values(Important)), previously is version 16
* Refer to your Google Account(firebase website) to read/edit the database(Shared)
* File → Settings → Build, Excecution, Deployment
    * Sync Project with Gradle before building, if needed(Tick this), to prevent Java Compiler Issue
    
## To use more for individual project(Optional):
```
    To this for your android app connect to your own firebase(Google Account):
    * Firebase Authentication(important):
    1) Tools → Firebase → Realtime Database → Save and retrieve data
    2) (1) Connect to Firebase → Choose your own Google Account for the firebase
        2.1) Back to Android Studio
        2.2) Choose "Create new Firebase Project" → Connect to Firebase(Repeat (1) if fail)
    3) Inside the Firebase Website → Select "Calbitica Android"
    4) Database → Realtime Database → Create database → "Start in test mode" → Enable
    5) Run the "app" from Android Studio
    
    
    Optional:
    To disconnect your own firebase(Google Account) from your android app(Connect to another Google account):
    1) Inside the Firebase Website → "Select Calbitica Android"
    2) Export JSON from the database(if you want to keep your data)
    3) Project Overview(Setting) → Project settings → "Remove this app" → "Remove Permanently"
    4) Delete project → tick all 4 boxes → Delete Project
    5) At "calbitica-android" folder → app → Delete "google-services.JSON"(ensure your Android Studio is closed)
    6) Open up your "Android Studio", "Error" appear(Ignore it), → Repeat the 1st step of "Firebase Authentication(important)"
    7) Connect to Firebase → Select the "Sign out" option → Cancel → "Connect to Firebase"  again
    8) Repeat the remaining steps according to "Firebase Authentication(important)"
    9) Lastly, import the JSON from the database(if you did keep your data)
```
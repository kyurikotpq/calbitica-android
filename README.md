# calbitica-android

## Notes:
* Reference Links: https://github.com/alamkanak/Android-Week-View/issues/541
    * Example: change "targetSdkVersion" 28 to 27 from app level build.gradle(from one of the comment in the link)
* minSdkVersion 24

## Because we are using Google Sign-In
which relies on my SHA1 fingerprint of my debug.keystore, I've included it in this project folder. Please do not share it with anyone else.

Back up your existing debug.keystore in your home folder, i.e. `C:\Users\<Username>\.android`, and use this provided debug.keystore instead. If this is not done, Google Sign in will not work at all.

## Access Token refreshing
Access Tokens from the Google OAuth process expire every hour. Therefore, you may encounter issues with the app due to the API not being able to pull anything from your Google Calendar.

Solution: signing in and out usually solves the problem, but if all else fails, revoke Calbitica's access to your account and try again.

## API
The API is hosted at [https://app.kyurikotpq.com/calbitica](https://app.kyurikotpq.com/calbitica).

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
The API is hosted at [https://app.kyurikotpq.com/calbitica/](https://app.kyurikotpq.com/calbitica/) (trailing backslash is needed). A Habitica account must be connected or the API will refuse to do anything. This is because rewards (gold coins, experience points, etc.) are tagged to Habitica tasks, so a Habitica account is crucial.

## Work breakdown:
- Google Auth Sign In, Session Management & Sign Out: Pei Qi
- WeekView - CRUD, Today, Refresh & Add buttons: Poh Heng
- AgendaView - CRUD, Display, Add: Poh Heng
- AgendaView - Troubleshooting: Pei Qi & Poh Heng
- Sync Calendars: Poh Heng
- Profile (Stats bars and Damage button): Pei Qi
- Profile (Quest accept/reject): Poh Heng
- Settings: Pei Qi
- About: Poh Heng
- Code structuring & cleanup: Pei Qi*
- Utilities (CAWrapper): Poh Heng (writing) & Pei Qi (structuring & troubleshooting)
- Utilities (others): Pei Qi

*: Since Poh Heng wanted to use the Retrofit library for HTTP requests, I wrote a class, CalbiticaAPI, to provide a more centralised way of using the library.

Retrofit is strongly typed as well, so the individual interfaces and classes (CalbitInterface, HabiticaInterface, etc.) were created to facilitate that. Though, I must admit, our API wasn't coded very well and we had to discard the type casting for some requests in order for our API to go through.

Poh Heng later mentioned that CalbiticaAPI wasn't centralised enough, especially when it came to requesting all of the user's events ("calbits") from our API for both AgendaView and WeekView. If relying solely on CalbiticaAPI alone, the `apiCall.enqueue(...)` has to be called twice, once in each fragment. 

So he wrote a class, originally named Database, to do HTTP requests *and* rendering of events. I moved the rendering codes to the fragments, triggering them through the use of interfaces (*ResultInterfaces) which was a design pattern we learnt in class.

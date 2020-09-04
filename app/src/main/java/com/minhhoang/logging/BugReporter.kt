package com.minhhoang.logging

import com.google.firebase.crashlytics.FirebaseCrashlytics

//import com.crashlytics.android.Crashlytics

class BugReporter {
    fun setUserIdentifier(userIdentifier: String) {
        //Crashlytics.setUserIdentifier(userIdentifier)
        FirebaseCrashlytics.getInstance().setUserId(userIdentifier)
    }
}

package am.a_t.myapplication

import android.app.Application

class UserIntentApplication: Application() {

    override fun onCreate() {
        super.onCreate()
        UserRepository.initialize(this)
    }
}
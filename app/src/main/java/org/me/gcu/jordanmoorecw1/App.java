package org.me.gcu.jordanmoorecw1;

import android.app.Application;

// application class used to keep a global app instance
public class App extends Application {

    // single shared instance of this application
    private static App instance;

    @Override
    public void onCreate() {
        super.onCreate();
        // store this instance so other classes can get the app context
        instance = this;
    }

    // returns the shared app instance
    public static App getInstance() {
        return instance;
    }
}

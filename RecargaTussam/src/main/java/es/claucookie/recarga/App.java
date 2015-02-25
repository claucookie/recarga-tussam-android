package es.claucookie.recarga;

import android.app.Application;

import com.crashlytics.android.Crashlytics;

import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        System.out.println("App started!");
        Fabric.with(this, new Crashlytics());
    }

}

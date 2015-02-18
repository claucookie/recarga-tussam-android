package es.claucookie.recarga;

import android.app.Application;
import com.crashlytics.android.Crashlytics;
import com.mopub.common.MoPub;
import io.fabric.sdk.android.Fabric;

public class App extends Application {

    @Override
    public void onCreate() {
        super.onCreate();
        Fabric.with(this, new Crashlytics(), new MoPub());
        System.out.println("App started!");
    }

}

package base;

import android.app.Application;

import api.AdjaranetAPI;
import ge.redefine.adjaranet.R;
import helpers.PreferencesManager;
import helpers.ResourcesProvider;
import io.realm.Realm;
import io.realm.RealmConfiguration;
import model.RMHelper;
import network.VolleySingleton;
import uk.co.chrisjenx.calligraphy.CalligraphyConfig;

public class App extends Application {

    private static App sInstance;

    @Override
    public void onCreate() {
        super.onCreate();

        // save instance for later use
        sInstance = this;

        // set default font
        CalligraphyConfig.initDefault(new CalligraphyConfig.Builder()
                .setDefaultFontPath("fonts/RobotoCondensed-Regular.ttf")
                .setFontAttrId(R.attr.fontPath)
                .build()
        );

        // initialize Realm
        Realm.init(this);
        RealmConfiguration configuration = new RealmConfiguration.Builder()
                .deleteRealmIfMigrationNeeded()
                .build();
        Realm.setDefaultConfiguration(configuration);

        // initialize realm helper
        RMHelper.init(Realm.getDefaultInstance());

        // initialize shared preferences manager
        PreferencesManager.init(this);

        // initialize resources provider
        ResourcesProvider.init(this);

        // initialize volley
        VolleySingleton.init(this);

        // initialize api
        AdjaranetAPI.init(VolleySingleton.getInstance());
    }

    public static App getInstance() {
        return sInstance;
    }
}

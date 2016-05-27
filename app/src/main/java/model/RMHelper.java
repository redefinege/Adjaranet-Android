package model;

import io.realm.Realm;

public abstract class RMHelper {
    protected static Realm sRealm;

    public static void init(Realm realm) {
        sRealm = realm;
    }
}

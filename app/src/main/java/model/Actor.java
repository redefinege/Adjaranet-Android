package model;

import org.parceler.Parcel;

import io.realm.ActorRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings("unused")
@Parcel(implementations = {ActorRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Actor.class})
public class Actor extends RealmObject {
    public static final String ID = "id";
    public static final String NAME = "name";

    @PrimaryKey
    String id;
    String name;

    public Actor() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }
}

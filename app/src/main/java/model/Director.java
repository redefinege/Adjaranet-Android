package model;

import org.parceler.Parcel;

import io.realm.DirectorRealmProxy;
import io.realm.RealmObject;
import io.realm.annotations.PrimaryKey;

@SuppressWarnings("unused")
@Parcel(implementations = {DirectorRealmProxy.class},
        value = Parcel.Serialization.BEAN,
        analyze = {Director.class})
public class Director extends RealmObject {
    public static final String ID = "id";
    public static final String NAME_EN = "nameEn";
    public static final String NAME_KA = "nameKa";

    @PrimaryKey
    String id;
    String nameEn;
    String nameKa;

    public Director() {
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getNameEn() {
        return nameEn;
    }

    public void setNameEn(String nameEn) {
        this.nameEn = nameEn;
    }

    public String getNameKa() {
        return nameKa;
    }

    public void setNameKa(String nameKa) {
        this.nameKa = nameKa;
    }
}

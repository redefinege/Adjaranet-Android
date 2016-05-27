package model;

import org.parceler.Parcel;

@SuppressWarnings("unused")
@Parcel
public class Genre {
    String id;
    String name;

    public Genre() {
    }

    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public void setId(String id) {
        this.id = id;
    }

    public void setName(String name) {
        this.name = name;
    }
}

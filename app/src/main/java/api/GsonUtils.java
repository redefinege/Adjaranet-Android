package api;

import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public final class GsonUtils {
    private GsonUtils() {
    }

    public static int getNullIntegerAsNegative(JsonObject object, String member) {
        if (object.has(member)) {
            final JsonElement element = object.get(member);
            if (!element.isJsonNull()) {
                return element.getAsInt();
            }
        }
        return -1;
    }

    public static String getNullStringAsEmpty(JsonObject object, String member) {
        if (object.has(member)) {
            final JsonElement element = object.get(member);
            if (!element.isJsonNull()) {
                return element.getAsString().trim();
            }
        }
        return "";
    }

    public static List<String> getCommaSeparatedList(JsonObject object, String member) {
        final List<String> list = new ArrayList<>();
        if (object.has(member)) {
            final JsonElement element = object.get(member);
            if (!element.isJsonNull()) {
                final String str = element.getAsString().trim();
                final String[] parts = str.split(",");
                Collections.addAll(list, parts);
            }
        }
        return list;
    }
}

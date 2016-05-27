package helpers;

import android.content.Context;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Calendar;
import java.util.HashMap;
import java.util.List;

import ge.redefine.adjaranet.R;

public class ResourcesProvider {
    private static Context sContext;

    private ResourcesProvider() {
    }

    public static void init(Context context) {
        sContext = context;
    }

    public static HashMap<String, String> getCountryMap() {
        String[] countryKeys = sContext.getResources().getStringArray(R.array.CountryKeys);
        String[] countryVals = sContext.getResources().getStringArray(R.array.CountryValues);
        HashMap<String, String> countryMap = new HashMap<>();

        for (int i = 0; i < countryKeys.length; i++) {
            countryMap.put(countryKeys[i], countryVals[i]);
        }

        return countryMap;
    }

    public static HashMap<String, String> getLanguageMap() {
        String[] languageKeys = sContext.getResources().getStringArray(R.array.LanguageKeys);
        String[] languageVals = sContext.getResources().getStringArray(R.array.LanguageValues);
        HashMap<String, String> languageMap = new HashMap<>();

        for (int i = 0; i < languageKeys.length; i++) {
            languageMap.put(languageKeys[i], languageVals[i]);
        }

        return languageMap;
    }

    public static List<String> getYearsList() {
        final List<String> yearsList = new ArrayList<>();
        int currentYear = Calendar.getInstance().get(Calendar.YEAR);

        for (int i = 1900; i <= currentYear; i++) {
            yearsList.add(String.valueOf(i));
        }

        return yearsList;
    }

    public static List<String> getSectionHeaderList() {
        return Arrays.asList(sContext.getResources().getStringArray(R.array.Sections));
    }

    public static String getErrorText() {
        return sContext.getResources().getString(R.string.filterError);
    }

    public static String getFavoriteMenuTitle(boolean isFavorite) {
        int stringName;
        if (isFavorite) {
            stringName = R.string.removeFromFavorites;
        } else {
            stringName = R.string.addToFavorites;
        }

        return sContext.getResources().getString(stringName);
    }

    public static String getKeyFromMap(HashMap<String, String> hashMap, String value) {
        for (String key : hashMap.keySet()) {
            if (hashMap.get(key).equals(value)) {
                return key;
            }
        }

        return "";
    }

    public static String getSeasonPrefixText() {
        return sContext.getResources().getString(R.string.season);
    }

    public static String getSeasonInfoText() {
        return sContext.getResources().getString(R.string.seasonInfo);
    }

    public static String getEpisodeInfoText() {
        return sContext.getResources().getString(R.string.episodeInfo);
    }
}

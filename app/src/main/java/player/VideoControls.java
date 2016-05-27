package player;

import android.content.Context;
import android.util.AttributeSet;
import android.view.View;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public abstract class VideoControls extends com.devbrackets.android.exomedia.ui.widget.VideoControls {
    protected List<String> languageList = new ArrayList<>();
    protected List<String> qualityList = new ArrayList<>();
    protected HashMap<String, String> languageMap = new HashMap<>();
    protected HashMap<String, String> qualityMap = new HashMap<>();
    protected int currentLanguageIndex = 0;
    protected int currentQualityIndex = 0;
    protected OnPlayerSettingsChangeListener settingsChangeListener;

    public VideoControls(Context context) {
        super(context);
    }

    public VideoControls(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoControls(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    public VideoControls(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    public void setTextContainerRemoved(boolean removed) {
        textContainer.setVisibility(removed ? View.GONE : View.VISIBLE);
    }

    public void setSettings(List<String> languageList, List<String> qualityList) {
        this.languageList = languageList;
        this.qualityList = qualityList;
    }

    public void setSettingsMap(HashMap<String, String> languageMap, HashMap<String, String> qualityMap) {
        this.languageMap = languageMap;
        this.qualityMap = qualityMap;
    }

    public void setCurrentSettings(int currentLanguageIndex, int currentQualityIndex) {
        this.currentLanguageIndex = currentLanguageIndex;
        this.currentQualityIndex = currentQualityIndex;
    }

    public void setSettingsChangeListener(OnPlayerSettingsChangeListener listener) {
        this.settingsChangeListener = listener;
    }
}
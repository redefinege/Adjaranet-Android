package player;

public interface OnPlayerSettingsChangeListener {
    void success(int languageIndex, int qualityIndex);
    void error();
}

package ui.activities;

import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;

import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.devbrackets.android.exomedia.listener.OnErrorListener;
import com.devbrackets.android.exomedia.listener.OnPreparedListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;

import org.parceler.Parcels;

import java.util.HashMap;

import api.AdjaranetAPI;
import ge.redefine.adjaranet.R;
import model.Movie;
import model.Series;
import player.OnFullscreenClickListener;
import player.OnPlayerSettingsChangeListener;
import player.VideoControlsMobile;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public abstract class MovieViewActivity extends AppCompatActivity implements OnPreparedListener {
    public static final String EXTRA_MOVIE = "MOVIE";
    public static final String EXTRA_SERIES = "SERIES";
    public static final String EXTRA_POSTER = "POSTER";

    protected Movie activeMovie = new Movie();
    protected Series currentSeries;
    protected Bitmap posterImage;
    protected int currentLanguageIndex = 0;
    protected int currentQualityIndex = 0;
    protected long currentPosition = 0;
    protected boolean isFullscreen = false;
    protected VideoView mVideoView;
    protected VideoControlsMobile mVideoControls;

    protected abstract void init();

    protected abstract void fullscreenClicked();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        isFullscreen = getResources().getConfiguration()
                .orientation == Configuration.ORIENTATION_LANDSCAPE;
        getIntentExtras();
        init();
        configureVideoView();
        preparePlayback();
    }

    @Override
    protected void onPause() {
        super.onPause();

        mVideoView.pause();
    }

    @Override
    public void onPrepared() {
        // Starts the video playback as soon as it is ready
        if (currentPosition > 0) {
            mVideoView.seekTo(currentPosition);
            mVideoView.start();
        }
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    protected void getIntentExtras() {
        if (getIntent() != null) {
            activeMovie = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_MOVIE));
            if (getIntent().hasExtra(EXTRA_SERIES)) {
                currentSeries = Parcels.unwrap(getIntent().getParcelableExtra(EXTRA_SERIES));
            }
            if (getIntent().hasExtra(EXTRA_POSTER)) {
                posterImage = getIntent().getParcelableExtra(EXTRA_POSTER);
            }
        }
    }

    protected void configureVideoView() {
        mVideoControls = new VideoControlsMobile(this);
//        mVideoControls.setNextButtonRemoved(false);
//        mVideoControls.setPreviousButtonRemoved(false);
        mVideoControls.setTextContainerRemoved(true);
        mVideoControls.setFullscreenClickListener(getOnFullscreenClickListener());
        mVideoControls.setSettingsMap(getLanguageMap(), getQualityMap());
        mVideoControls.setSettingsChangeListener(new OnPlayerSettingsChangeListener() {
            @Override
            public void success(int languageIndex, int qualityIndex) {
                // change player settings
                currentLanguageIndex = languageIndex - 1;
                currentQualityIndex = qualityIndex - 1;
                mVideoControls.setCurrentSettings(currentLanguageIndex, currentQualityIndex);
                updatePlayback();
            }

            @Override
            public void error() {
            }
        });

        mVideoView.setControls(mVideoControls);
        mVideoView.setOnPreparedListener(this);
        mVideoView.setBackgroundColor(Color.BLACK);
        mVideoView.setOnErrorListener(new OnErrorListener() {
            @Override
            public boolean onError(Exception e) {
                mVideoControls.showLoading(false);
                mVideoView.showControls();
                return true;
            }
        });
    }

    protected void preparePlayback() {
        mVideoControls.setSettings(activeMovie.getLanguagesAsList(), activeMovie.getQualitiesAsList());
        updatePlayback(false);

        AdjaranetAPI.getInstance()
                .getMovieThumbnailBitmap(activeMovie, new ImageLoader.ImageListener() {
                    @Override
                    public void onResponse(ImageLoader.ImageContainer response, boolean isImmediate) {
                        mVideoView.setPreviewImage(response.getBitmap());
                    }

                    @Override
                    public void onErrorResponse(VolleyError error) {
                    }
                });
    }

    protected void updatePlayback(boolean savePosition) {
        if (savePosition) {
            currentPosition = mVideoView.getCurrentPosition();
        } else {
            currentPosition = 0;
        }
        Uri uri = activeMovie.getPlaybackUri(currentLanguageIndex, currentQualityIndex);
        mVideoView.setVideoURI(uri);
    }

    protected void updatePlayback() {
        updatePlayback(true);
    }

    protected OnFullscreenClickListener getOnFullscreenClickListener() {
        return new OnFullscreenClickListener() {
            @Override
            public void fullscreenClicked() {
                if (mVideoView != null) {
                    MovieViewActivity.this.fullscreenClicked();
                }
            }
        };
    }

    protected HashMap<String, String> getLanguageMap() {
        String[] languageKeys = getResources().getStringArray(R.array.LanguageKeys);
        String[] languageValues = getResources().getStringArray(R.array.LanguageValues);
        HashMap<String, String> languageMap = new HashMap<>();
        for (int i = 0; i < languageKeys.length && i < languageValues.length; i++) {
            languageMap.put(languageKeys[i], languageValues[i]);
        }

        return languageMap;
    }

    protected HashMap<String, String> getQualityMap() {
        String[] qualityKeys = getResources().getStringArray(R.array.QualityKeys);
        String[] qualityValues = getResources().getStringArray(R.array.QualityValues);
        HashMap<String, String> qualityMap = new HashMap<>();
        for (int i = 0; i < qualityKeys.length && i < qualityValues.length; i++) {
            qualityMap.put(qualityKeys[i], qualityValues[i]);
        }

        return qualityMap;
    }
}

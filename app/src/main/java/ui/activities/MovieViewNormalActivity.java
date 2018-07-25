package ui.activities;

import android.annotation.TargetApi;
import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.graphics.Bitmap;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Environment;
import android.os.Handler;
import android.os.PersistableBundle;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.percent.PercentLayoutHelper;
import android.support.percent.PercentRelativeLayout;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.ImageLoader;
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener;
import com.devbrackets.android.exomedia.ui.widget.VideoView;
import com.devbrackets.android.exomedia.ui.widget.VideoControls;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.List;

import api.AdjaranetAPI;
import ge.redefine.adjaranet.R;
import helpers.PreferencesManager;
import helpers.ResourcesProvider;
import model.EpisodeList;
import model.Movie;
import network.VolleySingleton;
import ui.adapters.EpisodeItemsAdapter;
import ui.adapters.MovieItemsAdapter;
import ui.adapters.OnEpisodeClickListener;
import ui.adapters.OnMovieInteractionListener;
import ui.helpers.CustomNetworkImageView;
import ui.helpers.LoadingLayout;

class MovieInfoLayoutContainer {
    CustomNetworkImageView poster;
    TextView title;
    TextView info;
    TextView views;
    TextView description;
}

public class MovieViewNormalActivity extends MovieViewActivity
        implements OnEpisodeClickListener {
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private MovieItemsAdapter mRelatedAdapter;
    private LoadingLayout mLoadingLayout;
    private MaterialBetterSpinner mSeasonSpinner;
    private ArrayAdapter<String> mSeasonAdapter;
    private RecyclerView mEpisodeList;
    private EpisodeItemsAdapter mEpisodeItemsAdapter;
    private Integer mSelectedSeason = 1;
    private MovieInfoLayoutContainer mInfoContainer = new MovieInfoLayoutContainer();

    protected void init() {
        setContentView(R.layout.activity_movie_view);

        // Assign views
        mDrawerLayout = findViewById(R.id.movieview_drawer);
        mCoordinatorLayout = findViewById(R.id.movieview_coordinator);
        mVideoView = findViewById(R.id.movieview_video_view);

        mInfoContainer.poster = findViewById(R.id.movieview_poster);
        mInfoContainer.title = findViewById(R.id.movieview_title);
        mInfoContainer.info = findViewById(R.id.movieview_info);
        mInfoContainer.views = findViewById(R.id.movieview_views);
        mInfoContainer.description = findViewById(R.id.movieview_description);
        TextView relatedTitle = findViewById(R.id.section_title);
        Button downloadButton = findViewById(R.id.movieview_download);
        Button openButton = findViewById(R.id.movieview_open_player);
        RecyclerView relatedList = findViewById(R.id.section_recycler_view);
        RelativeLayout loadingContainer = findViewById(R.id.movieview_loading);

        mSeasonSpinner = findViewById(R.id.movieview_season_select);
        mEpisodeList = findViewById(R.id.movieview_episode_list);
        configureSeriesDrawer();

        // Handle button clicks
        downloadButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadManager downloadManager = (DownloadManager) getSystemService(Context.DOWNLOAD_SERVICE);
                Uri uri = activeMovie.getPlaybackUri(currentLanguageIndex, currentQualityIndex);
                DownloadManager.Request request = new DownloadManager.Request(uri);
                request.setTitle(getResources().getString(R.string.app_name))
                        .setDescription(activeMovie.getTitleEn())
                        .setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS,
                                uri.getLastPathSegment())
                        .setVisibleInDownloadsUi(true)
                        .setAllowedNetworkTypes(DownloadManager.Request.NETWORK_MOBILE
                                | DownloadManager.Request.NETWORK_WIFI);

                downloadManager.enqueue(request);
            }
        });

        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                Uri uri = activeMovie.getPlaybackUri(currentLanguageIndex, currentQualityIndex);
                intent.setDataAndType(uri, "video/*");
                startActivity(Intent.createChooser(intent, getResources().getString(R.string.videoPlayerChooserMessage)));
            }
        });

        // Build loading layout
        mLoadingLayout = new LoadingLayout(loadingContainer, new LoadingLayout.OnLoadingLayoutInteraction() {
            @Override
            public void onRetryClicked() {
                updateRelatedMovies();
            }
        });

        // Set movie info
        this.updateMovieInfo();
        this.updateMoviePoster(posterImage);

        // Build related movies
        relatedTitle.setText(getResources().getString(R.string.related));
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false);
        layoutManager.setAutoMeasureEnabled(true);
        relatedList.setLayoutManager(layoutManager);
        mRelatedAdapter = new MovieItemsAdapter(new OnMovieInteractionListener() {
            @Override
            public void onMovieClicked(final Movie model, final ImageView imageView) {
                AdjaranetAPI.getInstance().getMoviePlaybackInfo(model, new Response.Listener<String>() {
                    @Override
                    public void onResponse(String response) {
                        activeMovie = model;

                        updateMoviePoster();
                        updateMovieInfo();
                        preparePlayback();
                        updateRelatedMovies();
                    }
                }, new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        Snackbar.make(mCoordinatorLayout,
                                getResources().getString(R.string.errorFetchMovieData),
                                Snackbar.LENGTH_SHORT).show();
                    }
                });
            }

            @Override
            public void onFavoriteClicked(Movie movie) {
            }
        });
        relatedList.setAdapter(mRelatedAdapter);

        this.updateRelatedMovies();
        this.openDrawerIfRequired();
    }

    @Override
    public void onCreate(Bundle savedInstanceState, PersistableBundle persistentState) {
        super.onCreate(savedInstanceState, persistentState);
    }

    @Override
    protected void configureVideoView() {
        super.configureVideoView();

        mVideoControls.setVisibilityListener(new ControlsVisibilityListener());
        adjustVideoViewSize();
    }

    private void updateMovieInfo() {
        mInfoContainer.title.setText(activeMovie.getFullTitle());
        mInfoContainer.info.setText(String.format(getResources().getString(R.string.movieInfo),
                activeMovie.getReleaseYear(), activeMovie.getDuration()));
        mInfoContainer.views.setText(String.format(getResources().getString(R.string.viewCount),
                activeMovie.getViewCount()));
        mInfoContainer.description.setText(activeMovie.getDescription());
    }

    private void updateMoviePoster() {
        ImageLoader imageLoader = VolleySingleton.getInstance().getImageLoader();

        mInfoContainer.poster.setImageUrl(activeMovie.getPoster(), imageLoader);
    }

    private void updateMoviePoster(Bitmap posterImageBitmap) {
        mInfoContainer.poster.setLocalImageBitmap(posterImageBitmap);
    }

    private void configureSeriesDrawer() {
        // spinner
        mSeasonAdapter = new ArrayAdapter<>(this,
                android.R.layout.simple_dropdown_item_1line, new ArrayList());

        mSeasonSpinner.setAdapter(mSeasonAdapter);

        // recyclerview
        mEpisodeList.setLayoutManager(new LinearLayoutManager(this));
        mEpisodeItemsAdapter = new EpisodeItemsAdapter(this, this);
        mEpisodeList.setAdapter(mEpisodeItemsAdapter);

        updateSeriesDrawer();
    }

    private void updateSeriesDrawer() {
        if (currentSeries == null) {
            mDrawerLayout.setDrawerLockMode(DrawerLayout.LOCK_MODE_LOCKED_CLOSED);
            return;
        }

        String seasonPrefix = ResourcesProvider.getSeasonPrefixText();
        List<Integer> seasonList = currentSeries.getSeasonList();
        List<String> seasonListWithPrefix = new ArrayList<>();
        for (Integer seasonNumber : seasonList) {
            seasonListWithPrefix.add(seasonPrefix + " " + String.valueOf(seasonNumber));
        }

        mSeasonAdapter.clear();
        mSeasonAdapter.addAll(seasonListWithPrefix);
        mSeasonSpinner.setAdapter(mSeasonAdapter);

        mSeasonSpinner.setText(seasonListWithPrefix.get(activeMovie.getSeasonAsInteger() - 1));
        mSeasonSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                mSelectedSeason = currentSeries.getSeasonNumberByIndex(i);
                updateEpisodeList();
            }
        });

        updateEpisodeListForCurrentSeason();
    }

    @Override
    public void onEpisodeClicked(int position) {
        mDrawerLayout.closeDrawer(GravityCompat.END);
        EpisodeList episodeList = currentSeries.getEpisodesMap().get(mSelectedSeason);
        activeMovie.setSeason(String.valueOf(mSelectedSeason));
        activeMovie.setEpisode(String.valueOf(episodeList.getEpisodeNumberByIndex(position)));
        updatePlayback(false);
        updateEpisodeListForCurrentSeason();
    }

    private void updateEpisodeListForCurrentSeason() {
        mSelectedSeason = activeMovie.getSeasonAsInteger();
        updateEpisodeList();
    }

    private void updateEpisodeList() {
        Integer currentEpisode = -1;
        if (mSelectedSeason.equals(activeMovie.getSeasonAsInteger())) {
            currentEpisode = activeMovie.getEpisodeAsInteger();
        }

        EpisodeList episodeList = currentSeries.getEpisodesMap().get(mSelectedSeason);
        mEpisodeItemsAdapter.update(episodeList, currentEpisode);
    }

    private void adjustVideoViewSize() {
        PercentRelativeLayout.LayoutParams params = (PercentRelativeLayout.LayoutParams) mVideoView.getLayoutParams();
        PercentLayoutHelper.PercentLayoutInfo info = params.getPercentLayoutInfo();
        info.heightPercent = isFullscreen ? 1f : 0.35f;
        mVideoView.requestLayout();
    }

    private void adjustScreenOrientation() {
        if (isFullscreen) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
        } else {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }
    }

    private void adjustFullscreenFlags() {
        setUiFlags(isFullscreen);
    }

    @Override
    public void onBackPressed() {
        if (isFullscreen) {
            fullscreenClicked();
            return;
        }
        super.onBackPressed();
    }

    @Override
    protected void fullscreenClicked() {
        isFullscreen = !isFullscreen;

        adjustFullscreenFlags();
        adjustScreenOrientation();
        adjustVideoViewSize();
    }

    private void updateRelatedMovies() {
        if (activeMovie == null) {
            return;
        }

        mLoadingLayout.showProgressBar();
        AdjaranetAPI.getInstance()
                .getRelatedMovies(activeMovie,
                        new Response.Listener<List<Movie>>() {
                            @Override
                            public void onResponse(List<Movie> response) {
                                mRelatedAdapter.update(response);
                                mLoadingLayout.setVisibility(false);
                            }
                        },
                        new Response.ErrorListener() {
                            @Override
                            public void onErrorResponse(VolleyError error) {
                                mLoadingLayout.showRetryButton();
                            }
                        }
                );
    }

    private void openDrawerIfRequired() {
        if (currentSeries != null
                && PreferencesManager.getInstance().isFirstSeriesLaunch()) {
            new Handler().postDelayed(new Runnable() {
                @Override
                public void run() {
                    mDrawerLayout.openDrawer(GravityCompat.END);
                    PreferencesManager.getInstance().setFirstSeriesLaunched();
                }
            }, 2000);
        }
    }

    /**
     * Applies the correct flags to the windows decor view to enter
     * or exit fullscreen mode
     *
     * @param fullscreen True if entering fullscreen mode
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private void setUiFlags(boolean fullscreen) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.ICE_CREAM_SANDWICH) {
            View decorView = getWindow().getDecorView();
            if (decorView != null) {
                decorView.setSystemUiVisibility(fullscreen ? getFullscreenUiFlags() : View.SYSTEM_UI_FLAG_VISIBLE);
                decorView.setOnSystemUiVisibilityChangeListener(new FullScreenListener());
            }
        }
    }

    /**
     * Determines the appropriate fullscreen flags based on the
     * systems API version.
     *
     * @return The appropriate decor view flags to enter fullscreen mode when supported
     */
    @TargetApi(Build.VERSION_CODES.ICE_CREAM_SANDWICH)
    private int getFullscreenUiFlags() {
        int flags = View.SYSTEM_UI_FLAG_LOW_PROFILE | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.JELLY_BEAN) {
            flags |= View.SYSTEM_UI_FLAG_LAYOUT_STABLE
                    | View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_LAYOUT_HIDE_NAVIGATION
                    | View.SYSTEM_UI_FLAG_FULLSCREEN
                    | View.SYSTEM_UI_FLAG_HIDE_NAVIGATION;
        }

        return flags;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    /**
     * Listens to the system to determine when to show the default controls
     * for the {@link VideoView}
     */
    @TargetApi(Build.VERSION_CODES.HONEYCOMB)
    private class FullScreenListener implements View.OnSystemUiVisibilityChangeListener {
        @Override
        public void onSystemUiVisibilityChange(int visibility) {
            if ((visibility & View.SYSTEM_UI_FLAG_FULLSCREEN) == 0) {
                mVideoView.showControls();
            }
        }
    }

    /**
     * A Listener for the {@link VideoControls}
     * so that we can re-enter fullscreen mode when the controls are hidden.
     */
    private class ControlsVisibilityListener implements VideoControlsVisibilityListener {
        @Override
        public void onControlsShown() {
            // No additional functionality performed
        }

        @Override
        public void onControlsHidden() {
            if (isFullscreen) {
                setUiFlags(true);
            }
        }
    }
}

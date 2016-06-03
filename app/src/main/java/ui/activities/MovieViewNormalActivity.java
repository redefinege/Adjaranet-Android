package ui.activities;

import android.annotation.TargetApi;
import android.content.Intent;
import android.content.pm.ActivityInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.ScrollView;
import android.widget.TextView;

import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.devbrackets.android.exomedia.listener.VideoControlsVisibilityListener;
import com.devbrackets.android.exomedia.ui.widget.EMVideoView;
import com.devbrackets.android.exomedia.ui.widget.VideoControls;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.List;

import api.AdjaranetAPI;
import ge.redefine.adjaranet.R;
import helpers.PreferencesManager;
import helpers.ResourcesProvider;
import model.Episode;
import model.Movie;
import ui.adapters.EpisodeItemsAdapter;
import ui.adapters.MovieItemsAdapter;
import ui.adapters.OnEpisodeClickListener;
import ui.adapters.OnMovieInteractionListener;
import ui.helpers.CustomNetworkImageView;
import ui.helpers.LoadingLayout;

public class MovieViewNormalActivity extends MovieViewActivity
        implements OnEpisodeClickListener {
    private DrawerLayout mDrawerLayout;
    private CoordinatorLayout mCoordinatorLayout;
    private ScrollView mInfoContainerLayout;
    private MovieItemsAdapter mRelatedAdapter;
    private LoadingLayout mLoadingLayout;
    private MaterialBetterSpinner mSeasonSpinner;
    private ArrayAdapter<String> mSeasonAdapter;
    private RecyclerView mEpisodeList;
    private EpisodeItemsAdapter mEpisodeItemsAdapter;

    protected void init() {
        setContentView(R.layout.activity_movie_view);

        // Assign views
        mDrawerLayout = (DrawerLayout) findViewById(R.id.movieview_drawer);
        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.movieview_coordinator);
        mVideoView = (EMVideoView) findViewById(R.id.movieview_video_view);
        mInfoContainerLayout = (ScrollView) findViewById(R.id.movieview_info_container);

        CustomNetworkImageView poster = (CustomNetworkImageView) findViewById(R.id.movieview_poster);
        TextView title = (TextView) findViewById(R.id.movieview_title);
        TextView info = (TextView) findViewById(R.id.movieview_info);
        TextView views = (TextView) findViewById(R.id.movieview_views);
        TextView description = (TextView) findViewById(R.id.movieview_description);
        TextView relatedTitle = (TextView) findViewById(R.id.section_title);
        Button downloadButton = (Button) findViewById(R.id.movieview_download);
        Button openButton = (Button) findViewById(R.id.movieview_open_player);
        RecyclerView relatedList = (RecyclerView) findViewById(R.id.section_recycler_view);
        RelativeLayout loadingContainer = (RelativeLayout) findViewById(R.id.movieview_loading);

        mSeasonSpinner = (MaterialBetterSpinner) findViewById(R.id.movieview_season_select);
        mEpisodeList = (RecyclerView) findViewById(R.id.movieview_episode_list);
        configureSeriesDrawer();

        // Handle button clicks
        openButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.parse(activeMovie.getPlaybackUrl(currentLanguageIndex, currentQualityIndex)), "video/*");
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
        poster.setLocalImageBitmap(posterImage);
        title.setText(activeMovie.getFullTitle());
        info.setText(String.format(getResources().getString(R.string.movieInfo),
                activeMovie.getReleaseYear(), activeMovie.getDuration()));
        views.setText(String.format(getResources().getString(R.string.viewCount),
                activeMovie.getViewCount()));
        description.setText(activeMovie.getDescription());

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

        updateRelatedMovies();

        openDrawerIfRequired();
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
        for (int i = 0; i < seasonList.size(); i++) {
            seasonListWithPrefix.add(seasonPrefix + " " + seasonList.get(i));
        }

        mSeasonAdapter.clear();
        mSeasonAdapter.addAll(seasonListWithPrefix);
        mSeasonSpinner.setAdapter(mSeasonAdapter);

        mSeasonSpinner.setText(seasonListWithPrefix.get(activeMovie.getSeasonAsInt() - 1));
        mSeasonSpinner.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                updateEpisodeList(Integer.valueOf(i + 1));
            }
        });

        updateEpisodeList();
    }

    @Override
    public void onEpisodeClicked(int position) {
        mDrawerLayout.closeDrawer(GravityCompat.END);
        activeMovie.setEpisode(String.valueOf(position + 1));
        updatePlayback(false);
        updateEpisodeList();
    }

    private void updateEpisodeList() {
        updateEpisodeList(activeMovie.getSeasonAsInt());
    }

    private void updateEpisodeList(Integer season) {
        int currentEpisode = -1;
        if (season.equals(activeMovie.getSeason())) {
            currentEpisode = activeMovie.getEpisodeAsInt() - 1;
        }

        List<Episode> episodeList = currentSeries.getEpisodesMap().get(season);
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
     * for the {@link EMVideoView}
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

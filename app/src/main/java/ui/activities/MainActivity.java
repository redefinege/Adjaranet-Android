package ui.activities;

import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.support.design.widget.CoordinatorLayout;
import android.support.design.widget.Snackbar;
import android.support.design.widget.TabLayout;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.ActivityOptionsCompat;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.FragmentPagerAdapter;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AppCompatActivity;
import android.view.Gravity;
import android.widget.ImageView;
import android.widget.LinearLayout;

import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.github.silvestrpredko.dotprogressbar.DotProgressBar;

import org.parceler.Parcels;

import java.util.ArrayList;
import java.util.List;

import api.AdjaranetAPI;
import ge.redefine.adjaranet.R;
import helpers.ResourcesProvider;
import model.Episode;
import model.FavoriteRM;
import model.Movie;
import model.Series;
import ui.adapters.OnMovieInteractionListener;
import ui.fragments.FavoritesFragment;
import ui.fragments.FilterFragment;
import ui.fragments.HomeFragment;
import uk.co.chrisjenx.calligraphy.CalligraphyContextWrapper;

public class MainActivity extends AppCompatActivity implements OnMovieInteractionListener {
    private static final int FRAGMENT_HOME_INDEX = 0;
    private static final int FRAGMENT_FILTER_INDEX = 1;
    private static final int FRAGMENT_FAVORITES_INDEX = 2;

    private CoordinatorLayout mCoordinatorLayout;
    private TabLayout mTabLayout;
    private ViewPager mViewPager;
    private MainPagerAdapter mPagerAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        mCoordinatorLayout = (CoordinatorLayout) findViewById(R.id.main_coordinator);
        mViewPager = (ViewPager) findViewById(R.id.view_pager);
        mTabLayout = (TabLayout) findViewById(R.id.tab_layout);

        // Configure pager adapter
        mPagerAdapter = new MainPagerAdapter(getSupportFragmentManager());
        mPagerAdapter.addFragment(HomeFragment.newInstance()); // FRAGMENT_HOME_INDEX
        mPagerAdapter.addFragment(FilterFragment.newInstance()); // FRAGMENT_FILTER_INDEX
        mPagerAdapter.addFragment(FavoritesFragment.newInstance()); // FRAGMENT_FAVORITES_INDEX
//        mPagerAdapter.addFragment(FilterFragment.newInstance());

        // Configure view pager
        mViewPager.setAdapter(mPagerAdapter);
        mViewPager.setOffscreenPageLimit(3);

        // Configure tab layout
        mTabLayout.setupWithViewPager(mViewPager);
        mTabLayout.getTabAt(FRAGMENT_HOME_INDEX).setIcon(R.drawable.ic_home_white_24px);
        mTabLayout.getTabAt(FRAGMENT_FILTER_INDEX).setIcon(R.drawable.ic_search_white_24dp);
        mTabLayout.getTabAt(FRAGMENT_FAVORITES_INDEX).setIcon(R.drawable.ic_favorite_white_24px);
//        mTabLayout.getTabAt(3).setIcon(R.drawable.ic_menu_white_24px);
    }

    @Override
    protected void attachBaseContext(Context newBase) {
        super.attachBaseContext(CalligraphyContextWrapper.wrap(newBase));
    }

    @Override
    public void onMovieClicked(final Movie movie, final ImageView imageView) {
        // FIXME: 5/22/16 too much logic in this method
        // Build custom view for progressbar
        LinearLayout customViewLayout = new LinearLayout(this);
        DotProgressBar dotProgressBar = new DotProgressBar(this);
        customViewLayout.setGravity(Gravity.CENTER);
        customViewLayout.addView(dotProgressBar);

        // Show progressbar
        final MaterialDialog dialog = new MaterialDialog.Builder(this)
                .customView(customViewLayout, false)
                .canceledOnTouchOutside(false)
                .cancelListener(new DialogInterface.OnCancelListener() {
                    @Override
                    public void onCancel(DialogInterface dialogInterface) {
                        // Cancel all requests
                        AdjaranetAPI.getInstance().cancelAllRequests();
                    }
                })
                .build();

        dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(this, android.R.color.transparent)));
        dialog.getWindow().setDimAmount(0.75f);
        dialog.show();

        // Fetch playback info
        // Imitate loading delay for some eye-sugar
        new Handler().postDelayed(new Runnable() {
            @Override
            public void run() {
                AdjaranetAPI api = AdjaranetAPI.getInstance();
                final Response.ErrorListener errorListener = new Response.ErrorListener() {
                    @Override
                    public void onErrorResponse(VolleyError error) {
                        dialog.cancel();
                        Snackbar.make(mCoordinatorLayout, ResourcesProvider.getErrorText(), Snackbar.LENGTH_SHORT).show();
                    }
                };

                if (movie.isEpisode()) {
                    api.getSeriesInfo(movie.getId(), new Response.Listener<Series>() {
                        @Override
                        public void onResponse(Series response) {
                            dialog.dismiss();
                            Episode episode = response.getEpisode(movie.getSeasonAsInt(), movie.getEpisodeAsInt());
                            if (episode == null) {
                                errorListener.onErrorResponse(null);
                                return;
                            }

                            movie.setLanguages(episode.getLanguage());
                            movie.setQualities(episode.getQuality());
                            movie.setPlaybackUrl(response.getUrl());
                            startMovieActivity(movie, imageView, response);
                        }
                    }, errorListener);
                } else {
                    api.getMoviePlaybackInfo(movie, new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            dialog.dismiss();
                            startMovieActivity(movie, imageView, null);
                        }
                    }, errorListener);
                }
            }
        }, 500);
    }

    @Override
    public void onFavoriteClicked(Movie movie) {
        FavoriteRM.toggle(movie);
        Fragment fragment = mPagerAdapter.getItem(FRAGMENT_FAVORITES_INDEX);
        if (fragment != null) {
            ((FavoritesFragment) fragment).updateContent();
        }
    }

    private void startMovieActivity(Movie movie, ImageView imageView, Series extraSeries) {
        final Intent intent = new Intent(MainActivity.this, MovieViewNormalActivity.class);
        String transitionName = getString(R.string.transition_movie);
        final ActivityOptionsCompat options = ActivityOptionsCompat.
                makeSceneTransitionAnimation(MainActivity.this, imageView, transitionName);

        intent.putExtra(MovieViewActivity.EXTRA_MOVIE, Parcels.wrap(movie));
        if (extraSeries != null) {
            intent.putExtra(MovieViewActivity.EXTRA_SERIES, Parcels.wrap(extraSeries));
        }
        Bitmap posterBitmap = ((BitmapDrawable) imageView.getDrawable()).getBitmap();
        intent.putExtra(MovieViewActivity.EXTRA_POSTER, posterBitmap);

        ActivityCompat.startActivity(MainActivity.this, intent, options.toBundle());
    }

    class MainPagerAdapter extends FragmentPagerAdapter {
        private final List<Fragment> mFragmentList = new ArrayList<>();
        private final List<String> mFragmentTitleList = new ArrayList<>();

        public MainPagerAdapter(FragmentManager fm) {
            super(fm);
        }

        @Override
        public CharSequence getPageTitle(int position) {
            return mFragmentTitleList.get(position);
        }

        @Override
        public Fragment getItem(int position) {
            return mFragmentList.get(position);
        }

        @Override
        public int getCount() {
            return mFragmentList.size();
        }

        public void addFragment(Fragment fragment) {
            addFragment(fragment, "");
        }

        public void addFragment(Fragment fragment, String title) {
            mFragmentList.add(fragment);
            mFragmentTitleList.add(title);
        }
    }
}

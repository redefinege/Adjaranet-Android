package ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.RelativeLayout;

import com.android.volley.Response;

import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;

import ui.adapters.OnMovieInteractionListener;
import ui.adapters.SectionsAdapter;
import api.AdjaranetAPI;
import model.Movie;
import ge.redefine.adjaranet.R;
import ui.helpers.LoadingLayout;
import helpers.ResourcesProvider;
import ui.helpers.SimpleDividerItemDecoration;

public class HomeFragment extends Fragment {
    private SwipeRefreshLayout mSwipeRefresh;
    private RecyclerView mRecyclerView;
    private SectionsAdapter mSectionsAdapter;
    private RelativeLayout mLoadingContainer;
    private LoadingLayout mLoadingLayout;
    private OnMovieInteractionListener mListener;

    public HomeFragment() {
        // Required empty public constructor
    }

    public static HomeFragment newInstance() {
        HomeFragment fragment = new HomeFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_home, container, false);

        mSwipeRefresh = (SwipeRefreshLayout) v.findViewById(R.id.home_swipe_refresh);
        mRecyclerView = (RecyclerView) v.findViewById(R.id.home_section_list);
        mLoadingContainer = (RelativeLayout) v.findViewById(R.id.home_loading);

        configureSwipeRefresh();
        configureRecyclerView();
        configureLoadingLayout();
        loadContent(false);

        return v;
    }

    @Override
    public void onSaveInstanceState(Bundle outState) {
        super.onSaveInstanceState(outState);
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMovieInteractionListener) {
            mListener = (OnMovieInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void configureSwipeRefresh() {
        mSwipeRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                mLoadingLayout.hideLayout();
                loadContent(true);
            }
        });
    }

    private void configureRecyclerView() {
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(getActivity());
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.addItemDecoration(new SimpleDividerItemDecoration(getActivity().getApplicationContext()));
        mSectionsAdapter = new SectionsAdapter(ResourcesProvider.getSectionHeaderList(), mListener);
        mRecyclerView.setAdapter(mSectionsAdapter);
    }

    private void configureLoadingLayout() {
        mLoadingLayout = new LoadingLayout(mLoadingContainer, new LoadingLayout.OnLoadingLayoutInteraction() {
            @Override
            public void onRetryClicked() {
                loadContent(false);
            }
        });
    }

    private void loadContent(final boolean shouldUpdateContent) {
        if (!shouldUpdateContent) {
            mRecyclerView.setVisibility(View.INVISIBLE);
            mLoadingLayout.showProgressBar();
        }

        new Thread(new Runnable() {
            @Override
            public void run() {
                final AdjaranetAPI api = AdjaranetAPI.getInstance();
                final CountDownLatch updateLatch = new CountDownLatch(4);

                api.getHomeGeorgianMovies(new Response.Listener<List<Movie>>() {
                    @Override
                    public void onResponse(List<Movie> response) {
                        mSectionsAdapter.updateChild(0, response);
                        updateLatch.countDown();
                    }
                }, null);

                api.getHomeNewMovies(new Response.Listener<List<Movie>>() {
                    @Override
                    public void onResponse(List<Movie> response) {
                        mSectionsAdapter.updateChild(1, response);
                        updateLatch.countDown();
                    }
                }, null);

                api.getHomePremiereMovies(new Response.Listener<List<Movie>>() {
                    @Override
                    public void onResponse(List<Movie> response) {
                        mSectionsAdapter.updateChild(2, response);
                        updateLatch.countDown();
                    }
                }, null);

                api.getHomeTopSeries(new Response.Listener<List<Movie>>() {
                    @Override
                    public void onResponse(List<Movie> response) {
                        mSectionsAdapter.updateChild(3, response);
                        updateLatch.countDown();
                    }
                }, null);

                boolean updateSuccess = false;
                try {
                    if (updateLatch.await(5L, TimeUnit.SECONDS)) {
                        updateSuccess = true;
                    }
                } catch (InterruptedException ignored) {
                }

                final boolean updateSuccessFinal = updateSuccess;
                // run on ui thread
                new Handler(Looper.getMainLooper()).post(new Runnable() {
                    @Override
                    public void run() {
                        if (updateSuccessFinal) {
                            mLoadingLayout.setVisibility(false);
                            setRefreshingVisibility(false);
                            mRecyclerView.setVisibility(View.VISIBLE);
                        } else {
                            if (!shouldUpdateContent) {
                                mLoadingLayout.showRetryButton();
                            }
                        }
                    }
                });
            }
        }).start();
    }

    private void setRefreshingVisibility(boolean visibility) {
        mSwipeRefresh.setRefreshing(visibility);
    }

    public interface OnAdapterInteractionListener {
        void onUpdateFinished(boolean success);
    }
}

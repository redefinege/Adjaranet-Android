package ui.fragments;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.widget.SwipeRefreshLayout;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import java.util.ArrayList;
import java.util.List;

import ge.redefine.adjaranet.R;
import io.realm.Realm;
import io.realm.RealmResults;
import model.Favorite;
import model.Movie;
import ui.adapters.MovieItemsAdapter;
import ui.adapters.OnMovieInteractionListener;
import ui.helpers.AutofitRecyclerView;

public class FavoritesFragment extends Fragment {
    private OnMovieInteractionListener mListener;
    private SwipeRefreshLayout mSwipeRefreshLayout;
    private AutofitRecyclerView mRecyclerView;
    private MovieItemsAdapter mItemsAdapter;
    private Realm mRealm;

    public FavoritesFragment() {
        // Required empty public constructor
    }

    public static FavoritesFragment newInstance() {
        FavoritesFragment fragment = new FavoritesFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mRealm = Realm.getDefaultInstance();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_favorites, container, false);

        mSwipeRefreshLayout = (SwipeRefreshLayout) v.findViewById(R.id.favorite_swipe_refresh);
        mRecyclerView = (AutofitRecyclerView) v.findViewById(R.id.favorite_movie_list);

        mSwipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                updateContent();
            }
        });

        configureRecyclerView();
        updateContent();

        return v;
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnMovieInteractionListener) {
            mListener = (OnMovieInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnMovieInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void configureRecyclerView() {
        // Configure adapter
        mItemsAdapter = new MovieItemsAdapter(mListener);
        mRecyclerView.setAdapter(mItemsAdapter);

        // TODO: 5/22/16 implement onscroll listener
    }

    public void updateContent() {
        RealmResults<Favorite> realmResults = mRealm.where(Favorite.class)
                .findAll();

        List<Movie> movieList = new ArrayList<>();
        for (Favorite favorite : realmResults) {
            movieList.add(favorite.getMovie());
        }

        mItemsAdapter.update(movieList);
        mSwipeRefreshLayout.setRefreshing(false);
    }
}

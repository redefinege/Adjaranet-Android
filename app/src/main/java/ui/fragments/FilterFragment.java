package ui.fragments;

import android.content.Context;
import android.graphics.PorterDuff;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.widget.ActionMenuView;
import android.support.v7.widget.GridLayoutManager;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.RelativeLayout;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.MaterialDialog;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.mypopsy.widget.FloatingSearchView;
import com.weiwangcn.betterspinner.library.material.MaterialBetterSpinner;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

import api.AdjaranetAPI;
import api.SearchBuilder;
import ge.redefine.adjaranet.R;
import helpers.ResourcesProvider;
import model.Movie;
import ui.adapters.MovieItemsAdapter;
import ui.adapters.OnMovieInteractionListener;
import ui.helpers.AutofitRecyclerView;
import ui.helpers.EndlessRecyclerViewScrollListener;
import ui.helpers.LoadingLayout;

public class FilterFragment extends Fragment {
    private OnMovieInteractionListener mListener;
    private FloatingSearchView mSearchView;
    private AutofitRecyclerView mRecyclerView;
    private MovieItemsAdapter mItemsAdapter;
    private LoadingLayout mLoadingLayout;
    private SearchBuilder mSearchBuilder;

    public FilterFragment() {
        // Required empty public constructor
    }

    public static FilterFragment newInstance() {
        FilterFragment fragment = new FilterFragment();
        return fragment;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        mSearchBuilder = AdjaranetAPI.newSearchBuilder();
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        View v = inflater.inflate(R.layout.fragment_filter, container, false);

        mRecyclerView = (AutofitRecyclerView) v.findViewById(R.id.filter_results_list);
        mSearchView = (FloatingSearchView) v.findViewById(R.id.filter_search);
        RelativeLayout loadingLayout = (RelativeLayout) v.findViewById(R.id.filter_loading);
        mLoadingLayout = new LoadingLayout(loadingLayout, new LoadingLayout.OnLoadingLayoutInteraction() {
            @Override
            public void onRetryClicked() {
                performSearch();
            }
        });

        configureRecyclerView();
        configureSearch();

        return v;
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

    private void configureRecyclerView() {
        // Configure adapter
        mItemsAdapter = new MovieItemsAdapter(mListener);
        mRecyclerView.setAdapter(mItemsAdapter);

        mRecyclerView.addOnScrollListener(
                new EndlessRecyclerViewScrollListener((GridLayoutManager) mRecyclerView.getLayoutManager()) {
                    @Override
                    public void onLoadMore(int page, int totalItemsCount) {
                        AdjaranetAPI.getInstance().getAdvancedSearchResults(
                                mSearchBuilder.setOffset(mSearchBuilder.getOffset() + mSearchBuilder.getDisplay()),
                                new Response.Listener<List<Movie>>() {
                                    @Override
                                    public void onResponse(List<Movie> response) {
                                        if (response.size() <= 0) {
                                            return;
                                        }
                                        mItemsAdapter.add(response);
                                    }
                                },
                                null
                        );
                    }
                }
        );
    }

    private void configureSearch() {
        MenuItem filterItem = mSearchView.getMenu().getItem(0);
        filterItem.getIcon().setColorFilter(ContextCompat.getColor(getActivity(), R.color.colorTextGray),
                PorterDuff.Mode.SRC_ATOP);

        mSearchView.setOnMenuItemClickListener(new ActionMenuView.OnMenuItemClickListener() {
            @Override
            public boolean onMenuItemClick(MenuItem item) {
                switch (item.getItemId()) {
                    case R.id.menu_action_filter: {
                        // build custom view
                        // FIXME: 5/19/16 not a best place to inflate custom view
                        LayoutInflater inflater = getActivity().getLayoutInflater();
                        final View v = inflater.inflate(R.layout.dialog_filter, null, false);
                        final MaterialBetterSpinner yearFrom = (MaterialBetterSpinner) v.findViewById(R.id.filter_dialog_yearFrom);
                        final MaterialBetterSpinner yearTo = (MaterialBetterSpinner) v.findViewById(R.id.filter_dialog_yearTo);
                        final MaterialBetterSpinner languageSpinner = (MaterialBetterSpinner) v.findViewById(R.id.filter_dialog_language);
                        final MaterialBetterSpinner countrySpinner = (MaterialBetterSpinner) v.findViewById(R.id.filter_dialog_country);

                        final List<String> yearsList = ResourcesProvider.getYearsList();
                        ArrayAdapter<String> yearsAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_dropdown_item_1line, yearsList);
                        yearFrom.setAdapter(yearsAdapter);
                        yearTo.setAdapter(yearsAdapter);
                        yearFrom.setText(mSearchBuilder.getStartYear());
                        yearTo.setText(mSearchBuilder.getEndYear());

                        final HashMap<String, String> languageMap = ResourcesProvider.getLanguageMap();
                        ArrayAdapter<String> languageAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(languageMap.values()));
                        languageSpinner.setAdapter(languageAdapter);
                        if (languageMap.containsKey(mSearchBuilder.getLanguage())) {
                            languageSpinner.setText(languageMap.get(mSearchBuilder.getLanguage()));
                        }

                        final HashMap<String, String> countryMap = ResourcesProvider.getCountryMap();
                        ArrayAdapter<String> countryAdapter = new ArrayAdapter<>(getActivity(),
                                android.R.layout.simple_dropdown_item_1line, new ArrayList<>(countryMap.values()));
                        countrySpinner.setAdapter(countryAdapter);
                        if (countryMap.containsKey(mSearchBuilder.getCountry())) {
                            countrySpinner.setText(countryMap.get(mSearchBuilder.getCountry()));
                        }

                        new MaterialDialog.Builder(getActivity())
                                .customView(v, false)
                                .autoDismiss(false)
                                .title(R.string.filterTitle)
                                .neutralText(R.string.filterReset)
                                .neutralColor(ContextCompat.getColor(getActivity(), R.color.colorTextDark))
                                .positiveText(android.R.string.ok)
                                .positiveColor(ContextCompat.getColor(getActivity(), R.color.colorPrimary))
                                .negativeText(android.R.string.cancel)
                                .negativeColor(ContextCompat.getColor(getActivity(), R.color.materialRed))
                                .onNeutral(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        // set defaults
                                        yearFrom.setText(yearsList.get(0));
                                        yearTo.setText(yearsList.get(yearsList.size() - 1));
                                        languageSpinner.setText("");
                                        countrySpinner.setText("");

                                        // clear errors
                                        yearFrom.setError(null);
                                        yearTo.setError(null);
                                        languageSpinner.setError(null);
                                        countrySpinner.setError(null);
                                    }
                                })
                                .onNegative(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        dialog.dismiss();
                                    }
                                })
                                .onPositive(new MaterialDialog.SingleButtonCallback() {
                                    @Override
                                    public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                        final String yearFromValue = yearFrom.getText().toString();
                                        final String yearToValue = yearTo.getText().toString();
                                        final String languageValue = languageSpinner.getText().toString();
                                        final String countryValue = countrySpinner.getText().toString();

                                        // perform error checks
                                        if (!yearsList.contains(yearFromValue)) {
                                            yearFrom.setError(ResourcesProvider.getErrorText());
                                            return;
                                        }

                                        if (!yearsList.contains(yearToValue)) {
                                            yearTo.setError(ResourcesProvider.getErrorText());
                                            return;
                                        }

                                        if (Integer.parseInt(yearFromValue) > Integer.parseInt(yearToValue)) {
                                            yearFrom.setError(ResourcesProvider.getErrorText());
                                            yearTo.setError(ResourcesProvider.getErrorText());
                                            return;
                                        }

                                        if (!languageMap.containsValue(languageValue) && !languageValue.isEmpty()) {
                                            languageSpinner.setError(ResourcesProvider.getErrorText());
                                            return;
                                        }

                                        if (!countryMap.containsValue(countryValue) && !countryValue.isEmpty()) {
                                            countrySpinner.setError(ResourcesProvider.getErrorText());
                                            return;
                                        }

                                        mSearchBuilder.setStartYear(yearFromValue);
                                        mSearchBuilder.setEndYear(yearToValue);
                                        mSearchBuilder.setLanguage(ResourcesProvider.getKeyFromMap(languageMap, languageValue));
                                        mSearchBuilder.setCountry(ResourcesProvider.getKeyFromMap(countryMap, countryValue));

                                        dialog.dismiss();
                                        mSearchBuilder.setOffset(0);
                                        performSearch();
                                    }
                                })
                                .show();
                        return true;
                    }
                }
                return false;
            }
        });

        mSearchView.setOnSearchListener(new FloatingSearchView.OnSearchListener() {
            @Override
            public void onSearchAction(CharSequence charSequence) {
                mSearchView.clearFocus();
                mSearchBuilder.setOffset(0);
                performSearch(charSequence.toString());
            }
        });
    }

    private void performSearch() {
        performSearch(null);
    }

    private void performSearch(String keyword) {
        if (keyword != null) {
            mSearchBuilder.setKeyword(keyword);
        }

        setContentVisibility(false);
        mLoadingLayout.showProgressBar();
        AdjaranetAPI.getInstance().getAdvancedSearchResults(
                mSearchBuilder,
                new Response.Listener<List<Movie>>() {
                    @Override
                    public void onResponse(List<Movie> response) {
                        mLoadingLayout.setVisibility(false);
                        setContentVisibility(true);
                        mItemsAdapter.update(response);
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

    private void setContentVisibility(boolean visibility) {
        if (visibility) {
            mRecyclerView.setVisibility(View.VISIBLE);
        } else {
            mRecyclerView.setVisibility(View.INVISIBLE);
        }
    }
}

package ui.helpers;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import ge.redefine.adjaranet.R;
import me.zhanghai.android.materialprogressbar.MaterialProgressBar;

public class LoadingLayout {
    private ViewGroup mParentLayout;
    private OnLoadingLayoutInteraction mListener;
    private MaterialProgressBar mProgressBar;
    private Button mRetryButton;

    public LoadingLayout(ViewGroup parent, OnLoadingLayoutInteraction listener) {
        View v = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.loading_layout, parent, true);

        mParentLayout = parent;
        mListener = listener;
        onAttach();

        mProgressBar = (MaterialProgressBar) v.findViewById(R.id.loading_progressbar);
        mRetryButton = (Button) v.findViewById(R.id.loading_retry);

        mRetryButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                mListener.onRetryClicked();
            }
        });

        setVisibility(false);
        setRetryButtonVisibility(false);
    }

    private void onAttach() {
        if (mListener == null) {
            throw new RuntimeException("Must implement OnLoadingLayoutInteraction");
        }
    }

    public void hideLayout() {
        setVisibility(false);
    }

    public void showProgressBar() {
        setRetryButtonVisibility(false);
        setProgressBarVisibility(true);
        setVisibility(true);
    }

    public void showRetryButton() {
        setProgressBarVisibility(false);
        setRetryButtonVisibility(true);
        setVisibility(true);
    }

    public void setVisibility(boolean visibility) {
        if (visibility) {
            mParentLayout.setVisibility(View.VISIBLE);
        } else {
            mParentLayout.setVisibility(View.INVISIBLE);
        }
    }

    public void setProgressBarVisibility(boolean visibility) {
        if (visibility) {
            mProgressBar.setVisibility(View.VISIBLE);
        } else {
            mProgressBar.setVisibility(View.INVISIBLE);
        }
    }

    public void setRetryButtonVisibility(boolean visibility) {
        if (visibility) {
            mRetryButton.setVisibility(View.VISIBLE);
        } else {
            mRetryButton.setVisibility(View.INVISIBLE);
        }
    }

    public interface OnLoadingLayoutInteraction {
        void onRetryClicked();
    }

}

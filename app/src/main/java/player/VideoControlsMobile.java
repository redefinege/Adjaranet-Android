package player;

import android.annotation.TargetApi;
import android.content.Context;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Build;
import android.support.annotation.IntRange;
import android.support.annotation.NonNull;
import android.support.v4.content.ContextCompat;
import android.util.AttributeSet;
import android.view.Gravity;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.LinearLayout;
import android.widget.NumberPicker;
import android.widget.SeekBar;

import com.afollestad.materialdialogs.DialogAction;
import com.afollestad.materialdialogs.GravityEnum;
import com.afollestad.materialdialogs.MaterialDialog;
import com.devbrackets.android.exomedia.ui.animation.BottomViewHideShowAnimation;
import com.devbrackets.android.exomedia.ui.animation.TopViewHideShowAnimation;
import com.devbrackets.android.exomedia.util.TimeFormatUtil;

import java.util.LinkedList;
import java.util.List;

import biz.kasual.materialnumberpicker.MaterialNumberPicker;
import ge.redefine.adjaranet.R;

@SuppressWarnings("unused")
public class VideoControlsMobile extends VideoControls {
    protected SeekBar seekBar;
    protected LinearLayout extraViewsContainer;
    protected ImageButton settingsButton;
    protected ImageButton fullscreenToggle;
    protected OnFullscreenClickListener fullscreenClickListener;

    protected boolean userInteracting = false;

    public VideoControlsMobile(Context context) {
        super(context);
    }

    public VideoControlsMobile(Context context, AttributeSet attrs) {
        super(context, attrs);
    }

    public VideoControlsMobile(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
    }

    @TargetApi(Build.VERSION_CODES.LOLLIPOP)
    public VideoControlsMobile(Context context, AttributeSet attrs, int defStyleAttr, int defStyleRes) {
        super(context, attrs, defStyleAttr, defStyleRes);
    }

    @Override
    protected int getLayoutResource() {
        return R.layout.exomedia_controls_custom;
    }

    @Override
    public void setPosition(@IntRange(from = 0) long position) {
        currentTimeTextView.setText(TimeFormatUtil.formatMs(position));
        seekBar.setProgress((int) position);
    }

    @Override
    public void setDuration(@IntRange(from = 0) long duration) {
        if (duration != seekBar.getMax()) {
            endTimeTextView.setText(TimeFormatUtil.formatMs(duration));
            seekBar.setMax((int) duration);
        }
    }

    @Override
    public void updateProgress(@IntRange(from = 0) long position, @IntRange(from = 0) long duration, @IntRange(from = 0, to = 100) int bufferPercent) {
        if (!userInteracting) {
            seekBar.setSecondaryProgress((int) (seekBar.getMax() * ((float)bufferPercent / 100)));
            seekBar.setProgress((int) position);
            currentTimeTextView.setText(TimeFormatUtil.formatMs(position));
        }
    }

    @Override
    protected void retrieveViews() {
        super.retrieveViews();
        seekBar = findViewById(R.id.exomedia_controls_video_seek);
        extraViewsContainer = findViewById(R.id.exomedia_controls_extra_container);
        settingsButton = findViewById(R.id.exomedia_controls_settings);
        fullscreenToggle = findViewById(R.id.exomedia_controls_fullscreen);
    }

    @Override
    protected void registerListeners() {
        super.registerListeners();
        seekBar.setOnSeekBarChangeListener(new SeekBarChanged());
        settingsButton.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                final LinearLayout settingsPicker = buildSettingsPicker();

                if (settingsPicker == null) {
                    settingsChangeListener.error();
                    return;
                }

                final MaterialNumberPicker languagePicker = (MaterialNumberPicker) settingsPicker.getChildAt(0);
                final MaterialNumberPicker qualityPicker = (MaterialNumberPicker) settingsPicker.getChildAt(1);

                if (languagePicker == null || qualityPicker == null) {
                    settingsChangeListener.error();
                    return;
                }

                MaterialDialog dialog = new MaterialDialog.Builder(getContext())
                        .customView(settingsPicker, false)
                        .btnStackedGravity(GravityEnum.CENTER)
                        .forceStacking(true)
                        .positiveColor(Color.WHITE)
                        .positiveText(getResources().getString(android.R.string.ok))
                        .onPositive(new MaterialDialog.SingleButtonCallback() {
                            @Override
                            public void onClick(@NonNull MaterialDialog dialog, @NonNull DialogAction which) {
                                settingsChangeListener.success(languagePicker.getValue(), qualityPicker.getValue());
                            }
                        })
                        .build();

                dialog.getWindow().setBackgroundDrawable(new ColorDrawable(ContextCompat.getColor(getContext(), android.R.color.transparent)));
                dialog.getWindow().setDimAmount(0.85f);
                dialog.show();
            }
        });
        fullscreenToggle.setOnClickListener(new OnClickListener() {
            @Override
            public void onClick(View view) {
                fullscreenClickListener.fullscreenClicked();
            }
        });
    }

    @Override
    public void addExtraView(@NonNull View view) {
        extraViewsContainer.addView(view);
    }

    @Override
    public void removeExtraView(@NonNull View view) {
        extraViewsContainer.removeView(view);
    }

    @NonNull
    @Override
    public List<View> getExtraViews() {
        int childCount = extraViewsContainer.getChildCount();
        if (childCount <= 0) {
            return super.getExtraViews();
        }

        //Retrieves the layouts children
        List<View> children = new LinkedList<>();
        for (int i = 0; i < childCount; i++) {
            children.add(extraViewsContainer.getChildAt(i));
        }

        return children;
    }

    @Override
    public void hideDelayed(long delay) {
        hideDelay = delay;

        if (delay < 0 || !canViewHide || isLoading) {
            return;
        }

        //If the user is interacting with controls we don't want to start the delayed hide yet
        if (!userInteracting) {
            visibilityHandler.postDelayed(new Runnable() {
                @Override
                public void run() {
                    animateVisibility(false);
                }
            }, delay);
        }
    }

    @Override
    protected void animateVisibility(boolean toVisible) {
        if (isVisible == toVisible) {
            return;
        }

        if (!hideEmptyTextContainer || !isTextContainerEmpty()) {
            textContainer.startAnimation(new TopViewHideShowAnimation(textContainer, toVisible, CONTROL_VISIBILITY_ANIMATION_LENGTH));
        }

        if (!isLoading) {
            controlsContainer.startAnimation(new BottomViewHideShowAnimation(controlsContainer, toVisible, CONTROL_VISIBILITY_ANIMATION_LENGTH));
        }

        isVisible = toVisible;
        onVisibilityChanged();
    }

    @Override
    protected void updateTextContainerVisibility() {
        if (!isVisible) {
            return;
        }

        boolean emptyText = isTextContainerEmpty();
        if (hideEmptyTextContainer && emptyText && textContainer.getVisibility() == VISIBLE) {
            textContainer.clearAnimation();
            textContainer.startAnimation(new TopViewHideShowAnimation(textContainer, false, CONTROL_VISIBILITY_ANIMATION_LENGTH));
        } else if ((!hideEmptyTextContainer || !emptyText) && textContainer.getVisibility() != VISIBLE) {
            textContainer.clearAnimation();
            textContainer.startAnimation(new TopViewHideShowAnimation(textContainer, true, CONTROL_VISIBILITY_ANIMATION_LENGTH));
        }
    }

    @Override
    public void showLoading(boolean initialLoad) {
        if (isLoading) {
            return;
        }

        isLoading = true;
        loadingProgressBar.setVisibility(View.VISIBLE);

        if (initialLoad) {
            controlsContainer.setVisibility(View.GONE);
        } else {
            playPauseButton.setEnabled(false);
            previousButton.setEnabled(false);
            nextButton.setEnabled(false);
        }

        show();
    }

    @Override
    public void finishLoading() {
        if (!isLoading) {
            return;
        }

        isLoading = false;
        loadingProgressBar.setVisibility(View.GONE);
        controlsContainer.setVisibility(View.VISIBLE);

        playPauseButton.setEnabled(true);
        previousButton.setEnabled(enabledViews.get(R.id.exomedia_controls_previous_btn, true));
        nextButton.setEnabled(enabledViews.get(R.id.exomedia_controls_next_btn, true));

        updatePlaybackState(videoView != null && videoView.isPlaying());
    }

    public void setFullscreenClickListener(OnFullscreenClickListener fullscreenClickListener) {
        this.fullscreenClickListener = fullscreenClickListener;
    }

    private LinearLayout buildSettingsPicker() {
        if (languageList.size() == 0 || qualityList.size() == 0) {
            return null;
        }

        final int horizontalMargin = getResources().getDimensionPixelSize(R.dimen.exomedia_settings_horizontal_margin);
        LinearLayout.LayoutParams params =
                new LinearLayout.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(horizontalMargin, 0, horizontalMargin, 0);

        MaterialNumberPicker languagePicker =
                new MaterialNumberPicker.Builder(getContext())
                        .minValue(1)
                        .maxValue(languageList.size())
                        .defaultValue(currentLanguageIndex + 1)
                        .backgroundColor(Color.TRANSPARENT)
                        .separatorColor(Color.TRANSPARENT)
                        .textColor(Color.WHITE)
                        .textSize(18)
                        .enableFocusability(false)
                        .wrapSelectorWheel(true)
                        .formatter(new NumberPicker.Formatter() {
                            @Override
                            public String format(int i) {
                                String key = languageList.get(i - 1);
                                String value = languageMap.containsKey(key) ? languageMap.get(key) : key;
                                return value;
                            }
                        })
                        .build();

        MaterialNumberPicker qualityPicker =
                new MaterialNumberPicker.Builder(getContext())
                        .minValue(1)
                        .maxValue(qualityList.size())
                        .defaultValue(currentQualityIndex + 1)
                        .backgroundColor(Color.TRANSPARENT)
                        .separatorColor(Color.TRANSPARENT)
                        .textColor(Color.WHITE)
                        .textSize(18)
                        .enableFocusability(false)
                        .wrapSelectorWheel(true)
                        .formatter(new NumberPicker.Formatter() {
                            @Override
                            public String format(int i) {
                                String key = qualityList.get(i - 1);
                                String value = qualityMap.containsKey(key) ? qualityMap.get(key) : key;
                                return value;
                            }
                        })
                        .build();

        languagePicker.setLayoutParams(params);
        qualityPicker.setLayoutParams(params);

        LinearLayout linearLayout = new LinearLayout(getContext());
        linearLayout.setOrientation(LinearLayout.HORIZONTAL);
        linearLayout.setGravity(Gravity.CENTER_HORIZONTAL);
        linearLayout.addView(languagePicker);
        linearLayout.addView(qualityPicker);

        return linearLayout;
    }
    /**
     * Listens to the seek bar change events and correctly handles the changes
     */
    protected class SeekBarChanged implements SeekBar.OnSeekBarChangeListener {
        private long seekToTime;

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (!fromUser) {
                return;
            }

            seekToTime = progress;
            if (currentTimeTextView != null) {
                currentTimeTextView.setText(TimeFormatUtil.formatMs(seekToTime));
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
            userInteracting = true;
            if (seekListener == null || !seekListener.onSeekStarted()) {
                internalListener.onSeekStarted();
            }
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
            userInteracting = false;
            if (seekListener == null || !seekListener.onSeekEnded(seekToTime)) {
                internalListener.onSeekEnded(seekToTime);
            }
        }
    }
}
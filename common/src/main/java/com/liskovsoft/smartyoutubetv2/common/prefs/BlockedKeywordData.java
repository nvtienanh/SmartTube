package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.sharedutils.helpers.Helpers;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class BlockedKeywordData implements ProfileChangeListener {
    private static final String BLOCKED_KEYWORD_DATA = "blocked_keyword_data";
    @SuppressLint("StaticFieldLeak")
    private static BlockedKeywordData sInstance;
    private final AppPrefs mPrefs;
    private String mRawKeywords = "";
    private final List<String> mKeywords = new ArrayList<>();
    private final Runnable mPersistStateInt = this::persistStateInt;
    private final List<BlockedKeywordListener> mListeners = new ArrayList<>();

    public interface BlockedKeywordListener {
        void onChanged();
    }

    public BlockedKeywordData() {
        mPrefs = null;
    }

    private BlockedKeywordData(Context context) {
        mPrefs = AppPrefs.instance(context);
        if (mPrefs != null) {
            mPrefs.addListener(this);
            restoreState();
        }
    }

    public static BlockedKeywordData instance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new BlockedKeywordData(context.getApplicationContext());
        }

        return sInstance;
    }

    public String getRawKeywords() {
        return mRawKeywords != null ? mRawKeywords : "";
    }

    public void setRawKeywords(String rawKeywords) {
        mRawKeywords = rawKeywords != null ? rawKeywords : "";
        updateKeywordsList();
        persistState();
        notifyListeners();
    }

    public List<String> getKeywords() {
        return Collections.unmodifiableList(mKeywords);
    }

    public boolean isEmpty() {
        return mKeywords.isEmpty();
    }

    public boolean isBlocked(Video video) {
        if (video == null || isEmpty() || video.isChapter) {
            return false;
        }

        String title = video.getTitleFull();
        if (containsKeyword(title)) {
            return true;
        }

        if (video.secondTitle != null && containsKeyword(video.secondTitle.toString())) {
            return true;
        }

        String desc = video.description;
        if (containsKeyword(desc)) {
            return true;
        }

        return false;
    }

    private boolean containsKeyword(String text) {
        if (text == null || text.isEmpty()) {
            return false;
        }

        String textLower = text.toLowerCase();

        for (String keyword : mKeywords) {
            if (!keyword.isEmpty() && textLower.contains(keyword)) {
                return true;
            }
        }

        return false;
    }

    private void updateKeywordsList() {
        mKeywords.clear();

        if (mRawKeywords == null || mRawKeywords.trim().isEmpty()) {
            return;
        }

        String[] split = mRawKeywords.split("[,;\\r\\n]+");

        for (String item : split) {
            String trimmed = item.trim().toLowerCase();
            if (!trimmed.isEmpty() && !mKeywords.contains(trimmed)) {
                mKeywords.add(trimmed);
            }
        }
    }

    private synchronized void restoreState() {
        if (mPrefs != null) {
            mRawKeywords = mPrefs.getProfileData(BLOCKED_KEYWORD_DATA);
            updateKeywordsList();
        }
    }

    public void persistNow() {
        Utils.post(mPersistStateInt);
    }

    private void persistState() {
        Utils.postDelayed(mPersistStateInt, 10_000);
    }

    private void persistStateInt() {
        if (mPrefs != null) {
            mPrefs.setProfileData(BLOCKED_KEYWORD_DATA, mRawKeywords);
        }
    }

    public void addListener(BlockedKeywordListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(BlockedKeywordListener listener) {
        mListeners.remove(listener);
    }

    private void notifyListeners() {
        for (BlockedKeywordListener listener : mListeners) {
            listener.onChanged();
        }
    }

    @Override
    public void onProfileChanged() {
        restoreState();
    }
}

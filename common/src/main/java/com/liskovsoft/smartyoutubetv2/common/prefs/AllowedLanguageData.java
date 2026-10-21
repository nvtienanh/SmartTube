package com.liskovsoft.smartyoutubetv2.common.prefs;

import android.annotation.SuppressLint;
import android.content.Context;

import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.prefs.AppPrefs.ProfileChangeListener;
import com.liskovsoft.smartyoutubetv2.common.utils.Utils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class AllowedLanguageData implements ProfileChangeListener {
    private static final String ALLOWED_LANGUAGE_DATA = "allowed_language_data";

    public static final String SCRIPT_LATIN = "LATIN"; // English, Spanish, French, German, Vietnamese, etc.
    public static final String SCRIPT_CYRILLIC = "CYRILLIC"; // Russian, Ukrainian, Bulgarian, etc.
    public static final String SCRIPT_CJK = "CJK"; // Chinese, Japanese, Korean
    public static final String SCRIPT_ARABIC = "ARABIC"; // Arabic, Persian, Urdu
    public static final String SCRIPT_DEVANAGARI = "DEVANAGARI"; // Hindi, Marathi, Sanskrit
    public static final String SCRIPT_THAI = "THAI"; // Thai
    public static final String SCRIPT_HEBREW = "HEBREW"; // Hebrew
    public static final String SCRIPT_GREEK = "GREEK"; // Greek

    @SuppressLint("StaticFieldLeak")
    private static AllowedLanguageData sInstance;
    private final AppPrefs mPrefs;
    private final Set<String> mAllowedScripts = new HashSet<>();
    private final Runnable mPersistStateInt = this::persistStateInt;
    private final List<AllowedLanguageListener> mListeners = new ArrayList<>();

    public interface AllowedLanguageListener {
        void onChanged();
    }

    public AllowedLanguageData() {
        mPrefs = null;
    }

    private AllowedLanguageData(Context context) {
        mPrefs = AppPrefs.instance(context);
        if (mPrefs != null) {
            mPrefs.addListener(this);
            restoreState();
        }
    }

    public static AllowedLanguageData instance(Context context) {
        if (sInstance == null && context != null) {
            sInstance = new AllowedLanguageData(context.getApplicationContext());
        }

        return sInstance;
    }

    public Set<String> getAllowedScripts() {
        return Collections.unmodifiableSet(mAllowedScripts);
    }

    public boolean isScriptAllowed(String script) {
        return mAllowedScripts.contains(script);
    }

    public void setScriptAllowed(String script, boolean allowed) {
        if (allowed) {
            mAllowedScripts.add(script);
        } else {
            mAllowedScripts.remove(script);
        }
        persistState();
        notifyListeners();
    }

    public boolean isEmpty() {
        return mAllowedScripts.isEmpty();
    }

    public void clear() {
        mAllowedScripts.clear();
        persistState();
        notifyListeners();
    }

    public boolean isLanguageBlocked(Video video) {
        if (video == null || isEmpty() || video.isChapter) {
            return false;
        }

        String title = video.getTitleFull();
        if (containsDisallowedScript(title)) {
            return true;
        }

        if (video.secondTitle != null && containsDisallowedScript(video.secondTitle.toString())) {
            return true;
        }

        String desc = video.description;
        if (containsDisallowedScript(desc)) {
            return true;
        }

        return false;
    }

    private boolean containsDisallowedScript(String text) {
        if (text == null || text.isEmpty() || isEmpty()) {
            return false;
        }

        int length = text.length();

        for (int i = 0; i < length; i++) {
            char c = text.charAt(i);

            if (!Character.isLetter(c)) {
                continue;
            }

            String script = getScript(c);

            if (script != null && !mAllowedScripts.contains(script)) {
                return true;
            }
        }

        return false;
    }

    public static String getScript(char c) {
        Character.UnicodeBlock block = Character.UnicodeBlock.of(c);

        if (block == null) {
            return null;
        }

        if (block == Character.UnicodeBlock.BASIC_LATIN ||
                block == Character.UnicodeBlock.LATIN_1_SUPPLEMENT ||
                block == Character.UnicodeBlock.LATIN_EXTENDED_A ||
                block == Character.UnicodeBlock.LATIN_EXTENDED_B ||
                block == Character.UnicodeBlock.LATIN_EXTENDED_ADDITIONAL) {
            return SCRIPT_LATIN;
        }

        if (block == Character.UnicodeBlock.CYRILLIC ||
                block == Character.UnicodeBlock.CYRILLIC_SUPPLEMENTARY) {
            return SCRIPT_CYRILLIC;
        }

        if (block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS ||
                block == Character.UnicodeBlock.CJK_UNIFIED_IDEOGRAPHS_EXTENSION_A ||
                block == Character.UnicodeBlock.HIRAGANA ||
                block == Character.UnicodeBlock.KATAKANA ||
                block == Character.UnicodeBlock.HANGUL_SYLLABLES ||
                block == Character.UnicodeBlock.HANGUL_JAMO ||
                block == Character.UnicodeBlock.HANGUL_COMPATIBILITY_JAMO) {
            return SCRIPT_CJK;
        }

        if (block == Character.UnicodeBlock.ARABIC ||
                block == Character.UnicodeBlock.ARABIC_SUPPLEMENT ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_A ||
                block == Character.UnicodeBlock.ARABIC_PRESENTATION_FORMS_B) {
            return SCRIPT_ARABIC;
        }

        if (block == Character.UnicodeBlock.DEVANAGARI ||
                block == Character.UnicodeBlock.DEVANAGARI_EXTENDED) {
            return SCRIPT_DEVANAGARI;
        }

        if (block == Character.UnicodeBlock.THAI) {
            return SCRIPT_THAI;
        }

        if (block == Character.UnicodeBlock.HEBREW) {
            return SCRIPT_HEBREW;
        }

        if (block == Character.UnicodeBlock.GREEK ||
                block == Character.UnicodeBlock.GREEK_EXTENDED) {
            return SCRIPT_GREEK;
        }

        return null;
    }

    private synchronized void restoreState() {
        if (mPrefs != null) {
            String data = mPrefs.getProfileData(ALLOWED_LANGUAGE_DATA);
            mAllowedScripts.clear();

            if (data != null && !data.isEmpty()) {
                String[] split = data.split(",");
                for (String item : split) {
                    String trimmed = item.trim();
                    if (!trimmed.isEmpty()) {
                        mAllowedScripts.add(trimmed);
                    }
                }
            }
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
            StringBuilder builder = new StringBuilder();
            for (String script : mAllowedScripts) {
                if (builder.length() > 0) {
                    builder.append(",");
                }
                builder.append(script);
            }
            mPrefs.setProfileData(ALLOWED_LANGUAGE_DATA, builder.toString());
        }
    }

    public void addListener(AllowedLanguageListener listener) {
        if (!mListeners.contains(listener)) {
            mListeners.add(listener);
        }
    }

    public void removeListener(AllowedLanguageListener listener) {
        mListeners.remove(listener);
    }

    private void notifyListeners() {
        for (AllowedLanguageListener listener : mListeners) {
            listener.onChanged();
        }
    }

    @Override
    public void onProfileChanged() {
        restoreState();
    }
}

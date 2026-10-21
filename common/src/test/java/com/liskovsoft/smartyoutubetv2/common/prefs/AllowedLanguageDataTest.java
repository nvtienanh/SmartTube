package com.liskovsoft.smartyoutubetv2.common.prefs;

import com.liskovsoft.sharedutils.prefs.GlobalPreferences;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.Video;
import com.liskovsoft.smartyoutubetv2.common.app.models.data.VideoGroup;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.RuntimeEnvironment;
import org.robolectric.annotation.Config;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

@RunWith(RobolectricTestRunner.class)
@Config(sdk = 28)
public class AllowedLanguageDataTest {
    private AllowedLanguageData mAllowedLanguageData;

    @Before
    public void setUp() {
        GlobalPreferences.instance(RuntimeEnvironment.application);
        mAllowedLanguageData = AllowedLanguageData.instance(RuntimeEnvironment.application);
        mAllowedLanguageData.clear();
    }

    @Test
    public void testEmptyAllowedLanguagesDoesNotBlock() {
        Video video = new Video();
        video.videoId = "v1";
        video.title = "Релакс Музыка";

        assertFalse(mAllowedLanguageData.isLanguageBlocked(video));
    }

    @Test
    public void testLatinOnlyAllowsLatinAndBlocksCyrillicAndCJK() {
        mAllowedLanguageData.setScriptAllowed(AllowedLanguageData.SCRIPT_LATIN, true);

        Video latinVideo = new Video();
        latinVideo.videoId = "v1";
        latinVideo.title = "Relaxing Music 2026";

        Video cyrillicVideo = new Video();
        cyrillicVideo.videoId = "v2";
        cyrillicVideo.title = "Релакс Музыка";

        Video cjkVideo = new Video();
        cjkVideo.videoId = "v3";
        cjkVideo.title = "日本語の動画";

        assertFalse(mAllowedLanguageData.isLanguageBlocked(latinVideo));
        assertTrue(mAllowedLanguageData.isLanguageBlocked(cyrillicVideo));
        assertTrue(mAllowedLanguageData.isLanguageBlocked(cjkVideo));
    }

    @Test
    public void testMultipleAllowedScripts() {
        mAllowedLanguageData.setScriptAllowed(AllowedLanguageData.SCRIPT_LATIN, true);
        mAllowedLanguageData.setScriptAllowed(AllowedLanguageData.SCRIPT_CYRILLIC, true);

        Video latinVideo = new Video();
        latinVideo.videoId = "v1";
        latinVideo.title = "Relaxing Music";

        Video cyrillicVideo = new Video();
        cyrillicVideo.videoId = "v2";
        cyrillicVideo.title = "Релакс Музыка";

        Video cjkVideo = new Video();
        cjkVideo.videoId = "v3";
        cjkVideo.title = "日本語の動画";

        assertFalse(mAllowedLanguageData.isLanguageBlocked(latinVideo));
        assertFalse(mAllowedLanguageData.isLanguageBlocked(cyrillicVideo));
        assertTrue(mAllowedLanguageData.isLanguageBlocked(cjkVideo));
    }

    @Test
    public void testVideoGroupLanguageFiltering() {
        mAllowedLanguageData.setScriptAllowed(AllowedLanguageData.SCRIPT_LATIN, true);

        VideoGroup group = new VideoGroup();

        Video latinVideo = new Video();
        latinVideo.videoId = "v1";
        latinVideo.title = "English Video";

        Video nonLatinVideo = new Video();
        nonLatinVideo.videoId = "v2";
        nonLatinVideo.title = "日本語の動画";

        group.add(latinVideo);
        group.add(nonLatinVideo);

        assertEquals(1, group.getSize());
        assertEquals("v1", group.get(0).videoId);
    }
}

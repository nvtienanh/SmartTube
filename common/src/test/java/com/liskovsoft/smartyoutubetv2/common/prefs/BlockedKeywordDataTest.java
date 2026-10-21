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
public class BlockedKeywordDataTest {
    private BlockedKeywordData mBlockedKeywordData;

    @Before
    public void setUp() {
        GlobalPreferences.instance(RuntimeEnvironment.application);
        mBlockedKeywordData = BlockedKeywordData.instance(RuntimeEnvironment.application);
        mBlockedKeywordData.setRawKeywords("");
    }

    @Test
    public void testParsingKeywords() {
        mBlockedKeywordData.setRawKeywords("ASMR, troll\nAI content; clickbait");
        assertEquals(4, mBlockedKeywordData.getKeywords().size());
        assertTrue(mBlockedKeywordData.getKeywords().contains("asmr"));
        assertTrue(mBlockedKeywordData.getKeywords().contains("troll"));
        assertTrue(mBlockedKeywordData.getKeywords().contains("ai content"));
        assertTrue(mBlockedKeywordData.getKeywords().contains("clickbait"));
    }

    @Test
    public void testIsBlockedByTitle() {
        mBlockedKeywordData.setRawKeywords("ASMR, troll");

        Video video1 = new Video();
        video1.videoId = "v1";
        video1.title = "Relaxing ASMR Sleep Sounds";

        Video video2 = new Video();
        video2.videoId = "v2";
        video2.title = "Regular Tech Review";

        assertTrue(mBlockedKeywordData.isBlocked(video1));
        assertFalse(mBlockedKeywordData.isBlocked(video2));
    }

    @Test
    public void testIsBlockedByDescription() {
        mBlockedKeywordData.setRawKeywords("AI content");

        Video video = new Video();
        video.videoId = "v1";
        video.title = "Cool Video";
        video.description = "This video contains AI content generated automatically.";

        assertTrue(mBlockedKeywordData.isBlocked(video));
    }

    @Test
    public void testVideoGroupFiltering() {
        mBlockedKeywordData.setRawKeywords("troll");

        VideoGroup group = new VideoGroup();

        Video video1 = new Video();
        video1.videoId = "v1";
        video1.title = "Nice video";

        Video video2 = new Video();
        video2.videoId = "v2";
        video2.title = "Troll face meme compilation";

        group.add(video1);
        group.add(video2);

        assertEquals(1, group.getSize());
        assertEquals("v1", group.get(0).videoId);
    }
}

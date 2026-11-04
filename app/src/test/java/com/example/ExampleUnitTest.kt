package com.example

import com.example.data.model.MediaItemData
import com.example.data.model.PlaybackMode
import com.example.data.model.PlayerRepeatMode
import com.example.player.PlaybackSnapshot
import com.example.player.PlayerManager
import com.example.ui.AroyViewModel
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNotEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class ExampleUnitTest {
    @Test
    fun addition_isCorrect() {
        assertEquals(4, 2 + 2)
    }

    @Test
    fun securityUriValidator_blocksPathTraversalAndPrivateSandboxAndAllowsValidMedia() {
        assertTrue(PlayerManager.isSafeMediaUri("content://media/external/audio/media/101"))
        assertTrue(PlayerManager.isSafeMediaUri("https://www.soundhelix.com/examples/mp3/SoundHelix-Song-1.mp3"))
        assertFalse(PlayerManager.isSafeMediaUri("file:///data/data/com.example/shared_prefs/aroy_settings_pref.xml"))
        assertFalse(PlayerManager.isSafeMediaUri("content://media/../../etc/passwd"))
        assertFalse(PlayerManager.isSafeMediaUri("javascript:alert(1)"))
    }

    @Test
    fun defaultPlaybackSnapshot_isContinuousOrderAndRepeatAll() {
        val snapshot = PlaybackSnapshot()
        assertEquals(PlaybackMode.ORDER, snapshot.playbackMode)
        assertEquals(PlayerRepeatMode.ALL, snapshot.repeatMode)
    }

    @Test
    fun pinHashing_isSaltedSha256() {
        val hash1 = AroyViewModel.hashPin("1234", "salt_a")
        val hash2 = AroyViewModel.hashPin("1234", "salt_b")
        assertEquals(64, hash1.length)
        assertNotEquals(hash1, hash2)
    }
}

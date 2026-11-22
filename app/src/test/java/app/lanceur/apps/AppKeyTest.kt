package app.lanceur.apps

import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertNull
import org.junit.Assert.assertTrue
import org.junit.Test

class AppKeyTest {
    @Test
    fun encode_then_decode_gives_the_same_key() {
        val key = AppKey("com.exemple", "com.exemple.Main", 10)
        assertEquals("com.exemple/com.exemple.Main#10", key.encode())
        assertEquals(key, AppKey.decode(key.encode()))
    }

    @Test
    fun pinned_key_survives_encoding_even_with_slash_and_hash_in_id() {
        val key = AppKey.pinned("org.mozilla.firefox", "https://a.fr/x#y", 0)
        val decoded = AppKey.decode(key.encode())!!
        assertEquals(key, decoded)
        assertTrue(decoded.isPinned)
        assertEquals("https://a.fr/x#y", decoded.shortcutId)
        assertFalse(AppKey("com.a", "com.a.Main", 0).isPinned)
    }

    @Test
    fun decode_rejects_malformed_values() {
        listOf("", "com.a", "com.a/Main", "com.a/Main#", "/Main#0", "com.a/#0", "com.a/Main#x").forEach {
            assertNull(it, AppKey.decode(it))
        }
    }
}

package app.lanceur.focus

import java.time.LocalTime
import java.time.ZoneId
import java.time.ZonedDateTime
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Test

class FocusNotificationsTest {
    private val now = ZonedDateTime.of(2026, 10, 5, 10, 0, 0, 0, ZoneId.of("Europe/Paris")) // lundi
    private val launchable = setOf("insta" to 0L, "mail" to 0L, "phone" to 0L)
    private val hide = FocusMode(manual = true, filter = FocusFilter.HIDE, apps = setOf("insta" to 0L))

    private fun decide(focus: FocusMode, pkg: String, ongoing: Boolean = false, category: String? = null) =
        FocusNotifications.snoozeFor(focus, pkg, 0L, ongoing, category, launchable, now)

    @Test
    fun hidden_apps_are_snoozed_a_few_minutes_at_a_time_when_started_by_hand() {
        assertEquals(FocusNotifications.SNOOZE_MS, decide(hide, "insta"))
        assertNull(decide(hide, "mail"))
    }

    @Test
    fun during_a_schedule_the_wait_never_outlasts_a_stop() {
        val monday = setOf(java.time.DayOfWeek.MONDAY)
        val scheduled = hide.copy(manual = false, schedules = listOf(FocusSchedule(monday, LocalTime.of(9, 0), LocalTime.of(12, 30))))
        // Plage jusqu'à 12:30 : 5 minutes seulement, pour qu'un arrêt à 10:00 ne cache rien jusqu'à 12:30
        assertEquals(FocusNotifications.SNOOZE_MS, decide(scheduled, "insta"))
        val endingSoon = hide.copy(manual = false, schedules = listOf(FocusSchedule(monday, LocalTime.of(9, 0), LocalTime.of(10, 2))))
        assertEquals(2 * 60_000L, decide(endingSoon, "insta"))
    }

    @Test
    fun show_only_snoozes_every_other_launchable_app_but_never_system_ones() {
        val show = FocusMode(manual = true, filter = FocusFilter.SHOW_ONLY, apps = setOf("phone" to 0L))
        assertEquals(FocusNotifications.SNOOZE_MS, decide(show, "mail"))
        assertNull(decide(show, "phone"))
        assertNull(decide(show, "com.android.systemui"))
        assertNull(decide(show, "app.lanceur"))
    }

    @Test
    fun calls_alarms_ongoing_and_the_off_switch_are_respected() {
        assertNull(decide(hide, "insta", ongoing = true))
        assertNull(decide(hide, "insta", category = "call"))
        assertNull(decide(hide, "insta", category = "alarm"))
        assertNull(decide(hide.copy(blockNotifications = false), "insta"))
        assertNull(decide(hide.copy(manual = false), "insta"))
    }

    @Test
    fun the_notification_choice_is_saved() {
        val focus = hide.copy(blockNotifications = false)
        assertEquals(focus, FocusMode.decode(focus.encode()))
        assertEquals(true, FocusMode.decode(hide.copy(blockNotifications = true).encode()).blockNotifications)
    }
}

package app.lanceur.apps

import android.app.Activity
import android.content.pm.LauncherApps
import android.os.Bundle
import android.util.Log
import android.widget.Toast
import app.lanceur.i18n.tr

/**
 * « Ajouter à l'écran d'accueil » d'un navigateur (PWA) : le navigateur a déjà demandé confirmation,
 * on accepte et la PWA rejoint la liste des applis.
 */
class PinShortcutActivity : Activity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        val request = runCatching { getSystemService(LauncherApps::class.java).getPinItemRequest(intent) }.getOrNull()
        val accepted = request?.requestType == LauncherApps.PinItemRequest.REQUEST_TYPE_SHORTCUT && request.isValid &&
            runCatching { request.accept() }.onFailure { Log.w("Lanceur", "Épinglage refusé", it) }.getOrDefault(false)
        if (accepted) {
            val label = request?.shortcutInfo?.shortLabel ?: ""
            Toast.makeText(this, tr("« $label » ajoutée aux applis", "“$label” added to apps"), Toast.LENGTH_SHORT).show()
        }
        finish()
    }
}

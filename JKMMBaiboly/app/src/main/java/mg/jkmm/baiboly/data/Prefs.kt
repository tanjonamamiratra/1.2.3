package mg.jkmm.baiboly.data

import android.content.Context

/** Réglages de l'utilisateur : texte, thème, historique, versets surlignés. */
class Prefs(context: Context) {
    private val sp = context.getSharedPreferences("jkmm_baiboly", Context.MODE_PRIVATE)

    var fontSize: Float
        get() = sp.getFloat("font", 19f)
        set(v) = sp.edit().putFloat("font", v).apply()

    /** 0 = automatique (système), 1 = clair, 2 = sombre */
    var theme: Int
        get() = sp.getInt("theme", 0)
        set(v) = sp.edit().putInt("theme", v).apply()

    /** Derniers passages lus, le plus récent en premier. Format : "livre:chapitre:de:à". */
    var history: List<HistoryItem>
        get() = (sp.getString("hist", "") ?: "").split(",").mapNotNull { item ->
            val p = item.split(":")
            val b = p.getOrNull(0)?.toIntOrNull()
            val c = p.getOrNull(1)?.toIntOrNull()
            if (b == null || c == null) null
            else HistoryItem(b, c, p.getOrNull(2)?.toIntOrNull() ?: 0, p.getOrNull(3)?.toIntOrNull() ?: 0)
        }
        set(v) = sp.edit().putString(
            "hist", v.joinToString(",") { "${it.book}:${it.chapter}:${it.from}:${it.to}" },
        ).apply()

    /** Versets surlignés, au format "livre|chapitre|verset". */
    var highlights: Set<String>
        get() = (sp.getStringSet("hl", emptySet()) ?: emptySet()).toSet()
        set(v) = sp.edit().putStringSet("hl", v).apply()

    /** Personnalisation : couleur (0-4), fond (0-3), police sans-serif, grand interligne, texte justifié. */
    var accent: Int
        get() = sp.getInt("accent", 0)
        set(v) = sp.edit().putInt("accent", v).apply()

    var bgStyle: Int
        get() = sp.getInt("bgStyle", 0)
        set(v) = sp.edit().putInt("bgStyle", v).apply()

    var sansFont: Boolean
        get() = sp.getBoolean("sans", false)
        set(v) = sp.edit().putBoolean("sans", v).apply()

    var largeSpacing: Boolean
        get() = sp.getBoolean("largeSpacing", false)
        set(v) = sp.edit().putBoolean("largeSpacing", v).apply()

    var justify: Boolean
        get() = sp.getBoolean("justify", false)
        set(v) = sp.edit().putBoolean("justify", v).apply()

    var lastUpdateCheck: Long
        get() = sp.getLong("lastCheck", 0L)
        set(v) = sp.edit().putLong("lastCheck", v).apply()
}

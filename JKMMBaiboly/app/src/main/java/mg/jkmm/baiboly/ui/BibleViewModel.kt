package mg.jkmm.baiboly.ui

import android.app.Application
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableFloatStateOf
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.setValue
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import androidx.core.content.pm.PackageInfoCompat
import mg.jkmm.baiboly.data.AppUpdate
import mg.jkmm.baiboly.data.AppUpdater
import mg.jkmm.baiboly.data.BibleRepository
import mg.jkmm.baiboly.data.Book
import mg.jkmm.baiboly.data.ContentUpdater
import mg.jkmm.baiboly.data.HistoryItem
import mg.jkmm.baiboly.data.Prefs
import mg.jkmm.baiboly.data.UpdateResult
import mg.jkmm.baiboly.data.VerseOfDay

/** Versets proposés tour à tour comme « Andininy anio » : (livre_id, chapitre, verset). */
private val DAILY = listOf(
    Triple(43, 3, 16), Triple(19, 23, 1), Triple(50, 4, 13), Triple(45, 8, 28),
    Triple(24, 29, 11), Triple(23, 41, 10), Triple(40, 11, 28), Triple(20, 3, 5),
    Triple(58, 11, 1), Triple(62, 4, 8), Triple(19, 46, 2), Triple(43, 14, 6),
)

class BibleViewModel(app: Application) : AndroidViewModel(app) {
    val repo = BibleRepository(app)
    private val prefs = Prefs(app)
    private val packageInfo = app.packageManager.getPackageInfo(app.packageName, 0)
    val versionName: String = packageInfo.versionName ?: ""
    val versionCode: Long = PackageInfoCompat.getLongVersionCode(packageInfo)

    var books by mutableStateOf<List<Book>>(emptyList()); private set
    var verseOfDay by mutableStateOf<VerseOfDay?>(null); private set
    var fontSize by mutableFloatStateOf(prefs.fontSize); private set
    var theme by mutableIntStateOf(prefs.theme); private set
    var history by mutableStateOf(prefs.history); private set
    var accent by mutableIntStateOf(prefs.accent); private set
    var bgStyle by mutableIntStateOf(prefs.bgStyle); private set
    var sansFont by mutableStateOf(prefs.sansFont); private set
    var largeSpacing by mutableStateOf(prefs.largeSpacing); private set
    var justify by mutableStateOf(prefs.justify); private set
    var highlights by mutableStateOf(prefs.highlights); private set
    var updateStatus by mutableStateOf(""); private set
    var contentVersion by mutableIntStateOf(1); private set

    // Mise à jour de l'application elle-même (hors Play Store)
    var appUpdate by mutableStateOf<AppUpdate?>(null); private set
    var updateDialogOpen by mutableStateOf(false); private set
    var appUpdateStatus by mutableStateOf(""); private set
    var downloadProgress by mutableFloatStateOf(-1f); private set  // -1 = pas de téléchargement

    init {
        viewModelScope.launch(Dispatchers.IO) {
            reload()
            val now = System.currentTimeMillis()
            if (now - prefs.lastUpdateCheck > 24L * 3600 * 1000) {
                val result = ContentUpdater.check(getApplication(), repo)
                if (result !is UpdateResult.Failed) prefs.lastUpdateCheck = now
                if (result is UpdateResult.Updated) reload()
            }
            AppUpdater.check(versionCode)?.let { appUpdate = it; updateDialogOpen = true }
        }
    }

    private fun reload() {
        books = repo.books()
        contentVersion = repo.contentVersion()
        verseOfDay = computeVerseOfDay()
    }

    private fun computeVerseOfDay(): VerseOfDay? {
        val day = (System.currentTimeMillis() / 86_400_000L).toInt()
        for (i in DAILY.indices) {
            val (b, c, v) = DAILY[(day + i) % DAILY.size]
            val text = repo.verse(b, c, v) ?: continue
            val name = books.firstOrNull { it.id == b }?.name ?: continue
            return VerseOfDay(b, c, v, "$name $c:$v", text)
        }
        return null
    }

    fun updateFontSize(v: Float) { fontSize = v; prefs.fontSize = v }
    fun updateTheme(v: Int) { theme = v; prefs.theme = v }
    fun updateAccent(v: Int) { accent = v; prefs.accent = v }
    fun updateBgStyle(v: Int) { bgStyle = v; prefs.bgStyle = v }
    fun updateSansFont(v: Boolean) { sansFont = v; prefs.sansFont = v }
    fun updateLargeSpacing(v: Boolean) { largeSpacing = v; prefs.largeSpacing = v }
    fun updateJustify(v: Boolean) { justify = v; prefs.justify = v }

    /** Ajoute un passage en tête de l'historique (sans doublon, 8 maximum). from = 0 : chapitre entier. */
    fun addHistory(book: Int, chapter: Int, from: Int = 0, to: Int = 0) {
        val item = HistoryItem(book, chapter, from, if (from > 0) maxOf(from, to) else 0)
        val list = (listOf(item) + history.filter { it != item }).take(8)
        history = list
        prefs.history = list
    }

    fun clearHistory() { history = emptyList(); prefs.history = emptyList() }

    /** Surligne les versets choisis ; si tous le sont déjà, retire le surlignage. */
    fun toggleHighlights(book: Int, chapter: Int, verses: Set<Int>) {
        val keys = verses.map { "$book|$chapter|$it" }.toSet()
        highlights = if (highlights.containsAll(keys)) highlights - keys else highlights + keys
        prefs.highlights = highlights
    }

    /** manual = true : la personne a touché le bouton dans Safidy, on lui répond même s'il n'y a rien. */
    fun checkAppUpdate(manual: Boolean) {
        if (manual) appUpdateStatus = "Mamarina…"
        viewModelScope.launch {
            val u = AppUpdater.check(versionCode)
            appUpdate = u
            if (u != null) {
                appUpdateStatus = ""
                updateDialogOpen = true
            } else if (manual) {
                appUpdateStatus = "Efa vao indrindra ny application (version $versionName)."
            }
        }
    }

    fun dismissUpdateDialog() { updateDialogOpen = false; appUpdateStatus = "" }

    fun downloadAndInstall() {
        val u = appUpdate ?: return
        val context = getApplication<Application>()
        if (!AppUpdater.canInstall(context)) {
            appUpdateStatus = "Avelao hametraka avy amin’ity application ity ao amin’ny Android, avy eo miverena ary kitiho indray ny « Hanavao »."
            AppUpdater.openInstallSettings(context)
            return
        }
        appUpdateStatus = ""
        downloadProgress = 0f
        viewModelScope.launch {
            AppUpdater.download(context, u) { downloadProgress = it }
                .onSuccess { AppUpdater.install(context, it) }
                .onFailure { appUpdateStatus = "Tsy nahomby: ${it.message ?: "hadisoana tamin’ny tambajotra"}" }
            downloadProgress = -1f
        }
    }

    fun checkUpdate() {
        updateStatus = "Mamarina…"
        viewModelScope.launch {
            val r = ContentUpdater.check(getApplication(), repo)
            updateStatus = when (r) {
                is UpdateResult.UpToDate -> "Efa vao indrindra ny Baiboly."
                is UpdateResult.Updated -> "Voavao! (version ${r.version})"
                is UpdateResult.Failed -> "Tsy nahomby: ${r.reason}"
            }
            if (r is UpdateResult.Updated) launch(Dispatchers.IO) { reload() }
        }
    }
}

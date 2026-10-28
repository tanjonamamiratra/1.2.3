package mg.jkmm.baiboly

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.ui.Modifier
import androidx.lifecycle.viewmodel.compose.viewModel
import mg.jkmm.baiboly.ui.AppRoot
import mg.jkmm.baiboly.ui.BibleViewModel
import mg.jkmm.baiboly.ui.JKMMTheme
import mg.jkmm.baiboly.ui.appBackground
import mg.jkmm.baiboly.ui.isDark

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val vm: BibleViewModel = viewModel()
            JKMMTheme(dark = isDark(vm.theme), accent = vm.accent, bgStyle = vm.bgStyle) {
                Box(Modifier.fillMaxSize().appBackground()) {
                    AppRoot(vm)
                }
            }
        }
    }
}

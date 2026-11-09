package jpark.bro.subcal

import android.graphics.Color
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.DisposableEffect
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.isDarkTheme
import jpark.bro.subcal.navigation.SubCalApp
import org.koin.androidx.viewmodel.ext.android.viewModel
import org.koin.compose.koinInject
import org.koin.core.parameter.parametersOf

class MainActivity : ComponentActivity() {
    private val viewModel: MainViewModel by viewModel()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContent {
            val themeMode by viewModel.themeMode.collectAsStateWithLifecycle()
            val startKey by viewModel.startKey.collectAsStateWithLifecycle()
            val dark = isDarkTheme(themeMode)

            // 앱에서 고른 화면 모드에 맞춰 상태 표시줄 · 내비게이션 바 아이콘 색을 바꾼다
            DisposableEffect(dark) {
                enableEdgeToEdge(
                    statusBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                    navigationBarStyle = SystemBarStyle.auto(Color.TRANSPARENT, Color.TRANSPARENT) { dark },
                )
                onDispose {}
            }

            AppTheme(mode = themeMode) {
                val key = startKey
                if (key == null) {
                    // 첫 화면을 정하기 전 (DataStore 첫 읽기)
                    Box(modifier = Modifier.fillMaxSize().background(AppThemeTokens.colors.bg))
                } else {
                    // Navigator는 첫 화면을 받아 한 번만 만들어진다. 화면 회전 뒤에는 같은 Navigator를 돌려준다
                    SubCalApp(navigator = koinInject { parametersOf(key) })
                }
            }
        }
    }
}

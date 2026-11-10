package jpark.bro.subcal.feature.settings.impl.more

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.runtime.rememberUpdatedState
import androidx.compose.runtime.setValue
import androidx.compose.ui.platform.LocalContext
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.IOException

/** 시스템 파일 저장 · 선택 창 (SAF). 결과는 [MoreAction.Data]으로 돌려준다 */
internal class BackupFileLaunchers(
    val save: (fileName: String, json: String) -> Unit,
    val open: () -> Unit,
)

@Composable
internal fun rememberBackupFileLaunchers(onAction: (MoreAction) -> Unit): BackupFileLaunchers {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    val currentOnAction by rememberUpdatedState(onAction)
    // 저장 창이 열린 동안 쓸 내용. 화면이 다시 만들어지면 사라져 저장 실패로 알린다
    var pendingJson by remember { mutableStateOf<String?>(null) }

    val createLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument(BACKUP_MIME_TYPE)) { uri ->
            val json = pendingJson
            pendingJson = null
            if (uri != null) {
                scope.launch {
                    val written = json != null && context.writeText(uri, json)
                    currentOnAction(MoreAction.ExportFileWritten(written))
                }
            }
        }
    val openLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.OpenDocument()) { uri ->
            if (uri != null) {
                scope.launch { currentOnAction(MoreAction.BackupFileRead(context.readText(uri))) }
            }
        }
    return remember(createLauncher, openLauncher) {
        BackupFileLaunchers(
            save = { fileName, json ->
                pendingJson = json
                createLauncher.launch(fileName)
            },
            // 파일 앱마다 JSON을 다르게 표시해 텍스트 · 알 수 없는 형식도 고를 수 있게 한다
            open = { openLauncher.launch(arrayOf(BACKUP_MIME_TYPE, "text/plain", "application/octet-stream")) },
        )
    }
}

private suspend fun Context.writeText(
    uri: Uri,
    text: String,
): Boolean =
    withContext(Dispatchers.IO) {
        try {
            val stream = contentResolver.openOutputStream(uri, "wt") ?: return@withContext false
            stream.bufferedWriter().use { it.write(text) }
            true
        } catch (_: IOException) {
            false
        } catch (_: SecurityException) {
            false
        }
    }

/** 너무 크거나 읽지 못하면 null */
private suspend fun Context.readText(uri: Uri): String? =
    withContext(Dispatchers.IO) {
        try {
            contentResolver.openInputStream(uri)?.bufferedReader()?.use { reader ->
                val text = StringBuilder()
                val buffer = CharArray(READ_BUFFER_SIZE)
                while (true) {
                    val read = reader.read(buffer)
                    if (read < 0) break
                    text.appendRange(buffer, 0, read)
                    if (text.length > MAX_BACKUP_CHARS) return@withContext null
                }
                text.toString()
            }
        } catch (_: IOException) {
            null
        } catch (_: SecurityException) {
            null
        }
    }

private const val BACKUP_MIME_TYPE = "application/json"

/** 구독 수천 개도 넉넉한 크기. 이보다 크면 백업 파일이 아니라고 본다 */
private const val MAX_BACKUP_CHARS = 5_000_000

private const val READ_BUFFER_SIZE = 8_192

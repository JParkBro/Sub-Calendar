package jpark.bro.subcal.core.ui

import androidx.compose.material3.DatePicker
import androidx.compose.material3.DatePickerDialog
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.SelectableDates
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.rememberDatePickerState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.R
import java.time.Instant
import java.time.LocalDate
import java.time.ZoneOffset

/**
 * 날짜 선택 다이얼로그 (첫 결제일 · 해지일 등). [maxDate]가 있으면 그 날 이후는 고를 수 없다.
 * DatePicker는 UTC 자정 millis로 날짜를 주고받는다.
 */
@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun SubCalDatePickerDialog(
    initial: LocalDate?,
    onConfirm: (LocalDate) -> Unit,
    onDismiss: () -> Unit,
    maxDate: LocalDate? = null,
) {
    val selectable =
        remember(maxDate) {
            object : SelectableDates {
                override fun isSelectableDate(utcTimeMillis: Long): Boolean =
                    maxDate == null || !utcTimeMillis.toLocalDate().isAfter(maxDate)

                override fun isSelectableYear(year: Int): Boolean = maxDate == null || year <= maxDate.year
            }
        }
    val state =
        rememberDatePickerState(
            initialSelectedDateMillis = initial?.atStartOfDay(ZoneOffset.UTC)?.toInstant()?.toEpochMilli(),
            selectableDates = selectable,
        )
    DatePickerDialog(
        onDismissRequest = onDismiss,
        confirmButton = {
            TextButton(
                onClick = { state.selectedDateMillis?.let { onConfirm(it.toLocalDate()) } },
                enabled = state.selectedDateMillis != null,
            ) { Text(stringResource(R.string.common_confirm)) }
        },
        dismissButton = { TextButton(onClick = onDismiss) { Text(stringResource(R.string.common_cancel)) } },
    ) {
        DatePicker(state = state)
    }
}

private fun Long.toLocalDate(): LocalDate = Instant.ofEpochMilli(this).atZone(ZoneOffset.UTC).toLocalDate()

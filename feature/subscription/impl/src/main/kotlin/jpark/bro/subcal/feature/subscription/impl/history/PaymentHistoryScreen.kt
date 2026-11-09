package jpark.bro.subcal.feature.subscription.impl.history

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.feature.subscription.impl.R
import jpark.bro.subcal.feature.subscription.impl.detail.HistoryRow
import jpark.bro.subcal.feature.subscription.impl.detail.previewDetailState
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun PaymentHistoryRoot(
    key: PaymentHistoryKey,
    onBack: () -> Unit,
    viewModel: PaymentHistoryViewModel = koinViewModel { parametersOf(key.id) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    PaymentHistoryScreen(state = state, onBack = onBack)
}

/** 결제 이력 전체, 최신순 (product-spec 4-4 "더보기") */
@Composable
private fun PaymentHistoryScreen(
    state: PaymentHistoryState,
    onBack: () -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("payment_history"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.subscription_history_title, state.name),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.subscription_detail_back),
                    onClick = onBack,
                )
            },
        )
        LazyColumn(contentPadding = PaddingValues(vertical = AppSpacing.sm)) {
            items(state.entries, key = { "${it::class.simpleName}_${it.date}" }) { entry ->
                HistoryRow(entry)
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun PaymentHistoryPreview() {
    val content = previewDetailState().content
    AppTheme {
        PaymentHistoryScreen(
            state =
                PaymentHistoryState(
                    isLoading = false,
                    name = content?.name.orEmpty(),
                    entries = content?.recentHistory.orEmpty(),
                ),
            onBack = {},
        )
    }
}

package jpark.bro.subcal.feature.subscription.impl.picker

import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun ServicePickerRoot(
    onOpenEdit: (presetId: String?) -> Unit,
    onBack: () -> Unit,
    viewModel: ServicePickerViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            is ServicePickerEvent.OpenEdit -> onOpenEdit(event.presetId)
            ServicePickerEvent.NavigateBack -> onBack()
        }
    }
    ServicePickerScreen(state = state, onAction = viewModel::onAction)
}

/** 서비스 선택 (product-spec 4-3) */
@Composable
private fun ServicePickerScreen(
    state: ServicePickerState,
    onAction: (ServicePickerAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("service_picker"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.subscription_picker_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_close,
                    contentDescription = stringResource(R.string.subscription_picker_close),
                    onClick = { onAction(ServicePickerAction.CloseClicked) },
                )
            },
        )
        SubCalTextField(
            value = state.query,
            onValueChange = { onAction(ServicePickerAction.QueryChanged(it)) },
            placeholder = stringResource(R.string.subscription_picker_search),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.marginMobile)
                    .testTag("picker_search"),
        )
        CategoryChips(selected = state.category, categories = state.categories, onAction = onAction)
        if (state.loadFailed) {
            SubCalInfoBox(
                text = stringResource(R.string.subscription_picker_load_failed),
                modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
            )
        }
        LazyColumn(
            modifier = Modifier.weight(1f).fillMaxWidth(),
            contentPadding = PaddingValues(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(state.services, key = { it.id }) { item ->
                PresetRow(item = item, onClick = { onAction(ServicePickerAction.ServiceClicked(item.id)) })
            }
        }
        SubCalCard(
            onClick = { onAction(ServicePickerAction.ManualEntryClicked) },
            modifier =
                Modifier
                    .padding(AppSpacing.marginMobile)
                    .testTag("manual_entry"),
        ) {
            SubCalListItem(
                title = stringResource(R.string.subscription_picker_manual_title),
                subtitle = stringResource(R.string.subscription_picker_manual_subtitle),
                onClick = { onAction(ServicePickerAction.ManualEntryClicked) },
            )
        }
    }
}

@Composable
private fun CategoryChips(
    selected: Category?,
    categories: List<Category>,
    onAction: (ServicePickerAction) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SubCalFilterChip(
            text = stringResource(R.string.subscription_picker_category_all),
            selected = selected == null,
            onClick = { onAction(ServicePickerAction.CategorySelected(null)) },
        )
        categories.forEach { category ->
            SubCalFilterChip(
                text = categoryLabel(category),
                selected = selected == category,
                onClick = { onAction(ServicePickerAction.CategorySelected(category)) },
            )
        }
    }
}

@Composable
private fun PresetRow(
    item: PresetItem,
    onClick: () -> Unit,
) {
    SubCalCard(onClick = onClick, modifier = Modifier.testTag("preset_${item.id}")) {
        SubCalListItem(
            title = item.name,
            subtitle = categoryLabel(item.category),
            onClick = onClick,
            leading = { SubCalServiceIcon(name = item.name, colorKey = item.colorKey) },
            trailing = {
                Column(horizontalAlignment = Alignment.End, verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    item.lowestAmount?.let { amount ->
                        if (item.hasMorePlans) {
                            Text(
                                text = stringResource(R.string.subscription_picker_amount_from, formatMoney(amount)),
                                style = AppThemeTokens.typography.labelLg.copy(fontFeatureSettings = "tnum"),
                                color = AppThemeTokens.colors.textPrimary,
                            )
                        } else {
                            MoneyText(money = amount, style = AppThemeTokens.typography.labelLg)
                        }
                    }
                    if (item.isRegistered) {
                        SubCalBadge(
                            text = stringResource(R.string.subscription_picker_registered),
                            tone = SubCalBadgeTone.SUCCESS,
                        )
                    }
                }
            },
        )
    }
}

@ScreenPreviews
@Composable
private fun ServicePickerPreview() {
    fun item(
        id: String,
        name: String,
        category: Category,
        colorKey: String,
        amount: Money?,
        hasMorePlans: Boolean = false,
        isRegistered: Boolean = false,
    ) = PresetItem(id, name, category, colorKey, amount, hasMorePlans, isRegistered)
    AppTheme {
        ServicePickerScreen(
            state =
                ServicePickerState(
                    isLoading = false,
                    categories = listOf(Category.VIDEO, Category.MUSIC, Category.AI),
                    services =
                        listOf(
                            item("netflix", "넷플릭스", Category.VIDEO, "RED", Money(7_000, Currency.KRW), true, true),
                            item("disney_plus", "디즈니+", Category.VIDEO, "BLACK", Money(9_900, Currency.KRW), true),
                            item("watcha", "왓챠", Category.VIDEO, "PINK", null),
                            item(
                                "spotify",
                                "스포티파이",
                                Category.MUSIC,
                                "GREEN",
                                Money(10_900, Currency.KRW),
                                isRegistered = true,
                            ),
                            item("chatgpt_plus", "ChatGPT Plus", Category.AI, "TEAL", Money(2_000, Currency.USD)),
                        ),
                ),
            onAction = {},
        )
    }
}

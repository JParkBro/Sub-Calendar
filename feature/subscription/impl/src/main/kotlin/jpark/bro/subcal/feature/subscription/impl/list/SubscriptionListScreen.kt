package jpark.bro.subcal.feature.subscription.impl.list

import androidx.annotation.StringRes
import androidx.compose.foundation.background
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
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
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalFab
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.ListSort
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.billingCycleLabel
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.subscription.api.SubscriptionListKey
import jpark.bro.subcal.feature.subscription.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.subcal.core.designsystem.R as DesignR

/** 마지막 행이 FAB에 가리지 않게 (FAB 56dp + 여백) */
private val FAB_CLEARANCE = 88.dp

@Composable
internal fun SubscriptionListRoot(
    key: SubscriptionListKey,
    onAddClick: () -> Unit,
    onOpenDetail: (id: String) -> Unit,
    viewModel: SubscriptionListViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            SubscriptionListEvent.OpenServicePicker -> onAddClick()
            is SubscriptionListEvent.OpenDetail -> onOpenDetail(event.id)
        }
    }
    SubscriptionListScreen(state = state, onAction = viewModel::onAction)
}

@Composable
private fun SubscriptionListScreen(
    state: SubscriptionListState,
    onAction: (SubscriptionListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Box(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("subscription_list"),
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            SubCalTopBar(title = stringResource(R.string.subscription_list_title))
            FilterRow(state = state, onAction = onAction)
            SortAndTotalRow(sort = state.sort, monthlyTotalKrw = state.content.monthlyTotalKrw, onAction = onAction)
            if (!state.isLoading && state.content.sections.isEmpty()) {
                EmptyContent(modifier = Modifier.weight(1f))
            } else {
                SubscriptionSections(
                    sections = state.content.sections,
                    onRowClick = { onAction(SubscriptionListAction.RowClicked(it)) },
                    modifier = Modifier.weight(1f),
                )
            }
        }
        SubCalFab(
            icon = DesignR.drawable.ic_add,
            contentDescription = stringResource(R.string.subscription_list_add),
            onClick = { onAction(SubscriptionListAction.AddClicked) },
            modifier =
                Modifier
                    .align(Alignment.BottomEnd)
                    .padding(AppSpacing.marginMobile)
                    .testTag("add_fab"),
        )
    }

    if (state.isSortSheetOpen) {
        SortSheet(selected = state.sort, onAction = onAction)
    }
}

@Composable
private fun FilterRow(
    state: SubscriptionListState,
    onAction: (SubscriptionListAction) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .horizontalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm)
                .selectableGroup(),
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        state.filter.category?.let { category ->
            val label = categoryLabel(category)
            val clearDescription = stringResource(R.string.subscription_list_category_filter_clear, label)
            SubCalFilterChip(
                text = stringResource(R.string.subscription_list_category_filter, label),
                selected = true,
                onClick = { onAction(SubscriptionListAction.CategoryFilterCleared) },
                role = Role.Button,
                modifier =
                    Modifier
                        .semantics { contentDescription = clearDescription }
                        .testTag("category_filter"),
            )
        }
        state.content.filters.forEach { chip ->
            SubCalFilterChip(
                text = stringResource(chip.filter.labelRes, chip.count),
                selected = chip.filter == state.filter.status,
                onClick = { onAction(SubscriptionListAction.StatusFilterSelected(chip.filter)) },
                modifier = Modifier.testTag("status_filter_${chip.filter.name.lowercase()}"),
            )
        }
    }
}

@Composable
private fun SortAndTotalRow(
    sort: ListSort,
    monthlyTotalKrw: Long,
    onAction: (SubscriptionListAction) -> Unit,
) {
    Row(
        modifier =
            Modifier
                .fillMaxWidth()
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        SubCalFilterChip(
            text = stringResource(sort.labelRes),
            selected = false,
            onClick = { onAction(SubscriptionListAction.SortClicked) },
            role = Role.Button,
            modifier = Modifier.testTag("sort_button"),
        )
        Row(
            modifier = Modifier.weight(1f),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm, Alignment.End),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.subscription_list_total_label),
                style = AppThemeTokens.typography.labelMd,
                color = AppThemeTokens.colors.textSecondary,
            )
            MoneyText(
                money = Money(monthlyTotalKrw, Currency.KRW),
                style = AppThemeTokens.typography.labelLg,
                tone = MoneyTone.HIGHLIGHT,
                modifier = Modifier.testTag("monthly_total"),
            )
        }
    }
}

@Composable
private fun SubscriptionSections(
    sections: List<SubscriptionSection>,
    onRowClick: (id: String) -> Unit,
    modifier: Modifier = Modifier,
) {
    LazyColumn(
        modifier = modifier.fillMaxWidth(),
        contentPadding =
            PaddingValues(
                start = AppSpacing.marginMobile,
                end = AppSpacing.marginMobile,
                bottom = FAB_CLEARANCE,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        sections.forEach { section ->
            section.category?.let { category ->
                item(key = "header_${category.name}") {
                    SectionHeader(label = categoryLabel(category), count = section.rows.size)
                }
            }
            items(section.rows, key = { it.id }) { row ->
                SubscriptionRowCard(row = row, onClick = { onRowClick(row.id) })
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun SubscriptionListPreview() {
    AppTheme { SubscriptionListScreen(state = previewState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun SubscriptionListByAmountPreview() {
    AppTheme { SubscriptionListScreen(state = previewState(sort = ListSort.AMOUNT), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun SubscriptionListEmptyPreview() {
    AppTheme { SubscriptionListScreen(state = SubscriptionListState(isLoading = false), onAction = {}) }
}

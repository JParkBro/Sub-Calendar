package jpark.bro.subcal.feature.party.impl.list

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
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
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.party.impl.R
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun PartyListRoot(
    onOpenParty: (subscriptionId: String) -> Unit,
    onBack: () -> Unit,
    viewModel: PartyListViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            PartyListEvent.NavigateBack -> onBack()
            is PartyListEvent.OpenParty -> onOpenParty(event.subscriptionId)
        }
    }
    PartyListScreen(state = state, onAction = viewModel::onAction)
}

/** 파티 관리 (product-spec 6장) */
@Composable
private fun PartyListScreen(
    state: PartyListState,
    onAction: (PartyListAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("party_list"),
    ) {
        SubCalTopBar(
            title = stringResource(R.string.party_list_title),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.party_back),
                    onClick = { onAction(PartyListAction.BackClicked) },
                )
            },
        )
        if (state.isLoading) return@Column
        LazyColumn(
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            if (state.items.isEmpty()) {
                item(key = "empty") { SubCalInfoBox(text = stringResource(R.string.party_list_empty)) }
            }
            items(state.items, key = { it.subscriptionId }) { item ->
                SubCalCard {
                    SubCalListItem(
                        title = item.name,
                        subtitle =
                            stringResource(R.string.party_list_subtitle, item.partySize, formatMoney(item.myShare)),
                        onClick = { onAction(PartyListAction.PartyClicked(item.subscriptionId)) },
                        leading = { SubCalServiceIcon(name = item.name, colorKey = item.colorKey) },
                        trailing = {
                            if (item.unsettledCount > 0) {
                                SubCalBadge(
                                    text = stringResource(R.string.party_list_unsettled, item.unsettledCount),
                                    tone = SubCalBadgeTone.SOON,
                                )
                            }
                        },
                        modifier = Modifier.testTag("party_${item.subscriptionId}"),
                    )
                }
            }
        }
    }
}

@ScreenPreviews
@Composable
private fun PartyListPreview() {
    AppTheme {
        PartyListScreen(
            state =
                PartyListState(
                    isLoading = false,
                    items =
                        listOf(
                            PartyListItem("youtube_premium", "유튜브 프리미엄", "PINK", 4, Money(3_725, Currency.KRW), 1),
                        ),
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun PartyListEmptyPreview() {
    AppTheme { PartyListScreen(state = PartyListState(isLoading = false), onAction = {}) }
}

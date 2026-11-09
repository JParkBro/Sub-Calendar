package jpark.bro.subcal.feature.onboarding.impl

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.selection.selectableGroup
import androidx.compose.foundation.selection.toggleable
import androidx.compose.material3.Icon
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.Role
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.text.style.TextOverflow
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppShapes
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalInfoBox
import jpark.bro.subcal.core.designsystem.component.SubCalOutlineButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.categoryLabel
import jpark.bro.subcal.core.ui.formatMoney
import org.koin.compose.viewmodel.koinViewModel
import jpark.bro.subcal.core.designsystem.R as DesignR

private const val GRID_COLUMNS = 3

@Composable
internal fun OnboardingRoot(
    onFinished: () -> Unit,
    viewModel: OnboardingViewModel = koinViewModel(),
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.onboarding_save_failed)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            OnboardingEvent.Finished -> onFinished()
            OnboardingEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        OnboardingScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 첫 실행 서비스 선택 (product-spec 5장) */
@Composable
private fun OnboardingScreen(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("onboarding"),
    ) {
        Column(
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.lg),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            Text(
                text = stringResource(R.string.onboarding_title),
                style = AppThemeTokens.typography.headlineLg,
                color = AppThemeTokens.colors.textPrimary,
            )
            Text(
                text = stringResource(R.string.onboarding_subtitle),
                style = AppThemeTokens.typography.bodyMd,
                color = AppThemeTokens.colors.textSecondary,
            )
        }
        SubCalTextField(
            value = state.query,
            onValueChange = { onAction(OnboardingAction.QueryChanged(it)) },
            placeholder = stringResource(R.string.onboarding_search),
            modifier =
                Modifier
                    .fillMaxWidth()
                    .padding(horizontal = AppSpacing.marginMobile)
                    .testTag("onboarding_search"),
        )
        CategoryChips(selected = state.category, categories = state.categories, onAction = onAction)
        if (state.loadFailed) {
            SubCalInfoBox(
                text = stringResource(R.string.onboarding_load_failed),
                modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
            )
        }
        LazyVerticalGrid(
            columns = GridCells.Fixed(GRID_COLUMNS),
            modifier = Modifier.weight(1f),
            contentPadding = PaddingValues(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        ) {
            items(state.services, key = { it.id }) { service ->
                ServiceTile(service = service, onToggle = { onAction(OnboardingAction.ServiceToggled(service.id)) })
            }
        }
        BottomBar(state = state, onAction = onAction)
    }
}

@Composable
private fun CategoryChips(
    selected: Category?,
    categories: List<Category>,
    onAction: (OnboardingAction) -> Unit,
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
            text = stringResource(R.string.onboarding_category_all),
            selected = selected == null,
            onClick = { onAction(OnboardingAction.CategorySelected(null)) },
        )
        categories.forEach { category ->
            SubCalFilterChip(
                text = categoryLabel(category),
                selected = selected == category,
                onClick = { onAction(OnboardingAction.CategorySelected(category)) },
            )
        }
    }
}

/** 서비스 칸: 아이콘 · 이름 · 첫 요금제 금액. 고르면 primary 테두리 + 체크 */
@Composable
private fun ServiceTile(
    service: OnboardingService,
    onToggle: () -> Unit,
) {
    val colors = AppThemeTokens.colors
    Box(
        modifier =
            Modifier
                .clip(AppShapes.lg)
                .background(if (service.isSelected) colors.primarySoft else colors.surface)
                .border(
                    width = if (service.isSelected) 2.dp else 1.dp,
                    color = if (service.isSelected) colors.primary else colors.border,
                    shape = AppShapes.lg,
                ).toggleable(value = service.isSelected, role = Role.Checkbox, onValueChange = { onToggle() })
                .padding(vertical = AppSpacing.md, horizontal = AppSpacing.sm)
                .testTag("service_${service.id}"),
    ) {
        Column(
            modifier = Modifier.fillMaxWidth(),
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(AppSpacing.xs),
        ) {
            SubCalServiceIcon(name = service.name, colorKey = service.colorKey)
            Text(
                text = service.name,
                style = AppThemeTokens.typography.labelMd,
                color = colors.textPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1,
                overflow = TextOverflow.Ellipsis,
            )
            MoneyText(
                money = service.amount,
                style = AppThemeTokens.typography.labelSm,
            )
        }
        if (service.isSelected) {
            Icon(
                painter = painterResource(DesignR.drawable.ic_check),
                contentDescription = null,
                tint = colors.primary,
                modifier = Modifier.align(Alignment.TopEnd).size(18.dp),
            )
        }
    }
}

/** "n개 선택 · 월 n원" + 등록하고 시작하기 + 건너뛰기 */
@Composable
private fun BottomBar(
    state: OnboardingState,
    onAction: (OnboardingAction) -> Unit,
) {
    Column(
        modifier =
            Modifier
                .fillMaxWidth()
                .background(AppThemeTokens.colors.surface)
                .padding(AppSpacing.marginMobile),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.sm),
        horizontalAlignment = Alignment.CenterHorizontally,
    ) {
        if (state.selectedCount > 0) {
            Text(
                text =
                    stringResource(
                        R.string.onboarding_selected_summary,
                        state.selectedCount,
                        formatMoney(Money(state.monthlyTotalKrw, Currency.KRW)),
                    ),
                style = AppThemeTokens.typography.labelLg.copy(fontFeatureSettings = "tnum"),
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.testTag("selected_summary"),
            )
        }
        SubCalPrimaryButton(
            text =
                if (state.selectedCount > 0) {
                    stringResource(R.string.onboarding_register, state.selectedCount)
                } else {
                    stringResource(R.string.onboarding_register_empty)
                },
            onClick = { onAction(OnboardingAction.RegisterClicked) },
            enabled = state.selectedCount > 0 && !state.isSaving,
            modifier = Modifier.fillMaxWidth().testTag("register_button"),
        )
        SubCalOutlineButton(
            text = stringResource(R.string.onboarding_skip),
            onClick = { onAction(OnboardingAction.SkipClicked) },
            enabled = !state.isSaving,
            modifier = Modifier.fillMaxWidth().testTag("skip_button"),
        )
    }
}

@ScreenPreviews
@Composable
private fun OnboardingPreview() {
    fun service(
        id: String,
        name: String,
        colorKey: String,
        amount: Long,
        selected: Boolean = false,
    ) = OnboardingService(id, name, colorKey, Money(amount, Currency.KRW), selected)
    AppTheme {
        OnboardingScreen(
            state =
                OnboardingState(
                    isLoading = false,
                    categories = listOf(Category.VIDEO, Category.MUSIC, Category.SHOPPING, Category.CLOUD),
                    services =
                        listOf(
                            service("netflix", "넷플릭스", "RED", 13_500, selected = true),
                            service("youtube_premium", "유튜브 프리미엄", "PINK", 14_900),
                            service("coupang_wow", "쿠팡 와우", "ORANGE", 7_890, selected = true),
                            service("melon", "멜론", "GREEN", 10_900),
                            service("tving", "티빙", "ORANGE", 10_900),
                            service("google_one", "구글 원", "BLUE", 44_400),
                        ),
                    selectedCount = 2,
                    monthlyTotalKrw = 21_390,
                ),
            onAction = {},
        )
    }
}

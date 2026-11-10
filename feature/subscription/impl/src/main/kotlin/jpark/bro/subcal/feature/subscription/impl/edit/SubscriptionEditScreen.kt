package jpark.bro.subcal.feature.subscription.impl.edit

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.input.KeyboardType
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalAmountField
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalDangerButton
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.PresetPlan
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.SubCalDatePickerDialog
import jpark.bro.subcal.feature.subscription.api.SubscriptionEditKey
import jpark.bro.subcal.feature.subscription.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import java.time.LocalDate
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun SubscriptionEditRoot(
    key: SubscriptionEditKey,
    onSaved: () -> Unit,
    onBack: () -> Unit,
    viewModel: SubscriptionEditViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val toastHostState = remember { SnackbarHostState() }
    val saveFailed = stringResource(R.string.subscription_edit_save_failed)
    val deleteFailed = stringResource(R.string.subscription_edit_delete_failed)
    val requestNotificationPermission = rememberNotificationPermissionRequest(onDone = onSaved)

    ObserveEvents(viewModel.event) { event ->
        when (event) {
            SubscriptionEditEvent.NavigateBack -> onBack()
            is SubscriptionEditEvent.Saved -> if (event.isNew) requestNotificationPermission() else onSaved()
            SubscriptionEditEvent.Deleted -> onBack()
            SubscriptionEditEvent.SaveFailed -> toastHostState.showSnackbar(saveFailed)
            SubscriptionEditEvent.DeleteFailed -> toastHostState.showSnackbar(deleteFailed)
        }
    }

    Box(modifier = Modifier.fillMaxSize()) {
        SubscriptionEditScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/** 구독 입력 · 수정 (product-spec 4-3) */
@Composable
private fun SubscriptionEditScreen(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .imePadding()
                .testTag("subscription_edit"),
    ) {
        SubCalTopBar(
            title = stringResource(state.titleRes),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_close,
                    contentDescription = stringResource(R.string.subscription_edit_cancel),
                    onClick = { onAction(SubscriptionEditAction.CancelClicked) },
                )
            },
            actions = {
                SubCalPrimaryButton(
                    text = stringResource(R.string.subscription_edit_save),
                    onClick = { onAction(SubscriptionEditAction.SaveClicked) },
                    enabled = state.canSave,
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.testTag("save_button"),
                )
            },
        )
        if (!state.isLoading) {
            EditForm(state = state, onAction = onAction, modifier = Modifier.weight(1f))
        }
    }

    state.datePicker?.let { target ->
        SubCalDatePickerDialog(
            initial =
                when (target) {
                    DateTarget.FIRST_BILLING -> state.firstBillingDate
                    DateTarget.TRIAL_START -> state.trialStartedOn
                },
            onConfirm = { onAction(SubscriptionEditAction.DatePicked(it)) },
            onDismiss = { onAction(SubscriptionEditAction.DatePickerDismissed) },
        )
    }
    when (state.sheet) {
        EditSheet.PRICE_CHANGE -> PriceChangeSheet(onAction = onAction)
        EditSheet.DELETE -> DeleteSheet(onAction = onAction)
        null -> Unit
    }
}

private val SubscriptionEditState.titleRes: Int
    get() = if (isEditMode) R.string.subscription_edit_title_edit else R.string.subscription_edit_title_add

@Composable
private fun EditForm(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    Column(
        modifier =
            modifier
                .fillMaxWidth()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.xl),
    ) {
        FormField(label = stringResource(R.string.subscription_edit_name)) {
            NameRow(state = state, onAction = onAction)
        }
        if (state.plans.isNotEmpty()) {
            PlanChips(state = state, onAction = onAction)
        }
        FormField(label = stringResource(R.string.subscription_edit_amount)) {
            SubCalAmountField(
                value = state.amountInput,
                currency = state.currency,
                onValueChange = { onAction(SubscriptionEditAction.AmountChanged(it)) },
                onCurrencyChange = { onAction(SubscriptionEditAction.CurrencyChanged(it)) },
                modifier = Modifier.fillMaxWidth().testTag("amount_field"),
            )
        }
        CycleSection(state = state, onAction = onAction)
        DateSection(state = state, onAction = onAction)
        PaymentMethodSection(state = state, onAction = onAction)
        CategoryChips(selected = state.category, onAction = onAction)
        ReminderChips(selected = state.reminder, onAction = onAction)
        ColorSwatches(name = state.name, selected = state.colorKey, onAction = onAction)
        FormField(label = stringResource(R.string.subscription_edit_cancel_url)) {
            SubCalTextField(
                value = state.cancelUrl,
                onValueChange = { onAction(SubscriptionEditAction.CancelUrlChanged(it)) },
                placeholder = stringResource(R.string.subscription_edit_cancel_url_placeholder),
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Uri),
                modifier = Modifier.fillMaxWidth(),
            )
        }
        FormField(label = stringResource(R.string.subscription_edit_memo)) {
            SubCalTextField(
                value = state.memo,
                onValueChange = { onAction(SubscriptionEditAction.MemoChanged(it)) },
                singleLine = false,
                modifier = Modifier.fillMaxWidth(),
            )
        }
        if (state.isEditMode) {
            SubCalDangerButton(
                text = stringResource(R.string.subscription_edit_delete),
                onClick = { onAction(SubscriptionEditAction.DeleteClicked) },
                modifier = Modifier.fillMaxWidth().testTag("delete_button"),
            )
        }
    }
}

@Composable
private fun NameRow(
    state: SubscriptionEditState,
    onAction: (SubscriptionEditAction) -> Unit,
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        SubCalServiceIcon(name = state.name, colorKey = state.colorKey)
        SubCalTextField(
            value = state.name,
            onValueChange = { onAction(SubscriptionEditAction.NameChanged(it)) },
            placeholder = stringResource(R.string.subscription_edit_name_placeholder),
            modifier = Modifier.weight(1f).testTag("name_field"),
        )
    }
}

@ScreenPreviews
@Composable
private fun SubscriptionEditPreview() {
    AppTheme {
        SubscriptionEditScreen(
            state =
                SubscriptionEditState(
                    isLoading = false,
                    presetId = "disney_plus",
                    plans =
                        listOf(
                            PresetPlan("스탠다드", Money(9_900, Currency.KRW), BillingCycle(CycleUnit.MONTH)),
                            PresetPlan("프리미엄", Money(13_900, Currency.KRW), BillingCycle(CycleUnit.MONTH)),
                        ),
                    selectedPlanIndex = 1,
                    pricesAsOf = "2026.10",
                    name = "디즈니+",
                    amountInput = "13900",
                    firstBillingDate = LocalDate.of(2026, 10, 31),
                    trialStartedOn = LocalDate.of(2026, 10, 1),
                    paymentMethod = "신한",
                    paymentMethodSuggestions = listOf("신한카드"),
                    category = Category.VIDEO,
                    reminder = ReminderOption.ONE_DAY,
                    colorKey = "BLACK",
                    canSave = true,
                ),
            onAction = {},
        )
    }
}

@ScreenPreviews
@Composable
private fun SubscriptionEditManualPreview() {
    AppTheme {
        SubscriptionEditScreen(
            state =
                SubscriptionEditState(
                    isLoading = false,
                    isEditMode = true,
                    name = "헬스장",
                    amountInput = "60000",
                    cycleOption = CycleOption.CUSTOM,
                    customUnit = CycleUnit.MONTH,
                    customIntervalInput = "3",
                    firstBillingDate = LocalDate.of(2026, 3, 2),
                    pastPaymentCount = 3,
                    canSave = true,
                ),
            onAction = {},
        )
    }
}

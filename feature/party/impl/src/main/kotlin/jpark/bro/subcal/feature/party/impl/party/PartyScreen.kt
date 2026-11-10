package jpark.bro.subcal.feature.party.impl.party

import android.content.Context
import android.content.Intent
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.SnackbarHostState
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppTheme
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalIconButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalToastHost
import jpark.bro.subcal.core.designsystem.component.SubCalTopBar
import jpark.bro.subcal.core.designsystem.preview.ScreenPreviews
import jpark.bro.subcal.core.domain.party.vocativeParticle
import jpark.bro.subcal.core.ui.ObserveEvents
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.party.api.PartyKey
import jpark.bro.subcal.feature.party.impl.R
import org.koin.compose.viewmodel.koinViewModel
import org.koin.core.parameter.parametersOf
import jpark.bro.subcal.core.designsystem.R as DesignR

@Composable
internal fun PartyRoot(
    key: PartyKey,
    onBack: () -> Unit,
    viewModel: PartyViewModel = koinViewModel { parametersOf(key) },
) {
    val state by viewModel.state.collectAsStateWithLifecycle()
    val context = LocalContext.current
    val toastHostState = remember { SnackbarHostState() }
    val failed = stringResource(R.string.party_action_failed)
    val overTotal = stringResource(R.string.party_split_over_total)
    ObserveEvents(viewModel.event) { event ->
        when (event) {
            PartyEvent.NavigateBack -> onBack()
            is PartyEvent.ShareRequest -> context.shareRequest(event)
            PartyEvent.SplitOverTotal -> toastHostState.showSnackbar(overTotal)
            PartyEvent.ActionFailed -> toastHostState.showSnackbar(failed)
        }
    }
    Box(modifier = Modifier.fillMaxSize()) {
        PartyScreen(state = state, onAction = viewModel::onAction)
        SubCalToastHost(hostState = toastHostState, modifier = Modifier.align(Alignment.BottomCenter))
    }
}

/**
 * "{이름}아/야 이번 달 {서비스} {금액} 보내줘!"를 한 줄에 한 명씩 공유 시트로 보낸다 (product-spec 4-9).
 * 어디로 보냈는지는 모른다.
 */
private fun Context.shareRequest(request: PartyEvent.ShareRequest) {
    val text =
        request.lines.joinToString("\n") { line ->
            val name = line.name + vocativeParticle(line.name)
            val amount = this.formatMoney(line.amount)
            if (request.pastMonth == null) {
                getString(R.string.party_request_message, name, request.serviceName, amount)
            } else {
                getString(R.string.party_request_message_month, name, request.pastMonth, request.serviceName, amount)
            }
        }
    val send =
        Intent(Intent.ACTION_SEND).apply {
            type = "text/plain"
            putExtra(Intent.EXTRA_TEXT, text)
        }
    startActivity(Intent.createChooser(send, getString(R.string.party_request_chooser)))
}

/** 파티 정산 (product-spec 4-9) */
@Composable
private fun PartyScreen(
    state: PartyState,
    onAction: (PartyAction) -> Unit,
    modifier: Modifier = Modifier,
) {
    val content = state.content
    Column(
        modifier =
            modifier
                .fillMaxSize()
                .background(AppThemeTokens.colors.bg)
                .testTag("party"),
    ) {
        SubCalTopBar(
            title = content?.let { stringResource(R.string.party_title, it.name) }.orEmpty(),
            navigationIcon = {
                SubCalIconButton(
                    icon = DesignR.drawable.ic_arrow_back,
                    contentDescription = stringResource(R.string.party_back),
                    onClick = { onAction(PartyAction.BackClicked) },
                )
            },
        )
        if (content == null) return@Column
        val bill = state.selectedBill
        Column(
            modifier =
                Modifier
                    .weight(1f)
                    .verticalScroll(rememberScrollState())
                    .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.md),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.gutter),
        ) {
            SummaryCard(content = content, enabled = !state.isWorking, onAction = onAction)
            MembersCard(content = content, onAction = onAction)
            if (content.members.isNotEmpty()) {
                SettlementCard(content = content, bill = bill, enabled = !state.isWorking, onAction = onAction)
            }
        }
        if (bill != null && bill.unsettled.isNotEmpty()) {
            SubCalPrimaryButton(
                text = stringResource(R.string.party_request_all, bill.unsettled.size),
                onClick = { onAction(PartyAction.RequestAllClicked) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .background(AppThemeTokens.colors.surface)
                        .padding(AppSpacing.marginMobile)
                        .testTag("request_all"),
            )
        }
    }
    when (val sheet = state.sheet) {
        is PartySheet.MemberName -> MemberNameSheet(sheet = sheet, onAction = onAction)
        is PartySheet.Split -> SplitSheet(sheet = sheet, content = content, onAction = onAction)
        null -> Unit
    }
}

@ScreenPreviews
@Composable
private fun PartyPreview() {
    AppTheme { PartyScreen(state = previewPartyState(), onAction = {}) }
}

@ScreenPreviews
@Composable
private fun PartyEmptyPreview() {
    AppTheme { PartyScreen(state = previewPartyState(withMembers = false), onAction = {}) }
}

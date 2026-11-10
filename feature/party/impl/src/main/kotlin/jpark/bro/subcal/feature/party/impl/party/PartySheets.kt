package jpark.bro.subcal.feature.party.impl.party

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalAmountField
import jpark.bro.subcal.core.designsystem.component.SubCalBottomSheet
import jpark.bro.subcal.core.designsystem.component.SubCalDangerButton
import jpark.bro.subcal.core.designsystem.component.SubCalPrimaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalSegment
import jpark.bro.subcal.core.designsystem.component.SubCalSegmentedControl
import jpark.bro.subcal.core.designsystem.component.SubCalTextField
import jpark.bro.subcal.core.designsystem.component.amountInputToMinor
import jpark.bro.subcal.core.domain.party.PartyMemberNameLength
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.feature.party.impl.R

/** 멤버 추가 · 이름 바꾸기 (+ 지우기) */
@Composable
internal fun MemberNameSheet(
    sheet: PartySheet.MemberName,
    onAction: (PartyAction) -> Unit,
) {
    SubCalBottomSheet(onDismissRequest = { onAction(PartyAction.SheetDismissed) }) {
        SheetBody(
            title =
                stringResource(
                    if (sheet.memberId == null) R.string.party_member_add_title else R.string.party_member_edit_title,
                ),
        ) {
            SubCalTextField(
                value = sheet.input,
                onValueChange = { onAction(PartyAction.MemberNameChanged(it)) },
                placeholder = stringResource(R.string.party_member_name_hint),
                modifier = Modifier.fillMaxWidth().testTag("member_name"),
            )
            SubCalPrimaryButton(
                text = stringResource(R.string.party_save),
                onClick = { onAction(PartyAction.MemberSaveClicked) },
                enabled = sheet.input.trim().length in PartyMemberNameLength,
                modifier = Modifier.fillMaxWidth().testTag("member_save"),
            )
            if (sheet.memberId != null) {
                SubCalDangerButton(
                    text = stringResource(R.string.party_member_delete),
                    onClick = { onAction(PartyAction.MemberDeleteClicked) },
                    modifier = Modifier.fillMaxWidth().testTag("member_delete"),
                )
            }
        }
    }
}

/** 분담 방식: 균등 / 직접 입력(멤버별 금액, 내 몫 = 총액 − 합) */
@Composable
internal fun SplitSheet(
    sheet: PartySheet.Split,
    content: PartyContent?,
    onAction: (PartyAction) -> Unit,
) {
    content ?: return
    val modes = PartySplitMode.entries
    SubCalBottomSheet(onDismissRequest = { onAction(PartyAction.SheetDismissed) }) {
        SheetBody(title = stringResource(R.string.party_split_title)) {
            SubCalSegmentedControl(
                segments = modes.map { SubCalSegment(stringResource(splitLabel(it))) },
                selectedIndex = modes.indexOf(sheet.mode),
                onSelect = { onAction(PartyAction.SplitModeSelected(modes[it])) },
                modifier = Modifier.testTag("split_mode"),
            )
            when (sheet.mode) {
                PartySplitMode.EQUAL -> {
                    Text(
                        text = stringResource(R.string.party_split_equal_hint),
                        style = AppThemeTokens.typography.bodySm,
                        color = AppThemeTokens.colors.textSecondary,
                    )
                }

                PartySplitMode.CUSTOM -> {
                    CustomShares(sheet = sheet, content = content, onAction = onAction)
                }
            }
            SubCalPrimaryButton(
                text = stringResource(R.string.party_save),
                onClick = { onAction(PartyAction.SplitSaveClicked) },
                modifier = Modifier.fillMaxWidth().testTag("split_save"),
            )
        }
    }
}

@Composable
private fun CustomShares(
    sheet: PartySheet.Split,
    content: PartyContent,
    onAction: (PartyAction) -> Unit,
) {
    val currency = content.total.currency
    content.members.forEach { member ->
        Row(
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Text(
                text = member.name,
                style = AppThemeTokens.typography.labelLg,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            SubCalAmountField(
                value = sheet.inputs[member.id].orEmpty(),
                currency = currency,
                onValueChange = { onAction(PartyAction.ShareInputChanged(member.id, it)) },
                onCurrencyChange = null,
                placeholder = "0",
                modifier = Modifier.weight(1f).testTag("share_${member.id}"),
            )
        }
    }
    val others = content.members.sumOf { amountInputToMinor(sheet.inputs[it.id].orEmpty(), currency) ?: 0 }
    val mine = content.total.amountMinor - others
    Row(verticalAlignment = Alignment.CenterVertically) {
        Text(
            text = stringResource(R.string.party_split_my_share),
            style = AppThemeTokens.typography.labelLg,
            color = if (mine < 0) AppThemeTokens.colors.danger else AppThemeTokens.colors.textSecondary,
            modifier = Modifier.weight(1f),
        )
        if (mine < 0) {
            Text(
                text = stringResource(R.string.party_split_over_total),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.danger,
            )
        } else {
            MoneyText(money = content.total.copy(amountMinor = mine), modifier = Modifier.testTag("split_my_share"))
        }
    }
}

@Composable
private fun SheetBody(
    title: String,
    content: @Composable () -> Unit,
) {
    Column(
        modifier =
            Modifier.padding(
                start = AppSpacing.marginMobile,
                end = AppSpacing.marginMobile,
                top = AppSpacing.md,
                bottom = AppSpacing.xl,
            ),
        verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
    ) {
        Text(text = title, style = AppThemeTokens.typography.headlineSm, color = AppThemeTokens.colors.textPrimary)
        content()
    }
}

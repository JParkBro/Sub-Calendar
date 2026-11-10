package jpark.bro.subcal.feature.party.impl.party

import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.semantics.contentDescription
import androidx.compose.ui.semantics.semantics
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalBadge
import jpark.bro.subcal.core.designsystem.component.SubCalBadgeTone
import jpark.bro.subcal.core.designsystem.component.SubCalButtonSize
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalCheckbox
import jpark.bro.subcal.core.designsystem.component.SubCalDivider
import jpark.bro.subcal.core.designsystem.component.SubCalFilterChip
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.designsystem.component.SubCalOutlineButton
import jpark.bro.subcal.core.designsystem.component.SubCalSecondaryButton
import jpark.bro.subcal.core.designsystem.component.SubCalServiceIcon
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.ui.MoneyText
import jpark.bro.subcal.core.ui.MoneyTone
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.feature.party.impl.R

/** 위쪽 카드: "총 n원 ÷ n명" · 분담 방식 + 내 몫, 분담 방식 바꾸기 */
@Composable
internal fun SummaryCard(
    content: PartyContent,
    enabled: Boolean,
    onAction: (PartyAction) -> Unit,
) {
    SubCalCard {
        Column(
            modifier = Modifier.padding(AppSpacing.marginMobile),
            verticalArrangement = Arrangement.spacedBy(AppSpacing.md),
        ) {
            Row(
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.md),
            ) {
                SubCalServiceIcon(name = content.name, colorKey = content.colorKey)
                Column(modifier = Modifier.weight(1f), verticalArrangement = Arrangement.spacedBy(AppSpacing.xs)) {
                    Text(
                        text = stringResource(splitLabel(content.splitMode)),
                        style = AppThemeTokens.typography.labelMd,
                        color = AppThemeTokens.colors.textSecondary,
                    )
                    Text(
                        text = stringResource(R.string.party_total, formatMoney(content.total), content.partySize),
                        style = AppThemeTokens.typography.bodyMd.copy(fontFeatureSettings = "tnum"),
                        color = AppThemeTokens.colors.textPrimary,
                    )
                }
                Column(horizontalAlignment = Alignment.End) {
                    Text(
                        text = stringResource(R.string.party_my_share),
                        style = AppThemeTokens.typography.labelMd,
                        color = AppThemeTokens.colors.textSecondary,
                    )
                    MoneyText(
                        money = content.myShare,
                        style = AppThemeTokens.typography.headlineSm,
                        tone = MoneyTone.HIGHLIGHT,
                        modifier = Modifier.testTag("my_share"),
                    )
                }
            }
            if (content.members.isNotEmpty()) {
                SubCalSecondaryButton(
                    text = stringResource(R.string.party_split_change),
                    onClick = { onAction(PartyAction.SplitClicked) },
                    enabled = enabled,
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.testTag("split_button"),
                )
            }
        }
    }
}

/** 멤버: 나(지울 수 없음) + 추가한 멤버와 몫. 누르면 이름 바꾸기 · 지우기 */
@Composable
internal fun MembersCard(
    content: PartyContent,
    onAction: (PartyAction) -> Unit,
) {
    SubCalCard {
        SectionTitle(stringResource(R.string.party_members_title, content.partySize))
        SubCalListItem(
            title = stringResource(R.string.party_me),
            trailing = { MoneyText(money = content.myShare) },
        )
        content.members.forEach { member ->
            SubCalDivider()
            SubCalListItem(
                title = member.name,
                onClick = { onAction(PartyAction.MemberClicked(member.id)) },
                showChevron = false,
                trailing = { MoneyText(money = member.share) },
                modifier = Modifier.testTag("member_${member.id}"),
            )
        }
        if (content.members.isEmpty()) {
            Text(
                text = stringResource(R.string.party_members_empty),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
                modifier = Modifier.padding(horizontal = AppSpacing.marginMobile),
            )
        }
        if (content.canAddMember) {
            SubCalOutlineButton(
                text = stringResource(R.string.party_member_add),
                onClick = { onAction(PartyAction.AddMemberClicked) },
                modifier =
                    Modifier
                        .fillMaxWidth()
                        .padding(AppSpacing.marginMobile)
                        .testTag("add_member"),
            )
        } else {
            Text(
                text = stringResource(R.string.party_member_full),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textTertiary,
                modifier = Modifier.padding(AppSpacing.marginMobile),
            )
        }
    }
}

/** 기간별 "받음" 체크와 미정산 멤버 요청 (product-spec 4-9) */
@Composable
internal fun SettlementCard(
    content: PartyContent,
    bill: BillContent?,
    enabled: Boolean,
    onAction: (PartyAction) -> Unit,
) {
    SubCalCard {
        if (bill == null) {
            SectionTitle(stringResource(R.string.party_settlement_title_empty))
            Text(
                text = stringResource(R.string.party_settlement_empty),
                style = AppThemeTokens.typography.bodySm,
                color = AppThemeTokens.colors.textSecondary,
                modifier =
                    Modifier.padding(
                        start = AppSpacing.marginMobile,
                        end = AppSpacing.marginMobile,
                        bottom = AppSpacing.lg,
                    ),
            )
            return@SubCalCard
        }
        Row(
            modifier =
                Modifier.padding(
                    start = AppSpacing.marginMobile,
                    end = AppSpacing.marginMobile,
                    top = AppSpacing.lg,
                ),
            verticalAlignment = Alignment.CenterVertically,
        ) {
            Text(
                text = stringResource(R.string.party_settlement_title, bill.month.monthValue),
                style = AppThemeTokens.typography.headlineSm,
                color = AppThemeTokens.colors.textPrimary,
                modifier = Modifier.weight(1f),
            )
            SubCalBadge(
                text = stringResource(R.string.party_settlement_progress, bill.settledCount, bill.members.size),
                tone = if (bill.unsettled.isEmpty()) SubCalBadgeTone.SUCCESS else SubCalBadgeTone.SOON,
            )
        }
        Text(
            text =
                stringResource(
                    R.string.party_settlement_payment,
                    bill.firstPaymentDate.monthValue,
                    bill.firstPaymentDate.dayOfMonth,
                    formatMoney(bill.total),
                ),
            style = AppThemeTokens.typography.bodySm.copy(fontFeatureSettings = "tnum"),
            color = AppThemeTokens.colors.textSecondary,
            modifier = Modifier.padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.xs),
        )
        if (content.bills.size > 1) {
            Row(
                modifier =
                    Modifier
                        .horizontalScroll(rememberScrollState())
                        .padding(horizontal = AppSpacing.marginMobile, vertical = AppSpacing.sm),
                horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
            ) {
                content.bills.forEach { item ->
                    SubCalFilterChip(
                        text = stringResource(R.string.party_period_chip, item.month.monthValue),
                        selected = item.month == bill.month,
                        onClick = { onAction(PartyAction.PeriodSelected(item.month)) },
                    )
                }
            }
        }
        bill.members.forEach { member ->
            SubCalDivider()
            SettlementRow(member = member, enabled = enabled, onAction = onAction)
        }
    }
}

@Composable
private fun SettlementRow(
    member: MemberSettlement,
    enabled: Boolean,
    onAction: (PartyAction) -> Unit,
) {
    SubCalListItem(
        title = member.name,
        subtitle =
            if (member.settled) {
                stringResource(R.string.party_settled)
            } else {
                stringResource(R.string.party_unsettled, formatMoney(member.share))
            },
        showChevron = false,
        trailing = {
            if (!member.settled) {
                SubCalOutlineButton(
                    text = stringResource(R.string.party_request),
                    onClick = { onAction(PartyAction.RequestClicked(member.id)) },
                    size = SubCalButtonSize.SMALL,
                    modifier = Modifier.testTag("request_${member.id}"),
                )
            }
            val label = stringResource(R.string.party_settled_check, member.name)
            SubCalCheckbox(
                checked = member.settled,
                onCheckedChange = { onAction(PartyAction.SettledToggled(member.id, it)) },
                enabled = enabled,
                modifier =
                    Modifier
                        .semantics { contentDescription = label }
                        .testTag("settled_${member.id}"),
            )
        },
    )
}

@Composable
private fun SectionTitle(text: String) {
    Text(
        text = text,
        style = AppThemeTokens.typography.headlineSm,
        color = AppThemeTokens.colors.textPrimary,
        modifier =
            Modifier.padding(
                start = AppSpacing.marginMobile,
                end = AppSpacing.marginMobile,
                top = AppSpacing.lg,
            ),
    )
}

internal fun splitLabel(mode: PartySplitMode): Int =
    when (mode) {
        PartySplitMode.EQUAL -> R.string.party_split_equal
        PartySplitMode.CUSTOM -> R.string.party_split_custom
    }

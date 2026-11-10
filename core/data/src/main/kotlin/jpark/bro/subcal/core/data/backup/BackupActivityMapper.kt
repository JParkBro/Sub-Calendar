package jpark.bro.subcal.core.data.backup

import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.UsageLog
import java.time.LocalDate
import java.time.YearMonth

// 3차 행(사용 기록 · 파티) 백업 변환. 불러오기에서 날짜 · 달이 잘못되면 예외를 던진다

internal fun UsageLog.toBackup(): BackupUsageLog = BackupUsageLog(id, subscriptionId, usedOn.toString(), createdAt)

internal fun PartyMember.toBackup(): BackupPartyMember =
    BackupPartyMember(id, subscriptionId, name, shareAmountMinor, sortOrder, createdAt, updatedAt)

internal fun PartySettlement.toBackup(): BackupPartySettlement =
    BackupPartySettlement(partyMemberId, period.toString(), settledAt)

internal fun BackupUsageLog.toModel(): UsageLog = UsageLog(id, subscriptionId, LocalDate.parse(usedOn), createdAt)

internal fun BackupPartyMember.toModel(): PartyMember =
    PartyMember(id, subscriptionId, name, shareAmountMinor, sortOrder, createdAt, updatedAt)

internal fun BackupPartySettlement.toModel(): PartySettlement =
    PartySettlement(partyMemberId, YearMonth.parse(period), settledAt)

package jpark.bro.subcal.core.data.mapper

import jpark.bro.subcal.core.database.entity.PartyMemberEntity
import jpark.bro.subcal.core.database.entity.PartySettlementEntity
import jpark.bro.subcal.core.database.entity.UsageLogEntity
import jpark.bro.subcal.core.model.PartyMember
import jpark.bro.subcal.core.model.PartySettlement
import jpark.bro.subcal.core.model.UsageLog
import java.time.LocalDate
import java.time.YearMonth

internal fun UsageLogEntity.toModel(): UsageLog = UsageLog(id, subscriptionId, LocalDate.parse(usedOn), createdAt)

/** 모델에는 삭제된 기록이 없으므로 deleted_at은 항상 null이다 */
internal fun UsageLog.toEntity(): UsageLogEntity =
    UsageLogEntity(id, subscriptionId, usedOn.toString(), createdAt, null)

internal fun PartyMemberEntity.toModel(): PartyMember =
    PartyMember(id, subscriptionId, name, shareAmountMinor, sortOrder, createdAt, updatedAt)

/** 모델에는 삭제된 멤버가 없으므로 deleted_at은 항상 null이다 */
internal fun PartyMember.toEntity(): PartyMemberEntity =
    PartyMemberEntity(id, subscriptionId, name, shareAmountMinor, sortOrder, createdAt, updatedAt, null)

internal fun PartySettlementEntity.toModel(): PartySettlement =
    PartySettlement(partyMemberId, YearMonth.parse(period), settledAt)

internal fun PartySettlement.toEntity(): PartySettlementEntity =
    PartySettlementEntity(partyMemberId, period.toString(), settledAt)

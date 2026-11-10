package jpark.bro.subcal.core.data.backup

import kotlinx.serialization.Serializable

/**
 * 백업 JSON 형식 v1 (data-model-erd 6장). 키는 camelCase, 날짜는 yyyy-MM-dd, 시각은 epoch millis,
 * 열거형은 대문자 이름. 스키마가 바뀌면 version을 올리고 BackupV2를 추가해 V1 → V2로 변환한다.
 */
@Serializable
internal data class BackupV1(
    val format: String = BACKUP_FORMAT,
    val version: Int = BACKUP_VERSION,
    val exportedAt: Long,
    val appVersion: String,
    val settings: BackupSettings,
    val subscriptions: List<BackupSubscription>,
    val priceChanges: List<BackupPriceChange>,
    val pauses: List<BackupPause>,
    /** 3차 테이블. 3차 전에 만든 v1 파일에는 비어 있다 */
    val usageLogs: List<BackupUsageLog> = emptyList(),
    val partyMembers: List<BackupPartyMember> = emptyList(),
    val partySettlements: List<BackupPartySettlement> = emptyList(),
)

/** 형식 · 버전만 먼저 읽는다 */
@Serializable
internal data class BackupHeader(
    val format: String? = null,
    val version: Int? = null,
)

/** data-model-erd 4장에서 "백업 JSON ✓"인 설정 */
@Serializable
internal data class BackupSettings(
    val themeMode: String,
    val notifyPaymentEnabled: Boolean,
    val notifyDefaultDaysBefore: Int,
    val notifyTrialEnabled: Boolean,
    val notifyPartyEnabled: Boolean,
    val notifyMonthlySummaryEnabled: Boolean,
    /** HH:mm */
    val notifyTime: String,
    val monthStartDay: Int,
    val usdKrwRate: Long,
    val defaultPaymentMethod: String? = null,
)

@Serializable
internal data class BackupSubscription(
    val id: String,
    val presetId: String? = null,
    val name: String,
    val planName: String? = null,
    val category: String,
    val amountMinor: Long,
    val currency: String,
    val cycleUnit: String,
    val cycleInterval: Int,
    val firstBillingDate: String,
    val trialStartedOn: String? = null,
    val trialOutcome: String,
    val status: String,
    val cancelledOn: String? = null,
    val paymentMethod: String? = null,
    val reminderMode: String,
    val reminderDaysBefore: Int? = null,
    val colorKey: String,
    val cancelUrl: String? = null,
    val memo: String? = null,
    val considerCancel: Boolean = false,
    val trackUsage: Boolean = false,
    val partySplitMode: String,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
internal data class BackupPriceChange(
    val id: String,
    val subscriptionId: String,
    val effectiveFrom: String,
    val amountMinor: Long,
    val createdAt: Long,
)

@Serializable
internal data class BackupPause(
    val id: String,
    val subscriptionId: String,
    val startDate: String,
    val endDate: String? = null,
    val createdAt: Long,
)

@Serializable
internal data class BackupUsageLog(
    val id: String,
    val subscriptionId: String,
    val usedOn: String,
    val createdAt: Long,
)

@Serializable
internal data class BackupPartyMember(
    val id: String,
    val subscriptionId: String,
    val name: String,
    val shareAmountMinor: Long? = null,
    val sortOrder: Int,
    val createdAt: Long,
    val updatedAt: Long,
)

@Serializable
internal data class BackupPartySettlement(
    val partyMemberId: String,
    /** yyyy-MM */
    val period: String,
    val settledAt: Long,
)

internal const val BACKUP_FORMAT = "subcal-backup"
internal const val BACKUP_VERSION = 1

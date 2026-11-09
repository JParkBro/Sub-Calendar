package jpark.bro.subcal.core.database.entity

import androidx.room.ColumnInfo
import androidx.room.Entity
import androidx.room.Index
import androidx.room.PrimaryKey

/** 구독의 결제 규칙 (data-model-erd 2장). 날짜는 yyyy-MM-dd, 시각은 epoch millis */
@Entity(
    tableName = "subscription",
    indices = [Index("status"), Index("deleted_at")],
)
data class SubscriptionEntity(
    @PrimaryKey val id: String,
    @ColumnInfo(name = "preset_id") val presetId: String?,
    val name: String,
    @ColumnInfo(name = "plan_name") val planName: String?,
    /** VIDEO | MUSIC | SHOPPING | CLOUD | AI | READING | GAME | ETC */
    val category: String,
    @ColumnInfo(name = "amount_minor") val amountMinor: Long,
    /** KRW | USD */
    val currency: String,
    /** DAY | WEEK | MONTH | YEAR */
    @ColumnInfo(name = "cycle_unit") val cycleUnit: String,
    @ColumnInfo(name = "cycle_interval") val cycleInterval: Int,
    @ColumnInfo(name = "first_billing_date") val firstBillingDate: String,
    @ColumnInfo(name = "trial_started_on") val trialStartedOn: String?,
    /** NONE | PENDING | KEPT | CANCELLED */
    @ColumnInfo(name = "trial_outcome") val trialOutcome: String,
    /** ACTIVE | PAUSED | CANCELLED */
    val status: String,
    @ColumnInfo(name = "cancelled_on") val cancelledOn: String?,
    @ColumnInfo(name = "payment_method") val paymentMethod: String?,
    /** DEFAULT | OFF | CUSTOM */
    @ColumnInfo(name = "reminder_mode") val reminderMode: String,
    @ColumnInfo(name = "reminder_days_before") val reminderDaysBefore: Int?,
    @ColumnInfo(name = "color_key") val colorKey: String,
    @ColumnInfo(name = "cancel_url") val cancelUrl: String?,
    val memo: String?,
    @ColumnInfo(name = "consider_cancel") val considerCancel: Boolean,
    @ColumnInfo(name = "track_usage") val trackUsage: Boolean,
    /** EQUAL | CUSTOM */
    @ColumnInfo(name = "party_split_mode") val partySplitMode: String,
    @ColumnInfo(name = "created_at") val createdAt: Long,
    @ColumnInfo(name = "updated_at") val updatedAt: Long,
    @ColumnInfo(name = "deleted_at") val deletedAt: Long?,
)

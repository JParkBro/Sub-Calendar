package jpark.bro.subcal.core.data.backup

import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.AppThemeMode
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.CycleUnit
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.model.Pause
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.LocalDate
import java.time.LocalTime

// 내보내기: 모델 → 백업

internal fun Subscription.toBackup(): BackupSubscription =
    BackupSubscription(
        id = id,
        presetId = presetId,
        name = name,
        planName = planName,
        category = category.name,
        amountMinor = amount.amountMinor,
        currency = amount.currency.name,
        cycleUnit = cycle.unit.name,
        cycleInterval = cycle.interval,
        firstBillingDate = firstBillingDate.toString(),
        trialStartedOn = trialStartedOn?.toString(),
        trialOutcome = trialOutcome.name,
        status = status.name,
        cancelledOn = cancelledOn?.toString(),
        paymentMethod = paymentMethod,
        reminderMode = reminderMode.name,
        reminderDaysBefore = reminderDaysBefore,
        colorKey = colorKey,
        cancelUrl = cancelUrl,
        memo = memo,
        considerCancel = considerCancel,
        trackUsage = trackUsage,
        partySplitMode = partySplitMode.name,
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun PriceChange.toBackup(): BackupPriceChange =
    BackupPriceChange(id, subscriptionId, effectiveFrom.toString(), amountMinor, createdAt)

internal fun Pause.toBackup(): BackupPause =
    BackupPause(id, subscriptionId, startDate.toString(), endDate?.toString(), createdAt)

internal fun backupSettings(
    app: AppSettings,
    notification: NotificationSettings,
): BackupSettings =
    BackupSettings(
        themeMode = app.themeMode.name,
        notifyPaymentEnabled = notification.paymentEnabled,
        notifyDefaultDaysBefore = notification.defaultDaysBefore,
        notifyTrialEnabled = notification.trialEnabled,
        notifyPartyEnabled = notification.partyEnabled,
        notifyMonthlySummaryEnabled = notification.monthlySummaryEnabled,
        notifyTime = notification.time.toString(),
        monthStartDay = app.monthStartDay,
        usdKrwRate = app.usdKrwRate,
        defaultPaymentMethod = app.defaultPaymentMethod,
    )

// 불러오기: 백업 → 모델. 열거형 · 날짜 · 주기가 잘못되면 예외를 던진다

internal fun BackupSubscription.toModel(): Subscription =
    Subscription(
        id = id,
        presetId = presetId,
        name = name,
        planName = planName,
        category = Category.valueOf(category),
        amount = Money(amountMinor, Currency.valueOf(currency)),
        cycle = BillingCycle(CycleUnit.valueOf(cycleUnit), cycleInterval),
        firstBillingDate = LocalDate.parse(firstBillingDate),
        trialStartedOn = trialStartedOn?.let(LocalDate::parse),
        trialOutcome = TrialOutcome.valueOf(trialOutcome),
        status = SubscriptionStatus.valueOf(status),
        cancelledOn = cancelledOn?.let(LocalDate::parse),
        paymentMethod = paymentMethod,
        reminderMode = ReminderMode.valueOf(reminderMode),
        reminderDaysBefore = reminderDaysBefore,
        colorKey = colorKey,
        cancelUrl = cancelUrl,
        memo = memo,
        considerCancel = considerCancel,
        trackUsage = trackUsage,
        partySplitMode = PartySplitMode.valueOf(partySplitMode),
        createdAt = createdAt,
        updatedAt = updatedAt,
    )

internal fun BackupPriceChange.toModel(): PriceChange =
    PriceChange(id, subscriptionId, LocalDate.parse(effectiveFrom), amountMinor, createdAt)

internal fun BackupPause.toModel(): Pause =
    Pause(id, subscriptionId, LocalDate.parse(startDate), endDate?.let(LocalDate::parse), createdAt)

internal fun BackupSettings.toAppSettings(): AppSettings =
    AppSettings(
        themeMode = AppThemeMode.valueOf(themeMode),
        monthStartDay = monthStartDay,
        usdKrwRate = usdKrwRate,
        defaultPaymentMethod = defaultPaymentMethod,
    )

internal fun BackupSettings.toNotificationSettings(): NotificationSettings =
    NotificationSettings(
        paymentEnabled = notifyPaymentEnabled,
        defaultDaysBefore = notifyDefaultDaysBefore,
        trialEnabled = notifyTrialEnabled,
        partyEnabled = notifyPartyEnabled,
        monthlySummaryEnabled = notifyMonthlySummaryEnabled,
        time = LocalTime.parse(notifyTime),
    )

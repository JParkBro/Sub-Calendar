package jpark.bro.subcal.core.domain.trial

import jpark.bro.subcal.core.domain.billing.isPendingTrial
import jpark.bro.subcal.core.model.SubscriptionRecord
import jpark.bro.subcal.core.model.SubscriptionStatus
import jpark.bro.subcal.core.model.TrialOutcome
import java.time.LocalDate
import java.time.temporal.ChronoUnit

/** 홈 "종료 임박" 카드에 넣는 체험: 첫 결제일까지 이 일수 이내 (product-spec 4-1) */
const val TRIAL_ENDING_SOON_DAYS = 3L

/** 무료체험 화면의 "지난 체험" 기간 (product-spec 4-7) */
const val ENDED_TRIAL_DAYS = 90L

/** 진행 중인 체험: 결과를 고르기 전이고 첫 결제일(당일 포함)이 지나지 않은 것, 종료 가까운 순 → 이름순 */
fun activeTrials(
    records: List<SubscriptionRecord>,
    today: LocalDate,
): List<SubscriptionRecord> =
    records
        .filter { it.subscription.isPendingTrial(today) }
        .sortedWith(compareBy({ it.subscription.firstBillingDate }, { it.subscription.name }))

/** 체험 종료가 [TRIAL_ENDING_SOON_DAYS]일 이내인 진행 중 체험 */
fun trialsEndingSoon(
    records: List<SubscriptionRecord>,
    today: LocalDate,
): List<SubscriptionRecord> =
    activeTrials(records, today).filter {
        ChronoUnit.DAYS.between(today, it.subscription.firstBillingDate) <= TRIAL_ENDING_SOON_DAYS
    }

/**
 * "결제됐나요?" (product-spec 2-7): 첫 결제일이 지났는데(다음 날부터) 결과를 고르지 않은 체험.
 * 계산에서는 이미 KEPT로 간주한다. 해지한 구독은 뺀다.
 */
fun unresolvedTrials(
    records: List<SubscriptionRecord>,
    today: LocalDate,
): List<SubscriptionRecord> =
    records
        .filter {
            it.subscription.trialOutcome == TrialOutcome.PENDING &&
                it.subscription.status != SubscriptionStatus.CANCELLED &&
                today.isAfter(it.subscription.firstBillingDate)
        }.sortedWith(compareBy({ it.subscription.firstBillingDate }, { it.subscription.name }))

/** 지난 체험: 결과를 고른 체험 중 체험이 끝난 날이 최근 [ENDED_TRIAL_DAYS]일 이내, 최근 순 */
fun endedTrials(
    records: List<SubscriptionRecord>,
    today: LocalDate,
): List<SubscriptionRecord> =
    records
        .filter {
            val outcome = it.subscription.trialOutcome
            it.subscription.trialStartedOn != null &&
                (outcome == TrialOutcome.KEPT || outcome == TrialOutcome.CANCELLED) &&
                ChronoUnit.DAYS.between(it.trialEndedOn(), today) in 0..ENDED_TRIAL_DAYS
        }.sortedWith(compareByDescending<SubscriptionRecord> { it.trialEndedOn() }.thenBy { it.subscription.name })

/** 체험이 끝난 날: 첫 결제일 전에 해지했으면 해지일, 아니면 첫 결제일 */
fun SubscriptionRecord.trialEndedOn(): LocalDate {
    val cancelledOn = subscription.cancelledOn
    return if (cancelledOn != null && cancelledOn.isBefore(subscription.firstBillingDate)) {
        cancelledOn
    } else {
        subscription.firstBillingDate
    }
}

/** 진행 막대 (product-spec 2-7): (오늘 − 시작일) ÷ (첫 결제일 − 시작일), 0 ~ 1 */
fun SubscriptionRecord.trialProgress(today: LocalDate): Float {
    val start = subscription.trialStartedOn ?: return 0f
    val total = ChronoUnit.DAYS.between(start, subscription.firstBillingDate)
    return if (total <= 0) 1f else (ChronoUnit.DAYS.between(start, today).toFloat() / total).coerceIn(0f, 1f)
}

/**
 * "해지했어요"의 해지일 (product-spec 2-7): 오늘. 첫 결제일이 지났으면 첫 결제일
 * (첫 결제 전에 해지한 것이라 해지일 당일 결제는 없다)
 */
fun SubscriptionRecord.trialCancelDate(today: LocalDate): LocalDate =
    if (today.isAfter(subscription.firstBillingDate)) subscription.firstBillingDate else today

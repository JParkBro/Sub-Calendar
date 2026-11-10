package jpark.bro.subcal.core.navigation

/** 알림 · 위젯이 앱을 열 때 Intent에 넣는 값. app이 받아 백스택을 만든다 (module-map 4-3) */
object AppIntents {
    /** 이 구독의 상세를 연다: [HomeKey, SubscriptionDetailKey] */
    const val EXTRA_OPEN_SUBSCRIPTION_ID = "jpark.bro.subcal.extra.OPEN_SUBSCRIPTION_ID"

    /** true면 무료체험 화면을 연다: [HomeKey, TrialKey] */
    const val EXTRA_OPEN_TRIAL = "jpark.bro.subcal.extra.OPEN_TRIAL"

    /** 이 구독의 파티 정산을 연다: [HomeKey, PartyKey] */
    const val EXTRA_OPEN_PARTY_SUBSCRIPTION_ID = "jpark.bro.subcal.extra.OPEN_PARTY_SUBSCRIPTION_ID"
}

package jpark.bro.subcal.feature.subscription.impl.detail

internal sealed interface SubscriptionDetailEvent {
    /** 보던 구독이 삭제됐을 때 */
    data object NavigateBack : SubscriptionDetailEvent

    data class OpenEdit(
        val id: String,
    ) : SubscriptionDetailEvent

    data class OpenHistory(
        val id: String,
    ) : SubscriptionDetailEvent

    /** 해지 페이지를 브라우저로 */
    data class OpenCancelPage(
        val url: String,
    ) : SubscriptionDetailEvent

    /** 일시정지 · 다시 시작 · 해지 · 해지 취소 저장 실패 */
    data object ActionFailed : SubscriptionDetailEvent
}

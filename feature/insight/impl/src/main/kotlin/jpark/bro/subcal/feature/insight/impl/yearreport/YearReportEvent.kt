package jpark.bro.subcal.feature.insight.impl.yearreport

internal sealed interface YearReportEvent {
    data object NavigateBack : YearReportEvent

    /** 보고 있는 카드를 1080×1920 이미지로 그려 공유 시트를 연다 */
    data object ShareImage : YearReportEvent
}

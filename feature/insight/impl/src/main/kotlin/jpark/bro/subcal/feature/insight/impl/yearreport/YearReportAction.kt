package jpark.bro.subcal.feature.insight.impl.yearreport

internal sealed interface YearReportAction {
    data object BackClicked : YearReportAction

    /** 지난해 · 올해 칩 */
    data class YearSelected(
        val year: Int,
    ) : YearReportAction

    /** 스와이프로 보고 있는 카드가 바뀌었다 */
    data class PageChanged(
        val page: YearReportPage,
    ) : YearReportAction

    /** "이미지로 공유하기" */
    data object ShareClicked : YearReportAction
}

package jpark.bro.subcal.feature.home.impl

import androidx.compose.material3.Icon
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.designsystem.AppThemeTokens
import jpark.bro.subcal.core.designsystem.component.SubCalCard
import jpark.bro.subcal.core.designsystem.component.SubCalListItem
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.core.ui.formatMoney
import jpark.bro.subcal.core.designsystem.R as DesignR

/** 12월 1일 ~ 1월 31일 "n년 구독 결산" 배너 → 연말 리포트 (product-spec 4-11) */
@Composable
internal fun YearReportBannerCard(
    banner: YearReportBanner,
    onAction: (HomeAction) -> Unit,
) {
    SubCalCard(
        onClick = { onAction(HomeAction.YearReportBannerClicked) },
        modifier = Modifier.testTag("year_report_banner"),
    ) {
        SubCalListItem(
            title = stringResource(R.string.home_year_report_title, banner.year),
            subtitle =
                stringResource(
                    R.string.home_year_report_message,
                    banner.year,
                    formatMoney(Money(banner.totalKrw, Currency.KRW)),
                ),
            onClick = { onAction(HomeAction.YearReportBannerClicked) },
            leading = {
                Icon(
                    painter = painterResource(DesignR.drawable.ic_auto_awesome),
                    contentDescription = null,
                    tint = AppThemeTokens.colors.primary,
                )
            },
        )
    }
}

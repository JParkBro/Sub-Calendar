package jpark.bro.subcal.core.ui

import androidx.annotation.StringRes
import androidx.compose.runtime.Composable
import androidx.compose.ui.res.stringResource
import jpark.bro.subcal.core.model.BillingCycle
import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.CycleUnit
import java.time.LocalDate
import java.time.format.TextStyle
import java.util.Locale

/** 카테고리 표시 이름 (product-spec 2-11) */
@Composable
fun categoryLabel(category: Category): String = stringResource(category.labelRes)

@get:StringRes
private val Category.labelRes: Int
    get() =
        when (this) {
            Category.VIDEO -> R.string.common_category_video
            Category.MUSIC -> R.string.common_category_music
            Category.SHOPPING -> R.string.common_category_shopping
            Category.CLOUD -> R.string.common_category_cloud
            Category.AI -> R.string.common_category_ai
            Category.READING -> R.string.common_category_reading
            Category.GAME -> R.string.common_category_game
            Category.ETC -> R.string.common_category_etc
        }

/**
 * 결제 주기와 결제일 (product-spec 2-2): 매월 14일 · 매년 11월 9일 · 매주 금요일.
 * 간격이 2 이상이면 날짜 없이 3개월마다 · 2주마다.
 */
@Composable
fun billingCycleLabel(
    cycle: BillingCycle,
    firstBillingDate: LocalDate,
): String =
    when {
        cycle.interval == 1 && cycle.unit == CycleUnit.WEEK -> {
            stringResource(
                R.string.common_cycle_weekly,
                firstBillingDate.dayOfWeek.getDisplayName(TextStyle.FULL, Locale.KOREAN),
            )
        }

        cycle.interval == 1 && cycle.unit == CycleUnit.MONTH -> {
            stringResource(R.string.common_cycle_monthly, firstBillingDate.dayOfMonth)
        }

        cycle.interval == 1 && cycle.unit == CycleUnit.YEAR -> {
            stringResource(R.string.common_cycle_yearly, firstBillingDate.monthValue, firstBillingDate.dayOfMonth)
        }

        else -> {
            stringResource(cycle.unit.everyRes, cycle.interval)
        }
    }

@get:StringRes
private val CycleUnit.everyRes: Int
    get() =
        when (this) {
            CycleUnit.DAY -> R.string.common_cycle_every_days
            CycleUnit.WEEK -> R.string.common_cycle_every_weeks
            CycleUnit.MONTH -> R.string.common_cycle_every_months
            CycleUnit.YEAR -> R.string.common_cycle_every_years
        }

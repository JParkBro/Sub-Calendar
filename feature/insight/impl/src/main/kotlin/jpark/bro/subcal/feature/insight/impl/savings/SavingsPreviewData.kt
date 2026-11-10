package jpark.bro.subcal.feature.insight.impl.savings

import jpark.bro.subcal.core.model.Category
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import jpark.bro.subcal.feature.insight.api.SavingsTab
import java.time.LocalDate

/** product-spec 9장 샘플 (2026.10.30) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewSavingsState(tab: SavingsTab): SavingsState =
    SavingsState(
        isLoading = false,
        tab = tab,
        considerItems =
            listOf(
                ConsiderItem("tving", "티빙", "ORANGE", 10_900, duplicateWith = listOf("넷플릭스 프리미엄", "유튜브 프리미엄")),
                ConsiderItem("melon", "멜론", "GREEN", 10_900, duplicateWith = listOf("스포티파이")),
            ),
        considerAnnualKrw = 261_600,
        duplicates =
            listOf(
                DuplicateItem(Category.VIDEO, listOf("넷플릭스 프리미엄", "유튜브 프리미엄", "티빙"), 42_800),
                DuplicateItem(Category.MUSIC, listOf("멜론", "스포티파이"), 21_800),
            ),
        annualOptions =
            listOf(
                AnnualOptionItem(
                    "youtube_premium",
                    "유튜브 프리미엄",
                    Money(14_900, Currency.KRW),
                    Money(149_000, Currency.KRW),
                    29_800,
                ),
            ),
        savedTotalKrw = 15_800,
        savedItems = listOf(SavedListItem("watcha", "왓챠", "PINK", LocalDate.of(2026, 8, 30), 15_800)),
    )

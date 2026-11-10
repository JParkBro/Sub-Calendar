package jpark.bro.subcal.feature.trial.impl

import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money
import java.time.LocalDate

/** product-spec 9장 샘플 (2026.10.30) */
@Suppress("MagicNumber") // 샘플 데이터
internal fun previewTrialState(): TrialState =
    TrialState(
        isLoading = false,
        active =
            listOf(
                ActiveTrial(
                    "disney_plus",
                    "디즈니+ 프리미엄",
                    "BLACK",
                    LocalDate.of(2026, 10, 31),
                    Money(13_900, Currency.KRW),
                    1,
                    29f / 30f,
                    30,
                    29,
                ),
                ActiveTrial(
                    "millie",
                    "밀리의 서재",
                    "AMBER",
                    LocalDate.of(2026, 11, 12),
                    Money(9_900, Currency.KRW),
                    13,
                    17f / 30f,
                    30,
                    17,
                ),
            ),
        ended =
            listOf(
                EndedTrial("spotify", "스포티파이", "GREEN", LocalDate.of(2026, 10, 30), cancelled = false, savedKrw = 0),
                EndedTrial("watcha", "왓챠", "PINK", LocalDate.of(2026, 8, 30), cancelled = true, savedKrw = 15_800),
            ),
    )

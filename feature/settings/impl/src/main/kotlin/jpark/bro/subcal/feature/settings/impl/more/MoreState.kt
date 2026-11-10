package jpark.bro.subcal.feature.settings.impl.more

import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.BackupSummary
import jpark.bro.subcal.core.model.NotificationSettings

/**
 * 더보기 (product-spec 6장). 개인용 단계에서는 로컬 메뉴만 있고,
 * 아직 만들지 않은 메뉴(2 · 3차, 출시 단계)는 숨긴다 (비활성 표시하지 않음).
 */
internal data class MoreState(
    val isLoading: Boolean = true,
    /** 알림 메뉴 설명: "결제 1일 전 · 오전 9:00" 또는 "꺼짐" */
    val notification: NotificationSettings = NotificationSettings(),
    val appSettings: AppSettings = AppSettings(),
    val appVersion: String = "",
    val sheet: MoreSheet? = null,
    /** 환율 시트 입력: 숫자만 */
    val usdKrwRateInput: String = "",
    /** 기본 결제 수단 시트 입력. 비우고 저장하면 지운다 */
    val paymentMethodInput: String = "",
    /** 백업을 만들거나 불러오는 중: 데이터 메뉴를 막는다 */
    val isBackupBusy: Boolean = false,
    /** 불러오기 확인 시트에 보여줄 백업 요약 */
    val restorePreview: BackupSummary? = null,
) {
    /** 0보다 큰 원 단위 숫자여야 저장할 수 있다 (product-spec 2-4) */
    val canSaveUsdKrwRate: Boolean get() = (usdKrwRateInput.toLongOrNull() ?: 0) > 0
}

internal enum class MoreSheet {
    MONTH_START_DAY,
    USD_KRW_RATE,
    PAYMENT_METHOD,
    THEME_MODE,
    RESTORE_CONFIRM,
}

/** 환율 입력 최대 자릿수 (999,999원) */
internal const val MAX_RATE_DIGITS = 6

/** 결제 수단 이름 최대 길이 (구독 입력과 같게 짧은 이름) */
internal const val MAX_PAYMENT_METHOD_LENGTH = 20

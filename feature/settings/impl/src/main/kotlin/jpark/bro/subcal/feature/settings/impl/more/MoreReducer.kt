package jpark.bro.subcal.feature.settings.impl.more

/** 시트를 열 때 입력란을 지금 저장된 값으로 채운다 */
internal fun MoreState.openSheet(action: MoreAction.OpenSheet): MoreState =
    when (action) {
        MoreAction.MonthStartDayClicked -> {
            copy(sheet = MoreSheet.MONTH_START_DAY)
        }

        MoreAction.UsdKrwRateClicked -> {
            copy(sheet = MoreSheet.USD_KRW_RATE, usdKrwRateInput = appSettings.usdKrwRate.toString())
        }

        MoreAction.PaymentMethodClicked -> {
            copy(sheet = MoreSheet.PAYMENT_METHOD, paymentMethodInput = appSettings.defaultPaymentMethod.orEmpty())
        }

        MoreAction.ThemeModeClicked -> {
            copy(sheet = MoreSheet.THEME_MODE)
        }
    }

/** 환율은 숫자만(앞의 0 제외, 6자리까지), 결제 수단은 길이만 자른다 */
internal fun MoreState.reduce(action: MoreAction.SheetInput): MoreState =
    when (action) {
        is MoreAction.UsdKrwRateInputChanged -> {
            copy(
                usdKrwRateInput =
                    action.input
                        .filter(Char::isDigit)
                        .trimStart('0')
                        .take(MAX_RATE_DIGITS),
            )
        }

        is MoreAction.PaymentMethodInputChanged -> {
            copy(paymentMethodInput = action.input.take(MAX_PAYMENT_METHOD_LENGTH))
        }
    }

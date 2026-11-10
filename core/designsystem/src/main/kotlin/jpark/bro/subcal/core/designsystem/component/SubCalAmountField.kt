package jpark.bro.subcal.core.designsystem.component

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.AnnotatedString
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.text.input.OffsetMapping
import androidx.compose.ui.text.input.TransformedText
import androidx.compose.ui.text.input.VisualTransformation
import androidx.compose.ui.unit.dp
import jpark.bro.subcal.core.designsystem.AppSpacing
import jpark.bro.subcal.core.designsystem.R
import jpark.bro.subcal.core.designsystem.preview.ComponentPreviews
import jpark.bro.subcal.core.designsystem.preview.SubCalPreviewSurface
import jpark.bro.subcal.core.model.Currency
import jpark.bro.subcal.core.model.Money

/** Long 범위 안에서 다룰 수 있도록 정수부 자릿수를 제한한다 */
private const val MAX_INTEGER_DIGITS = 12
private const val MAX_FRACTION_DIGITS = 2
private const val GROUP_SIZE = 3
private const val CENTS_PER_DOLLAR = 100L

/**
 * 금액 입력: 숫자 키패드 · 천 단위 쉼표 · 통화 선택 (theme-guide.md 7장).
 * [value]는 쉼표 없는 입력 원문이고, 항상 [sanitizeAmountInput]을 거친 값이 [onValueChange]로 나간다.
 */
@Composable
fun SubCalAmountField(
    value: String,
    currency: Currency,
    onValueChange: (String) -> Unit,
    onCurrencyChange: ((Currency) -> Unit)?,
    modifier: Modifier = Modifier,
    placeholder: String? = null,
) {
    val currencies = Currency.entries
    Row(
        modifier = modifier,
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(AppSpacing.sm),
    ) {
        SubCalTextField(
            value = value,
            onValueChange = { onValueChange(sanitizeAmountInput(it, currency)) },
            modifier = Modifier.weight(1f),
            placeholder = placeholder,
            keyboardOptions =
                KeyboardOptions(
                    keyboardType =
                        when (currency) {
                            Currency.KRW -> KeyboardType.Number
                            Currency.USD -> KeyboardType.Decimal
                        },
                ),
            visualTransformation = ThousandsSeparatorTransformation,
        )
        // 통화를 바꿀 수 없으면(파티 몫 등) 통화 선택을 숨긴다
        if (onCurrencyChange == null) return@Row
        SubCalSegmentedControl(
            segments =
                currencies.map {
                    SubCalSegment(
                        stringResource(
                            when (it) {
                                Currency.KRW -> R.string.common_currency_krw
                                Currency.USD -> R.string.common_currency_usd
                            },
                        ),
                    )
                },
            selectedIndex = currencies.indexOf(currency),
            onSelect = { index ->
                val selected = currencies[index]
                onCurrencyChange(selected)
                onValueChange(sanitizeAmountInput(value, selected))
            },
            modifier = Modifier.width(120.dp),
        )
    }
}

/** 숫자만 남긴다. USD는 소수점 하나와 소수 둘째 자리까지 허용한다 */
internal fun sanitizeAmountInput(
    input: String,
    currency: Currency,
): String {
    val digits = input.substringBefore('.').filter(Char::isDigit)
    val integer = (if (digits.isEmpty()) "" else digits.trimStart('0').ifEmpty { "0" }).take(MAX_INTEGER_DIGITS)
    if (currency == Currency.KRW || '.' !in input) return integer
    val fraction =
        input
            .substringAfter('.')
            .filter(Char::isDigit)
            .take(MAX_FRACTION_DIGITS)
    return "${integer.ifEmpty { "0" }}.$fraction"
}

/** [SubCalAmountField] 입력 원문을 최소 단위 금액으로 바꾼다. 비었으면 null (product-spec 2-4) */
fun amountInputToMinor(
    input: String,
    currency: Currency,
): Long? {
    if (input.isEmpty()) return null
    val integer = input.substringBefore('.').ifEmpty { "0" }.toLong()
    return when (currency) {
        Currency.KRW -> {
            integer
        }

        Currency.USD -> {
            val cents = input.substringAfter('.', "").padEnd(MAX_FRACTION_DIGITS, '0').toLong()
            integer * CENTS_PER_DOLLAR + cents
        }
    }
}

/** 저장한 금액을 [SubCalAmountField] 입력 원문으로 바꾼다. USD는 센트가 0이면 소수를 붙이지 않는다 */
fun minorToAmountInput(money: Money): String =
    when (money.currency) {
        Currency.KRW -> {
            money.amountMinor.toString()
        }

        Currency.USD -> {
            val cents = money.amountMinor % CENTS_PER_DOLLAR
            val dollars = money.amountMinor / CENTS_PER_DOLLAR
            if (cents == 0L) dollars.toString() else "$dollars.${cents.toString().padStart(MAX_FRACTION_DIGITS, '0')}"
        }
    }

/** 입력 원문은 그대로 두고 화면에만 쉼표를 그린다. 커서 위치를 원문과 맞춘다 */
internal object ThousandsSeparatorTransformation : VisualTransformation {
    override fun filter(text: AnnotatedString): TransformedText {
        val raw = text.text
        val integerLength = raw.indexOf('.').takeIf { it >= 0 } ?: raw.length
        val grouped =
            buildString {
                raw.take(integerLength).forEachIndexed { index, digit ->
                    if (index > 0 && (integerLength - index) % GROUP_SIZE == 0) append(',')
                    append(digit)
                }
                append(raw.drop(integerLength))
            }
        val mapping =
            object : OffsetMapping {
                override fun originalToTransformed(offset: Int): Int {
                    val digits = offset.coerceAtMost(integerLength)
                    val commasAfter = if (digits == 0) 0 else (integerLength - digits) / GROUP_SIZE
                    val commasBefore = if (digits == 0) 0 else (integerLength - 1) / GROUP_SIZE - commasAfter
                    return offset + commasBefore
                }

                override fun transformedToOriginal(offset: Int): Int = grouped.take(offset).count { it != ',' }
            }
        return TransformedText(AnnotatedString(grouped), mapping)
    }
}

@ComponentPreviews
@Composable
private fun SubCalAmountFieldPreview() {
    SubCalPreviewSurface {
        var krw by remember { mutableStateOf("17000") }
        SubCalAmountField(value = krw, currency = Currency.KRW, onValueChange = { krw = it }, onCurrencyChange = {})
        SubCalAmountField(value = "9.99", currency = Currency.USD, onValueChange = {}, onCurrencyChange = {})
        SubCalAmountField(
            value = "",
            currency = Currency.KRW,
            onValueChange = {},
            onCurrencyChange = {},
            placeholder = "금액",
        )
    }
}

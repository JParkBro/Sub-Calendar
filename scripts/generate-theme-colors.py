#!/usr/bin/env python3
"""docs/subcal/assets/theme-tokens.json → core:designsystem ThemeTokens.kt 생성.

사용: python3 scripts/generate-theme-colors.py
값을 바꿀 때는 JSON만 고치고 이 스크립트를 다시 실행한다 (theme-guide.md).
"""
import json
import re
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent
SRC = ROOT / "docs/subcal/assets/theme-tokens.json"
OUT = ROOT / "core/designsystem/src/main/kotlin/jpark/bro/subcal/core/designsystem/ThemeTokens.kt"


def camel(name):
    return re.sub(r"-(\w)", lambda m: m.group(1).upper(), name)


def argb(value):
    """'#RRGGBB' | '#RRGGBBAA' → 0xAARRGGBB"""
    digits = value.strip().removeprefix("#").upper()
    if len(digits) == 6:
        return f"0xFF{digits}"
    if len(digits) == 8:
        return f"0x{digits[6:]}{digits[:6]}"
    raise ValueError(f"지원하지 않는 색 형식: {value}")


def main():
    data = json.loads(SRC.read_text(encoding="utf-8"))
    indigo = data["indigo"]
    tokens = list(indigo["light"])
    if set(tokens) != set(indigo["dark"]):
        raise ValueError("light · dark 토큰 목록이 다르다")
    out = [
        "// 생성 파일 — 직접 고치지 않는다. 원본: docs/subcal/assets/theme-tokens.json",
        "// 다시 만들기: python3 scripts/generate-theme-colors.py",
        '@file:Suppress("MagicNumber")',
        "",
        "package jpark.bro.subcal.core.designsystem",
        "",
        "import androidx.compose.ui.graphics.Color",
        "",
    ]
    for mode, label in (("light", "라이트"), ("dark", "다크")):
        out.append(f"/** 인디고 · {label} */")
        out.append(f"internal val Indigo{mode.capitalize()} =")
        out.append("    AppColors(")
        out += [f"        {camel(t)} = Color({argb(indigo[mode][t])})," for t in tokens]
        out += ["    )", ""]

    out.append("internal fun palette(dark: Boolean): AppColors = if (dark) IndigoDark else IndigoLight")
    out.append("")
    out.append("/** 테마와 무관한 고정 색 (theme-guide 5장) */")
    out.append("object AppFixedColors {")
    out.append('    private val serviceColors =')
    out.append("        mapOf(")
    out += [f'            "{k}" to Color({argb(v)}),' for k, v in data["service"].items()]
    out.append("        )")
    out.append("")
    out.append("    /** 서비스 아이콘 위 이니셜 */")
    out.append("    val onService = Color(0xFFFFFFFF)")
    out.append("")
    out.append("    /** [colorKey]의 서비스 색. 모르는 키(옛 백업 등)는 BLACK으로 그린다 */")
    out.append('    fun service(colorKey: String): Color = serviceColors[colorKey] ?: serviceColors.getValue("BLACK")')
    out.append("}")

    OUT.write_text("\n".join(out) + "\n", encoding="utf-8")
    print(f"{OUT.relative_to(ROOT)}: 2 palettes × {len(tokens)} tokens, {len(data['service'])} service colors")


if __name__ == "__main__":
    main()

# Parcelable 클래스가 삭제되거나 난독화되지 않도록 유지
-keepclassmembers class * implements android.os.Parcelable {
    public static final android.os.Parcelable$Creator *;
}

# @Keep 애노테이션이 적용된 클래스와 메서드 유지
-keep @androidx.annotation.Keep class *

# Android Data Binding에서 생성된 클래스를 유지
-keep class **.databinding.* { *; }
-keep class **.BR { *; }

# Dagger/Hilt 의존성 주입에 필요한 클래스 유지
-keep class dagger.** { *; }
-keep class javax.inject.** { *; }
-keepclassmembers class ** {
    @javax.inject.* <fields>;
}

# 커스텀 뷰에서 사용하는 생성자를 유지 (XML에서 inflate 시 필요)
-keepclassmembers class * extends android.view.View {
    public <init>(android.content.Context);
    public <init>(android.content.Context, android.util.AttributeSet);
    public <init>(android.content.Context, android.util.AttributeSet, int);
}

# Room 데이터베이스 관련 클래스 유지
-keep class androidx.room.* { *; }
-keep @androidx.room.* class *

# 오픈소스 라이선스(OSS Licenses) 화면 관련 클래스 유지
-keep class com.google.android.gms.oss.licenses.** { *; }

# 로그에서 클래스 이름과 메서드가 난독화되지 않도록 유지
-keepattributes SourceFile, LineNumberTable

# 디버깅 시 난독화를 비활성화하려면 다음 줄 활성화
# -dontobfuscate

# 스택 트레이스에서 라인 번호와 파일명을 유지하려면 다음 줄 활성화
-keepattributes LineNumberTable, SourceFile

# Allow intent actions for external links
-keep class android.net.Uri { *; }
-keep class android.content.Intent { *; }
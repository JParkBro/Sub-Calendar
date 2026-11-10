package jpark.bro.subcal

import android.app.Application
import jpark.bro.subcal.core.data.cleanup.DeletedSubscriptionCleanupScheduler
import jpark.bro.subcal.core.notification.ReminderScheduler
import jpark.bro.subcal.di.appModule
import jpark.bro.subcal.widget.WidgetUpdater
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import org.koin.android.ext.android.get
import org.koin.android.ext.koin.androidContext
import org.koin.android.ext.koin.androidLogger
import org.koin.androidx.workmanager.koin.workManagerFactory
import org.koin.core.context.startKoin
import org.koin.core.logger.Level

class SubCalApplication : Application() {
    /** 앱 프로세스가 살아 있는 동안 도는 작업 (알림 시각 변경 감시) */
    private val applicationScope = CoroutineScope(SupervisorJob() + Dispatchers.Default)

    override fun onCreate() {
        super.onCreate()
        startKoin {
            androidLogger(if (BuildConfig.DEBUG) Level.DEBUG else Level.NONE)
            androidContext(this@SubCalApplication)
            workManagerFactory()
            modules(appModule)
        }
        // 설치 · 업데이트 · 자동 백업 복원 뒤 첫 실행에도 결제 알림이 예약되도록 시작마다 확인한다 (infra.md 2장)
        get<ReminderScheduler>().start(applicationScope)
        get<DeletedSubscriptionCleanupScheduler>().start()
        // 구독 · 설정이 바뀌면 위젯을 다시 그리고 자정 갱신을 예약한다
        get<WidgetUpdater>().start()
    }
}

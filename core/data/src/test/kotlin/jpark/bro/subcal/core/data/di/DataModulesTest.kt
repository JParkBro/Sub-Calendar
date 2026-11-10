package jpark.bro.subcal.core.data.di

import android.content.Context
import androidx.work.WorkerParameters
import jpark.bro.subcal.core.database.di.databaseModule
import jpark.bro.subcal.core.datastore.di.datastoreModule
import org.junit.Test
import org.koin.core.annotation.KoinExperimentalAPI
import org.koin.dsl.module
import org.koin.test.verify.verify

class DataModulesTest {
    @OptIn(KoinExperimentalAPI::class)
    @Test
    fun `데이터 계층 Koin 모듈의 의존성이 해결된다`() {
        // Worker는 WorkManager가 WorkerParameters를 넘겨 만든다
        module { includes(dataModule, databaseModule, datastoreModule) }
            .verify(extraTypes = listOf(Context::class, WorkerParameters::class))
    }
}

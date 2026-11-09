package jpark.bro.subcal.core.datastore

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import jpark.bro.subcal.core.model.AppSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Test
import java.io.IOException

class SettingsDataStoreTest {
    /** 읽기마다 [error]를 던지는 DataStore */
    private class FailingDataStore(
        private val error: Throwable,
    ) : DataStore<Preferences> {
        override val data: Flow<Preferences> = flow { throw error }

        override suspend fun updateData(transform: suspend (t: Preferences) -> Preferences): Preferences = throw error
    }

    @Test
    fun `파일을 읽지 못하면 기본값을 쓴다`() =
        runTest {
            val store = SettingsDataStore(FailingDataStore(IOException("corrupt")))

            assertFalse(store.onboardingDone.first())
            assertEquals(AppSettings(), store.appSettings.first())
            assertFalse(store.billingDateHintPending.first())
        }

    @Test(expected = IllegalStateException::class)
    fun `읽기 오류가 아닌 예외는 그대로 던진다`() =
        runTest {
            SettingsDataStore(FailingDataStore(IllegalStateException("bug"))).onboardingDone.first()
            Unit
        }
}

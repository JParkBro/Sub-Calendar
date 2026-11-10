package jpark.bro.subcal.core.data.backup

import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.data.mapper.toEntity
import jpark.bro.subcal.core.data.mapper.toModel
import jpark.bro.subcal.core.data.repository.BackupRepository
import jpark.bro.subcal.core.database.dao.DataDao
import jpark.bro.subcal.core.database.dao.SubscriptionDao
import jpark.bro.subcal.core.database.entity.SubscriptionWithRules
import jpark.bro.subcal.core.datastore.SettingsDataStore
import jpark.bro.subcal.core.model.AppSettings
import jpark.bro.subcal.core.model.BackupSummary
import jpark.bro.subcal.core.model.NotificationSettings
import jpark.bro.subcal.core.model.Pause
import jpark.bro.subcal.core.model.PriceChange
import jpark.bro.subcal.core.model.ReminderMode
import jpark.bro.subcal.core.model.Subscription
import kotlinx.coroutines.flow.first
import kotlinx.serialization.json.Json
import java.time.Clock

internal class DefaultBackupRepository(
    private val subscriptionDao: SubscriptionDao,
    private val dataDao: DataDao,
    private val settingsDataStore: SettingsDataStore,
    private val clock: Clock,
) : BackupRepository {
    override suspend fun export(appVersion: String): Result<String> =
        suspendRunCatching {
            val records = subscriptionDao.observeAll().first().map(SubscriptionWithRules::toModel)
            val backup =
                BackupV1(
                    exportedAt = clock.millis(),
                    appVersion = appVersion,
                    settings =
                        backupSettings(
                            app = settingsDataStore.appSettings.first(),
                            notification = settingsDataStore.notificationSettings.first(),
                        ),
                    subscriptions = records.map { it.subscription.toBackup() },
                    priceChanges = records.flatMap { record -> record.priceChanges.map { it.toBackup() } },
                    pauses = records.flatMap { record -> record.pauses.map { it.toBackup() } },
                )
            BackupJson.encodeToString(BackupV1.serializer(), backup)
        }

    override suspend fun inspect(json: String): Result<BackupSummary> =
        suspendRunCatching {
            val backup = parseBackup(json)
            BackupSummary(subscriptionCount = backup.subscriptions.size, exportedAt = backup.exportedAt)
        }

    override suspend fun restore(json: String): Result<Unit> =
        suspendRunCatching {
            val backup = parseBackup(json)
            // DB는 한 트랜잭션이라 실패하면 그대로다. 설정은 검사를 마친 값이라 쓰기만 남는다
            dataDao.replaceAll(
                subscriptions = backup.subscriptions.map(Subscription::toEntity),
                priceChanges = backup.priceChanges.map(PriceChange::toEntity),
                pauses = backup.pauses.map(Pause::toEntity),
            )
            settingsDataStore.setAppSettings(backup.appSettings)
            settingsDataStore.setNotificationSettings(backup.notificationSettings)
        }

    override suspend fun deleteAll(): Result<Unit> =
        suspendRunCatching {
            dataDao.deleteAll()
            settingsDataStore.clear()
        }
}

private val BackupJson =
    Json {
        ignoreUnknownKeys = true
        encodeDefaults = true
    }

/** 검사를 마친 백업 내용 */
private class ParsedBackup(
    val exportedAt: Long,
    val subscriptions: List<Subscription>,
    val priceChanges: List<PriceChange>,
    val pauses: List<Pause>,
    val appSettings: AppSettings,
    val notificationSettings: NotificationSettings,
)

/**
 * 불러오기 검사 (data-model-erd 6장): 형식 일치, 앱이 아는 버전 이하, 모든 행 파싱,
 * 참조 무결성(없는 구독을 가리키는 행), 설정 값 범위. 하나라도 어긋나면 예외를 던진다.
 */
private fun parseBackup(json: String): ParsedBackup {
    val header = BackupJson.decodeFromString(BackupHeader.serializer(), json)
    require(header.format == BACKUP_FORMAT) { "백업 파일 형식이 아닙니다." }
    val version = requireNotNull(header.version) { "백업 버전이 없습니다." }
    require(version in 1..BACKUP_VERSION) { "이 앱이 모르는 백업 버전입니다: $version" }

    val backup = BackupJson.decodeFromString(BackupV1.serializer(), json)
    val subscriptions = backup.subscriptions.map(BackupSubscription::toModel)
    val priceChanges = backup.priceChanges.map(BackupPriceChange::toModel)
    val pauses = backup.pauses.map(BackupPause::toModel)
    val appSettings = backup.settings.toAppSettings()
    val notificationSettings = backup.settings.toNotificationSettings()

    val ids = subscriptions.map { it.id }
    require(ids.size == ids.toSet().size) { "구독 id가 겹칩니다." }
    require(priceChanges.all { it.subscriptionId in ids }) { "없는 구독의 가격 변경이 있습니다." }
    require(pauses.all { it.subscriptionId in ids }) { "없는 구독의 일시정지가 있습니다." }
    require(
        subscriptions.all {
            it.reminderMode != ReminderMode.CUSTOM || it.reminderDaysBefore in NotificationSettings.DaysBeforeOptions
        },
    ) { "고를 수 없는 알림 시점이 있습니다." }
    require(appSettings.isSelectable) { "앱 설정 값이 범위를 벗어났습니다." }
    require(notificationSettings.isSelectable) { "알림 설정 값이 범위를 벗어났습니다." }

    return ParsedBackup(backup.exportedAt, subscriptions, priceChanges, pauses, appSettings, notificationSettings)
}

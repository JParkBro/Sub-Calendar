package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.common.IdGenerator
import jpark.bro.subcal.core.common.suspendRunCatching
import jpark.bro.subcal.core.data.mapper.toEntity
import jpark.bro.subcal.core.data.mapper.toModel
import jpark.bro.subcal.core.database.dao.SubscriptionDao
import jpark.bro.subcal.core.database.dao.UsageLogDao
import jpark.bro.subcal.core.database.entity.UsageLogEntity
import jpark.bro.subcal.core.model.UsageLog
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import java.time.Clock
import java.time.LocalDate

internal class DefaultUsageRepository(
    private val usageLogDao: UsageLogDao,
    private val subscriptionDao: SubscriptionDao,
    private val idGenerator: IdGenerator,
    private val clock: Clock,
) : UsageRepository {
    override fun observeUsageLogs(): Flow<List<UsageLog>> =
        usageLogDao.observeAll().map { rows -> rows.map(UsageLogEntity::toModel) }

    override suspend fun add(
        subscriptionId: String,
        usedOn: LocalDate,
    ): Result<String> =
        suspendRunCatching {
            require(!usedOn.isAfter(LocalDate.now(clock))) { "오늘 이후에는 기록할 수 없습니다." }
            requireNotNull(subscriptionDao.get(subscriptionId)) { "구독을 찾을 수 없습니다." }
            val id = idGenerator.newId()
            usageLogDao.insert(UsageLog(id, subscriptionId, usedOn, clock.millis()).toEntity())
            id
        }

    override suspend fun remove(id: String): Result<Unit> =
        suspendRunCatching {
            require(usageLogDao.softDelete(id, clock.millis()) == 1) { "사용 기록을 찾을 수 없습니다." }
        }
}

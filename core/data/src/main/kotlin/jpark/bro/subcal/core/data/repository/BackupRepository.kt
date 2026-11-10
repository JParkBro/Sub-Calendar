package jpark.bro.subcal.core.data.repository

import jpark.bro.subcal.core.model.BackupSummary

/** 백업 파일 내보내기 · 불러오기 (data-model-erd 6장). 파일 읽기 · 쓰기는 화면이 하고 여기서는 JSON 문자열만 다룬다 */
interface BackupRepository {
    /** 삭제하지 않은 구독 전부와 백업할 설정을 JSON으로 */
    suspend fun export(appVersion: String): Result<String>

    /** 불러오기 전 미리보기. 형식 · 버전 · 내용 검사에 실패하면 실패 */
    suspend fun inspect(json: String): Result<BackupSummary>

    /** 지금 구독 데이터와 설정을 백업 내용으로 바꾼다 (덮어쓰기만, 합치기 없음). 검사에 실패하면 아무것도 바꾸지 않는다 */
    suspend fun restore(json: String): Result<Unit>
}

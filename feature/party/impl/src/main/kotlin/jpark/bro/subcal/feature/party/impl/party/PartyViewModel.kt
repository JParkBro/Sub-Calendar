package jpark.bro.subcal.feature.party.impl.party

import androidx.lifecycle.viewModelScope
import jpark.bro.subcal.core.data.repository.PartyRepository
import jpark.bro.subcal.core.data.repository.SettingsRepository
import jpark.bro.subcal.core.data.repository.SubscriptionRepository
import jpark.bro.subcal.core.designsystem.component.amountInputToMinor
import jpark.bro.subcal.core.designsystem.component.minorToAmountInput
import jpark.bro.subcal.core.domain.party.PartyMemberNameLength
import jpark.bro.subcal.core.model.PartySplitMode
import jpark.bro.subcal.core.ui.MviViewModel
import jpark.bro.subcal.feature.party.api.PartyKey
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.launchIn
import kotlinx.coroutines.launch
import java.time.Clock
import java.time.LocalDate

internal class PartyViewModel(
    key: PartyKey,
    subscriptionRepository: SubscriptionRepository,
    private val partyRepository: PartyRepository,
    settingsRepository: SettingsRepository,
    private val clock: Clock,
) : MviViewModel<PartyState, PartyAction, PartyEvent>(PartyState()) {
    private val subscriptionId = key.subscriptionId

    /** 삭제돼서 이미 뒤로 보냈다 */
    private var isClosed = false

    init {
        combine(
            subscriptionRepository.observeRecord(subscriptionId),
            partyRepository.observeSettlements(),
            settingsRepository.observeAppSettings(),
        ) { record, settlements, settings ->
            if (record == null) {
                if (!isClosed) {
                    isClosed = true
                    sendEvent(PartyEvent.NavigateBack)
                }
                return@combine
            }
            val content = buildPartyContent(record, settlements, settings.monthStartDay, LocalDate.now(clock))
            setState { copy(isLoading = false, content = content) }
        }.launchIn(viewModelScope)
    }

    override fun onAction(action: PartyAction) {
        when (action) {
            PartyAction.BackClicked -> sendEvent(PartyEvent.NavigateBack)
            PartyAction.SheetDismissed -> setState { copy(sheet = null) }
            is PartyAction.Settlement -> onSettlementAction(action)
            is PartyAction.Member -> onMemberAction(action)
            is PartyAction.Split -> onSplitAction(action)
        }
    }

    private fun onSettlementAction(action: PartyAction.Settlement) {
        when (action) {
            is PartyAction.PeriodSelected -> {
                setState { copy(selectedMonth = action.month) }
            }

            is PartyAction.SettledToggled -> {
                val month = currentState.selectedBill?.month ?: return
                write { partyRepository.setSettled(action.memberId, month, action.settled) }
            }

            is PartyAction.RequestClicked -> {
                requestTo { it.id == action.memberId }
            }

            PartyAction.RequestAllClicked -> {
                requestTo { true }
            }
        }
    }

    private fun onMemberAction(action: PartyAction.Member) {
        val sheet = currentState.sheet as? PartySheet.MemberName
        when (action) {
            PartyAction.AddMemberClicked -> {
                setState { copy(sheet = PartySheet.MemberName(memberId = null, input = "")) }
            }

            is PartyAction.MemberClicked -> {
                currentState.content
                    ?.members
                    ?.firstOrNull { it.id == action.memberId }
                    ?.let { member -> setState { copy(sheet = PartySheet.MemberName(member.id, member.name)) } }
            }

            is PartyAction.MemberNameChanged -> {
                sheet?.let { setState { copy(sheet = it.copy(input = action.input.take(PartyMemberNameLength.last))) } }
            }

            PartyAction.MemberSaveClicked -> {
                sheet?.let(::saveMember)
            }

            PartyAction.MemberDeleteClicked -> {
                sheet?.memberId?.let { memberId ->
                    setState { copy(sheet = null) }
                    write { partyRepository.removeMember(memberId) }
                }
            }
        }
    }

    /** 이름이 1~10자일 때만: 새 멤버면 추가, 아니면 이름 바꾸기 */
    private fun saveMember(sheet: PartySheet.MemberName) {
        if (sheet.input.trim().length !in PartyMemberNameLength) return
        setState { copy(sheet = null) }
        write {
            if (sheet.memberId == null) {
                partyRepository.addMember(subscriptionId, sheet.input).map { }
            } else {
                partyRepository.renameMember(sheet.memberId, sheet.input)
            }
        }
    }

    private fun onSplitAction(action: PartyAction.Split) {
        val sheet = currentState.sheet as? PartySheet.Split
        when (action) {
            PartyAction.SplitClicked -> {
                currentState.content?.let { content ->
                    val inputs = content.members.associate { it.id to minorToAmountInput(it.share) }
                    setState { copy(sheet = PartySheet.Split(content.splitMode, inputs)) }
                }
            }

            is PartyAction.SplitModeSelected -> {
                sheet?.let { setState { copy(sheet = it.copy(mode = action.mode)) } }
            }

            is PartyAction.ShareInputChanged -> {
                sheet?.let {
                    setState {
                        copy(
                            sheet = it.copy(inputs = it.inputs + (action.memberId to action.input)),
                        )
                    }
                }
            }

            PartyAction.SplitSaveClicked -> {
                saveSplit()
            }
        }
    }

    /** 직접 입력은 빈 칸을 0으로 보고, 합이 구독 금액을 넘으면 저장하지 않는다 */
    private fun saveSplit() {
        val sheet = currentState.sheet as? PartySheet.Split
        val content = currentState.content
        if (sheet == null || content == null) return
        val shares =
            content.members.associate { member ->
                member.id to (amountInputToMinor(sheet.inputs[member.id].orEmpty(), content.total.currency) ?: 0)
            }
        if (sheet.mode == PartySplitMode.CUSTOM && shares.values.sum() > content.total.amountMinor) {
            sendEvent(PartyEvent.SplitOverTotal)
        } else {
            setState { copy(sheet = null) }
            write { partyRepository.setSplit(subscriptionId, sheet.mode, shares) }
        }
    }

    /** 고른 기간의 미정산 멤버 중 [filter]에 맞는 사람에게 요청 문구 */
    private fun requestTo(filter: (MemberSettlement) -> Boolean) {
        val content = currentState.content
        val bill = currentState.selectedBill
        val targets = bill?.unsettled?.filter(filter).orEmpty()
        if (content == null || bill == null || targets.isEmpty()) return
        sendEvent(
            PartyEvent.ShareRequest(
                serviceName = content.name,
                pastMonth = bill.month.monthValue.takeUnless { bill.isCurrent },
                lines = targets.map { RequestLine(it.name, it.share) },
            ),
        )
    }

    private fun write(block: suspend () -> Result<Unit>) {
        setState { copy(isWorking = true) }
        viewModelScope.launch {
            block().onFailure { sendEvent(PartyEvent.ActionFailed) }
            setState { copy(isWorking = false) }
        }
    }
}

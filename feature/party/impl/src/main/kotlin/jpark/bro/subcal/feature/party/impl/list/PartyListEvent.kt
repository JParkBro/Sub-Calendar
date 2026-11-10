package jpark.bro.subcal.feature.party.impl.list

internal sealed interface PartyListEvent {
    data object NavigateBack : PartyListEvent

    data class OpenParty(
        val subscriptionId: String,
    ) : PartyListEvent
}

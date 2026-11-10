package jpark.bro.subcal.feature.party.impl.list

internal sealed interface PartyListAction {
    data object BackClicked : PartyListAction

    data class PartyClicked(
        val subscriptionId: String,
    ) : PartyListAction
}

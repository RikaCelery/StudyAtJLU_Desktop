sealed interface SyncState {
    object SYNCED:SyncState

    object OUT_DATE:SyncState

    object SYNCING:SyncState
    class FAILED(
        val reason:Throwable?=null
    ):SyncState
}
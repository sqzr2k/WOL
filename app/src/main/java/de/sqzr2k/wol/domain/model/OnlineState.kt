package de.sqzr2k.wol.domain.model

sealed interface OnlineState {
    data object Unknown : OnlineState
    data object Checking : OnlineState
    data object Offline : OnlineState
    data class Online(val latencyMs: Long) : OnlineState
}

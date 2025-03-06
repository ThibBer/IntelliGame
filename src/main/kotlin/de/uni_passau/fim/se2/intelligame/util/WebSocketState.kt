package de.uni_passau.fim.se2.intelligame.util

enum class WebSocketState {
    CONNECTING, CONNECTED, ERROR, DISCONNECTING, DISCONNECTED;

    override fun toString(): String {
        return when (this) {
            CONNECTING -> "Connecting"
            CONNECTED -> "Connected"
            ERROR -> "Error"
            DISCONNECTING -> "Disconnecting"
            DISCONNECTED -> "Disconnected"
        }
    }
}
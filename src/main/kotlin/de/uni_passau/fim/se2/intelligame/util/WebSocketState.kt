package de.uni_passau.fim.se2.intelligame.util

enum class WebSocketState {
    CONNECTING, CONNECTED, ERROR, DISCONNECTING, DISCONNECTED, INVALID_API_KEY;

    override fun toString(): String {
        return when (this) {
            CONNECTING -> "Connecting"
            CONNECTED -> "Connected"
            ERROR -> "Error"
            DISCONNECTING -> "Disconnecting"
            DISCONNECTED -> "Disconnected"
            INVALID_API_KEY -> "Invalid API key"
        }
    }
}
package com.bios.walkietalkie2.models.messages2

data class MessageBye(override var text: String = "Bye-Bye") : ISocketTextMessage {
    override fun getType(): TypeSocketMessage = TypeSocketMessage.Bye
}

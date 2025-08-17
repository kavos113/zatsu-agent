package com.github.kavos113.zatsuagent.ui

import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel
import javax.swing.JTextArea

class ChatMessage(chat : Chat) {
    private val messageArea = JTextArea(chat.message).apply {
        lineWrap = true
        wrapStyleWord = true
        isEditable = false
        isOpaque = false
        border = null
    }

    val panel: JPanel = panel {
        row {
            label(chat.name)
        }
        row {
            cell(messageArea)
                .align(AlignX.FILL)
                .resizableColumn()
        }
    }
}
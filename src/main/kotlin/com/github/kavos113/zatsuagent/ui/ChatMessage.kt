package com.github.kavos113.zatsuagent.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import javax.swing.JPanel

class ChatMessage(chat : Chat, project: Project) {
    val panel: JPanel = panel {
        row {
            label(chat.name)
        }
        row {
            cell(MarkdownPanel(chat.message, project))
                .align(AlignX.FILL)
                .resizableColumn()
        }
    }
}
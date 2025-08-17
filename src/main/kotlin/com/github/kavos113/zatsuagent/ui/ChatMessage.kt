package com.github.kavos113.zatsuagent.ui

import com.intellij.openapi.project.Project
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import java.awt.Font
import javax.swing.JPanel

class ChatMessage(chat : Chat, project: Project) {
    val panel: JPanel = panel {
        row {
            label(chat.name).applyToComponent {
                font = font.deriveFont(Font.BOLD, 16f)
            }
        }
        row {
            cell(MarkdownPanel(chat.message, project))
                .align(AlignX.FILL)
                .resizableColumn()
        }
    }
}
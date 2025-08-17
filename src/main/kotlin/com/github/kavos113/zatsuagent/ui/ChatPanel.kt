package com.github.kavos113.zatsuagent.ui

import com.intellij.openapi.application.ApplicationManager
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class ChatPanel : JPanel(BorderLayout()) {
    private val messages: MutableList<Chat> = SAMPLE_MESSAGES

    private fun buildMessagesPanel(): JPanel = panel {
        for (chat in messages) {
            row {
                cell(ChatMessage(chat).panel)
                    .align(AlignX.FILL)
                    .resizableColumn()
            }
        }
    }

    private val messagesContainer = JPanel(BorderLayout())

    private val inputArea = JTextArea(3, 40).apply {
        lineWrap = true
        wrapStyleWord = true
    }

    private val sendButton = JButton("Send").apply {
        addActionListener {
            val text = inputArea.text.trim()
            if (text.isEmpty()) return@addActionListener
            inputArea.text = ""
            appendUserMessage(text)
            requestAiResponse(text)
        }
    }

    init {
        val initialMessagesPanel = buildMessagesPanel()
        messagesContainer.add(initialMessagesPanel, BorderLayout.CENTER)

        val scrollPane = JBScrollPane(messagesContainer).apply {
            preferredSize = Dimension(400, 300)
            verticalScrollBar.unitIncrement = 16
        }
        add(scrollPane, BorderLayout.CENTER)

        val inputPanel = panel {
            row {
                cell(inputArea)
                    .align(AlignX.FILL)
            }
            row {
                cell(sendButton)
                    .align(AlignX.RIGHT)
            }
        }
        add(inputPanel, BorderLayout.SOUTH)

        SwingUtilities.invokeLater { scrollToBottom(scrollPane) }
    }

    private fun refreshMessages(scrollPane: JBScrollPane? = null) {
        messagesContainer.removeAll()
        messagesContainer.add(buildMessagesPanel(), BorderLayout.CENTER)
        messagesContainer.revalidate()
        messagesContainer.repaint()
        if (scrollPane != null) scrollToBottom(scrollPane)
    }

    private fun scrollToBottom(scrollPane: JBScrollPane) {
        val vertical = scrollPane.verticalScrollBar
        vertical.value = vertical.maximum
    }

    private fun appendUserMessage(text: String) {
        messages.add(Chat(name = "User", message = text))
        refreshMessages(findScrollPane())
    }

    private fun appendAiMessage(text: String) {
        messages.add(Chat(name = "AI", message = text))
        refreshMessages(findScrollPane())
    }

    private fun findScrollPane(): JBScrollPane? =
        (components.firstOrNull { it is JBScrollPane } as? JBScrollPane)

    private fun requestAiResponse(userText: String) {
        ApplicationManager.getApplication().executeOnPooledThread {
            val response = generateAiResponse(userText)
            SwingUtilities.invokeLater {
                appendAiMessage(response)
            }
        }
    }

    private fun generateAiResponse(userText: String): String {
        // Stubbed LLM reply. Replace with real LLM API integration if needed.
        return buildString {
            append("You said: \"")
            append(userText)
            append("\"\n\n")
            append("(This is a stubbed AI response. Integrate with an LLM API to get real answers.)")
        }
    }
}
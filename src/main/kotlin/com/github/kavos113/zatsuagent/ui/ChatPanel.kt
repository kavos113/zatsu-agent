package com.github.kavos113.zatsuagent.ui

import com.github.kavos113.zatsuagent.services.AiService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.AlignX
import com.intellij.ui.dsl.builder.panel
import java.awt.BorderLayout
import java.awt.Dimension
import javax.swing.JButton
import javax.swing.JPanel
import javax.swing.JTextArea
import javax.swing.SwingUtilities

class ChatPanel(val project: Project) : JPanel(BorderLayout()) {
    private val messages: MutableList<Chat> = SAMPLE_MESSAGES
    private val aiService = project.service<AiService>()

    private fun buildMessagesPanel(): JPanel = panel {
        for (chat in messages) {
            row {
                cell(ChatMessage(chat, project).panel)
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

    private fun appendAiPlaceholder(): Int {
        val index = messages.size
        messages.add(Chat(name = "AI", message = ""))
        refreshMessages(findScrollPane())
        return index
    }

    private fun appendToAiMessage(index: Int, delta: String) {
        if (index in messages.indices) {
            val current = messages[index]
            if (current.name == "AI") {
                messages[index] = Chat(name = current.name, message = current.message + delta)
            } else {
                messages.add(Chat(name = "AI", message = delta))
            }
        } else {
            messages.add(Chat(name = "AI", message = delta))
        }
        refreshMessages(findScrollPane())
    }

    private fun requestAiResponse(userText: String) {
        // Add a placeholder AI message to stream into
        val aiIndex = appendAiPlaceholder()
        val scrollPane = findScrollPane()

        ApplicationManager.getApplication().executeOnPooledThread {
            try {
                aiService.sendRequest(
                    prompt = userText,
                    onRecieveMessage = { delta ->
                        SwingUtilities.invokeLater {
                            appendToAiMessage(aiIndex, delta)
                            scrollPane?.let { scrollToBottom(it) }
                        }
                    },
                    onRecieveReasoning = { _ ->
                        // For minimal change, ignore reasoning stream for now.
                        // Could be surfaced in a separate message or tooltip later.
                    }
                )
            } catch (t: Throwable) {
                val err = t.message ?: "Unknown error"
                SwingUtilities.invokeLater {
                    appendToAiMessage(aiIndex, "\n\n[Error] $err")
                }
            }
        }
    }
}
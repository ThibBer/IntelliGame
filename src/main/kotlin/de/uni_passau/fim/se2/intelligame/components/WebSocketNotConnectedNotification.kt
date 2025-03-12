package de.uni_passau.fim.se2.intelligame.components

import com.intellij.openapi.components.service
import com.intellij.openapi.fileEditor.FileEditor
import com.intellij.openapi.project.Project
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.ui.EditorNotificationPanel
import com.intellij.ui.EditorNotificationProvider
import com.intellij.ui.EditorNotifications
import de.uni_passau.fim.se2.intelligame.services.GamificationService
import de.uni_passau.fim.se2.intelligame.util.WebSocketState
import java.util.function.Function
import javax.swing.JComponent

class WebSocketNotConnectedNotificationProvider : EditorNotificationProvider {
    private var canBeShowed = true

    override fun collectNotificationData(project: Project, virtualFile: VirtualFile): Function<in FileEditor, out JComponent?>? {
        if(!virtualFile.name.endsWith("Test.java") || !canBeShowed){
            return null
        }

        val gamificationService = project.service<GamificationService>()
        if(gamificationService.getWebSocketState() == WebSocketState.CONNECTED){
            return null
        }

        return Function { fileEditor: FileEditor ->
            createNotification(fileEditor, project, virtualFile)
        }
    }

    private fun createNotification(fileEditor: FileEditor, project: Project, virtualFile: VirtualFile): EditorNotificationPanel {
        val gamificationService = project.service<GamificationService>()

        return EditorNotificationPanel(fileEditor, EditorNotificationPanel.Status.Warning).apply {
            text = "Not connected to gamification server, leaderboard achievements will be lost !"
            createActionLabel("Connect") {
                gamificationService.connect()
                EditorNotifications.getInstance(project).updateNotifications(virtualFile)
            }
            setCloseAction {
                canBeShowed = false
                EditorNotifications.getInstance(project).updateNotifications(virtualFile)
            }
            icon(TrophyIcons.trophyToolWindowIcon)
        }
    }
}
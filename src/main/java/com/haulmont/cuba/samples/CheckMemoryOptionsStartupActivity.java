package com.haulmont.cuba.samples;

import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.project.DumbAware;
import com.intellij.openapi.project.Project;
import com.intellij.openapi.startup.StartupActivity;
import com.intellij.openapi.ui.MessageType;
import com.intellij.openapi.ui.popup.Balloon;
import com.intellij.openapi.ui.popup.JBPopupFactory;
import com.intellij.openapi.wm.StatusBar;
import com.intellij.openapi.wm.WindowManager;
import com.intellij.ui.awt.RelativePoint;
import org.jetbrains.annotations.NotNull;

/**
 * @author Alexander Budarov
 */
public class CheckMemoryOptionsStartupActivity implements StartupActivity, DumbAware {

    @Override
    public void runActivity(@NotNull Project project) {
        long maxMemoryMb = Runtime.getRuntime().maxMemory() / 1024 / 1024;
        if (maxMemoryMb < 1000) {
            String message = String.format(
                    "Dude, IDEA has only %s MB of heap configured.\nHaven't you forgotten to change idea64.vmoptions?", maxMemoryMb);
            ApplicationManager.getApplication().invokeLater(() -> showNotification(message, project));
        }
    }

    protected void showNotification(String message, Project project) {
        StatusBar statusBar = WindowManager.getInstance().getStatusBar(project);
        if (statusBar != null) {
            JBPopupFactory.getInstance()
                    .createHtmlTextBalloonBuilder(message, MessageType.WARNING, null)
                    .setFadeoutTime(15000)
                    .createBalloon()
                    .show(RelativePoint.getCenterOf(statusBar.getComponent()), Balloon.Position.atRight);
        }
    }

}

package com.vittoriomattei.contextfetcher.ui.panel;

import com.intellij.markdown.utils.doc.DocMarkdownToHtmlConverter;
import com.intellij.openapi.project.Project;
import com.intellij.ui.components.JBHtmlPane;
import com.intellij.ui.components.JBScrollPane;
import com.intellij.util.ui.JBUI;
import com.intellij.util.ui.UIUtil;

public class CodePanel extends JBScrollPane {

    private final JBHtmlPane editorPane;
    private final Project project;

    public CodePanel(Project project) {
        this.project = project;
        editorPane = new JBHtmlPane();
        editorPane.setEditable(false);
        editorPane.setBorder(JBUI.Borders.empty(10));
        editorPane.setBackground(UIUtil.getPanelBackground());
        setViewportView(editorPane);
    }

    public void setMarkdownText(String markdownText) {
        String html = DocMarkdownToHtmlConverter.convert(project, markdownText);

        editorPane.setText(html);
        getVerticalScrollBar().setValue(0);
    }
}
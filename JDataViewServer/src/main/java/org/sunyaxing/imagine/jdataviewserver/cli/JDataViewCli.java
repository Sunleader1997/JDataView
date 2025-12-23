package org.sunyaxing.imagine.jdataviewserver.cli;

import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.sunyaxing.imagine.jdataviewserver.controller.JavaAppController;

import java.util.List;
import java.util.Set;

@Configuration
public class JDataViewCli implements ApplicationRunner {

    @Autowired
    private JavaAppController javaAppController;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> modes = args.getOptionValues("mode");
        if (modes != null && !modes.isEmpty() && modes.get(0).equals("cli")) {
            System.setProperty("java.awt.headless", "false");
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            MultiWindowTextGUI gui = new MultiWindowTextGUI(screen);
            gui.updateScreen();
            BasicWindow window = new BasicWindow("JDataView CLI");
            window.setHints(Set.of(
                    Window.Hint.CENTERED,
                    Window.Hint.NO_POST_RENDERING,
                    Window.Hint.EXPANDED,
                    Window.Hint.FULL_SCREEN
            ));
            Component mainPanel = createMainPanel();
            window.setComponent(mainPanel);
            gui.addWindow(window);
            gui.addWindowAndWait(window); // 阻塞等待窗口关闭
        }
    }

    private Panel createMainPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new BorderLayout());
        // Create title bar
        Panel titleBar = createTitleBar();
        panel.addComponent(titleBar, BorderLayout.Location.TOP);
        // Create app list
        Panel listPanel = createAppListPanel();
        panel.addComponent(listPanel, BorderLayout.Location.CENTER);

        return panel;
    }

    private Panel createTitleBar() {
        Panel titleBar = new Panel();
        titleBar.setLayoutManager(new BorderLayout());

        // Title
        Label titleLabel = new Label("Java App Manager");
        titleLabel.setForegroundColor(TextColor.ANSI.WHITE);
        titleLabel.setBackgroundColor(TextColor.ANSI.BLACK);
        titleBar.addComponent(titleLabel, BorderLayout.Location.CENTER);

        // Refresh button
        Button refreshButton = new Button("Refresh (R)", () -> {
//            refreshAppList();
        });
        titleBar.addComponent(refreshButton, BorderLayout.Location.RIGHT);
        titleBar.withBorder(Borders.singleLine());
        return titleBar;
    }

    private Panel createAppListPanel() {
        Panel listPanel = new Panel();
        listPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));

        Table<String> table = new Table<String>("Application", "Status", "Attached");
        javaAppController.getJavaApps().getData().forEach(javaAppDto -> {
            table.getTableModel().addRow(javaAppDto.getAppName(), javaAppDto.isAlive() ? "Alive" : "Dead", javaAppDto.isHasAttached() ? "Attached" : "Detached");
        });
        listPanel.addComponent(table);
        return listPanel;
    }
}

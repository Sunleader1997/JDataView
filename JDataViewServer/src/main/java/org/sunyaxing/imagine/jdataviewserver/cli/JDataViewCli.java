package org.sunyaxing.imagine.jdataviewserver.cli;

import cn.hutool.core.collection.CollectionUtil;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.sunyaxing.imagine.jdataviewserver.controller.JavaAppController;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
public class JDataViewCli implements ApplicationRunner {

    @Autowired
    private JavaAppController javaAppController;

    private MultiWindowTextGUI gui;
    private Table<String> mainAppListTable;
    private List<JavaAppDto> appListCache;
    private JavaAppDto selectedApp;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> modes = args.getOptionValues("mode");
        if (modes != null && !modes.isEmpty() && modes.get(0).equals("cli")) {
            System.setProperty("java.awt.headless", "false");
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            gui = new MultiWindowTextGUI(screen);
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
        Button refreshButton = new Button("Refresh", this::refreshAppList);
        titleBar.addComponent(refreshButton, BorderLayout.Location.RIGHT);
        titleBar.withBorder(Borders.singleLine());
        return titleBar;
    }

    private Panel createAppListPanel() {
        Panel listPanel = new Panel();
        listPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        mainAppListTable = new Table<String>("Application", "Status", "Attached");
        appListCache = new ArrayList<>();
        refreshAppList();
        mainAppListTable.setSelectAction(() -> {
            int selectedRow = mainAppListTable.getSelectedRow();
            if (selectedRow >= 0) {
                // 选中app
                selectedApp = appListCache.get(selectedRow);
                // 显示
                showActionDialog();
            }
        });
        listPanel.addComponent(mainAppListTable);
        return listPanel;
    }

    public void refreshAppList() {
        TableModel<String> tableModel = mainAppListTable.getTableModel();
        tableModel.clear();
        CollectionUtil.clear(appListCache);
        appListCache.addAll(javaAppController.getJavaApps().getData());
        appListCache.forEach(javaAppDto -> {
            tableModel.addRow(javaAppDto.getAppName(), javaAppDto.isAlive() ? "Alive" : "Dead", javaAppDto.isHasAttached() ? "Attached" : "Detached");
        });
    }

    public String showAttachDialog() {
        return new TextInputDialogBuilder()
                .setTitle("Input package: (com.xxx.xxx)")
                //.setValidationPattern(Pattern.compile("[0-9]"), "You didn't enter a single number!")
                .build()
                .showDialog(gui);
    }

    public void showActionDialog() {
        new ActionListDialogBuilder()
                .setTitle(selectedApp.getAppName())
                .setDescription("")
                .addAction("Stack", new Runnable() {
                    @Override
                    public void run() {
                        // Do 1st thing...
                    }
                })
                .addAction("Attach", new Runnable() {
                    @Override
                    public void run() {
                        String packagePrefix = showAttachDialog();
                        selectedApp.setScanPackage(packagePrefix);
                        System.out.println(selectedApp);
                        // doAttach
                        refreshAppList();
                    }
                })
                .addAction("Detach", new Runnable() {
                    @Override
                    public void run() {
                        // Do 3rd thing...
                    }
                })
                .build()
                .showDialog(gui);
    }
}

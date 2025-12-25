package org.sunyaxing.imagine.jdataviewserver.cli;

import cn.hutool.core.collection.CollectionUtil;
import cn.hutool.core.util.StrUtil;
import com.googlecode.lanterna.TextColor;
import com.googlecode.lanterna.graphics.SimpleTheme;
import com.googlecode.lanterna.gui2.*;
import com.googlecode.lanterna.gui2.dialogs.ActionListDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.TextInputDialogBuilder;
import com.googlecode.lanterna.gui2.dialogs.WaitingDialog;
import com.googlecode.lanterna.gui2.table.Table;
import com.googlecode.lanterna.gui2.table.TableModel;
import com.googlecode.lanterna.screen.Screen;
import com.googlecode.lanterna.screen.TerminalScreen;
import com.googlecode.lanterna.terminal.DefaultTerminalFactory;
import com.googlecode.lanterna.terminal.Terminal;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Configuration;
import org.sunyaxing.imagine.jdataviewserver.controller.JavaAppController;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.JavaAppDto;
import org.sunyaxing.imagine.jdataviewserver.controller.dtos.ThreadDto;
import org.sunyaxing.imagine.jdataviewserver.service.AgentMsgService;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Configuration
public class JDataViewCli implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(JDataViewCli.class);
    @Autowired
    private JavaAppController javaAppController;

    private MultiWindowTextGUI gui;
    private Table<String> mainAppListTable;
    // 堆栈 TABLE
    private Table<String> stackListTable;
    private List<JavaAppDto> appListCache;
    // 线程列表
    private List<ThreadDto> threadsCache;
    // 堆栈缓存
    private List<AgentMsgService.MethodCall> methodCallsCache;
    private JavaAppDto selectedApp;

    private Panel appListPanel;
    private Panel stackPanel;

    private BasicWindow window ;

    @Override
    public void run(ApplicationArguments args) throws Exception {
        List<String> modes = args.getOptionValues("mode");
        if (modes != null && !modes.isEmpty() && modes.get(0).equals("cli")) {
            System.setProperty("java.awt.headless", "false");
            SimpleTheme simpleTheme = SimpleTheme.makeTheme(
                    true,
                    TextColor.ANSI.WHITE,
                    TextColor.ANSI.BLACK,
                    TextColor.ANSI.BLACK,
                    TextColor.ANSI.WHITE,
                    TextColor.ANSI.BLACK,
                    TextColor.ANSI.WHITE,
                    TextColor.ANSI.BLACK
            );
            Terminal terminal = new DefaultTerminalFactory().createTerminal();
            Screen screen = new TerminalScreen(terminal);
            screen.startScreen();
            gui = new MultiWindowTextGUI(screen);
            gui.updateScreen();
            gui.setTheme(simpleTheme);
            window = new BasicWindow("JDataView CLI");
            window.setHints(Set.of(
                    Window.Hint.CENTERED,
                    Window.Hint.NO_POST_RENDERING,
                    Window.Hint.EXPANDED,
                    Window.Hint.FULL_SCREEN
            ));
            appListPanel = appListPanel();
            stackPanel = stackPanel();
            window.setComponent(appListPanel);
            gui.addWindow(window);
            gui.addWindowAndWait(window); // 阻塞等待窗口关闭
        }
    }

    private Panel appListPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new BorderLayout());
        // Create title bar
        Panel titleBar = createTitleBar(this::refreshAppList);
        panel.addComponent(titleBar, BorderLayout.Location.TOP);
        // Create app list
        Panel listPanel = createAppListPanel();
        panel.addComponent(listPanel, BorderLayout.Location.CENTER);
        return panel;
    }

    private Panel stackPanel() {
        Panel panel = new Panel();
        panel.setLayoutManager(new LinearLayout(Direction.HORIZONTAL));

        Panel leftPanel = new Panel();
        panel.addComponent(leftPanel.withBorder(Borders.singleLine("Threads")));

        Panel rightPanel = new Panel();
        panel.addComponent(rightPanel.withBorder(Borders.singleLine("Stacks")));

        Panel stackTable = createStackTable();
        rightPanel.addComponent(stackTable, BorderLayout.Location.CENTER);
        return panel;
    }

    private Panel createTitleBar(Runnable refreshAction) {
        Panel titleBar = new Panel();
        titleBar.setLayoutManager(new BorderLayout());

        // Refresh button
        Button refreshButton = new Button("Refresh", refreshAction);
        titleBar.addComponent(refreshButton, BorderLayout.Location.RIGHT);
        titleBar.withBorder(Borders.singleLine());
        return titleBar;
    }

    private Panel createAppListPanel() {
        Panel listPanel = new Panel();
        listPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        mainAppListTable = new Table<String>("Application", "Status", "Attached");
        appListCache = new ArrayList<>();
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
    private Panel createStackTable(){
        Panel listPanel = new Panel();
        listPanel.setLayoutManager(new LinearLayout(Direction.VERTICAL));
        stackListTable = new Table<>("CLASS","COST");
        listPanel.addComponent(stackListTable);
        return listPanel;
    }

    public void refreshAppList() {
        TableModel<String> tableModel = mainAppListTable.getTableModel();
        tableModel.clear();
        CollectionUtil.clear(appListCache);
        showLoading("LOADING APP", () -> {
            appListCache.addAll(javaAppController.getJavaApps().getData());
            appListCache.forEach(javaAppDto -> {
                tableModel.addRow(javaAppDto.getAppName(), javaAppDto.isAlive() ? "Alive" : "Dead", javaAppDto.isHasAttached() ? "Attached" : "Detached");
            });
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
                        window.setComponent(stackPanel);
                    }
                })
                .addAction("Attach", new Runnable() {
                    @Override
                    public void run() {
                        String packagePrefix = showAttachDialog();
                        if (StrUtil.isNotEmpty(packagePrefix)) {
                            selectedApp.setScanPackage(packagePrefix);
                            showLoading("Attaching", () -> {
                                javaAppController.attach(selectedApp);
                                refreshAppList();
                            });
                        }
                    }
                })
                .addAction("Detach", new Runnable() {
                    @Override
                    public void run() {
                        showLoading("Detaching", () -> {
                            javaAppController.detach(selectedApp);
                            refreshAppList();
                        });
                    }
                })
                .build()
                .showDialog(gui);
    }

    public void showLoading(String title, Runnable runnable) {
        WaitingDialog dialogWindow = WaitingDialog.createDialog(title, "");
        dialogWindow.showDialog(gui, false);
        new Thread(() -> {
            try {
                runnable.run();
            } catch (Exception e) {
                log.error("ERROR ", e);
            } finally {
                dialogWindow.close();
            }
        }).start();
    }
}

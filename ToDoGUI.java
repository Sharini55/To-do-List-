import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import javax.swing.*;

public class ToDoGUI {

    private static final Color BG_COLOR = new Color(248, 241, 228); 
    private static final Color BUTTON_COLOR = new Color(10, 45, 100); 
    private static final Color TEXT_COLOR = new Color(30, 30, 30);

    private static ArrayList<Task> taskList;
    private static JPanel taskPanel;
    private static boolean showIncompleteOnly = false;
    private static JFrame frame; 

    private static CardLayout cardLayout = new CardLayout();
    private static JPanel mainCards = new JPanel(cardLayout);

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        taskList = To.loadTasks();

        frame = new JFrame("Productivity Dashboard");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800); 
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG_COLOR);

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(15, 20, 15, 20));

        JButton switchViewBtn = new JButton("Switch View 🔄");
        switchViewBtn.setBackground(BUTTON_COLOR);
        switchViewBtn.setForeground(Color.WHITE);
        switchViewBtn.setFont(new Font("Serif", Font.BOLD, 14));
        switchViewBtn.addActionListener(e -> {
            cardLayout.next(mainCards); 
        });

        JButton profileBtn = new JButton("👤");
        profileBtn.setBackground(BUTTON_COLOR);
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setFont(new Font("SansSerif", Font.PLAIN, 20));
        
        topBar.add(switchViewBtn, BorderLayout.WEST);
        topBar.add(profileBtn, BorderLayout.EAST);

        // --- CARDS (SCREENS) ---
        mainCards.add(createListView(), "List");
        mainCards.add(createDashboardView(), "Dashboard");

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(mainCards, BorderLayout.CENTER);

        refreshTasks();

        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
    }

    private static JPanel createListView() {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBackground(BG_COLOR);

        JPanel inputRow = createInputRow();

        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBackground(BG_COLOR);
        taskPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 20, 20)); 

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(BG_COLOR);
        wrapperPanel.add(taskPanel, BorderLayout.NORTH);

        panel.add(inputRow, BorderLayout.NORTH);
        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(null);
        panel.add(scrollPane, BorderLayout.CENTER);

        return panel;
    }

    private static JPanel createDashboardView() {
        JPanel dashboard = new JPanel(new GridLayout(1, 2, 10, 0)); 
        dashboard.setBackground(Color.WHITE);
        dashboard.setBorder(BorderFactory.createEmptyBorder(10, 20, 20, 20));

        // --- LEFT COLUMN (Tasks & Goals) ---
        JPanel leftCol = new JPanel(new GridLayout(3, 1, 0, 10)); 
        leftCol.setBackground(Color.WHITE);

        // 1. Pink Panel (Top Priority)
        JPanel pinkPanel = new JPanel(new BorderLayout());
        pinkPanel.setBackground(new Color(238, 188, 192)); 
        pinkPanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel priorityTitle = new JLabel("Top priority tasks", SwingConstants.CENTER);
        priorityTitle.setFont(new Font("Serif", Font.PLAIN, 28));
        pinkPanel.add(priorityTitle, BorderLayout.NORTH);
        
        // NEW: Injects all your "Red" Priority tasks right into the Dashboard!
        JPanel redTasksPanel = new JPanel();
        redTasksPanel.setLayout(new BoxLayout(redTasksPanel, BoxLayout.Y_AXIS));
        redTasksPanel.setBackground(new Color(238, 188, 192)); 
        redTasksPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        for (Task t : taskList) {
            // Checks if the priority is set to "Red" and if it's NOT completed yet
            if ("Red".equals(t.getPriority()) && !t.isCompleted()) {
                JLabel rtLabel = new JLabel("• " + t.getTitle());
                rtLabel.setFont(new Font("Serif", Font.PLAIN, 22));
                redTasksPanel.add(rtLabel);
                redTasksPanel.add(Box.createRigidArea(new Dimension(0, 5)));
            }
        }
        JScrollPane redScroll = new JScrollPane(redTasksPanel);
        redScroll.setBorder(null);
        pinkPanel.add(redScroll, BorderLayout.CENTER);

        // 2. Orange Panel (Progress)
        JPanel orangePanel = new JPanel(new BorderLayout());
        orangePanel.setBackground(new Color(252, 219, 185)); 
        orangePanel.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        JLabel progressTitle = new JLabel("Progress on goals", SwingConstants.CENTER);
        progressTitle.setFont(new Font("Serif", Font.PLAIN, 24));
        
        JProgressBar goalProgress = new JProgressBar(0, 100);
        goalProgress.setValue(45); 
        goalProgress.setStringPainted(true);
        goalProgress.setForeground(new Color(248, 168, 172)); 
        
        orangePanel.add(progressTitle, BorderLayout.NORTH);
        orangePanel.add(goalProgress, BorderLayout.CENTER);

        // 3. Blue Panel (Notes/Extra)
        JPanel bluePanel = new JPanel();
        bluePanel.setBackground(new Color(187, 222, 240)); 

        leftCol.add(pinkPanel);
        leftCol.add(orangePanel);
        leftCol.add(bluePanel);

        // --- RIGHT COLUMN (Calendar Timeline) ---
        JPanel rightCol = new JPanel(new BorderLayout());
        rightCol.setBackground(new Color(246, 224, 193)); 
        rightCol.setBorder(BorderFactory.createEmptyBorder(15, 15, 15, 15));
        
        JLabel calendarTitle = new JLabel("Calendar", SwingConstants.CENTER);
        calendarTitle.setFont(new Font("Serif", Font.PLAIN, 36));
        rightCol.add(calendarTitle, BorderLayout.NORTH);

        JPanel timelinePanel = new JPanel();
        timelinePanel.setLayout(new BoxLayout(timelinePanel, BoxLayout.Y_AXIS));
        timelinePanel.setBackground(new Color(246, 224, 193));
        
        for(int i = 9; i <= 21; i++) {
            String timeStr = (i > 12 ? i - 12 : i) + " " + (i >= 12 ? "PM" : "AM");
            JPanel hourRow = new JPanel(new BorderLayout());
            hourRow.setBackground(new Color(246, 224, 193));
            hourRow.setBorder(BorderFactory.createEmptyBorder(10, 0, 10, 0));
            
            JLabel timeLabel = new JLabel(timeStr + "   ");
            timeLabel.setFont(new Font("SansSerif", Font.PLAIN, 12));
            
            JSeparator line = new JSeparator();
            line.setForeground(Color.DARK_GRAY);

            hourRow.add(timeLabel, BorderLayout.WEST);
            hourRow.add(line, BorderLayout.CENTER);
            timelinePanel.add(hourRow);
        }
        
        JScrollPane calendarScroll = new JScrollPane(timelinePanel);
        calendarScroll.setBorder(null);
        rightCol.add(calendarScroll, BorderLayout.CENTER);

        dashboard.add(leftCol);
        dashboard.add(rightCol);

        return dashboard;
    }

    private static JPanel createInputRow() {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BG_COLOR);
        panel.setBorder(BorderFactory.createEmptyBorder(0, 100, 10, 20)); 

        JLabel plusLabel = new JLabel("+  ");
        plusLabel.setForeground(BUTTON_COLOR);
        plusLabel.setFont(new Font("SansSerif", Font.BOLD, 22));

        JTextField taskInput = new JTextField(20);
        taskInput.setFont(new Font("Serif", Font.PLAIN, 20));

        JButton btnToday = new JButton("Today");
        JButton btnTomorrow = new JButton("Tomorrow");
        JButton btnDate = new JButton("📅"); 

        btnToday.addActionListener(e -> addNewTask(panel, taskInput, LocalDate.now()));
        btnTomorrow.addActionListener(e -> addNewTask(panel, taskInput, LocalDate.now().plusDays(1)));
        
        btnDate.addActionListener(e -> {
            LocalDateTime selected = CalendarPicker.showCalendar(frame, false);
            if (selected != null) {
                addNewTask(panel, taskInput, selected.toLocalDate());
            }
        });

        panel.add(plusLabel);
        panel.add(taskInput);
        panel.add(btnToday);
        panel.add(btnTomorrow);
        panel.add(btnDate);

        return panel;
    }

    private static void addNewTask(Component parent, JTextField taskInput, LocalDate date) {
        String title = taskInput.getText().trim();
        if (!title.isEmpty()) {
            Task newTask = new Task(title, "", date);
            
            int wantReminder = JOptionPane.showConfirmDialog(parent, "Do you want to add a specific time reminder for this task?", "Add Reminder?", JOptionPane.YES_NO_OPTION);
            if (wantReminder == JOptionPane.YES_OPTION) {
                LocalDateTime remDate = CalendarPicker.showCalendar(frame, true);
                if (remDate != null) {
                    newTask.setReminder(remDate);
                }
            }

            taskList.add(newTask);
            To.saveTasks(taskList); 
            taskInput.setText(""); 
            refreshTasks(); 
        }
    }

    private static void refreshTasks() {
        if (taskPanel != null) {
            taskPanel.removeAll(); 
            Collections.sort(taskList);

            for (Task t : taskList) {
                if (showIncompleteOnly && t.isCompleted()) continue; 
                
                taskPanel.add(createTaskItem(t));
                taskPanel.add(Box.createRigidArea(new Dimension(0, 5))); 
            }

            taskPanel.revalidate(); 
            taskPanel.repaint(); 
        }

        // NEW: This forces the Dashboard to rebuild so Red tasks appear immediately
        if (mainCards != null) {
            mainCards.removeAll();
            mainCards.add(createListView(), "List");
            mainCards.add(createDashboardView(), "Dashboard");
            mainCards.revalidate();
            mainCards.repaint();
        }
    }

    private static JPanel createTaskItem(Task task) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BG_COLOR);

        JLabel bullet = new JLabel(task.isCompleted() ? "\u2611  " : "\u25A1  ");
        bullet.setForeground(BUTTON_COLOR);
        bullet.setFont(new Font("SansSerif", Font.BOLD, 22));
        bullet.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        
        bullet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                task.setCompleted(!task.isCompleted()); 
                To.saveTasks(taskList); 
                refreshTasks(); 
            }
        });

        JLabel taskLabel = new JLabel();
        taskLabel.setFont(new Font("Serif", Font.PLAIN, 24));
        taskLabel.setForeground(TEXT_COLOR);
        
        // NEW: Checks the Priority and assigns the matching dot
        String priorityIcon = "";
        if ("Red".equals(task.getPriority())) priorityIcon = " 🔴";
        else if ("Yellow".equals(task.getPriority())) priorityIcon = " 🟡";
        else if ("Green".equals(task.getPriority())) priorityIcon = " 🟢";

        String remText = (task.getReminder() != null) ? " [⏰ " + task.getReminder().format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")) + "]" : "";
        String text = task.getTitle() + priorityIcon + " (Due: " + task.getDueDate() + ")" + remText;

        if (task.isCompleted()) {
            taskLabel.setText("<html><font color='gray'><s>" + text + "</s></font></html>");
        } else {
            taskLabel.setText(text);
        }

        taskLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPopupMenu taskMenu = new JPopupMenu();
        
        // NEW: The Priority Selection Menu
        JMenu priorityMenu = new JMenu("Set Priority...");
        JMenuItem prioRed = new JMenuItem("🔴 High (Red)");
        JMenuItem prioYellow = new JMenuItem("🟡 Medium (Yellow)");
        JMenuItem prioGreen = new JMenuItem("🟢 Low (Green)");
        JMenuItem prioNone = new JMenuItem("⚪ None");

        prioRed.addActionListener(e -> { task.setPriority("Red"); To.saveTasks(taskList); refreshTasks(); });
        prioYellow.addActionListener(e -> { task.setPriority("Yellow"); To.saveTasks(taskList); refreshTasks(); });
        prioGreen.addActionListener(e -> { task.setPriority("Green"); To.saveTasks(taskList); refreshTasks(); });
        prioNone.addActionListener(e -> { task.setPriority("None"); To.saveTasks(taskList); refreshTasks(); });

        priorityMenu.add(prioRed);
        priorityMenu.add(prioYellow);
        priorityMenu.add(prioGreen);
        priorityMenu.add(prioNone);

        JMenuItem editTitle = new JMenuItem("Edit Task Name");
        editTitle.addActionListener(e -> {
            String newTitle = JOptionPane.showInputDialog(panel, "New Title:", task.getTitle());
            if (newTitle != null && !newTitle.trim().isEmpty()) {
                task.setTitle(newTitle);
                To.saveTasks(taskList);
                refreshTasks();
            }
        });

        JMenuItem editDate = new JMenuItem("Change Due Date");
        editDate.addActionListener(e -> {
            LocalDateTime newDate = CalendarPicker.showCalendar(frame, false);
            if (newDate != null) {
                task.setDueDate(newDate.toLocalDate());
                To.saveTasks(taskList);
                refreshTasks();
            }
        });

        JMenuItem editReminder = new JMenuItem("Set/Change Reminder");
        editReminder.addActionListener(e -> {
            LocalDateTime remDate = CalendarPicker.showCalendar(frame, true);
            if (remDate != null) {
                task.setReminder(remDate);
                To.saveTasks(taskList);
                refreshTasks();
            }
        });

        JMenuItem deleteTask = new JMenuItem("Delete Task");
        deleteTask.setForeground(Color.RED);
        deleteTask.addActionListener(e -> {
            taskList.remove(task);
            To.saveTasks(taskList);
            refreshTasks();
        });

        taskMenu.add(priorityMenu); // Injects priority menu at the top
        taskMenu.addSeparator();
        taskMenu.add(editTitle);
        taskMenu.add(editDate);
        taskMenu.add(editReminder);
        taskMenu.addSeparator();
        taskMenu.add(deleteTask);

        taskLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                taskMenu.show(taskLabel, e.getX(), e.getY());
            }
        });
        
        panel.add(bullet);
        panel.add(taskLabel);

        return panel;
    }

    // ==========================================================
    // CUSTOM VISUAL CALENDAR DIALOG (Remains Unchanged)
    // ==========================================================
    static class CalendarPicker extends JDialog {
        private LocalDateTime finalSelection = null;
        private YearMonth currentMonth;
        private LocalDate selectedDate = LocalDate.now();
        private JPanel daysPanel;
        private JLabel monthLabel;
        private JSpinner timeSpinner;
        private boolean includeTime;

        public static LocalDateTime showCalendar(JFrame parent, boolean includeTime) {
            CalendarPicker picker = new CalendarPicker(parent, includeTime);
            picker.setVisible(true);
            return picker.finalSelection;
        }

        private CalendarPicker(JFrame parent, boolean includeTime) {
            super(parent, "Select Date & Time", true);
            this.includeTime = includeTime;
            this.currentMonth = YearMonth.now();

            setSize(350, includeTime ? 450 : 380);
            setLocationRelativeTo(parent);
            setLayout(new BorderLayout());
            getContentPane().setBackground(new Color(30, 30, 30)); 

            JPanel topPanel = new JPanel(new BorderLayout());
            topPanel.setBackground(new Color(30, 30, 30));
            topPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            JButton prevBtn = new JButton("<");
            JButton nextBtn = new JButton(">");
            styleNavButton(prevBtn);
            styleNavButton(nextBtn);

            monthLabel = new JLabel("", SwingConstants.CENTER);
            monthLabel.setForeground(Color.WHITE);
            monthLabel.setFont(new Font("SansSerif", Font.BOLD, 16));

            prevBtn.addActionListener(e -> changeMonth(-1));
            nextBtn.addActionListener(e -> changeMonth(1));

            topPanel.add(prevBtn, BorderLayout.WEST);
            topPanel.add(monthLabel, BorderLayout.CENTER);
            topPanel.add(nextBtn, BorderLayout.EAST);

            daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
            daysPanel.setBackground(new Color(30, 30, 30));
            daysPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            updateCalendar();

            JPanel bottomPanel = new JPanel(new BorderLayout());
            bottomPanel.setBackground(new Color(30, 30, 30));
            bottomPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

            if (includeTime) {
                JPanel timePanel = new JPanel(new FlowLayout(FlowLayout.CENTER));
                timePanel.setBackground(new Color(30, 30, 30));
                JLabel timeLabel = new JLabel("Time: ");
                timeLabel.setForeground(Color.WHITE);
                
                timeSpinner = new JSpinner(new SpinnerDateModel());
                JSpinner.DateEditor timeEditor = new JSpinner.DateEditor(timeSpinner, "hh:mm a");
                timeSpinner.setEditor(timeEditor);
                timeSpinner.setValue(new Date()); 
                
                timePanel.add(timeLabel);
                timePanel.add(timeSpinner);
                bottomPanel.add(timePanel, BorderLayout.NORTH);
            }

            JPanel buttonPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
            buttonPanel.setBackground(new Color(30, 30, 30));
            
            JButton cancelBtn = new JButton("Cancel");
            JButton doneBtn = new JButton("Done");
            styleDoneButton(cancelBtn);
            styleDoneButton(doneBtn);

            cancelBtn.addActionListener(e -> dispose());
            doneBtn.addActionListener(e -> confirmSelection());

            buttonPanel.add(cancelBtn);
            buttonPanel.add(doneBtn);
            bottomPanel.add(buttonPanel, BorderLayout.SOUTH);

            add(topPanel, BorderLayout.NORTH);
            add(daysPanel, BorderLayout.CENTER);
            add(bottomPanel, BorderLayout.SOUTH);
        }

        private void changeMonth(int offset) {
            currentMonth = currentMonth.plusMonths(offset);
            updateCalendar();
        }

        private void updateCalendar() {
            daysPanel.removeAll();
            monthLabel.setText(currentMonth.getMonth().name() + " " + currentMonth.getYear());

            String[] daysOfWeek = {"S", "M", "T", "W", "T", "F", "S"};
            for (String day : daysOfWeek) {
                JLabel dLabel = new JLabel(day, SwingConstants.CENTER);
                dLabel.setForeground(Color.LIGHT_GRAY);
                daysPanel.add(dLabel);
            }

            LocalDate firstOfMonth = currentMonth.atDay(1);
            int dayOfWeekValue = firstOfMonth.getDayOfWeek().getValue() % 7; 
            int daysInMonth = currentMonth.lengthOfMonth();

            for (int i = 0; i < dayOfWeekValue; i++) {
                daysPanel.add(new JLabel(""));
            }

            for (int i = 1; i <= daysInMonth; i++) {
                int day = i;
                LocalDate thisDate = currentMonth.atDay(day);
                JButton dayBtn = new JButton(String.valueOf(day));
                
                dayBtn.setFocusPainted(false);
                dayBtn.setBackground(new Color(45, 45, 45));
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                if (thisDate.isBefore(LocalDate.now())) {
                    dayBtn.setForeground(Color.DARK_GRAY);
                    dayBtn.setEnabled(false);
                }

                if (thisDate.equals(selectedDate)) {
                    dayBtn.setBackground(new Color(100, 150, 255));
                    dayBtn.setForeground(Color.BLACK);
                }

                dayBtn.addActionListener(e -> {
                    selectedDate = thisDate;
                    updateCalendar(); 
                });

                daysPanel.add(dayBtn);
            }

            daysPanel.revalidate();
            daysPanel.repaint();
        }

        private void confirmSelection() {
            if (includeTime) {
                Date timeVal = (Date) timeSpinner.getValue();
                LocalDateTime timeConverted = timeVal.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                finalSelection = selectedDate.atTime(timeConverted.toLocalTime());
                
                if (finalSelection.isBefore(LocalDateTime.now())) {
                    JOptionPane.showMessageDialog(this, "Cannot set a reminder in the past!", "Error", JOptionPane.ERROR_MESSAGE);
                    return;
                }
            } else {
                finalSelection = selectedDate.atStartOfDay();
            }
            dispose();
        }

        private void styleNavButton(JButton btn) {
            btn.setBackground(new Color(30, 30, 30));
            btn.setForeground(Color.WHITE);
            btn.setBorderPainted(false);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }

        private void styleDoneButton(JButton btn) {
            btn.setBackground(new Color(100, 150, 255));
            btn.setForeground(Color.BLACK);
            btn.setFocusPainted(false);
            btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        }
    }
}
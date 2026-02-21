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
    private static JFrame frame; // Store frame for the dialogs

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        taskList = To.loadTasks();

        frame = new JFrame("To Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1000, 800);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG_COLOR);

        // --- TOP BAR ---
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20));

        JButton menuBtn = new JButton("Menu");
        menuBtn.setBackground(BUTTON_COLOR);
        //menuBtn.setForeground(Color.WHITE);
        menuBtn.setFont(new Font("Serif", Font.BOLD, 14));
        menuBtn.setPreferredSize(new Dimension(80, 40));
        
        JPopupMenu dropDownMenu = new JPopupMenu();
        JMenuItem toggleView = new JMenuItem("Hide Completed Tasks");
        toggleView.addActionListener(e -> {
            showIncompleteOnly = !showIncompleteOnly;
            toggleView.setText(showIncompleteOnly ? "Show All Tasks" : "Hide Completed Tasks");
            refreshTasks();
        });
        dropDownMenu.add(toggleView);
        menuBtn.addActionListener(e -> dropDownMenu.show(menuBtn, 0, menuBtn.getHeight()));

        JButton profileBtn = new JButton("👤");
        profileBtn.setBackground(BUTTON_COLOR);
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setFont(new Font("SansSerif", Font.PLAIN, 20));
        profileBtn.setPreferredSize(new Dimension(50, 50));

        topBar.add(menuBtn, BorderLayout.WEST);
        topBar.add(profileBtn, BorderLayout.EAST);

        // --- INPUT ROW & TASK LIST ---
        JPanel inputRow = createInputRow();

        taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBackground(BG_COLOR);
        taskPanel.setBorder(BorderFactory.createEmptyBorder(10, 100, 20, 20)); 

        JPanel wrapperPanel = new JPanel(new BorderLayout());
        wrapperPanel.setBackground(BG_COLOR);
        wrapperPanel.add(taskPanel, BorderLayout.NORTH);

        JPanel centerPanel = new JPanel(new BorderLayout());
        centerPanel.setBackground(BG_COLOR);
        centerPanel.add(inputRow, BorderLayout.NORTH);
        
        JScrollPane scrollPane = new JScrollPane(wrapperPanel);
        scrollPane.setBorder(null); 
        centerPanel.add(scrollPane, BorderLayout.CENTER);

        frame.add(topBar, BorderLayout.NORTH);
        frame.add(centerPanel, BorderLayout.CENTER);

        refreshTasks();

        frame.setLocationRelativeTo(null); 
        frame.setVisible(true);
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
        
        // NEW: Uses the custom Calendar GUI
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
                // NEW: Uses the custom Calendar GUI with Time Selector
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
        
        String remText = (task.getReminder() != null) ? " [⏰ " + task.getReminder().format(DateTimeFormatter.ofPattern("MMM dd, hh:mm a")) + "]" : "";
        String text = task.getTitle() + " (Due: " + task.getDueDate() + ")" + remText;

        if (task.isCompleted()) {
            taskLabel.setText("<html><font color='gray'><s>" + text + "</s></font></html>");
        } else {
            taskLabel.setText(text);
        }

        taskLabel.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));
        JPopupMenu taskMenu = new JPopupMenu();
        
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
    // NEW: CUSTOM VISUAL CALENDAR DIALOG
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

            // --- Top Month Navigation ---
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

            // --- Calendar Grid ---
            daysPanel = new JPanel(new GridLayout(0, 7, 5, 5));
            daysPanel.setBackground(new Color(30, 30, 30));
            daysPanel.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
            updateCalendar();

            // --- Bottom Section (Time + Controls) ---
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

            // Blanks before start of month
            for (int i = 0; i < dayOfWeekValue; i++) {
                daysPanel.add(new JLabel(""));
            }

            // Actual days
            for (int i = 1; i <= daysInMonth; i++) {
                int day = i;
                LocalDate thisDate = currentMonth.atDay(day);
                JButton dayBtn = new JButton(String.valueOf(day));
                
                dayBtn.setFocusPainted(false);
                dayBtn.setBackground(new Color(45, 45, 45));
                dayBtn.setForeground(Color.WHITE);
                dayBtn.setBorder(BorderFactory.createEmptyBorder(5, 5, 5, 5));
                dayBtn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

                // Disable past dates
                if (thisDate.isBefore(LocalDate.now())) {
                    dayBtn.setForeground(Color.DARK_GRAY);
                    dayBtn.setEnabled(false);
                }

                // Highlight selected
                if (thisDate.equals(selectedDate)) {
                    dayBtn.setBackground(new Color(100, 150, 255));
                    dayBtn.setForeground(Color.BLACK);
                }

                dayBtn.addActionListener(e -> {
                    selectedDate = thisDate;
                    updateCalendar(); // refresh colors
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
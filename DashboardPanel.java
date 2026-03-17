import java.awt.*;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import javax.swing.*;
import javax.swing.border.EmptyBorder;

public class DashboardPanel extends JPanel {

    private static final Color BG_COLOR = new Color(252, 247, 237);
    private static final Color ACCENT_TEAL = new Color(163, 219, 216);
    private static final Color DARK_TEAL = new Color(74, 124, 121);
    private static final Color TEXT_COLOR = new Color(30, 30, 30);

    private JPanel taskListContainer;
    private JPanel calendarPanelContainer; // Holds the calendar so we can redraw it
    private ArrayList<Task> taskList;
    private JFrame parentFrame;

    // --- NEW STATE VARIABLES ---
    private LocalDate selectedDate = LocalDate.now();
    private int weekOffset = 0; // Tracks if we scrolled the calendar forward or backward

    public DashboardPanel(JFrame parentFrame, ArrayList<Task> taskList) {
        this.parentFrame = parentFrame;
        this.taskList = taskList;
        
        setLayout(new BorderLayout());
        setBackground(BG_COLOR);

        // Calendar Container
        calendarPanelContainer = new JPanel(new BorderLayout());
        calendarPanelContainer.setBackground(BG_COLOR);
        refreshCalendar();
        add(calendarPanelContainer, BorderLayout.NORTH);

        // Task List Container
        taskListContainer = new JPanel();
        taskListContainer.setLayout(new BoxLayout(taskListContainer, BoxLayout.Y_AXIS));
        taskListContainer.setBackground(BG_COLOR);
        taskListContainer.setBorder(new EmptyBorder(20, 40, 20, 20));

        JScrollPane scrollPane = new JScrollPane(taskListContainer);
        scrollPane.setBorder(null);
        scrollPane.getVerticalScrollBar().setUnitIncrement(16);
        add(scrollPane, BorderLayout.CENTER);

        // Add Button
        JPanel bottomPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 30, 30));
        bottomPanel.setBackground(BG_COLOR);
        bottomPanel.setOpaque(false);
        JButton addBtn = createCircularAddButton();
        addBtn.addActionListener(e -> showTaskDialog(null)); 
        bottomPanel.add(addBtn);
        add(bottomPanel, BorderLayout.SOUTH);

        updateTasks();
    }

    // --- CALENDAR SCROLLING & GENERATION ---
    private void refreshCalendar() {
        calendarPanelContainer.removeAll();

        JPanel calendarPanel = new JPanel(new BorderLayout());
        calendarPanel.setBackground(BG_COLOR);
        calendarPanel.setBorder(new EmptyBorder(10, 20, 10, 20));

        // Get the start of the currently viewed week
        LocalDate startOfWeek = LocalDate.now().plusWeeks(weekOffset);
        // Rewind to Monday
        while (startOfWeek.getDayOfWeek().getValue() != 1) {
            startOfWeek = startOfWeek.minusDays(1);
        }

        // Header with Arrows
        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(BG_COLOR);
        JLabel monthLabel = new JLabel(startOfWeek.format(DateTimeFormatter.ofPattern("MMMM yyyy")));
        monthLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
        
        JPanel arrowPanel = new JPanel(new FlowLayout(FlowLayout.RIGHT));
        arrowPanel.setBackground(BG_COLOR);
        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        styleNavButton(prevBtn); styleNavButton(nextBtn);
        
        prevBtn.addActionListener(e -> { weekOffset--; refreshCalendar(); });
        nextBtn.addActionListener(e -> { weekOffset++; refreshCalendar(); });

        arrowPanel.add(prevBtn); arrowPanel.add(nextBtn);
        headerPanel.add(monthLabel, BorderLayout.WEST);
        headerPanel.add(arrowPanel, BorderLayout.EAST);
        
        // Days Row
        JPanel daysPanel = new JPanel(new GridLayout(1, 7, 10, 0));
        daysPanel.setBackground(BG_COLOR);
        daysPanel.setBorder(new EmptyBorder(10, 0, 10, 0));

        String[] dayNames = {"M", "T", "W", "T", "F", "S", "S"};
        
        for (int i = 0; i < 7; i++) {
            LocalDate currentDate = startOfWeek.plusDays(i);
            JPanel dayBox = new JPanel(new GridLayout(2, 1));
            dayBox.setBackground(BG_COLOR);
            dayBox.setCursor(new Cursor(Cursor.HAND_CURSOR));
            
            JLabel dayName = new JLabel(dayNames[i], SwingConstants.CENTER);
            dayName.setFont(new Font("SansSerif", Font.PLAIN, 14));
            
            JLabel dateNum = new JLabel(String.valueOf(currentDate.getDayOfMonth()), SwingConstants.CENTER);
            dateNum.setFont(new Font("SansSerif", Font.BOLD, 16));

            // Highlight selected date
            if (currentDate.equals(selectedDate)) {
                dateNum.setOpaque(true);
                dateNum.setBackground(ACCENT_TEAL);
                dateNum.setForeground(Color.WHITE);
            }

            dayBox.add(dayName);
            dayBox.add(dateNum);
            
            // Clicking a date filters the tasks!
            dayBox.addMouseListener(new MouseAdapter() {
                @Override
                public void mouseClicked(MouseEvent e) {
                    selectedDate = currentDate;
                    refreshCalendar(); // Update highlight
                    updateTasks();     // Filter tasks
                }
            });

            daysPanel.add(dayBox);
        }

        calendarPanel.add(headerPanel, BorderLayout.NORTH);
        calendarPanel.add(daysPanel, BorderLayout.CENTER);
        
        JSeparator separator = new JSeparator();
        separator.setForeground(Color.GRAY);
        calendarPanel.add(separator, BorderLayout.SOUTH);

        calendarPanelContainer.add(calendarPanel, BorderLayout.CENTER);
        calendarPanelContainer.revalidate();
        calendarPanelContainer.repaint();
    }

    private void styleNavButton(JButton btn) {
        btn.setBackground(BG_COLOR);
        btn.setBorderPainted(false);
        btn.setFocusPainted(false);
        btn.setFont(new Font("SansSerif", Font.BOLD, 18));
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- SMART SORTING ALGORITHM ---
    private void sortTaskList(List<Task> list) {
        list.sort((t1, t2) -> {
            int p1 = getPriorityScore(t1.getPriority());
            int p2 = getPriorityScore(t2.getPriority());
            if (p1 != p2) return Integer.compare(p1, p2); // Sort by Priority First
            return t1.getDueDate().compareTo(t2.getDueDate()); // Then by Date
        });
    }

    private int getPriorityScore(String priority) {
        if (priority.equals("Red")) return 1;
        if (priority.equals("Yellow")) return 2;
        if (priority.equals("Green")) return 3;
        return 4; // None
    }

    // --- TASK RENDERING (Filtering & Categories) ---
    public void updateTasks() {
        taskListContainer.removeAll();

        List<Task> overdueTasks = new ArrayList<>();
        List<Task> currentDayTasks = new ArrayList<>();
        List<Task> completedTasks = new ArrayList<>();

        // 1. Categorize Tasks
        for (Task t : taskList) {
            if (t.isCompleted()) {
                completedTasks.add(t);
            } else if (t.getDueDate().isBefore(LocalDate.now())) {
                overdueTasks.add(t); // Overdue stays at the top always!
            } else if (t.getDueDate().equals(selectedDate)) {
                currentDayTasks.add(t); // Matches calendar filter
            }
        }

        // 2. Sort Each Category
        sortTaskList(overdueTasks);
        sortTaskList(currentDayTasks);
        sortTaskList(completedTasks);

        // 3. Render Overdue
        if (!overdueTasks.isEmpty()) {
            taskListContainer.add(createSectionHeader("Overdue", Color.RED));
            for (Task t : overdueTasks) taskListContainer.add(createTaskRow(t));
            taskListContainer.add(Box.createRigidArea(new Dimension(0, 20)));
        }

        // 4. Render Current Day
        String dayTitle = selectedDate.equals(LocalDate.now()) ? "Today's Tasks" : "Tasks for " + selectedDate.format(DateTimeFormatter.ofPattern("MMM dd"));
        taskListContainer.add(createSectionHeader(dayTitle, DARK_TEAL));
        if (currentDayTasks.isEmpty()) {
            JLabel empty = new JLabel("  No tasks scheduled.");
            empty.setForeground(Color.GRAY);
            taskListContainer.add(empty);
        } else {
            for (Task t : currentDayTasks) taskListContainer.add(createTaskRow(t));
        }
        taskListContainer.add(Box.createRigidArea(new Dimension(0, 20)));

        // 5. Render Completed
        if (!completedTasks.isEmpty()) {
            taskListContainer.add(createSectionHeader("Finished Tasks", Color.GRAY));
            for (Task t : completedTasks) taskListContainer.add(createTaskRow(t));
        }

        taskListContainer.revalidate();
        taskListContainer.repaint();
    }

    private JLabel createSectionHeader(String text, Color color) {
        JLabel header = new JLabel(text);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(color);
        header.setBorder(new EmptyBorder(0, 0, 10, 0));
        return header;
    }

    private JPanel createTaskRow(Task t) {
        JPanel taskRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 15, 5));
        taskRow.setBackground(BG_COLOR);
        taskRow.setMaximumSize(new Dimension(800, 40)); 

        JLabel bullet = new JLabel("\u25CF");
        bullet.setFont(new Font("SansSerif", Font.PLAIN, 24));
        bullet.setForeground(t.isCompleted() ? DARK_TEAL : ACCENT_TEAL);
        bullet.setCursor(new Cursor(Cursor.HAND_CURSOR));

        String priorityMarker = t.getPriority().equals("Red") ? " \uD83D\uDD34" : 
                                t.getPriority().equals("Yellow") ? " \uD83D\uDFE1" : 
                                t.getPriority().equals("Green") ? " \uD83D\uDFE2" : "";
        String reminderMarker = (t.getReminder() != null) ? " \u23F0" : "";

        JLabel taskLabel = new JLabel();
        taskLabel.setFont(new Font("SansSerif", Font.PLAIN, 20));
        taskLabel.setForeground(TEXT_COLOR);
        taskLabel.setCursor(new Cursor(Cursor.HAND_CURSOR)); 

        if (t.isCompleted()) {
            taskLabel.setText("<html><s><font color='gray'>" + t.getTitle() + "</font></s></html>");
        } else {
            taskLabel.setText(t.getTitle() + priorityMarker + reminderMarker);
        }

        // Click bullet to complete
        bullet.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                t.setCompleted(!t.isCompleted());
                To.saveTasks(taskList);
                updateTasks();
            }
        });

        // Click text to edit
        taskLabel.addMouseListener(new MouseAdapter() {
            @Override
            public void mouseClicked(MouseEvent e) {
                showTaskDialog(t); 
            }
        });

        taskRow.add(bullet);
        taskRow.add(taskLabel);
        return taskRow;
    }

    // --- REUSE YOUR EXISTING SHOW TASK DIALOG METHOD BELOW ---
    // (Paste the showTaskDialog, createDialogButton, and createCircularAddButton methods exactly as they were in the previous version here to keep the edit window working!)
    
    // --- DIALOG CODE STARTS ---
    private void showTaskDialog(Task existingTask) {
        boolean isEditing = (existingTask != null);
        String dialogTitle = isEditing ? "Edit Task" : "Add New Task";

        JDialog dialog = new JDialog(parentFrame, dialogTitle, true);
        dialog.setSize(420, 300);
        dialog.setLocationRelativeTo(parentFrame);
        dialog.getContentPane().setBackground(ACCENT_TEAL);
        dialog.setLayout(new BorderLayout());

        final LocalDate[] tempDate = {isEditing ? existingTask.getDueDate() : selectedDate};
        final LocalDateTime[] tempReminder = {isEditing ? existingTask.getReminder() : null};
        final String[] tempPriority = {isEditing ? existingTask.getPriority() : "None"};

        JPanel formPanel = new JPanel();
        formPanel.setLayout(new BoxLayout(formPanel, BoxLayout.Y_AXIS));
        formPanel.setBackground(ACCENT_TEAL);
        formPanel.setBorder(new EmptyBorder(20, 20, 20, 20));

        JTextField titleField = new JTextField(isEditing ? existingTask.getTitle() : "Enter the task");
        titleField.setFont(new Font("SansSerif", Font.PLAIN, 18));
        titleField.setBackground(Color.WHITE);
        titleField.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true), new EmptyBorder(10, 10, 10, 10)));
        
        JPanel buttonsRow = new JPanel(new FlowLayout(FlowLayout.LEFT, 10, 0));
        buttonsRow.setBackground(ACCENT_TEAL);
        
        String remText = (tempReminder[0] != null) ? "⏰ " + tempReminder[0].format(DateTimeFormatter.ofPattern("hh:mm a")) : "Reminder";
        JButton reminderBtn = createDialogButton(remText);
        JButton priorityBtn = createDialogButton("Priority: " + tempPriority[0]);
        JButton dateBtn = createDialogButton("📅  " + tempDate[0].format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "  >");
        dateBtn.setAlignmentX(Component.LEFT_ALIGNMENT);

        priorityBtn.addActionListener(e -> {
            String[] options = {"Red (High)", "Yellow (Medium)", "Green (Low)", "None"};
            String choice = (String) JOptionPane.showInputDialog(dialog, "Select Priority Level:", "Priority", JOptionPane.QUESTION_MESSAGE, null, options, tempPriority[0]);
            if (choice != null) {
                tempPriority[0] = choice.split(" ")[0]; 
                priorityBtn.setText("Priority: " + tempPriority[0]);
            }
        });

        reminderBtn.addActionListener(e -> {
            JSpinner timeSpinner = new JSpinner(new SpinnerDateModel());
            timeSpinner.setEditor(new JSpinner.DateEditor(timeSpinner, "MMM dd, yyyy  hh:mm a"));
            if (JOptionPane.showOptionDialog(dialog, timeSpinner, "Set Reminder Time", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                tempReminder[0] = ((Date) timeSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
                reminderBtn.setText("⏰ " + tempReminder[0].format(DateTimeFormatter.ofPattern("hh:mm a")));
            }
        });

        dateBtn.addActionListener(e -> {
            JSpinner dateSpinner = new JSpinner(new SpinnerDateModel());
            dateSpinner.setEditor(new JSpinner.DateEditor(dateSpinner, "MMM dd, yyyy"));
            if (JOptionPane.showOptionDialog(dialog, dateSpinner, "Select Due Date", JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE, null, null, null) == JOptionPane.OK_OPTION) {
                tempDate[0] = ((Date) dateSpinner.getValue()).toInstant().atZone(ZoneId.systemDefault()).toLocalDate();
                dateBtn.setText("📅  " + tempDate[0].format(DateTimeFormatter.ofPattern("MMM dd, yyyy")) + "  >");
            }
        });

        buttonsRow.add(reminderBtn); buttonsRow.add(priorityBtn);
        formPanel.add(titleField); formPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        formPanel.add(buttonsRow); formPanel.add(Box.createRigidArea(new Dimension(0, 15))); formPanel.add(dateBtn);

        JButton saveBtn = new JButton(isEditing ? "Save Changes" : "Add Task +");
        saveBtn.setBackground(Color.WHITE);
        saveBtn.setForeground(DARK_TEAL);
        saveBtn.setFocusPainted(false);
        saveBtn.setFont(new Font("SansSerif", Font.BOLD, 16));
        
        saveBtn.addActionListener(e -> {
            String title = titleField.getText().trim();
            if (!title.isEmpty() && !title.equals("Enter the task")) {
                if (isEditing) {
                    existingTask.setTitle(title); existingTask.setDueDate(tempDate[0]); existingTask.setPriority(tempPriority[0]); existingTask.setReminder(tempReminder[0]);
                } else {
                    Task newTask = new Task(title, "None", tempDate[0]);
                    newTask.setPriority(tempPriority[0]); newTask.setReminder(tempReminder[0]);
                    taskList.add(newTask);
                }
                To.saveTasks(taskList);
                updateTasks();
                dialog.dispose();
            }
        });

        dialog.add(formPanel, BorderLayout.CENTER);
        JPanel bottomPanel = new JPanel(); bottomPanel.setBackground(ACCENT_TEAL); bottomPanel.add(saveBtn);
        dialog.add(bottomPanel, BorderLayout.SOUTH);
        dialog.setVisible(true);
    }

    private JButton createDialogButton(String text) {
        JButton btn = new JButton(text);
        btn.setBackground(Color.WHITE); btn.setFocusPainted(false);
        btn.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createLineBorder(Color.WHITE, 1, true), new EmptyBorder(8, 15, 8, 15)));
        return btn;
    }

    private JButton createCircularAddButton() {
        JButton btn = new JButton("+") {
            @Override protected void paintComponent(Graphics g) {
                Graphics2D g2 = (Graphics2D) g.create();
                g2.setRenderingHint(RenderingHints.KEY_ANTIALIASING, RenderingHints.VALUE_ANTIALIAS_ON);
                g2.setColor(getModel().isArmed() ? DARK_TEAL : ACCENT_TEAL);
                g2.fillOval(0, 0, getSize().width - 1, getSize().height - 1);
                super.paintComponent(g2); g2.dispose();
            }
        };
        btn.setPreferredSize(new Dimension(60, 60)); btn.setFont(new Font("SansSerif", Font.PLAIN, 30));
        btn.setForeground(Color.WHITE); btn.setContentAreaFilled(false); btn.setBorderPainted(false);
        btn.setFocusPainted(false); btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
        return btn;
    }
}
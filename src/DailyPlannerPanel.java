import java.awt.*;
import java.awt.datatransfer.DataFlavor;
import java.awt.event.*;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import javax.swing.*;
import javax.swing.border.EmptyBorder;
import javax.swing.border.LineBorder;

public class DailyPlannerPanel extends JPanel {

    private static final Color BEIGE = new Color(248, 241, 228);
    private static final Color TEAL = new Color(164, 216, 216);
    private static final Color DARK_TEAL = new Color(50, 120, 120);
    private static final Color CALENDAR_BG = new Color(255, 255, 255);

    private ArrayList<Task> taskList;
    private LocalDate currentDate; // Knows what day we are looking at!
    
    private JPanel priorityContainer;
    private JPanel remindersContainer;
    private JPanel goalsList;
    private TimelinePanel timeline;
    private JLabel dateHeaderLabel;

    public DailyPlannerPanel(ArrayList<Task> taskList) {
        this.taskList = taskList;
        this.currentDate = LocalDate.now(); // Start on today
        
        setLayout(new GridLayout(1, 2, 20, 0)); 
        setBackground(BEIGE);
        setBorder(new EmptyBorder(20, 20, 20, 20));

        // --- LEFT COLUMN ---
        JPanel leftColumn = new JPanel(new GridLayout(3, 1, 0, 20)); 
        leftColumn.setBackground(BEIGE);
        
        leftColumn.add(createCard("Top priority tasks", priorityContainer = new JPanel(), false));
        leftColumn.add(createRemindersCard());
        leftColumn.add(createGoalsCard());

        // --- RIGHT COLUMN ---
        add(leftColumn);
        add(createCalendarTimeline());

        updateView();
    }

    // --- GENERIC CARD BUILDER ---
    private JPanel createCard(String title, JPanel container, boolean hasAddButton) {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(TEAL);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JLabel header = new JLabel(title);
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(DARK_TEAL);
        card.add(header, BorderLayout.NORTH);

        container.setLayout(new BoxLayout(container, BoxLayout.Y_AXIS));
        container.setBackground(TEAL);
        
        JScrollPane scroll = new JScrollPane(container);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TEAL);
        card.add(scroll, BorderLayout.CENTER);
        
        return card;
    }

    // --- REMINDERS CARD (With + Button) ---
    private JPanel createRemindersCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(TEAL);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TEAL);
        JLabel header = new JLabel("Reminders");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(DARK_TEAL);
        
        JButton addBtn = new JButton("+");
        styleButton(addBtn);
        addBtn.addActionListener(e -> addNewReminder());

        headerPanel.add(header, BorderLayout.WEST);
        headerPanel.add(addBtn, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        remindersContainer = new JPanel();
        remindersContainer.setLayout(new BoxLayout(remindersContainer, BoxLayout.Y_AXIS));
        remindersContainer.setBackground(TEAL);

        JScrollPane scroll = new JScrollPane(remindersContainer);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TEAL);
        card.add(scroll, BorderLayout.CENTER);
        
        return card;
    }

    private void addNewReminder() {
        String reminderText = JOptionPane.showInputDialog(this, "Enter new reminder for " + currentDate.format(DateTimeFormatter.ofPattern("MMM dd")) + ":");
        if (reminderText != null && !reminderText.trim().isEmpty()) {
            // Create a task, set the date to our currently viewed date, and add a dummy time (12:00 PM)
            Task newReminder = new Task(reminderText, "Reminder", currentDate);
            newReminder.setReminder(currentDate.atTime(12, 0)); 
            taskList.add(newReminder);
            
            // SAVE IT SO IT DOESN'T DISAPPEAR!
            ToDoApplication.saveTasks(taskList);
            updateView();
        }
    }

    // --- GOALS CARD (With + Button) ---
    private JPanel createGoalsCard() {
        JPanel card = new JPanel(new BorderLayout());
        card.setBackground(TEAL);
        card.setBorder(new EmptyBorder(15, 15, 15, 15));

        JPanel headerPanel = new JPanel(new BorderLayout());
        headerPanel.setBackground(TEAL);
        JLabel header = new JLabel("Goals");
        header.setFont(new Font("SansSerif", Font.BOLD, 22));
        header.setForeground(DARK_TEAL);
        
        JButton addBtn = new JButton("+");
        styleButton(addBtn);
        addBtn.addActionListener(e -> addNewGoal());

        headerPanel.add(header, BorderLayout.WEST);
        headerPanel.add(addBtn, BorderLayout.EAST);
        card.add(headerPanel, BorderLayout.NORTH);

        goalsList = new JPanel();
        goalsList.setLayout(new BoxLayout(goalsList, BoxLayout.Y_AXIS));
        goalsList.setBackground(TEAL);

        JScrollPane scroll = new JScrollPane(goalsList);
        scroll.setBorder(null);
        scroll.getViewport().setBackground(TEAL);
        card.add(scroll, BorderLayout.CENTER);
        
        return card;
    }

    private void addNewGoal() {
        String goalName = JOptionPane.showInputDialog(this, "Enter new goal:");
        if (goalName != null && !goalName.trim().isEmpty()) {
            goalsList.add(new JLabel(goalName));
            JProgressBar prog = new JProgressBar(0, 100);
            prog.setValue(0);
            prog.setForeground(DARK_TEAL);
            goalsList.add(prog);
            goalsList.add(Box.createRigidArea(new Dimension(0, 10)));
            goalsList.revalidate();
            goalsList.repaint();
        }
    }

    private void styleButton(JButton btn) {
        btn.setBackground(Color.WHITE);
        btn.setForeground(DARK_TEAL);
        btn.setFocusPainted(false);
        btn.setCursor(new Cursor(Cursor.HAND_CURSOR));
    }

    // --- CALENDAR WITH DATE NAVIGATION ---
    private JPanel createCalendarTimeline() {
        JPanel wrapper = new JPanel(new BorderLayout());
        wrapper.setBackground(CALENDAR_BG);
        wrapper.setBorder(new LineBorder(DARK_TEAL, 2, true));

        // Header for Date Navigation
        JPanel navHeader = new JPanel(new BorderLayout());
        navHeader.setBackground(CALENDAR_BG);
        navHeader.setBorder(new EmptyBorder(10, 10, 10, 10));

        JButton prevBtn = new JButton("<");
        JButton nextBtn = new JButton(">");
        styleButton(prevBtn);
        styleButton(nextBtn);

        dateHeaderLabel = new JLabel("", SwingConstants.CENTER);
        dateHeaderLabel.setFont(new Font("SansSerif", Font.BOLD, 20));
        dateHeaderLabel.setForeground(DARK_TEAL);

        prevBtn.addActionListener(e -> changeDate(-1));
        nextBtn.addActionListener(e -> changeDate(1));

        navHeader.add(prevBtn, BorderLayout.WEST);
        navHeader.add(dateHeaderLabel, BorderLayout.CENTER);
        navHeader.add(nextBtn, BorderLayout.EAST);
        
        wrapper.add(navHeader, BorderLayout.NORTH);

        timeline = new TimelinePanel();
        JScrollPane scroll = new JScrollPane(timeline);
        scroll.setBorder(null);
        scroll.getVerticalScrollBar().setUnitIncrement(16);
        wrapper.add(scroll, BorderLayout.CENTER);

        return wrapper;
    }

    private void changeDate(int days) {
        currentDate = currentDate.plusDays(days);
        timeline.removeAll(); // Clear visually drawn events for the new day
        timeline.repaint();
        updateView(); // Refresh tasks for the new date
    }

    // --- POPULATE TASKS FOR THE CURRENT DATE ---
    public void updateView() {
        dateHeaderLabel.setText(currentDate.format(DateTimeFormatter.ofPattern("EEEE, MMMM dd")));
        
        priorityContainer.removeAll();
        remindersContainer.removeAll();

        for (Task t : taskList) {
            // Priority: Matches Current Date + Is Red + Not Completed
            if (!t.isCompleted() && t.getDueDate().equals(currentDate) && "Red".equals(t.getPriority())) {
                JLabel taskLabel = new JLabel(t.getTitle());
                taskLabel.setFont(new Font("SansSerif", Font.BOLD, 16));
                taskLabel.setForeground(DARK_TEAL);
                
                // Allow Dragging
                taskLabel.setTransferHandler(new TransferHandler("text"));
                taskLabel.addMouseListener(new MouseAdapter() {
                    public void mousePressed(MouseEvent e) {
                        JComponent c = (JComponent) e.getSource();
                        c.getTransferHandler().exportAsDrag(c, e, TransferHandler.COPY);
                    }
                });
                
                priorityContainer.add(taskLabel);
                priorityContainer.add(Box.createRigidArea(new Dimension(0, 10)));
            }

            // Reminders: Matches Current Date + Has a Reminder Time
            if (!t.isCompleted() && t.getDueDate().equals(currentDate) && t.getReminder() != null) {
                String timeStr = t.getReminder().format(DateTimeFormatter.ofPattern("h:mm a"));
                JLabel remLabel = new JLabel("⏰ " + timeStr + " - " + t.getTitle());
                remLabel.setFont(new Font("SansSerif", Font.PLAIN, 15));
                remLabel.setForeground(TEXT_COLOR);
                remindersContainer.add(remLabel);
                remindersContainer.add(Box.createRigidArea(new Dimension(0, 8)));
            }
        }
        
        priorityContainer.revalidate(); priorityContainer.repaint();
        remindersContainer.revalidate(); remindersContainer.repaint();
    }

    // ==========================================================
    // CUSTOM TIMELINE WITH "DRAW TO CREATE EVENT" LOGIC
    // ==========================================================
    class TimelinePanel extends JPanel {
        private final int ROW_HEIGHT = 60; 
        private final int LEFT_MARGIN = 60; 
        
        // Variables to handle drawing a new event
        private int dragStartY = -1;
        private int dragCurrentY = -1;
        private boolean isDrawingEvent = false;

        public TimelinePanel() {
            setLayout(null); 
            setBackground(CALENDAR_BG);
            setPreferredSize(new Dimension(300, 14 * ROW_HEIGHT)); // 8 AM to 10 PM

            // --- 1. SUPPORT DROPPING EXISTING TASKS ---
            setTransferHandler(new TransferHandler() {
                public boolean canImport(TransferSupport support) {
                    return support.isDataFlavorSupported(DataFlavor.stringFlavor);
                }
                public boolean importData(TransferSupport support) {
                    try {
                        String taskTitle = (String) support.getTransferable().getTransferData(DataFlavor.stringFlavor);
                        int dropY = support.getDropLocation().getDropPoint().y;
                        int snappedY = Math.round((float)dropY / 30) * 30; // Snap to nearest 30 mins
                        addEventBlock(taskTitle, snappedY, 60); 
                        return true;
                    } catch (Exception e) { return false; }
                }
            });

            // --- 2. SUPPORT DRAGGING TO CREATE NEW EVENTS ---
            MouseAdapter dragToDraw = new MouseAdapter() {
                @Override
                public void mousePressed(MouseEvent e) {
                    if (e.getX() > LEFT_MARGIN) { // Only draw if clicking in the grid, not the time labels
                        isDrawingEvent = true;
                        dragStartY = Math.round((float)e.getY() / 30) * 30; // Snap start to grid
                        dragCurrentY = dragStartY + 30; // Minimum 30 min duration
                        repaint();
                    }
                }

                @Override
                public void mouseDragged(MouseEvent e) {
                    if (isDrawingEvent) {
                        int rawY = Math.max(e.getY(), dragStartY + 30); // Prevent dragging upwards past start
                        dragCurrentY = Math.round((float)rawY / 30) * 30; // Snap end to grid
                        repaint();
                    }
                }

                @Override
                public void mouseReleased(MouseEvent e) {
                    if (isDrawingEvent) {
                        isDrawingEvent = false;
                        int durationMinutes = dragCurrentY - dragStartY;
                        
                        String title = JOptionPane.showInputDialog(TimelinePanel.this, "New Event Title:");
                        if (title != null && !title.trim().isEmpty()) {
                            addEventBlock(title, dragStartY, durationMinutes);
                        }
                        repaint(); // Clear the ghost box
                    }
                }
            };

            addMouseListener(dragToDraw);
            addMouseMotionListener(dragToDraw);
        }

        private void addEventBlock(String title, int yPos, int durationMinutes) {
            JPanel block = new JPanel(new BorderLayout());
            block.setBackground(new Color(164, 216, 216, 200)); // Slightly transparent teal
            block.setBorder(BorderFactory.createCompoundBorder(
                new LineBorder(DARK_TEAL, 2),
                new EmptyBorder(5, 5, 5, 5)
            ));

            JLabel titleLabel = new JLabel("<html>" + title + "</html>");
            titleLabel.setFont(new Font("SansSerif", Font.BOLD, 14));
            titleLabel.setForeground(DARK_TEAL);
            titleLabel.setVerticalAlignment(SwingConstants.TOP);
            block.add(titleLabel, BorderLayout.CENTER);

            block.setBounds(LEFT_MARGIN + 10, yPos, getWidth() - LEFT_MARGIN - 30, durationMinutes);
            add(block);
            revalidate();
            repaint();
        }

        @Override
        protected void paintComponent(Graphics g) {
            super.paintComponent(g);
            g.setColor(Color.LIGHT_GRAY);
            g.setFont(new Font("SansSerif", Font.PLAIN, 12));

            // Draw Grid
            for (int i = 0; i < 14; i++) { 
                int y = i * ROW_HEIGHT;
                int currentHour = 8 + i;
                String timeStr = (currentHour > 12 ? currentHour - 12 : currentHour) + ":00 " + (currentHour >= 12 ? "PM" : "AM");
                
                g.drawString(timeStr, 5, y + 15);
                g.drawLine(LEFT_MARGIN, y, getWidth(), y);
                
                Graphics2D g2d = (Graphics2D) g.create();
                Stroke dashed = new BasicStroke(1, BasicStroke.CAP_BUTT, BasicStroke.JOIN_BEVEL, 0, new float[]{3}, 0);
                g2d.setStroke(dashed);
                g2d.drawLine(LEFT_MARGIN, y + 30, getWidth(), y + 30);
                g2d.dispose();
            }

            // Draw "Ghost Box" while dragging
            if (isDrawingEvent) {
                g.setColor(new Color(164, 216, 216, 100)); // Very transparent teal
                g.fillRect(LEFT_MARGIN + 10, dragStartY, getWidth() - LEFT_MARGIN - 30, dragCurrentY - dragStartY);
                g.setColor(DARK_TEAL);
                g.drawRect(LEFT_MARGIN + 10, dragStartY, getWidth() - LEFT_MARGIN - 30, dragCurrentY - dragStartY);
            }
        }
    }

    private static final Color TEXT_COLOR = new Color(30, 30, 30);
}

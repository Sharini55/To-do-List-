import java.awt.*;
import javax.swing.*;

public class MainGUI {

    // Your exact color palette
    private static final Color BG_COLOR = new Color(248, 241, 228);
    private static final Color SIDEBAR_COLOR = new Color(164, 216, 216); // Dark sidebar for contrast
    private static final Color BUTTON_HOVER = new Color(70, 70, 70);

    private JFrame frame;
    private JPanel mainContent;
    private CardLayout cardLayout;

    public static void main(String[] args) {
        WebApp.start();
        SwingUtilities.invokeLater(() -> new MainGUI().createAndShowGUI());
    }

    public void createAndShowGUI() {
        frame = new JFrame("Smart To-Do Application");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(1100, 800);
        frame.setLayout(new BorderLayout());

        // 1. Create the Left Sidebar
        JPanel sidebar = createSidebar();
        frame.add(sidebar, BorderLayout.WEST);

        // 2. Create the Center Content Area (The Cards)
        cardLayout = new CardLayout();
        mainContent = new JPanel(cardLayout);
        mainContent.setBackground(BG_COLOR);

        // 3. Load your actual task list!
        java.util.ArrayList<Task> myTasks = ToDoApplication.loadTasks();
        
        // 4. Create the real panels and pass the tasks to them
        DashboardPanel dashboard = new DashboardPanel(frame, myTasks);
        DailyPlannerPanel planner = new DailyPlannerPanel(myTasks);

        // 5. Add panels to the Card Layout so your sidebar buttons can switch between them
        mainContent.add(dashboard, "Dashboard");
        mainContent.add(planner, "Planner");

        frame.add(mainContent, BorderLayout.CENTER);

        // THE MOST IMPORTANT LINE: Turn the lights on!
        frame.setVisible(true);
    }

    private JPanel createSidebar() {
        JPanel sidebar = new JPanel();
        sidebar.setLayout(new BoxLayout(sidebar, BoxLayout.Y_AXIS));
        sidebar.setBackground(SIDEBAR_COLOR);
        sidebar.setPreferredSize(new Dimension(220, 0));
        sidebar.setBorder(BorderFactory.createEmptyBorder(20, 10, 20, 10));

        // App Title
        JLabel title = new JLabel("My App");
        title.setForeground(Color.WHITE);
        title.setFont(new Font("Serif", Font.BOLD, 24));
        title.setAlignmentX(Component.CENTER_ALIGNMENT);
        
        sidebar.add(title);
        sidebar.add(Box.createRigidArea(new Dimension(0, 40))); // Spacer

        // Menu Buttons
        JButton dashboardBtn = createMenuButton("🏠 Dashboard");
        JButton plannerBtn = createMenuButton("📝 Daily Planner");

        // Add actions to switch the cards!
        dashboardBtn.addActionListener(e -> cardLayout.show(mainContent, "Dashboard"));
        plannerBtn.addActionListener(e -> cardLayout.show(mainContent, "Planner"));

        sidebar.add(dashboardBtn);
        sidebar.add(Box.createRigidArea(new Dimension(0, 10)));
        sidebar.add(plannerBtn);

        return sidebar;
    }

    // Helper to make buttons look like a sleek menu list
    private JButton createMenuButton(String text) {
        JButton btn = new JButton(text);
        btn.setForeground(BG_COLOR); // Text color same as background for a "ghost" effect
        btn.setBackground(SIDEBAR_COLOR);
        btn.setFocusPainted(false);
        btn.setBorderPainted(false);
        btn.setFont(new Font("SansSerif", Font.PLAIN, 18));
        btn.setAlignmentX(Component.CENTER_ALIGNMENT);
        btn.setMaximumSize(new Dimension(200, 40));
        btn.setCursor(Cursor.getPredefinedCursor(Cursor.HAND_CURSOR));

        // Add hover effect
        btn.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseEntered(java.awt.event.MouseEvent evt) {
                btn.setBackground(BUTTON_HOVER);
            }
            public void mouseExited(java.awt.event.MouseEvent evt) {
                btn.setBackground(SIDEBAR_COLOR);
            }
        });
        return btn;
    }

    // Temporary method just so the code runs right now
    private JPanel createDummyPanel(String text) {
        JPanel panel = new JPanel(new GridBagLayout());
        panel.setBackground(BG_COLOR);
        JLabel label = new JLabel(text);
        label.setFont(new Font("Serif", Font.BOLD, 36));
        panel.add(label);
        return panel;
    }
}

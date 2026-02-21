import javax.swing.*;
import java.awt.*;

public class ToDoGUI {

    // Custom Colors matching your design
    private static final Color BG_COLOR = new Color(248, 241, 228); // Light Beige
    private static final Color BUTTON_COLOR = new Color(10, 45, 100); // Dark Navy Blue
    private static final Color TEXT_COLOR = new Color(30, 30, 30);

    public static void main(String[] args) {
        // This is the safe way to start a Swing GUI
        SwingUtilities.invokeLater(() -> createAndShowGUI());
    }

    private static void createAndShowGUI() {
        // 1. Create the Main Window
        JFrame frame = new JFrame("To Do List");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(800, 500);
        frame.setLayout(new BorderLayout());
        frame.getContentPane().setBackground(BG_COLOR);

        // 2. Create the Top Bar (Menu and Profile)
        JPanel topBar = new JPanel(new BorderLayout());
        topBar.setBackground(BG_COLOR);
        topBar.setBorder(BorderFactory.createEmptyBorder(20, 20, 20, 20)); // Padding

        // Menu Button
        JButton menuBtn = new JButton("Menu");
        menuBtn.setBackground(BUTTON_COLOR);
        menuBtn.setForeground(Color.WHITE);
        menuBtn.setFocusPainted(false);
        menuBtn.setFont(new Font("Serif", Font.BOLD, 14));
        menuBtn.setPreferredSize(new Dimension(80, 40));

        // Profile Placeholder (Just a dark blue circle-like button for now)
        JButton profileBtn = new JButton("👤");
        profileBtn.setBackground(BUTTON_COLOR);
        profileBtn.setForeground(Color.WHITE);
        profileBtn.setFocusPainted(false);
        profileBtn.setFont(new Font("SansSerif", Font.PLAIN, 20));
        profileBtn.setPreferredSize(new Dimension(50, 50));

        topBar.add(menuBtn, BorderLayout.WEST);
        topBar.add(profileBtn, BorderLayout.EAST);

        // 3. Create the Task List Area
        JPanel taskPanel = new JPanel();
        taskPanel.setLayout(new BoxLayout(taskPanel, BoxLayout.Y_AXIS));
        taskPanel.setBackground(BG_COLOR);
        taskPanel.setBorder(BorderFactory.createEmptyBorder(40, 100, 20, 20)); // Indent the list

        // Add the hardcoded tasks from your image to show how it looks
        taskPanel.add(createTaskItem("Finish designing GUI for TO do List app", false, new Font("Serif", Font.PLAIN, 24)));
        taskPanel.add(Box.createRigidArea(new Dimension(0, 15))); // Space between tasks
        
        taskPanel.add(createTaskItem("Add basic GUI", true, new Font("Serif", Font.PLAIN, 24)));
        taskPanel.add(Box.createRigidArea(new Dimension(0, 15)));
        
        taskPanel.add(createTaskItem("Add a task", false, new Font("Serif", Font.ITALIC, 24)));

        // 4. Put it all together
        frame.add(topBar, BorderLayout.NORTH);
        frame.add(taskPanel, BorderLayout.CENTER);

        // Display the window
        frame.setLocationRelativeTo(null); // Centers on screen
        frame.setVisible(true);
    }

    // --- HELPER METHOD TO DRAW A TASK ---
    private static JPanel createTaskItem(String text, boolean isDone, Font font) {
        JPanel panel = new JPanel(new FlowLayout(FlowLayout.LEFT));
        panel.setBackground(BG_COLOR);

        // The bullet point (circle)
        JLabel bullet = new JLabel("●  ");
        bullet.setForeground(BUTTON_COLOR);
        bullet.setFont(new Font("SansSerif", Font.PLAIN, 18));

        // The Text
        JLabel taskLabel = new JLabel();
        taskLabel.setFont(font);
        taskLabel.setForeground(TEXT_COLOR);

        // Cool trick: We use basic HTML to strike through the text!
    // Temporary fix to avoid Java 8 font crash
        if (isDone) {
            taskLabel.setText(text + " (DONE)"); 
            taskLabel.setForeground(Color.GRAY); // Make it look faded instead
        } else {
            taskLabel.setText(text);
        }
        panel.add(bullet);
        panel.add(taskLabel);

        return panel;
    }
}
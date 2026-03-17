# To-Do List Application

A modern, multi-view Graphical User Interface (GUI) productivity application built in Java Swing.

This application goes beyond a standard to-do list by combining a powerful task manager with a visual daily timeline planner. It allows users to visually manage their workflow, set precise reminders, assign priority levels, and keep their days organized. All tasks are stored locally, so they persist seamlessly between sessions.

✨ Features
Multi-View Navigation: Use the sleek left sidebar to seamlessly toggle between your high-level Dashboard and your detailed Daily Planner.

Interactive Dashboard: A clean, visually appealing interface. Click the circular checkboxes to strike through tasks and mark them as complete. Track your daily momentum with a dynamic progress bar.

Visual Daily Planner: A dedicated timeline view (8:00 AM - 9:00 PM) that allows you to visually block out your day, see tasks with specific time reminders, and view your top priorities at a glance.

Priority System: Assign color-coded priority levels (Red, Yellow, Green) to tasks to ensure you tackle your most important work first.

Click-to-Edit Menus: Easily interact with any task to update its title, change its due date, update its reminder time, or delete it entirely.

Smart Sorting: Tasks are automatically sorted chronologically by their due dates every time the application refreshes.

Clean Workspace: Use the top menu to toggle between viewing all tasks or hiding completed tasks to declutter your list.

Persistent Storage: Automatically saves all task data, completion status, priorities, and timeframes to a local tasks.txt file. You never lose your progress.

🚀 Installation & Setup
Prerequisites: Ensure you have the Java Development Kit (JDK) installed on your machine.

Clone the repository:

Bash
git clone https://github.com/Sharini55/To-do-List-.git

Bash
java MainGUI
📂 Architecture Overview
MainGUI.java: The main entry point. Sets up the modern window frame, the sidebar navigation, and the CardLayout that switches between views.

DashboardPanel.java: The primary view containing the general task list, visual calendar, and progress bar.

DailyPlannerPanel.java: The secondary view featuring the hourly timeline, priority lists, and goal tracking.

Task.java: The core data model representing a single task (stores title, dates, completion status, and priority).

ToDoApplication.java: The backend file handling the reading and writing of tasks to the local tasks.txt file.

# To-Do List Application

A modern, interactive Graphical User Interface (GUI) To-Do List application built in Java Swing, with a fully functional classic console-based alternative. 

This application allows users to visually manage their daily tasks, set precise reminders, and keep their workflow organized. All tasks are stored locally in a file so they persist seamlessly between sessions.

## Features

* **Interactive GUI:** Clean, visually appealing interface with clickable checkboxes to easily strike through and mark tasks as complete.
* **Custom Visual Calendar:** Includes a custom-built, interactive drop-down calendar and time spinner for effortlessly selecting due dates and precise reminders.
* **Click-to-Edit Menus:** Left-click any task to quickly edit its title, change its due date, update its reminder time, or delete it entirely.
* **Smart Sorting:** Tasks are automatically sorted chronologically by their due dates every time the screen refreshes.
* **Clean Workspace:** Use the top menu to toggle between viewing all tasks or hiding completed tasks to declutter your list.
* **Persistent Storage:** Automatically saves all task data, completion status, and timeframes to a local `tasks.txt` file.


This project has two different `main` classes. 

* **`ToDoGUI.java` (Graphical Mode):** The primary, modern way to use the app. It launches the visual window with clickable menus, the custom calendar, and checkboxes. 
* **`To.java` (Console Mode):** The original, lightweight, text-based terminal version. It runs entirely in the command line using numbered menus. Great for quick edits, developers, or running on systems without a graphical interface.

*Note: Both versions read and save to the same `tasks.txt` file. A task added in the GUI will perfectly sync and appear in the console version, and vice versa!*

## Installation

1. Clone the repository:
```bash
git clone [https://github.com/Sharini55/To-do-List-.git]

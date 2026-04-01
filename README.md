# 🚀 AI-Powered To-Do & Productivity Platform

![Java](https://img.shields.io/badge/Java-21-orange.svg)
![Spring Boot](https://img.shields.io/badge/Spring%20Boot-3.2.0-brightgreen.svg)
![Azure](https://img.shields.io/badge/Deployed%20on-Azure-blue.svg)


A modern, cloud-native task management and habit-tracking application featuring a sleek mobile-first UI and an integrated AI productivity assistant. 

**Live Demo:** [todoapp-a7a8f7f3djb7h8b8.eastus-01.azurewebsites.net](https://todoapp-a7a8f7f3djb7h8b8.eastus-01.azurewebsites.net/)

## 💡 The Problem & The Solution
For many people, organizing their day is a daunting task. Setting up planners, migrating tasks, and tracking habits can be more intimidating and time-consuming than the actual work itself. This friction drives people away from staying organized.

**The Solution:** An app that does the organizing for you. By integrating an AI agent, users can bypass manual data entry entirely. Simply tell the AI, *"I have a math assignment due tomorrow and a dentist appointment on Friday,"* or ask, *"What do I have overdue?"* and the application handles the creation, scheduling, and progress reporting automatically.

## ✨ Key Features

* 🤖 **AI Productivity Assistant:** A conversational interface. Use natural language to seamlessly add tasks, schedule calendar events, and request daily overviews. 
* 📱 **Sleek Mobile-First UI:** A highly responsive, intuitive interface featuring dedicated pages for Task Management, Habit Tracking, and Calendar Views.
* 📊 **Progress Reports & Analytics:** The AI agent analyzes your workflow to generate personalized productivity states and progress reports, helping you maintain momentum.
* 🚨 **Smart Overdue & Priority Tracking:** Automatically tracks overdue tasks, sets reminders, and categorizes priorities (Red, Yellow, Green) so you always know what to tackle first.
* ☁️ **Cloud Persistence:** Fully deployed on Azure App Service with robust backend architecture, ensuring your data is persistently saved and accessible from anywhere.

## 🛠️ Technical Architecture

This project transitioned from a local Java Swing application to a modern microservices architecture:

* **Backend:** Java 21 & Spring Boot 3.2.0
* **Testing:** Comprehensive automated testing using JUnit 5 and MockMvc.
* **CI/CD Pipeline:** Fully automated deployment utilizing GitHub Actions (`main_todoapp.yml`) for continuous integration and delivery.
* **Hosting:** Microsoft Azure App Service.

## 📂 Core Backend Structure

* `TodoAppApplication.java`: The main Spring Boot application entry point.
* `TaskController.java`: Handles secure RESTful API routing for all CRUD operations related to tasks and habits.
* `GeminiController.java`: The AI engine. Takes user prompts, injects system context, and forces the LLM to output structured JSON actions (`add_task`, `add_event`, `get_summary`) to dynamically update the UI.
* `Task.java`: The core data model representing task states, deadlines, and priorities.
* `TaskControllerTest.java`: Automated test suites ensuring endpoint reliability, HTTP method validation, and stress testing.

## 🚀 Local Installation & Setup

**Prerequisites:** Ensure you have Java 21 and Maven installed on your machine.

1. **Clone the repository:**
   ```bash
   git clone [https://github.com/Sharini55/To-do-List-.git](https://github.com/Sharini55/To-do-List-.git)
   cd To-do-List-

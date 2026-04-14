# Workflow Management System

## Overview

This project is a simplified workflow and task management system built with Spring Boot. It is designed to support a small team workflow similar to a lightweight Jira-style application.

The system supports:

- login-based access
- manager and employee roles
- task creation and assignment
- task editing
- task status tracking
- project-based organization
- priority and task type tracking
- task archiving
- persistent storage with an H2 database

## Current Roles

The application currently supports two user roles:

### Manager

Managers can:

- log in and view all active tasks
- create tasks
- edit tasks
- assign tasks to employees
- archive tasks
- update task statuses

### Employee

Employees can:

- log in and view only tasks assigned to them
- update the status of their own tasks

Employees cannot:

- create tasks
- edit tasks
- archive tasks
- see tasks assigned to other employees

## Current Task Fields

Each workflow item can include:

- title
- description
- project
- priority
- task type
- assignee
- creator
- due date
- status
- created timestamp
- archive state

## Workflow Status Flow

The current workflow progression is:

`SUBMITTED -> IN_REVIEW -> APPROVED -> COMPLETED`

The service enforces valid transitions.

## Tech Stack

- Java 17
- Spring Boot 4
- Spring MVC
- Spring Security
- Spring Data JPA
- H2 Database
- Thymeleaf
- Maven

## How To Run

From the project directory:

```bash
cd /Users/justin/Projects/software-engineering/workflow
./mvnw spring-boot:run
```

Then open:

[http://localhost:8080](http://localhost:8080)

## Demo Accounts

### Manager

- Username: `manager`
- Password: `manager123`

### Employees

- Username: `justin`
- Password: `employee123`

- Username: `johnny`
- Password: `employee123`

- Username: `sevin`
- Password: `employee123`

## How To Test The System Manually

### Manager workflow

1. Log in as `manager`
2. View all tasks on the dashboard
3. Create a new task
4. Assign the task to `justin`, `johnny`, or `sevin`
5. Edit a task
6. Update a task status
7. Archive a task
8. Log out

### Employee workflow

1. Log in as one of the employee accounts
2. Confirm that only assigned tasks are visible
3. Update the status of one assigned task
4. Confirm there are no create, edit, or archive options
5. Log out

### Persistence check

1. Start the application
2. Make a change such as creating or updating a task
3. Stop the application
4. Start it again
5. Confirm the task data is still present

## Running Tests

Run:

```bash
./mvnw test
```

This verifies:

- application startup
- task creation
- role-based task visibility
- role-based task permissions
- status transition behavior
- archiving behavior

## Project Structure

Main files and responsibilities:

- [WorkflowApplication.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowApplication.java) - Spring Boot entry point
- [SecurityConfig.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/SecurityConfig.java) - login and authorization rules
- [WorkflowController.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowController.java) - web routes and page handling
- [WorkflowService.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowService.java) - business logic and permissions
- [WorkflowItem.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowItem.java) - task entity
- [AppUser.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/AppUser.java) - user entity
- [WorkflowRepository.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowRepository.java) - task persistence
- [AppUserRepository.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/AppUserRepository.java) - user persistence
- [DataInitializer.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/DataInitializer.java) - seeded demo users and starter tasks
- [dashboard.html](/Users/justin/Projects/software-engineering/workflow/src/main/resources/templates/dashboard.html) - dashboard UI
- [create.html](/Users/justin/Projects/software-engineering/workflow/src/main/resources/templates/create.html) - create/edit task UI

## Current Limitations

The system is functional, but still simplified:

- no notifications
- no comments
- no drag-and-drop Kanban board
- no advanced role hierarchy
- no file attachments
- no email integration

## Notes For The Team

- If you change the database model significantly, you may need to reset the local H2 database files.
- The seeded users are created automatically when the app starts.
- The application is meant for class demonstration and sprint deliverables, not production use.

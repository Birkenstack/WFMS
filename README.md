# Workflow Management System

## Overview

This project is a simplified workflow management system built for the software engineering semester project. It supports authenticated users, manager and employee roles, task assignment, workflow status tracking, and persistent storage.

The current system is intended to demonstrate an integrated task workflow application similar to a lightweight Jira-style tool.

## Core Features

- Login-based access using Spring Security
- Two user roles: `MANAGER` and `EMPLOYEE`
- Manager task creation, editing, assignment, and archiving
- Employee task visibility limited to assigned work
- Workflow status progression:
  - `SUBMITTED`
  - `IN_REVIEW`
  - `APPROVED`
  - `COMPLETED`
- Task metadata:
  - project
  - priority
  - task type
  - assignee
  - creator
  - due date
- Persistent storage using H2 and Spring Data JPA
- Server-rendered UI with Thymeleaf

## Roles

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

- log in and view only the tasks assigned to them
- update the status of their own tasks

Employees cannot:

- create tasks
- edit tasks
- archive tasks
- see tasks assigned to other employees

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

[http://localhost:8080/login](http://localhost:8080/login)

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

## Manual Demo Flow

### Manager flow

1. Log in as `manager`
2. View all workflow items on the dashboard
3. Create a new task
4. Edit a task
5. Update a task status
6. Archive a task
7. Log out

### Employee flow

1. Log in as `justin`, `johnny`, or `sevin`
2. Confirm only assigned tasks are visible
3. Update one task status
4. Confirm create/edit/archive controls are not available
5. Log out

## Running Tests

Run:

```bash
./mvnw test
```

The test suite covers:

- application startup
- role-based task visibility
- role-based permissions
- task creation
- task editing
- status transition rules
- archiving behavior

## Important Files

- [src/main/java/com/example/workflow/WorkflowApplication.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowApplication.java) - Spring Boot entry point
- [src/main/java/com/example/workflow/SecurityConfig.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/SecurityConfig.java) - authentication and authorization rules
- [src/main/java/com/example/workflow/WorkflowController.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowController.java) - page routing and web requests
- [src/main/java/com/example/workflow/WorkflowService.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowService.java) - workflow rules and role-based behavior
- [src/main/java/com/example/workflow/WorkflowItem.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/WorkflowItem.java) - task entity
- [src/main/java/com/example/workflow/AppUser.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/AppUser.java) - authenticated user entity
- [src/main/java/com/example/workflow/DataInitializer.java](/Users/justin/Projects/software-engineering/workflow/src/main/java/com/example/workflow/DataInitializer.java) - seeded demo users and tasks
- [src/main/resources/templates/login.html](/Users/justin/Projects/software-engineering/workflow/src/main/resources/templates/login.html) - login page
- [src/main/resources/templates/dashboard.html](/Users/justin/Projects/software-engineering/workflow/src/main/resources/templates/dashboard.html) - main dashboard
- [src/main/resources/templates/create.html](/Users/justin/Projects/software-engineering/workflow/src/main/resources/templates/create.html) - create/edit task form

## Notes

- The local H2 database is created automatically when the app starts.
- If demo data needs a full reset, stop the app and remove local H2 database files before restarting.
- This is a class project system, not a production deployment.

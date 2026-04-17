# Sprint 3 Report

## 1. Sprint Plan

### Sprint Goal

The goal of Sprint 3 was to move the project from a basic workflow prototype into a more integrated task management system. This sprint focused on implementing multiple high-priority user stories, refining component responsibilities, correcting the architecture description from Sprint 2, and preparing the system for final presentation.

### Selected User Stories / Tasks

- As a manager, I can log in and view all active workflow items.
- As a manager, I can create and assign workflow items to employees.
- As a manager, I can edit and archive workflow items.
- As an employee, I can log in and see only the tasks assigned to me.
- As an employee, I can update the status of my assigned tasks.
- As a user, I can return to the system and still see saved workflow data.

### Why These Stories Were Selected

These stories were selected because they complete the core workflow cycle of creating, assigning, tracking, and updating work items while also supporting multiple users and permissions. They were the highest-value features needed to turn the project into a coherent, runnable system instead of a simple prototype.

## 2. Updated Architecture and Design Notes

### Updated System Overview

The system uses a **client-server architectural style**. The client side is the user interface, delivered through server-rendered web pages. The server side handles authentication, business logic, workflow rules, and persistent data storage.

Inside the server application, the project uses the **Model-View-Controller (MVC)** pattern to organize code. This corrects the misunderstanding from Sprint 2: MVC is an internal design pattern, not the top-level system architecture.

### Refined Components and Responsibilities

The major components for the current system are:

- **UI Component**  
  Presents the login page, manager dashboard, employee dashboard, and task create/edit forms.

- **Authentication Component**  
  Handles login, logout, session management, and role-based access using Spring Security.

- **Workflow Service**  
  Applies business rules for creating tasks, assigning work, updating status, archiving tasks, and enforcing manager/employee permissions.

- **Workflow Repository**  
  Stores and retrieves workflow items using Spring Data JPA and H2.

- **User Repository**  
  Stores and retrieves authenticated users.

- **Workflow Item**  
  Represents a task in the system with title, description, project, priority, type, assignee, creator, due date, archive state, and workflow status.

- **Workflow Status**  
  Represents the lifecycle state of a workflow item.

- **User**  
  Represents an authenticated actor in the system with a username, password, display name, and role.

### Component Interactions

The integrated workflow is:

1. A manager or employee interacts with the **UI Component**.
2. The UI sends requests to the controller layer in the Spring Boot application.
3. The **Authentication Component** verifies identity and role access.
4. The **Workflow Service** applies workflow rules and permissions.
5. The **Workflow Repository** and **User Repository** retrieve and store data in the database.
6. Updated data is returned to the UI and displayed to the user.

### Changes from Sprint 2 and Justification

The most important design changes from Sprint 2 were:

- The architecture description was corrected from mixing MVC with overall system architecture.
- Browser, view layer, controller, and filenames were removed as top-level architectural components.
- Authentication and user roles were added as actual system-level functionality.
- The system moved from an earlier lightweight persistence approach to a database-backed model using H2 and JPA.
- Component names were made consistent between the design description and implementation.

These changes were necessary because the Sprint 2 feedback showed that the earlier design discussion was using the wrong level of abstraction.

### Component-Level Design Improvements

Sprint 3 improved the design at the component level by:

- adding `AppUser` and role support
- connecting workflow items to actual user records
- splitting manager and employee behavior in the service layer
- enforcing workflow status transitions
- moving task storage to the database
- adding seeded demo users and demo tasks for integrated testing and demonstration

## 3. Integrated Runnable System

The current system is a working integrated application. It now includes:

- authenticated login
- manager and employee roles
- manager workflows for creating, editing, assigning, and archiving tasks
- employee workflows for viewing assigned tasks and updating status
- persistent storage using H2
- project-based organization and richer task metadata
- a runnable UI built with Thymeleaf

This means the system now demonstrates multiple integrated components and multiple user workflows, which directly satisfies the Sprint 3 implementation requirement.

## 4. Testing Evidence

Testing was verified with:

```bash
./mvnw test
```

The test suite covers:

- application startup
- task creation
- role-based task visibility
- role-based task permissions
- valid status progression
- restricted status behavior for employees
- archiving behavior

This provides both unit/service-level evidence and application-level evidence that the integrated system is functioning.

## 5. Sprint Report

### What Was Completed

Sprint 3 completed the major system integration work:

- authentication with Spring Security
- manager and employee roles
- persistent storage with H2 and Spring Data JPA
- manager workflows for create, edit, assign, and archive
- employee workflows for assigned-task visibility and status updates
- richer task model with project, priority, task type, assignee, creator, and due date
- UI improvements for login, dashboard, and task forms
- expanded automated testing

### Challenges Encountered

The main challenges were integration-related:

- correcting the architectural misunderstanding from Sprint 2
- migrating from prototype-style persistence to database-backed storage
- integrating authentication and authorization into the existing task workflow
- handling seeded demo users and task data consistently
- resolving login/logout and custom login page behavior

### What Was Deferred or Changed

The following items were not implemented in Sprint 3:

- notifications
- drag-and-drop Kanban behavior
- comments/history
- advanced role hierarchy
- attachments

These features were deferred because the priority for Sprint 3 was delivering a stable integrated core system.

### Reflection on Sprint Effectiveness

Sprint 3 was effective because it delivered the most important transition in the project: moving from a basic task prototype to a much more complete workflow management system. It also corrected the architectural issues identified in Sprint 2 and produced a system that is much easier to explain, test, and demonstrate.

## 6. Updated Product Backlog

### Completed

- login/logout
- manager role
- employee role
- role-based permissions
- database-backed persistence
- task creation
- task editing
- task assignment
- task archiving
- employee task visibility
- status updates
- project, priority, type, and due date fields
- seeded demo accounts and tasks
- improved UI
- expanded testing

### Remaining / Lower Priority

- drag-and-drop Kanban board
- comments or history tracking
- notifications
- deeper controller/integration tests
- final report polish
- final demo preparation and screenshots

## Conclusion

Sprint 3 corrected the architectural weaknesses from Sprint 2 and delivered a much stronger integrated system. The project now demonstrates authentication, role-based access, database persistence, multiple user workflows, and a clearer component-based design. As a result, the system is in a much better position for final presentation and final project completion.

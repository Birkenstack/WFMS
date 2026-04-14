# Sprint 3 Report

## Project

**Project Name:** Workflow Management System  
**Sprint:** Sprint 3

## Introduction

Sprint 3 focused on turning the workflow project into a more complete task workflow application. Earlier versions of the project supported only basic task creation, assignment, and status updates. In this sprint, the system was extended so that it behaves more like a simplified project workflow tool. The sprint also continued to align the documentation with the instructor's architecture feedback from Sprint 2.

## Architectural Position

The system uses a **client-server architectural style**. The client side provides the user interface through HTML pages, while the server side handles business logic, workflow rules, and persistent storage.

Inside the server application, the implementation uses the **Model-View-Controller (MVC)** pattern. MVC is used to organize the code internally and is not the same thing as the overall system architecture.

The major components of the system are:

- UI Component
- Workflow Service
- Workflow Repository
- Workflow Item
- Workflow Status

These component names are consistent with the current codebase and with the design terminology requested in Sprint 2 feedback.

## Sprint 3 Goals

The main goals for Sprint 3 were:

1. Improve the workflow application beyond a basic prototype.
2. Add richer task data so the system feels more like a project management tool.
3. Support more realistic task management actions such as editing, filtering, and archiving.
4. Keep persistent storage and testing in place.
5. Prepare the project for a strong final presentation phase.

## Work Completed

### 1. Richer Task Model

The workflow item model was expanded so that tasks now include additional fields beyond title, description, assignee, and status. Each task can now also store:

- project name
- priority
- task type
- creator
- due date
- created timestamp
- archive state

This makes the application closer to a lightweight Jira-style workflow tracker.

### 2. Persistent Storage Maintained

The repository continues to save workflow data to a local JSON file. Sprint 3 preserved this persistence while extending the stored data model to support the new task fields.

### 3. Task Editing and Archiving

The application now supports editing existing workflow items. Users can update important task details without recreating the item from scratch. The system also supports archiving tasks so completed or removed work items do not remain in the active dashboard view.

### 4. Project-Based Dashboard and Filtering

The dashboard was improved so tasks can be viewed in a more organized way. Tasks are now grouped by workflow status, and the user can filter tasks by:

- assignee
- project
- priority

This makes the application significantly easier to demonstrate and more useful for project tracking.

### 5. Workflow Rules and Validation

The workflow still enforces a controlled status progression:

`SUBMITTED -> IN_REVIEW -> APPROVED -> COMPLETED`

The system validates required fields during task creation and editing, rejects invalid status transitions, and prevents updates to missing tasks.

### 6. Expanded Automated Testing

The test suite was expanded to cover the new behavior introduced in Sprint 3. The tests now verify:

- workflow item creation with richer fields
- valid workflow status progression
- invalid status transition handling
- repository persistence across reloads
- task editing
- archiving behavior
- filtering by project and priority

This provides stronger evidence that the system works correctly.

## Current System Behavior

At the end of Sprint 3, the system supports:

- creating workflow items
- editing workflow items
- assigning tasks to users
- organizing tasks by project
- tracking task type and priority
- filtering tasks on the dashboard
- updating workflow status in valid order
- archiving tasks
- saving workflow data between application restarts

## Remaining Limitations

Even with the Sprint 3 improvements, the project still has some limitations:

- there is no full login or authentication system
- persistence is file-based rather than database-backed
- the UI is functional but still simple
- the system does not yet include comments, notifications, or role-based permissions
- there is no Kanban drag-and-drop interface

These limitations are acceptable for the current stage, but they define the difference between this project and a full production project management platform.

## Verification

The project was verified with:

`./mvnw test`

All tests passed successfully.

## Conclusion

Sprint 3 successfully moved the project from a basic workflow prototype to a more capable task workflow system. The system now supports richer task information, project-based organization, editing, archiving, filtering, and stronger automated testing. As a result, the project is in a much better position for the final presentation phase and can now be presented as a simplified workflow management tool rather than only a status-tracking demo.

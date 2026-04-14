# Sprint 3 Readiness Report

## Project Status

The workflow project is in a workable state for beginning Sprint 3. The current implementation runs as a Spring Boot web application and the project passes its existing test suite with `./mvnw test`. At this stage, the project should be described as a functioning prototype rather than a finished product. The core workflow concepts already exist in the codebase, including workflow items, workflow status, a workflow service, a workflow repository, and a user-facing interface for creating and updating items.

Although the codebase is ready enough to support Sprint 3 work, the main area that needs correction is the architectural documentation. Based on the Sprint 2 feedback, the team should revise the architecture description so that it uses the correct level of abstraction and consistent terminology.

## Architectural Clarifications for Sprint 3

The system should be described using a **client-server architectural style**. This explains where parts of the system execute. The client side is responsible for presenting the user interface, and the server side is responsible for managing workflow logic and data.

Within the server-side application, the team may state that the implementation uses the **Model-View-Controller (MVC)** pattern to organize code. MVC is not the overall system architecture. Instead, it is a design pattern used inside the application to separate data, presentation, and control flow.

For Sprint 3, the major components should be listed at the system functionality level rather than at the framework or file level. Based on the current prototype and the instructor feedback, the following components are appropriate:

- UI Component
- Workflow Service
- Workflow Repository
- Workflow Item
- Workflow Status

The following should **not** be presented as major system components:

- Browser
- View Layer
- Controller
- Specific file names or class files

The browser is part of the client environment, not a system component. The controller is part of the MVC pattern used inside the server application, not a top-level functional component. File names should also not appear in the architecture section because they are implementation details rather than design-level elements.

## Alignment with Current Implementation

The current implementation already supports the component names recommended by the instructor:

- `WorkflowService` manages workflow-related logic.
- `WorkflowRepository` stores and retrieves workflow items.
- `WorkflowItem` represents the main workflow data object.
- `Status` represents workflow state.
- The HTML templates provide the user-facing interface and can be described together as the UI component.

This means the team does not need to invent a new architecture for Sprint 3. Instead, the team should refine the documentation so that it accurately describes what the system is doing at the correct architectural level.

## Current Strengths

The project has several strengths that make it suitable for moving into Sprint 3:

- The codebase is organized into clear responsibilities.
- The application already supports creating workflow items.
- Assigned users can view their tasks on the dashboard.
- Workflow status can be updated through the interface.
- The project builds and the current automated test passes.

These strengths show that the project has a usable starting point for continued development.

## Current Risks and Limitations

Even though the project is ready for Sprint 3, several limitations should be acknowledged:

- The current user is hardcoded as `Justin`, so there is no real authentication or user management yet.
- Data is stored only in memory, so workflow items are lost when the application restarts.
- The status update logic is minimal and does not handle missing IDs or invalid transitions.
- The automated testing is very limited and currently checks only that the Spring application context loads.
- The user interface is basic and may need additional validation, usability improvements, and error handling before final submission.

These issues do not prevent Sprint 3 from starting, but they should shape the team’s priorities going forward.

## Recommendation for Sprint 3

The project is **ready to begin Sprint 3**, but the team should treat Sprint 3 as an opportunity to correct the design documentation and strengthen the implementation for the final phase of the project.

The recommended Sprint 3 priorities are:

1. Revise the architecture section to clearly distinguish client-server architecture from MVC.
2. Use consistent component names throughout the document.
3. Remove references to browser, controller, view layer, and file names as major components.
4. Expand the implementation with more robust workflow behavior where needed.
5. Add more meaningful tests for creating items, viewing assigned items, and updating status.
6. Plan for how persistence and user handling will be addressed before the final submission.

## Conclusion

Overall, the workflow project is in acceptable shape for starting Sprint 3. The codebase provides a reasonable functional prototype, and there are no immediate technical blockers preventing continued development. The most important correction is to improve the architectural description so that it reflects proper software engineering terminology and matches the instructor’s feedback. If the team makes those documentation changes and uses Sprint 3 to reduce the current implementation risks, the project should be in a much stronger position for completion.

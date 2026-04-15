package com.example.workflow;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.TestPropertySource;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
@TestPropertySource(properties = {
        "spring.datasource.url=jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1",
        "spring.jpa.hibernate.ddl-auto=create-drop"
})
class WorkflowServiceTests {

    @Autowired
    private WorkflowService service;

    @Autowired
    private AppUserRepository userRepository;

    @Test
    void managerCanCreateRichWorkflowItem() {
        WorkflowItem item = service.createItem(
                "Sprint Demo",
                "Prepare the final sprint demo",
                "justin",
                "Workflow",
                Priority.HIGH,
                TaskType.STORY,
                "manager",
                LocalDate.now().plusDays(2));

        assertEquals("Workflow", item.getProject());
        assertEquals("justin", item.getAssignee().getUsername());
        assertEquals("manager", item.getCreatedBy().getUsername());
        assertEquals(Status.SUBMITTED, item.getStatus());
    }

    @Test
    void managerCanEditTaskDetails() {
        WorkflowItem item = service.createItem(
                "Old Title", "Old Desc", "justin", "Workflow",
                Priority.LOW, TaskType.TASK, "manager", null);

        WorkflowItem updated = service.updateItem(
                item.getId(), "New Title", "New Desc", "johnny",
                "Platform", Priority.HIGH, TaskType.BUG,
                LocalDate.now().plusDays(5), "manager");

        assertEquals("New Title", updated.getTitle());
        assertEquals("johnny", updated.getAssignee().getUsername());
        assertEquals("Platform", updated.getProject());
    }

    @Test
    void employeeOnlySeesAssignedTasks() {
        service.createItem("Assigned to Justin", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);
        service.createItem("Assigned to Johnny", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        var visible = service.getVisibleItems("justin");

        assertTrue(visible.stream().allMatch(item -> item.getAssignee().getUsername().equals("justin")));
    }

    @Test
    void employeeCannotArchiveTasks() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        assertThrows(IllegalStateException.class, () -> service.archiveItem(item.getId(), "justin"));
    }

    @Test
    void employeeCanProgressOwnTaskThroughAllowedStatuses() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.updateStatus(item.getId(), Status.IN_REVIEW, "justin");
        service.updateStatus(item.getId(), Status.APPROVED, "manager");
        service.updateStatus(item.getId(), Status.COMPLETED, "justin");

        WorkflowItem updated = service.getItem(item.getId(), "manager");
        assertEquals(Status.COMPLETED, updated.getStatus());
    }

    @Test
    void employeeCannotApproveTaskDirectly() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        assertThrows(IllegalStateException.class,
                () -> service.updateStatus(item.getId(), Status.APPROVED, "justin"));
    }

    @Test
    void managerCanArchiveTask() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.archiveItem(item.getId(), "manager");

        assertTrue(service.getVisibleItems("manager").stream().noneMatch(task -> task.getId().equals(item.getId())));
    }

    @Test
    void seededUsersExistWithExpectedRoles() {
        assertEquals(Role.MANAGER, userRepository.findByUsernameIgnoreCase("manager").orElseThrow().getRole());
        assertEquals(Role.EMPLOYEE, userRepository.findByUsernameIgnoreCase("justin").orElseThrow().getRole());
        assertEquals(Role.EMPLOYEE, userRepository.findByUsernameIgnoreCase("johnny").orElseThrow().getRole());
        assertEquals(Role.EMPLOYEE, userRepository.findByUsernameIgnoreCase("sevin").orElseThrow().getRole());
    }

    @Test
    void archivedTaskAppearsInArchivedList(){
        WorkflowItem item = service.createItem("Employee Task",
            "Justin's work",
            "justin",
            "Platform",
            Priority.MEDIUM,
            TaskType.TASK,
            "manager",
            null);
        
        service.archiveItem(item.getId(), "manager");
        var archivedItems = service.getArchivedItems();

        assertTrue(archivedItems.stream().anyMatch(archivedItem -> archivedItem.getId().equals(item.getId())));

    }
}

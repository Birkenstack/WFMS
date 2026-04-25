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

    @Autowired
    private AuditLogRepository auditLogRepository;

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
        assertEquals(Status.BACKLOG, item.getStatus());
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
    void employeeDefaultViewShowsOnlyAssignedWork() {
        WorkflowItem justinTask = service.createItem("Assigned to Justin", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);
        WorkflowItem johnnyTask = service.createItem("Assigned to Johnny", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        var visible = service.getVisibleItems("justin");

        assertTrue(visible.stream().anyMatch(item -> item.getId().equals(justinTask.getId())));
        assertTrue(visible.stream().noneMatch(item -> item.getId().equals(johnnyTask.getId())));
    }

    @Test
    void employeeCanViewClaimableTeamBacklogSeparately() {
        WorkflowItem justinTask = service.createItem("Assigned to Justin", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);
        WorkflowItem johnnyTask = service.createItem("Assigned to Johnny", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);
        WorkflowItem johnnyInProgress = service.createItem("Johnny in progress", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.updateStatus(johnnyInProgress.getId(), Status.IN_PROGRESS, "johnny");

        var backlog = service.getClaimableBacklogItems("justin");

        assertTrue(backlog.stream().anyMatch(item -> item.getId().equals(johnnyTask.getId())));
        assertTrue(backlog.stream().noneMatch(item -> item.getId().equals(justinTask.getId())));
        assertTrue(backlog.stream().noneMatch(item -> item.getId().equals(johnnyInProgress.getId())));
    }

    @Test
    void employeeCanClaimBacklogItemAssignedToSomeoneElse() {
        WorkflowItem item = service.createItem("Shared backlog task", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        WorkflowItem claimed = service.claimItem(item.getId(), "justin");

        assertEquals("justin", claimed.getAssignee().getUsername());
        assertEquals(Status.IN_PROGRESS, claimed.getStatus());
    }

    @Test
    void employeeCannotClaimNonBacklogItem() {
        WorkflowItem item = service.createItem("Task", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);
        service.updateStatus(item.getId(), Status.IN_PROGRESS, "johnny");

        assertThrows(IllegalStateException.class, () -> service.claimItem(item.getId(), "justin"));
    }

    @Test
    void employeeCannotArchiveTasks() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        assertThrows(IllegalStateException.class, () -> service.archiveItem(item.getId(), "justin"));
    }

    @Test
    void employeeCanProgressOwnTaskIntoReviewAndManagerCanAcceptIt() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.updateStatus(item.getId(), Status.IN_PROGRESS, "justin");
        service.updateStatus(item.getId(), Status.IN_REVIEW, "justin");
        service.updateStatus(item.getId(), Status.ACCEPTED, "manager");

        WorkflowItem updated = service.getItem(item.getId(), "manager");
        assertEquals(Status.ACCEPTED, updated.getStatus());
    }

    @Test
    void managerCanReturnReviewedTaskToInProgress() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.updateStatus(item.getId(), Status.IN_PROGRESS, "justin");
        service.updateStatus(item.getId(), Status.IN_REVIEW, "justin");
        service.updateStatus(item.getId(), Status.IN_PROGRESS, "manager");

        WorkflowItem updated = service.getItem(item.getId(), "manager");
        assertEquals(Status.IN_PROGRESS, updated.getStatus());
    }

    @Test
    void employeeCannotAcceptTaskDirectly() {
        WorkflowItem item = service.createItem("Task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.updateStatus(item.getId(), Status.IN_PROGRESS, "justin");
        service.updateStatus(item.getId(), Status.IN_REVIEW, "justin");

        assertThrows(IllegalStateException.class,
                () -> service.updateStatus(item.getId(), Status.ACCEPTED, "justin"));
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
    void archivedTaskAppearsInArchivedList() {
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

    @Test
    void createClaimAndStatusUpdatesWriteAuditEntries() {
        WorkflowItem created = service.createItem("Audit Demo", "Desc", "johnny", "Workflow",
                Priority.HIGH, TaskType.TASK, "manager", null);

        service.claimItem(created.getId(), "justin");
        service.updateStatus(created.getId(), Status.IN_REVIEW, "justin");

        var activity = auditLogRepository.findAllByOrderByCreatedAtDesc();

        assertTrue(activity.stream().anyMatch(entry ->
                entry.getAction() == AuditAction.CREATED
                        && entry.getWorkflowItem().getId().equals(created.getId())));
        assertTrue(activity.stream().anyMatch(entry ->
                entry.getAction() == AuditAction.CLAIMED
                        && entry.getActor().getUsername().equals("justin")));
        assertTrue(activity.stream().anyMatch(entry ->
                entry.getAction() == AuditAction.STATUS_CHANGED
                        && entry.getDetails().contains("In Review")));
    }

    @Test
    void employeeRecentActivityOnlyShowsRelevantEntries() {
        WorkflowItem justinTask = service.createItem("Justin task", "Desc", "justin", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);
        WorkflowItem johnnyTask = service.createItem("Johnny task", "Desc", "johnny", "Workflow",
                Priority.MEDIUM, TaskType.TASK, "manager", null);

        service.updateStatus(justinTask.getId(), Status.IN_PROGRESS, "justin");
        service.updateStatus(johnnyTask.getId(), Status.IN_PROGRESS, "johnny");

        var justinActivity = service.getRecentActivity("justin");

        assertTrue(justinActivity.stream().anyMatch(entry ->
                entry.getWorkflowItem().getId().equals(justinTask.getId())));
        assertTrue(justinActivity.stream().noneMatch(entry ->
                entry.getWorkflowItem().getId().equals(johnnyTask.getId())
                        && entry.getActor().getUsername().equals("johnny")));
    }
}

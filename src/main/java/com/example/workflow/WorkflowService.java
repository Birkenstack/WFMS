package com.example.workflow;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.NoSuchElementException;

import org.springframework.stereotype.Service;

@Service
public class WorkflowService {
    private final WorkflowRepository workflowRepository;
    private final AppUserRepository userRepository;

    public WorkflowService(WorkflowRepository workflowRepository, AppUserRepository userRepository) {
        this.workflowRepository = workflowRepository;
        this.userRepository = userRepository;
    }

    public WorkflowItem createItem(String title, String description, String assigneeUsername,
                                   String project, Priority priority, TaskType taskType,
                                   String creatorUsername, LocalDate dueDate) {
        AppUser assignee = getUser(assigneeUsername);
        AppUser createdBy = getUser(creatorUsername);

        WorkflowItem item = new WorkflowItem();
        applySharedFields(item, title, description, project, priority, taskType, dueDate);
        item.setAssignee(assignee);
        item.setCreatedBy(createdBy);
        item.setCreatedAt(LocalDateTime.now());
        item.setArchived(false);
        item.setStatus(Status.BACKLOG);
        return workflowRepository.save(item);
    }

    public WorkflowItem updateItem(Long id, String title, String description, String assigneeUsername,
                                   String project, Priority priority, TaskType taskType,
                                   LocalDate dueDate, String actingUsername) {
        AppUser actor = getUser(actingUsername);
        ensureManager(actor);

        WorkflowItem item = getItem(id, actingUsername);
        item.setAssignee(getUser(assigneeUsername));
        applySharedFields(item, title, description, project, priority, taskType, dueDate);
        return workflowRepository.save(item);
    }

    public List<WorkflowItem> getVisibleItems(String username) {
        AppUser user = getUser(username);
        if (user.getRole() == Role.MANAGER) {
            return workflowRepository.findByArchivedFalseOrderByCreatedAtDesc();
        }
        return workflowRepository.findByArchivedFalseOrderByCreatedAtDesc().stream()
                .filter(item -> item.getStatus() == Status.BACKLOG
                        || item.getAssignee().getUsername().equalsIgnoreCase(user.getUsername()))
                .toList();
    }

    public List<WorkflowItem> filterItems(String username, String project, Status status, Priority priority) {
        String trimmedProject = project == null ? "" : project.trim();
        return getVisibleItems(username).stream()
                .filter(item -> trimmedProject.isEmpty() || trimmedProject.equalsIgnoreCase(item.getProject()))
                .filter(item -> status == null || item.getStatus() == status)
                .filter(item -> priority == null || item.getPriority() == priority)
                .toList();
    }

    public WorkflowItem getItem(Long id, String username) {
        AppUser user = getUser(username);
        WorkflowItem item = workflowRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Workflow item " + id + " was not found."));
        if (item.isArchived()) {
            throw new NoSuchElementException("Workflow item " + id + " was not found.");
        }
        if (user.getRole() == Role.EMPLOYEE
                && item.getStatus() != Status.BACKLOG
                && !item.getAssignee().getUsername().equalsIgnoreCase(user.getUsername())) {
            throw new IllegalStateException("You do not have access to this workflow item.");
        }
        return item;
    }

    public WorkflowItem claimItem(Long id, String actingUsername) {
        AppUser actor = getUser(actingUsername);
        if (actor.getRole() != Role.EMPLOYEE) {
            throw new IllegalStateException("Only employees can claim backlog work.");
        }

        WorkflowItem item = workflowRepository.findById(id)
                .orElseThrow(() -> new NoSuchElementException("Workflow item " + id + " was not found."));
        if (item.isArchived()) {
            throw new NoSuchElementException("Workflow item " + id + " was not found.");
        }
        if (item.getStatus() != Status.BACKLOG) {
            throw new IllegalStateException("Only backlog items can be claimed.");
        }

        item.setAssignee(actor);
        item.setStatus(Status.IN_PROGRESS);
        return workflowRepository.save(item);
    }

    public void updateStatus(Long id, Status newStatus, String actingUsername) {
        AppUser actor = getUser(actingUsername);
        WorkflowItem item = getItem(id, actingUsername);

        if (actor.getRole() == Role.EMPLOYEE
                && newStatus != Status.IN_PROGRESS
                && newStatus != Status.IN_REVIEW) {
            throw new IllegalStateException("Employees can only move tasks to IN_PROGRESS or IN_REVIEW.");
        }

        if (!isValidTransition(item.getStatus(), newStatus)) {
            throw new IllegalStateException(
                    "Invalid status transition from " + item.getStatus() + " to " + newStatus + ".");
        }

        item.setStatus(newStatus);
        workflowRepository.save(item);
    }

    public void archiveItem(Long id, String actingUsername) {
        AppUser actor = getUser(actingUsername);
        ensureManager(actor);

        WorkflowItem item = getItem(id, actingUsername);
        item.setArchived(true);
        workflowRepository.save(item);
    }

    public List<String> getProjects(String username) {
        return getVisibleItems(username).stream()
                .map(WorkflowItem::getProject)
                .filter(project -> project != null && !project.isBlank())
                .distinct()
                .sorted(String.CASE_INSENSITIVE_ORDER)
                .toList();
    }

    public List<WorkflowItem> getArchivedItems() {
        return workflowRepository.findByArchivedTrueOrderByCreatedAtDesc();
    }

    public List<AppUser> getAssignableUsers() {
        return userRepository.findByRoleOrderByDisplayNameAsc(Role.EMPLOYEE);
    }

    public AppUser getUser(String username) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseThrow(() -> new NoSuchElementException("User not found: " + username));
    }

    private void ensureManager(AppUser user) {
        if (user.getRole() != Role.MANAGER) {
            throw new IllegalStateException("Only managers can perform this action.");
        }
    }

    private void applySharedFields(WorkflowItem item, String title, String description, String project,
                                   Priority priority, TaskType taskType, LocalDate dueDate) {
        String trimmedTitle = title == null ? "" : title.trim();
        String trimmedDescription = description == null ? "" : description.trim();
        String trimmedProject = project == null ? "" : project.trim();

        if (trimmedTitle.isEmpty() || trimmedDescription.isEmpty() || trimmedProject.isEmpty()
                || priority == null || taskType == null) {
            throw new IllegalArgumentException("All workflow fields are required.");
        }

        item.setTitle(trimmedTitle);
        item.setDescription(trimmedDescription);
        item.setProject(trimmedProject);
        item.setPriority(priority);
        item.setTaskType(taskType);
        item.setDueDate(dueDate);
    }

    private boolean isValidTransition(Status currentStatus, Status newStatus) {
        if (currentStatus == newStatus) {
            return true;
        }

        return switch (currentStatus) {
            case BACKLOG -> newStatus == Status.IN_PROGRESS;
            case IN_PROGRESS -> newStatus == Status.IN_REVIEW;
            case IN_REVIEW -> newStatus == Status.IN_PROGRESS || newStatus == Status.ACCEPTED;
            case ACCEPTED -> false;
        };
    }
}

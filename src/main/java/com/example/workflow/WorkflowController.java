package com.example.workflow;

import java.time.LocalDate;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.security.core.Authentication;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.mvc.support.RedirectAttributes;

@Controller
public class WorkflowController {
    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @GetMapping("/login")
    public String loginPage() {
        return "login";
    }

    @GetMapping("/")
    public String dashboard(@RequestParam(defaultValue = "") String project,
                            @RequestParam(required = false) Status status,
                            @RequestParam(required = false) Priority priority,
                            @RequestParam(required = false) String message,
                            @RequestParam(required = false) String error,
                            Authentication authentication,
                            Model model) {
        String username = authentication.getName();
        AppUser currentUser = service.getUser(username);
        boolean isManager = currentUser.getRole() == Role.MANAGER;
        List<WorkflowItem> items = service.filterItems(username, project, status, priority);
        List<BoardColumn> boardColumns = buildBoardColumns(items, isManager);

        model.addAttribute("items", items);
        model.addAttribute("boardColumns", boardColumns);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isManager", isManager);
        model.addAttribute("projectFilter", project);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("availableStatuses", isManager
                ? Arrays.asList(Status.values())
                : Arrays.asList(Status.IN_PROGRESS, Status.IN_REVIEW));
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("projects", service.getProjects(username));
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        model.addAttribute("dashboardTitle", isManager ? "Manager Delivery Board" : "My Delivery Board");
        model.addAttribute("dashboardSubtitle", isManager
                ? "See the whole team workflow, review submitted work, and move accepted items toward closure."
                : "Focus on your assigned backlog, what you are actively building, and what is waiting for review.");
        model.addAttribute("boardHint", isManager
                ? "Use this board to review backlog, monitor active work, accept completed work, or send items back for rework."
                : "Use this board to pull your assigned work forward, submit it for review, and track what has already been accepted.");
        return "dashboard";
    }

    @GetMapping("/archived")
    public String archivedPage(Authentication authentication, Model model) {
        AppUser currentUser = service.getUser(authentication.getName());
        if (currentUser.getRole() != Role.MANAGER) {
            return "redirect:/";
        }
        model.addAttribute("items", service.getArchivedItems());
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isManager", true);
        return "archived";
    }

    @GetMapping("/items/{id}")
    public String itemDetailPage(@PathVariable Long id,
                                 Authentication authentication,
                                 Model model,
                                 RedirectAttributes redirectAttributes) {
        AppUser currentUser = service.getUser(authentication.getName());
        try {
            WorkflowItem item = service.getItem(id, authentication.getName());
            model.addAttribute("item", item);
            model.addAttribute("currentUser", currentUser);
            model.addAttribute("isManager", currentUser.getRole() == Role.MANAGER);
            return "task-detail";
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
            return "redirect:/";
        }
    }

    @GetMapping("/create")
    public String createPage(Authentication authentication, Model model) {
        String username = authentication.getName();
        AppUser currentUser = service.getUser(username);
        return renderItemForm(model, null, currentUser, new WorkflowItem(), false);
    }

    @PostMapping("/create")
    public String createItem(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String assignee,
                             @RequestParam String project,
                             @RequestParam Priority priority,
                             @RequestParam TaskType taskType,
                             @RequestParam(required = false) LocalDate dueDate,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        AppUser currentUser = service.getUser(username);
        try {
            WorkflowItem item = service.createItem(
                    title, description, assignee, project, priority, taskType, username, dueDate);
            redirectAttributes.addAttribute("message", "Workflow item created for " + item.getAssignee().getDisplayName());
            return "redirect:/";
        } catch (RuntimeException e) {
            WorkflowItem item = buildFormItem(null, title, description, assignee, project, priority, taskType, currentUser, dueDate);
            return renderItemForm(model, e.getMessage(), currentUser, item, false);
        }
    }

    @GetMapping("/items/{id}/edit")
    public String editPage(@PathVariable Long id,
                           Authentication authentication,
                           Model model) {
        AppUser currentUser = service.getUser(authentication.getName());
        WorkflowItem item = service.getItem(id, authentication.getName());
        return renderItemForm(model, null, currentUser, item, true);
    }

    @PostMapping("/items/{id}/edit")
    public String updateItem(@PathVariable Long id,
                             @RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String assignee,
                             @RequestParam String project,
                             @RequestParam Priority priority,
                             @RequestParam TaskType taskType,
                             @RequestParam(required = false) LocalDate dueDate,
                             Authentication authentication,
                             Model model,
                             RedirectAttributes redirectAttributes) {
        String username = authentication.getName();
        AppUser currentUser = service.getUser(username);
        try {
            service.updateItem(id, title, description, assignee, project, priority, taskType, dueDate, username);
            redirectAttributes.addAttribute("message", "Workflow item updated");
            return "redirect:/";
        } catch (RuntimeException e) {
            WorkflowItem item = buildFormItem(id, title, description, assignee, project, priority, taskType, currentUser, dueDate);
            return renderItemForm(model, e.getMessage(), currentUser, item, true);
        }
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long id,
                               @RequestParam Status status,
                               RedirectAttributes redirectAttributes,
                               Authentication authentication) {
        try {
            service.updateStatus(id, status, authentication.getName());
            redirectAttributes.addAttribute("message", "Workflow status updated");
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    @PostMapping("/items/{id}/archive")
    public String archiveItem(@PathVariable Long id,
                              RedirectAttributes redirectAttributes,
                              Authentication authentication) {
        try {
            service.archiveItem(id, authentication.getName());
            redirectAttributes.addAttribute("message", "Workflow item archived");
        } catch (RuntimeException e) {
            redirectAttributes.addAttribute("error", e.getMessage());
        }
        return "redirect:/";
    }

    private String renderItemForm(Model model, String error, AppUser currentUser, WorkflowItem item, boolean editMode) {
        model.addAttribute("error", error);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("item", item);
        model.addAttribute("assignees", service.getAssignableUsers());
        model.addAttribute("projects", service.getProjects(currentUser.getUsername()));
        model.addAttribute("priorities", Arrays.asList(Priority.values()));
        model.addAttribute("taskTypes", Arrays.asList(TaskType.values()));
        model.addAttribute("editMode", editMode);
        model.addAttribute("formAction", editMode ? "/items/" + item.getId() + "/edit" : "/create");
        return "create";
    }

    private WorkflowItem buildFormItem(Long id, String title, String description, String assigneeUsername,
                                       String project, Priority priority, TaskType taskType,
                                       AppUser currentUser, LocalDate dueDate) {
        WorkflowItem item = new WorkflowItem();
        item.setId(id);
        item.setTitle(title);
        item.setDescription(description);
        item.setProject(project);
        item.setPriority(priority);
        item.setTaskType(taskType);
        item.setDueDate(dueDate);
        item.setCreatedBy(currentUser);
        if (assigneeUsername != null && !assigneeUsername.isBlank()) {
            item.setAssignee(service.getUser(assigneeUsername));
        }
        return item;
    }

    private List<BoardColumn> buildBoardColumns(List<WorkflowItem> items, boolean isManager) {
        Map<Status, String> labels = new LinkedHashMap<>();
        if (isManager) {
            labels.put(Status.BACKLOG, "Product Backlog");
            labels.put(Status.IN_PROGRESS, "In Progress");
            labels.put(Status.IN_REVIEW, "Review Queue");
            labels.put(Status.ACCEPTED, "Accepted");
        } else {
            labels.put(Status.BACKLOG, "Assigned Backlog");
            labels.put(Status.IN_PROGRESS, "In Progress");
            labels.put(Status.IN_REVIEW, "In Review");
            labels.put(Status.ACCEPTED, "Accepted");
        }

        return labels.entrySet().stream()
                .map(entry -> new BoardColumn(
                        entry.getKey(),
                        entry.getValue(),
                        items.stream().filter(item -> item.getStatus() == entry.getKey()).toList()))
                .toList();
    }

    private record BoardColumn(Status status, String label, List<WorkflowItem> items) {
    }
}

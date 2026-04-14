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
        List<WorkflowItem> items = service.filterItems(username, project, status, priority);
        Map<Status, List<WorkflowItem>> itemsByStatus = new LinkedHashMap<>();
        for (Status workflowStatus : Status.values()) {
            itemsByStatus.put(workflowStatus, items.stream()
                    .filter(item -> item.getStatus() == workflowStatus)
                    .toList());
        }

        model.addAttribute("items", items);
        model.addAttribute("itemsByStatus", itemsByStatus);
        model.addAttribute("currentUser", currentUser);
        model.addAttribute("isManager", currentUser.getRole() == Role.MANAGER);
        model.addAttribute("projectFilter", project);
        model.addAttribute("selectedStatus", status);
        model.addAttribute("selectedPriority", priority);
        model.addAttribute("statuses", Status.values());
        model.addAttribute("priorities", Priority.values());
        model.addAttribute("projects", service.getProjects(username));
        model.addAttribute("message", message);
        model.addAttribute("error", error);
        return "dashboard";
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
}

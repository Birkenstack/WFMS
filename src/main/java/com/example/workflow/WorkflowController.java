package com.example.workflow;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;

@Controller
public class WorkflowController {
    String currentUser = "Justin"; // hardcoded for now
    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        String currentUser = "Justin"; // hardcoded for now

        var items = service.getItemsForUser(currentUser);
        model.addAttribute("items", items);
        model.addAttribute("username", currentUser);

        return "dashboard";
    }

    @GetMapping("/create")
    public String createPage() {
        return "create";
    }

    @PostMapping("/create")
    public String createItem(@RequestParam String title,
                             @RequestParam String description,
                             @RequestParam String assignee) {
        if (title.isEmpty() || description.isEmpty() || assignee.isEmpty()) {
            return "create"; // simple validation for now change later
        }
        service.createItem(title, description, assignee);
        return "redirect:/";
    }

    @PostMapping("/updateStatus")
    public String updateStatus(@RequestParam Long id,
                            @RequestParam Status status) {
        service.updateStatus(id, status);
        return "redirect:/";
    }

}

package com.example.workflow;

import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
public class WorkflowController {

    private final WorkflowService service;

    public WorkflowController(WorkflowService service) {
        this.service = service;
    }

    @GetMapping("/")
    public String dashboard(Model model) {
        model.addAttribute("items", service.getAllItems());
        return "dashboard";
    }

    @GetMapping("/create")
    public String createPage() {
        return "create";
    }

    @PostMapping("/create")
    public String createItem(@RequestParam String title,
                             @RequestParam String description) {
        service.createItem(title, description);
        return "redirect:/";
    }
}

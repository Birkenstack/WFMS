package com.example.workflow;

import java.util.List;

import org.springframework.stereotype.Service;

@Service
public class WorkflowService {

    private final WorkflowRepository repo;

    public WorkflowService(WorkflowRepository repo) {
        this.repo = repo;
    }

    public WorkflowItem createItem(String title, String description) {
        WorkflowItem item = new WorkflowItem();
        item.setTitle(title);
        item.setDescription(description);
        return repo.save(item);
    }

    public List<WorkflowItem> getAllItems() {
        return repo.findAll();
    }

    public void updateStatus(Long id, Status newStatus) {
    for (WorkflowItem item : repo.findAll()) {
        if (item.getId().equals(id)) {
            item.setStatus(newStatus);
            break;
        }
    }
}
}

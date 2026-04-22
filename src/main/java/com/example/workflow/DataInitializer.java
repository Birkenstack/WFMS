package com.example.workflow;

import java.time.LocalDate;

import org.springframework.boot.CommandLineRunner;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Component
public class DataInitializer implements CommandLineRunner {
    private final AppUserRepository userRepository;
    private final WorkflowRepository workflowRepository;
    private final PasswordEncoder passwordEncoder;
    private final JdbcTemplate jdbcTemplate;

    public DataInitializer(AppUserRepository userRepository,
                           WorkflowRepository workflowRepository,
                           PasswordEncoder passwordEncoder,
                           JdbcTemplate jdbcTemplate) {
        this.userRepository = userRepository;
        this.workflowRepository = workflowRepository;
        this.passwordEncoder = passwordEncoder;
        this.jdbcTemplate = jdbcTemplate;
    }

    @Override
    @Transactional
    public void run(String... args) {
        migrateLegacyStatuses();

        AppUser manager = ensureUser("manager", "manager123", "Maya Manager", Role.MANAGER);
        AppUser employeeOne = ensureUser("justin", "employee123", "Justin Employee", Role.EMPLOYEE);
        AppUser employeeTwo = ensureUser("johnny", "employee123", "Johnny Employee", Role.EMPLOYEE);
        AppUser employeeThree = ensureUser("sevin", "employee123", "Sevin Employee", Role.EMPLOYEE);
        migrateLegacyCasey(employeeTwo, manager);

        ensureTask("Sprint 3 Demo Prep",
                "Prepare the integrated sprint 3 demo workflow.",
                "Workflow",
                Priority.HIGH,
                TaskType.STORY,
                Status.IN_REVIEW,
                manager,
                employeeOne,
                LocalDate.now().plusDays(3));

        ensureTask("Fix task permissions",
                "Verify managers and employees see the correct actions.",
                "Platform",
                Priority.MEDIUM,
                TaskType.BUG,
                Status.BACKLOG,
                manager,
                employeeTwo,
                LocalDate.now().plusDays(5));

        ensureTask("Review employee dashboard",
                "Check that employee task visibility only shows assigned work.",
                "Platform",
                Priority.LOW,
                TaskType.TASK,
                Status.BACKLOG,
                manager,
                employeeThree,
                LocalDate.now().plusDays(7));

        ensureTask("Build kanban walkthrough",
                "Prepare a polished board-style walkthrough for the presentation.",
                "Presentation",
                Priority.HIGH,
                TaskType.STORY,
                Status.ACCEPTED,
                manager,
                employeeOne,
                LocalDate.now().plusDays(2));

        ensureTask("Validate manager role rules",
                "Confirm manager-only actions are visible and functional.",
                "Security",
                Priority.HIGH,
                TaskType.BUG,
                Status.IN_PROGRESS,
                manager,
                employeeTwo,
                LocalDate.now().plusDays(4));

        ensureTask("Review seeded demo data",
                "Make sure each employee has at least one assigned task for the demo.",
                "Presentation",
                Priority.MEDIUM,
                TaskType.TASK,
                Status.BACKLOG,
                manager,
                employeeThree,
                LocalDate.now().plusDays(6));
    }

    private void migrateLegacyStatuses() {
        try {
            jdbcTemplate.execute("ALTER TABLE workflow_item ALTER COLUMN status VARCHAR(32)");
            jdbcTemplate.update("UPDATE workflow_item SET status = 'BACKLOG' WHERE status = 'SUBMITTED'");
            jdbcTemplate.update("UPDATE workflow_item SET status = 'ACCEPTED' WHERE status = 'APPROVED'");
            jdbcTemplate.update("UPDATE workflow_item SET status = 'ACCEPTED' WHERE status = 'COMPLETED'");
        } catch (DataAccessException ignored) {
            // Fresh databases or already-migrated schemas do not need the legacy enum rewrite.
        }
    }

    private void migrateLegacyCasey(AppUser replacementAssignee, AppUser fallbackCreator) {
        userRepository.findByUsernameIgnoreCase("casey").ifPresent(legacyUser -> {
            for (WorkflowItem item : workflowRepository.findAll()) {
                boolean updated = false;
                if (item.getAssignee() != null
                        && item.getAssignee().getUsername().equalsIgnoreCase("casey")) {
                    item.setAssignee(replacementAssignee);
                    updated = true;
                }
                if (item.getCreatedBy() != null
                        && item.getCreatedBy().getUsername().equalsIgnoreCase("casey")) {
                    item.setCreatedBy(fallbackCreator);
                    updated = true;
                }
                if (updated) {
                    workflowRepository.save(item);
                }
            }
            userRepository.delete(legacyUser);
        });
    }

    private AppUser ensureUser(String username, String rawPassword, String displayName, Role role) {
        return userRepository.findByUsernameIgnoreCase(username)
                .orElseGet(() -> {
                    AppUser user = new AppUser();
                    user.setUsername(username);
                    user.setPassword(passwordEncoder.encode(rawPassword));
                    user.setDisplayName(displayName);
                    user.setRole(role);
                    return userRepository.save(user);
                });
    }

    private WorkflowItem seedTask(String title, String description, String project, Priority priority,
                                  TaskType taskType, Status status, AppUser createdBy, AppUser assignee,
                                  LocalDate dueDate) {
        WorkflowItem item = new WorkflowItem();
        item.setTitle(title);
        item.setDescription(description);
        item.setProject(project);
        item.setPriority(priority);
        item.setTaskType(taskType);
        item.setStatus(status);
        item.setCreatedBy(createdBy);
        item.setAssignee(assignee);
        item.setDueDate(dueDate);
        item.setCreatedAt(java.time.LocalDateTime.now());
        item.setArchived(false);
        return item;
    }

    private void ensureTask(String title, String description, String project, Priority priority,
                            TaskType taskType, Status status, AppUser createdBy, AppUser assignee,
                            LocalDate dueDate) {
        if (!workflowRepository.existsByTitleIgnoreCase(title)) {
            workflowRepository.save(seedTask(title, description, project, priority, taskType, status, createdBy, assignee, dueDate));
        }
    }
}

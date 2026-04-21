document.addEventListener('DOMContentLoaded', () => {
  const modal = document.getElementById('task-modal');
  const closeButton = document.querySelector('.modal-close');
  const cards = Array.from(document.querySelectorAll('.task-card'));
  const columns = Array.from(document.querySelectorAll('.board-column'));
  const dropForm = document.getElementById('status-drop-form');
  const dropId = document.getElementById('status-drop-id');
  const dropStatus = document.getElementById('status-drop-value');

  if (!modal || !dropForm) {
    return;
  }

  const modalFields = {
    id: document.getElementById('modal-id'),
    title: document.getElementById('modal-title'),
    description: document.getElementById('modal-description'),
    project: document.getElementById('modal-project'),
    type: document.getElementById('modal-type'),
    priority: document.getElementById('modal-priority'),
    status: document.getElementById('modal-status'),
    assignee: document.getElementById('modal-assignee'),
    createdBy: document.getElementById('modal-created-by'),
    dueDate: document.getElementById('modal-due-date'),
    createdAt: document.getElementById('modal-created-at'),
    detailLink: document.getElementById('modal-detail-link'),
    editLink: document.getElementById('modal-edit-link'),
    statusId: document.getElementById('modal-status-id'),
    statusSelect: document.getElementById('modal-status-select'),
    archiveForm: document.getElementById('modal-archive-form')
  };

  let activeCard = null;

  const openModal = (card) => {
    const data = card.dataset;
    activeCard = card;

    modalFields.id.textContent = `Task #${data.itemId}`;
    modalFields.title.textContent = data.title;
    modalFields.description.textContent = data.description;
    modalFields.project.textContent = data.project;
    modalFields.type.textContent = data.taskType;
    modalFields.priority.textContent = data.priority;
    modalFields.status.textContent = data.status;
    modalFields.assignee.textContent = data.assignee;
    modalFields.createdBy.textContent = data.createdBy;
    modalFields.dueDate.textContent = data.dueDate;
    modalFields.createdAt.textContent = data.createdAt;
    modalFields.detailLink.href = data.detailUrl;
    modalFields.statusId.value = data.itemId;
    modalFields.statusSelect.value = data.status;

    const canEdit = data.canEdit === 'true';
    const canArchive = data.canArchive === 'true';

    if (canEdit) {
      modalFields.editLink.href = data.editUrl;
      modalFields.editLink.hidden = false;
    } else {
      modalFields.editLink.hidden = true;
    }

    modalFields.archiveForm.hidden = !canArchive;
    modalFields.archiveForm.action = data.archiveUrl || '#';

    modal.showModal();
  };

  const closeModal = () => {
    if (modal.open) {
      modal.close();
    }
    activeCard = null;
  };

  cards.forEach((card) => {
    card.addEventListener('click', (event) => {
      if (event.target.closest('a') || event.target.closest('button')) {
        return;
      }
      openModal(card);
    });

    card.addEventListener('keydown', (event) => {
      if (event.key === 'Enter' || event.key === ' ') {
        event.preventDefault();
        openModal(card);
      }
    });

    card.querySelector('.open-task-modal')?.addEventListener('click', (event) => {
      event.preventDefault();
      event.stopPropagation();
      openModal(card);
    });

    card.addEventListener('dragstart', () => {
      activeCard = card;
      card.classList.add('dragging');
    });

    card.addEventListener('dragend', () => {
      card.classList.remove('dragging');
      columns.forEach((column) => column.classList.remove('drop-target'));
    });
  });

  columns.forEach((column) => {
    column.addEventListener('dragover', (event) => {
      event.preventDefault();
      column.classList.add('drop-target');
    });

    column.addEventListener('dragleave', () => {
      column.classList.remove('drop-target');
    });

    column.addEventListener('drop', (event) => {
      event.preventDefault();
      column.classList.remove('drop-target');
      if (!activeCard) {
        return;
      }

      const targetStatus = column.dataset.dropStatus;
      if (!targetStatus || activeCard.dataset.status === targetStatus) {
        return;
      }

      dropId.value = activeCard.dataset.itemId;
      dropStatus.value = targetStatus;
      dropForm.submit();
    });
  });

  closeButton?.addEventListener('click', closeModal);

  modal.addEventListener('click', (event) => {
    const frame = modal.querySelector('.task-modal-frame');
    if (!frame.contains(event.target)) {
      closeModal();
    }
  });

  document.addEventListener('keydown', (event) => {
    if (event.key === 'Escape') {
      closeModal();
    }
  });
});

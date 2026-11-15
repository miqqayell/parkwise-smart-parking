// Notification bell system
document.addEventListener('DOMContentLoaded', function() {
    const dropdown = document.getElementById('notificationDropdown');
    if (!dropdown) return;

    // Load badge count on page load
    loadBadgeCount();

    // Refresh badge every 30 seconds
    setInterval(loadBadgeCount, 30000);

    // When dropdown opens, load notifications
    dropdown.addEventListener('show.bs.dropdown', function() {
        loadNotifications();
    });

    // Mark all read button
    const markAllBtn = document.getElementById('markAllReadBtnDropdown');
    if (markAllBtn) {
        markAllBtn.addEventListener('click', function(e) {
            e.preventDefault();
            e.stopPropagation();
            markAllAsRead();
        });
    }
});

function loadBadgeCount() {
    fetch('/api/notifications/unread-count')
        .then(res => res.json())
        .then(data => {
            const badge = document.getElementById('notificationBadge');
            if (badge) {
                const count = data.count || 0;
                badge.textContent = count;
                badge.style.display = count > 0 ? 'block' : 'none';
            }
        })
        .catch(err => console.error('Failed to load notification count'));
}

function loadNotifications() {
    const container = document.getElementById('notificationList');
    if (!container) return;

    // Show loading
    container.innerHTML = '<div class="text-center py-4"><div class="spinner-border spinner-border-sm text-primary"></div><div class="text-light small mt-2">Loading...</div></div>';

    fetch('/api/notifications/recent?limit=5')
        .then(res => {
            if (!res.ok) throw new Error('HTTP ' + res.status);
            return res.json();
        })
        .then(notifications => {
            if (!notifications || notifications.length === 0) {
                container.innerHTML = `
                    <div class="notification-empty">
                        <div class="notification-empty-icon"><i class="bi bi-bell-slash"></i></div>
                        <div class="notification-empty-text">No new notifications</div>
                    </div>
                `;
                return;
            }

            // Render notifications
            container.innerHTML = '';
            notifications.forEach(notif => {
                const item = createNotificationItem(notif);
                container.appendChild(item);
            });
        })
        .catch(err => {
            container.innerHTML = `
                <div class="notification-empty">
                    <div class="notification-empty-text text-danger">
                        <i class="bi bi-exclamation-triangle me-2"></i>Failed to load
                    </div>
                </div>
            `;
        });
}

function createNotificationItem(notif) {
    const item = document.createElement('div');
    item.className = 'notification-item';
    item.dataset.notificationId = notif.id;

    if (notif.status === 'UNREAD') {
        item.classList.add('unread');
    }

    const iconClass = (notif.priority === 'HIGH' || notif.priority === 'CRITICAL')
        ? 'bi-exclamation-triangle-fill text-warning'
        : 'bi-info-circle-fill text-primary';

    item.innerHTML = `
        <div class="notification-icon">
            <i class="bi ${iconClass}"></i>
        </div>
        <div class="notification-content">
            <div class="notification-title">${escapeHtml(notif.title)}</div>
            <div class="notification-message">${escapeHtml(notif.message)}</div>
            <div class="notification-time">${escapeHtml(notif.timeAgo || 'Just now')}</div>
        </div>
        <button class="notification-dismiss">
            <i class="bi bi-x"></i>
        </button>
    `;

    // Click item to mark as read
    item.addEventListener('click', function(e) {
        if (!e.target.closest('.notification-dismiss')) {
            markAsRead(notif.id);
        }
    });

    // Click X to dismiss
    const dismissBtn = item.querySelector('.notification-dismiss');
    dismissBtn.addEventListener('click', function(e) {
        e.preventDefault();
        e.stopPropagation();
        dismissNotification(notif.id);
    });

    return item;
}

function markAsRead(id) {
    fetch('/api/notifications/' + id + '/read', { method: 'POST' })
        .then(res => {
            if (res.ok) {
                const item = document.querySelector('[data-notification-id="' + id + '"]');
                if (item) item.classList.remove('unread');
                loadBadgeCount();
            }
        });
}

function dismissNotification(id) {
    fetch('/api/notifications/' + id + '/dismiss', { method: 'POST' })
        .then(res => {
            if (res.ok) {
                const item = document.querySelector('[data-notification-id="' + id + '"]');
                if (item) {
                    item.style.opacity = '0';
                    item.style.transition = 'opacity 0.3s';
                    setTimeout(() => {
                        item.remove();

                        const container = document.getElementById('notificationList');
                        if (container && container.children.length === 0) {
                            container.innerHTML = `
                                <div class="notification-empty">
                                    <div class="notification-empty-icon"><i class="bi bi-bell-slash"></i></div>
                                    <div class="notification-empty-text">No new notifications</div>
                                </div>
                            `;
                        }
                    }, 300);
                }
                loadBadgeCount();
            }
        });
}

function markAllAsRead() {
    fetch('/api/notifications/mark-all-read', { method: 'POST' })
        .then(res => {
            if (res.ok) {
                loadNotifications();
                loadBadgeCount();
            }
        });
}

function escapeHtml(text) {
    const div = document.createElement('div');
    div.textContent = text;
    return div.innerHTML;
}

(function initSharedHeader() {
  var slot = document.getElementById('header-slot');
  if (!slot) return;

  var wsClient = null;
  var wsUnreadSub = null;
  var wsTotalUnreadSub = null;
  var totalUnreadConversations = 0;

  var notificationItems = [];
  var notificationCursor = null;
  var notificationHasNext = false;
  var notificationMode = 'all';
  var unreadNotificationCount = 0;
  var isNotifLoading = false;

  function hasToken() {
    return Boolean(localStorage.getItem('accessToken'));
  }

  function headerLog(msg, level) {
    var out = document.getElementById('log-output');
    if (!out) return;
    var ts = new Date().toLocaleTimeString('vi-VN');
    var div = document.createElement('div');
    div.className = 'log-line ' + (level || 'info');
    div.textContent = '[HEADER ' + ts + '] ' + msg;
    out.appendChild(div);
    out.scrollTop = out.scrollHeight;
  }

  function esc(value) {
    return String(value || '')
      .replace(/&/g, '&amp;')
      .replace(/</g, '&lt;')
      .replace(/>/g, '&gt;')
      .replace(/"/g, '&quot;');
  }

  function parseNotificationPage(payload) {
    var page = payload && payload.data;
    if (page && Array.isArray(page.content)) {
      return {
        content: page.content,
        nextCursor: page.nextCursor || null,
        hasNext: Boolean(page.hasNext)
      };
    }
    return { content: [], nextCursor: null, hasNext: false };
  }

  function isNotificationRead(item) {
    return item && (item.isRead === true || item.read === true);
  }

  function updateUnreadConvBadge() {
    var badge = document.getElementById('shared-unread-conv-badge');
    if (!badge) return;
    var total = Math.max(0, Number(totalUnreadConversations) || 0);
    badge.textContent = String(total);
    if (total > 0) {
      badge.classList.remove('hidden');
    } else {
      badge.classList.add('hidden');
    }
  }

  function updateNotifBadge() {
    var badge = document.getElementById('shared-notif-badge');
    if (!badge) return;
    var count = Math.max(0, Number(unreadNotificationCount) || 0);
    if (count > 0) {
      badge.textContent = count > 99 ? '99+' : String(count);
      badge.classList.remove('hidden');
    } else {
      badge.textContent = '0';
      badge.classList.add('hidden');
    }
  }

  function renderNotificationFilter() {
    var allBtn = document.getElementById('shared-notif-filter-all');
    var unreadBtn = document.getElementById('shared-notif-filter-unread');
    if (!allBtn || !unreadBtn) return;
    allBtn.classList.toggle('active', notificationMode === 'all');
    unreadBtn.classList.toggle('active', notificationMode === 'unread');
  }

  function renderNotifications() {
    var list = document.getElementById('shared-notif-list');
    var loadMoreBtn = document.getElementById('shared-notif-load-more');
    if (!list || !loadMoreBtn) return;

    if (!notificationItems.length) {
      list.innerHTML = '<div class="shared-notif-empty">Chưa có thông báo</div>';
    } else {
      list.innerHTML = notificationItems.map(function (item) {
        var createdAt = item && item.createdAt ? new Date(item.createdAt).toLocaleString('vi-VN') : '--';
        var read = isNotificationRead(item);
        return '' +
          '<div class="shared-notif-item ' + (read ? 'read' : 'unread') + '">' +
          '  <div class="shared-notif-dot"></div>' +
          '  <div class="shared-notif-text">' +
          '    <div>' + esc(item && item.text) + '</div>' +
          '    <div class="shared-notif-time">' + esc(createdAt) + '</div>' +
          '    <div class="shared-notif-item-meta">' +
          '      <span class="shared-notif-type">' + esc(item && item.type) + ' · ' + esc(item && item.targetType) + '</span>' +
          (read
            ? '<span class="shared-notif-type">Đã đọc</span>'
            : '<button class="shared-notif-mark-btn" data-action="mark-notif-read" data-id="' + Number(item && item.id) + '">Đánh dấu đã đọc</button>') +
          '    </div>' +
          '  </div>' +
          '</div>';
      }).join('');
    }

    loadMoreBtn.classList.toggle('hidden', !notificationHasNext);
    loadMoreBtn.disabled = !notificationHasNext || isNotifLoading;
    renderNotificationFilter();
  }

  function resetNotifications() {
    notificationItems = [];
    notificationCursor = null;
    notificationHasNext = false;
    notificationMode = 'all';
    unreadNotificationCount = 0;
    updateNotifBadge();
    renderNotifications();
  }

  function loadUnreadNotificationCount() {
    var token = localStorage.getItem('accessToken');
    if (!token) {
      unreadNotificationCount = 0;
      updateNotifBadge();
      return Promise.resolve();
    }

    return fetch('/notifications/unread-count', {
      headers: { Authorization: 'Bearer ' + token },
      credentials: 'include'
    })
      .then(function (res) {
        return res.json().then(function (json) {
          return { ok: res.ok, json: json };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          throw new Error(result.json && result.json.message ? result.json.message : 'Không tải được unread count');
        }
        unreadNotificationCount = Number(result.json && result.json.data || 0);
        updateNotifBadge();
      })
      .catch(function (e) {
        headerLog('Notification unread-count error: ' + e.message, 'warn');
      });
  }

  function loadNotifications(mode, reset) {
    var token = localStorage.getItem('accessToken');
    if (!token || isNotifLoading) {
      return Promise.resolve();
    }
    isNotifLoading = true;

    var nextMode = mode === 'unread' ? 'unread' : 'all';
    if (reset || nextMode !== notificationMode) {
      notificationMode = nextMode;
      notificationCursor = null;
      notificationHasNext = false;
      notificationItems = [];
    }

    var query = new URLSearchParams();
    query.set('limit', '20');
    if (notificationCursor) {
      query.set('cursor', notificationCursor);
    }
    var endpoint = nextMode === 'unread' ? '/notifications/unread' : '/notifications';

    return fetch(endpoint + '?' + query.toString(), {
      headers: { Authorization: 'Bearer ' + token },
      credentials: 'include'
    })
      .then(function (res) {
        return res.json().then(function (json) {
          return { ok: res.ok, json: json };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          throw new Error(result.json && result.json.message ? result.json.message : 'Không tải được notifications');
        }

        var parsed = parseNotificationPage(result.json);
        var existingIds = new Set(notificationItems.map(function (item) { return Number(item && item.id); }));
        var merged = notificationItems.concat(parsed.content.filter(function (item) {
          return !existingIds.has(Number(item && item.id));
        }));

        notificationItems = merged;
        notificationCursor = parsed.nextCursor;
        notificationHasNext = parsed.hasNext;
        renderNotifications();
      })
      .catch(function (e) {
        headerLog('Notification load error: ' + e.message, 'warn');
      })
      .finally(function () {
        isNotifLoading = false;
      });
  }

  function markNotificationAsRead(id) {
    var token = localStorage.getItem('accessToken');
    if (!token || !id) return Promise.resolve();

    return fetch('/notifications/mark-read?id=' + encodeURIComponent(id), {
      method: 'PUT',
      headers: { Authorization: 'Bearer ' + token },
      credentials: 'include'
    })
      .then(function (res) {
        return res.json().then(function (json) {
          return { ok: res.ok, json: json };
        });
      })
      .then(function (result) {
        if (!result.ok) {
          throw new Error(result.json && result.json.message ? result.json.message : 'Không đánh dấu được notification');
        }

        notificationItems = notificationItems.map(function (item) {
          if (Number(item && item.id) === Number(id)) {
            return Object.assign({}, item, { isRead: true });
          }
          return item;
        });

        if (notificationMode === 'unread') {
          notificationItems = notificationItems.filter(function (item) {
            return !isNotificationRead(item);
          });
        }

        return loadUnreadNotificationCount().then(function () {
          renderNotifications();
        });
      })
      .catch(function (e) {
        headerLog('Notification mark-read error: ' + e.message, 'warn');
      });
  }

  function markAllVisibleNotificationsAsRead() {
    var ids = notificationItems
      .filter(function (item) { return !isNotificationRead(item); })
      .map(function (item) { return Number(item && item.id); })
      .filter(function (id) { return Number.isInteger(id) && id > 0; });

    if (!ids.length) return Promise.resolve();

    var chain = Promise.resolve();
    ids.forEach(function (id) {
      chain = chain.then(function () {
        return markNotificationAsRead(id);
      });
    });
    return chain;
  }

  function fetchInitialUnreadCount() {
    var token = localStorage.getItem('accessToken');
    if (!token) return;
    var currentUser = null;
    try {
      currentUser = JSON.parse(localStorage.getItem('currentUser') || 'null');
    } catch (e) {
      currentUser = null;
    }
    var userId = currentUser && currentUser.id;
    if (!userId) return;

    fetch('/conversations/total-unread?userId=' + userId, {
      headers: { Authorization: 'Bearer ' + token },
      credentials: 'include'
    })
      .then(function (res) { return res.json(); })
      .then(function (json) {
        var count = Number(json && json.data || 0);
        totalUnreadConversations = Number.isFinite(count) && count > 0 ? count : 0;
        updateUnreadConvBadge();
      })
      .catch(function (e) {
        headerLog('API total-unread error: ' + e.message, 'warn');
      });
  }

  function connectWsForUnread() {
    if (wsClient && wsClient.active) return;
    var token = localStorage.getItem('accessToken');
    if (!token) return;

    if (typeof SockJS === 'undefined' || typeof StompJs === 'undefined') {
      var s = document.createElement('script');
      s.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
      s.onload = function () {
        var s2 = document.createElement('script');
        s2.src = 'https://cdn.jsdelivr.net/npm/@stomp/stompjs@7/bundles/stomp.umd.min.js';
        s2.onload = initWs;
        document.head.appendChild(s2);
      };
      document.head.appendChild(s);
      return;
    }
    initWs();
  }

  function initWs() {
    if (!window.StompJs) {
      headerLog('STOMP chưa ready', 'error');
      return;
    }
    wsClient = new StompJs.Client({
      webSocketFactory: function () { return new SockJS('/ws'); },
      connectHeaders: { Authorization: 'Bearer ' + localStorage.getItem('accessToken') },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000
    });

    wsClient.onConnect = function () {
      wsUnreadSub = wsClient.subscribe('/user/queue/conversationUnread', function (frame) {
        try {
          var payload = JSON.parse(frame.body);
          var total = Number(payload && payload.totalUnreadConversations);
          if (!Number.isFinite(total)) return;
          totalUnreadConversations = Math.max(0, total);
          updateUnreadConvBadge();
          loadUnreadNotificationCount();
        } catch (e) {
          headerLog('WS unread parse error: ' + e.message, 'error');
        }
      });

      wsTotalUnreadSub = wsClient.subscribe('/user/queue/unreadConversations', function (frame) {
        try {
          var total = Number(frame.body);
          if (!Number.isFinite(total)) return;
          totalUnreadConversations = Math.max(0, total);
          updateUnreadConvBadge();
        } catch (e) {
          headerLog('WS total-unread parse error: ' + e.message, 'error');
        }
      });
    };

    wsClient.onDisconnect = function () {
      headerLog('WS disconnected', 'warn');
    };

    wsClient.onStompError = function (frame) {
      headerLog('WS STOMP error: ' + (frame.headers && frame.headers.message), 'error');
    };

    wsClient.activate();
  }

  function disconnectWsForUnread() {
    if (wsUnreadSub) { wsUnreadSub.unsubscribe(); wsUnreadSub = null; }
    if (wsTotalUnreadSub) { wsTotalUnreadSub.unsubscribe(); wsTotalUnreadSub = null; }
    if (wsClient) { wsClient.deactivate(); wsClient = null; }
    totalUnreadConversations = 0;
    updateUnreadConvBadge();
  }

  function emitAuthChanged(loggedIn, payload) {
    window.dispatchEvent(new CustomEvent('shared-auth-changed', {
      detail: {
        loggedIn: loggedIn,
        token: payload && payload.token ? payload.token : null,
        user: payload && payload.user ? payload.user : null
      }
    }));
  }

  function syncAuthButtons() {
    var loginBtn = document.getElementById('shared-btn-login');
    var logoutBtn = document.getElementById('shared-btn-logout');
    var notifBtn = document.getElementById('shared-btn-notif');
    var notifPanel = document.getElementById('shared-notif-panel');
    var unreadBadge = document.getElementById('shared-unread-conv-badge');
    if (!loginBtn || !logoutBtn) return;

    if (hasToken()) {
      loginBtn.classList.add('hidden');
      logoutBtn.classList.remove('hidden');
      if (notifBtn) notifBtn.classList.remove('hidden');
      connectWsForUnread();
      fetchInitialUnreadCount();
      loadUnreadNotificationCount();
    } else {
      loginBtn.classList.remove('hidden');
      logoutBtn.classList.add('hidden');
      if (notifBtn) notifBtn.classList.add('hidden');
      if (notifPanel) notifPanel.classList.add('hidden');
      if (notifBtn) notifBtn.classList.remove('open');
      if (unreadBadge) {
        unreadBadge.textContent = '0';
        unreadBadge.classList.add('hidden');
      }
      disconnectWsForUnread();
      resetNotifications();
    }
  }

  function ensureAuthModal() {
    if (document.getElementById('shared-auth-modal')) return;
    var wrapper = document.createElement('div');
    wrapper.id = 'shared-auth-modal';
    wrapper.className = 'shared-auth-modal hidden';
    wrapper.innerHTML = '' +
      '<div class="shared-auth-box">' +
      '  <h3>Đăng nhập</h3>' +
      '  <input id="shared-auth-user" placeholder="Username" />' +
      '  <input id="shared-auth-pass" type="password" placeholder="Password" />' +
      '  <input id="shared-auth-token" placeholder="Dán accessToken nếu cần" />' +
      '  <div id="shared-auth-error" class="shared-auth-error"></div>' +
      '  <div class="shared-auth-row">' +
      '    <button id="shared-auth-submit" class="shared-auth-primary" type="button">Login API</button>' +
      '    <button id="shared-auth-use-token" class="shared-auth-secondary" type="button">Dùng token</button>' +
      '  </div>' +
      '  <div class="shared-auth-row">' +
      '    <button id="shared-auth-close" class="shared-auth-secondary" type="button">Đóng</button>' +
      '  </div>' +
      '</div>';
    document.body.appendChild(wrapper);

    document.getElementById('shared-auth-close').addEventListener('click', function () {
      wrapper.classList.add('hidden');
    });

    document.getElementById('shared-auth-use-token').addEventListener('click', function () {
      var tokenInput = document.getElementById('shared-auth-token');
      var value = tokenInput.value.trim();
      if (!value) return;
      localStorage.setItem('accessToken', value);
      wrapper.classList.add('hidden');
      syncAuthButtons();
      emitAuthChanged(true, { token: value });
    });

    document.getElementById('shared-auth-submit').addEventListener('click', function () {
      var user = document.getElementById('shared-auth-user').value.trim();
      var pass = document.getElementById('shared-auth-pass').value.trim();
      var err = document.getElementById('shared-auth-error');
      err.textContent = '';
      if (!user || !pass) {
        err.textContent = 'Nhập đủ username/password';
        return;
      }
      fetch('/auth/login', {
        method: 'POST',
        headers: { 'Content-Type': 'application/json' },
        credentials: 'include',
        body: JSON.stringify({ username: user, password: pass })
      })
        .then(function (res) {
          return res.json().then(function (json) {
            return { ok: res.ok, json: json };
          });
        })
        .then(function (result) {
          if (!result.ok) {
            throw new Error(result.json && result.json.message ? result.json.message : 'Đăng nhập thất bại');
          }
          var token = result.json && result.json.data ? result.json.data.accessToken : null;
          var userObj = result.json && result.json.data ? result.json.data.user : null;
          if (!token) throw new Error('Không nhận được accessToken');
          localStorage.setItem('accessToken', token);
          if (userObj) {
            localStorage.setItem('currentUser', JSON.stringify(userObj));
          }
          wrapper.classList.add('hidden');
          syncAuthButtons();
          emitAuthChanged(true, { token: token, user: userObj });
        })
        .catch(function (e) {
          err.textContent = e.message || 'Đăng nhập thất bại';
        });
    });
  }

  fetch('/shared-header.html')
    .then(function (res) { return res.text(); })
    .then(function (html) {
      slot.innerHTML = html;
      var page = document.body && document.body.dataset ? document.body.dataset.page : null;
      if (page) {
        var active = slot.querySelector('[data-nav="' + page + '"]');
        if (active) active.classList.add('active');
      }

      ensureAuthModal();
      syncAuthButtons();
      renderNotifications();

      if (hasToken()) {
        connectWsForUnread();
        fetchInitialUnreadCount();
        loadUnreadNotificationCount();
      }

      var loginBtn = document.getElementById('shared-btn-login');
      var logoutBtn = document.getElementById('shared-btn-logout');
      var notifBtn = document.getElementById('shared-btn-notif');
      var notifPanel = document.getElementById('shared-notif-panel');
      var notifList = document.getElementById('shared-notif-list');
      var notifFilterAllBtn = document.getElementById('shared-notif-filter-all');
      var notifFilterUnreadBtn = document.getElementById('shared-notif-filter-unread');
      var notifLoadMoreBtn = document.getElementById('shared-notif-load-more');
      var notifMarkAllBtn = document.getElementById('shared-notif-mark-all');
      var modal = document.getElementById('shared-auth-modal');

      if (loginBtn && modal) {
        loginBtn.addEventListener('click', function () {
          modal.classList.remove('hidden');
        });
      }

      if (logoutBtn) {
        logoutBtn.addEventListener('click', function () {
          localStorage.removeItem('accessToken');
          localStorage.removeItem('currentUser');
          syncAuthButtons();
          emitAuthChanged(false, null);
        });
      }

      if (notifBtn && notifPanel) {
        notifBtn.addEventListener('click', function (e) {
          e.stopPropagation();
          var willOpen = notifPanel.classList.contains('hidden');
          notifPanel.classList.toggle('hidden', !willOpen);
          notifBtn.classList.toggle('open', willOpen);

          if (willOpen && hasToken()) {
            if (!notificationItems.length) {
              loadNotifications(notificationMode, true);
            }
            loadUnreadNotificationCount();
          }
        });

        document.addEventListener('click', function (e) {
          if (notifPanel.classList.contains('hidden')) return;
          if (!notifPanel.contains(e.target) && !notifBtn.contains(e.target)) {
            notifPanel.classList.add('hidden');
            notifBtn.classList.remove('open');
          }
        });
      }

      if (notifList) {
        notifList.addEventListener('click', function (event) {
          var markBtn = event.target.closest('button[data-action="mark-notif-read"][data-id]');
          if (!markBtn) return;
          var id = Number(markBtn.getAttribute('data-id'));
          if (!Number.isInteger(id) || id <= 0) return;
          markNotificationAsRead(id);
        });
      }

      if (notifFilterAllBtn) {
        notifFilterAllBtn.addEventListener('click', function () {
          loadNotifications('all', true);
        });
      }

      if (notifFilterUnreadBtn) {
        notifFilterUnreadBtn.addEventListener('click', function () {
          loadNotifications('unread', true);
        });
      }

      if (notifLoadMoreBtn) {
        notifLoadMoreBtn.addEventListener('click', function () {
          loadNotifications(notificationMode, false);
        });
      }

      if (notifMarkAllBtn) {
        notifMarkAllBtn.addEventListener('click', function () {
          markAllVisibleNotificationsAsRead();
        });
      }

      var msgLink = slot.querySelector('[data-nav="messages"]');
      if (msgLink) {
        msgLink.addEventListener('click', function () {
          fetchInitialUnreadCount();
        });
      }

      window.addEventListener('shared-auth-changed', function (event) {
        var detail = event && event.detail ? event.detail : {};
        if (detail.loggedIn) {
          loadUnreadNotificationCount();
          return;
        }
        resetNotifications();
      });
    })
    .catch(function () {
      slot.innerHTML = '<nav class="shared-header"><a class="shared-brand" href="/test-app.html">SocialApp</a></nav>';
    });
})();

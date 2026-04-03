(function initSharedHeader() {
  var slot = document.getElementById('header-slot');
  if (!slot) return;

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
      headerLog('Đã đăng nhập - Kết nối WS...', 'info');
      connectWsForUnread();
      fetchInitialUnreadCount();
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
      headerLog('Đã đăng xuất - Ngắt WS', 'warn');
      disconnectWsForUnread();
    }
  }

  var wsClient = null;
  var wsSub = null;
  var unreadConvIds = new Set();

  function updateUnreadConvBadge() {
    var badge = document.getElementById('shared-unread-conv-badge');
    if (!badge) return;
    var total = unreadConvIds.size;
    badge.textContent = String(total);
    if (total > 0) {
      badge.classList.remove('hidden');
      headerLog('Unread conversation: ' + total, 'info');
    } else {
      badge.classList.add('hidden');
    }
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
    var userId = currentUser?.id;
    if (!userId) return;

    fetch('/conversations/total-unread?userId=' + userId, {
      headers: { Authorization: 'Bearer ' + token },
      credentials: 'include'
    })
      .then(function(res) { return res.json(); })
      .then(function(json) {
        var count = json?.data || 0;
        if (count > 0) {
          headerLog('API total-unread: ' + count, 'info');
          var existing = unreadConvIds.size;
          if (count > existing) {
            unreadConvIds.add('initial');
            updateUnreadConvBadge();
          }
        }
      })
      .catch(function(e) {
        headerLog('API total-unread error: ' + e.message, 'warn');
      });
  }

  function connectWsForUnread() {
    if (wsClient && wsClient.active) return;
    var token = localStorage.getItem('accessToken');
    if (!token) return;

    headerLog('Đang load SockJS + STOMP...', 'info');
    if (typeof SockJS === 'undefined' || typeof StompJs === 'undefined') {
      var s = document.createElement('script');
      s.src = 'https://cdn.jsdelivr.net/npm/sockjs-client@1/dist/sockjs.min.js';
      s.onload = function() {
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
    var savedUser = localStorage.getItem('currentUser');
    var currentUserId = savedUser ? JSON.parse(savedUser).id : null;

    wsClient = new StompJs.Client({
      webSocketFactory: function() { return new SockJS('/ws'); },
      connectHeaders: { Authorization: 'Bearer ' + localStorage.getItem('accessToken') },
      reconnectDelay: 5000,
      heartbeatIncoming: 10000,
      heartbeatOutgoing: 10000,
    });

    wsClient.onConnect = function() {
      headerLog('WS connected! Subscribe /user/queue/messages', 'success');
      wsSub = wsClient.subscribe('/user/queue/messages', function(frame) {
        try {
          var payload = JSON.parse(frame.body);
          var convId = Number(payload.conversationId);
          if (!convId || convId <= 0) return;
          if (payload.sender?.id == currentUserId) return;
          headerLog('WS nhận tin nhắn mới - conv #' + convId, 'info');
          unreadConvIds.add(convId);
          updateUnreadConvBadge();
        } catch(e) {
          headerLog('WS parse error: ' + e.message, 'error');
        }
      });
    };

    wsClient.onDisconnect = function() {
      headerLog('WS disconnected', 'warn');
    };

    wsClient.onStompError = function(frame) {
      headerLog('WS STOMP error: ' + frame.headers?.message, 'error');
    };

    wsClient.activate();
  }

  function disconnectWsForUnread() {
    if (wsSub) { wsSub.unsubscribe(); wsSub = null; }
    if (wsClient) { wsClient.deactivate(); wsClient = null; }
    unreadConvIds.clear();
    updateUnreadConvBadge();
  }

  function emitAuthChanged(loggedIn, payload) {
    window.dispatchEvent(new CustomEvent('shared-auth-changed', {
      detail: {
        loggedIn: loggedIn,
        token: payload && payload.token ? payload.token : null,
        user: payload && payload.user ? payload.user : null,
      },
    }));
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
        body: JSON.stringify({ username: user, password: pass }),
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
          fetchInitialUnreadCount();
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
      if (!page) return;
      var active = slot.querySelector('[data-nav="' + page + '"]');
      if (active) active.classList.add('active');

      ensureAuthModal();
      syncAuthButtons();

      if (hasToken()) {
        headerLog('Có sẵn token - Kết nối WS...', 'info');
        connectWsForUnread();
        fetchInitialUnreadCount();
      }

      var loginBtn = document.getElementById('shared-btn-login');
      var logoutBtn = document.getElementById('shared-btn-logout');
      var notifBtn = document.getElementById('shared-btn-notif');
      var notifPanel = document.getElementById('shared-notif-panel');
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
          var open = notifPanel.classList.contains('hidden');
          notifPanel.classList.toggle('hidden', !open);
          notifBtn.classList.toggle('open', open);
        });

        document.addEventListener('click', function (e) {
          if (notifPanel.classList.contains('hidden')) return;
          if (!notifPanel.contains(e.target) && !notifBtn.contains(e.target)) {
            notifPanel.classList.add('hidden');
            notifBtn.classList.remove('open');
          }
        });
      }

      var msgLink = slot.querySelector('[data-nav="messages"]');
      if (msgLink) {
        msgLink.addEventListener('click', function() {
          unreadConvIds.clear();
          updateUnreadConvBadge();
        });
      }
    })
    .catch(function () {
      slot.innerHTML = '<nav class="shared-header"><a class="shared-brand" href="/test-app.html">SocialApp</a></nav>';
    });
})();

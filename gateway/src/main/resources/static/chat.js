let stompClient = null;
// Load session from localStorage if present
let mySessionId = localStorage.getItem('mySessionId') || crypto.randomUUID();
let myUsername = localStorage.getItem('myUsername') || null;
let myColor = null;

const COLOR_PALETTE = [
    "#0715cd", "#b536da", "#e00707", "#4ac925", "#00d5f2", "#1f9400", "#ff6ff2",
    "#f2a400", "#a10000", "#a15000", "#a1a100", "#626262", "#416600", "#008141",
    "#008282", "#005682", "#000056", "#2b0057", "#6a006a", "#77003c"
];

document.addEventListener('DOMContentLoaded', () => {
    const connectBtn = document.getElementById('connectBtn');
    const senderInput = document.getElementById('senderInput');
    const messageInput = document.getElementById('messageInput');
    const sendBtn = document.getElementById('sendBtn');

    function setChatInputState(enabled, placeholder) {
        if (messageInput) {
            messageInput.disabled = !enabled;
            messageInput.placeholder = placeholder || '';
        }
        if (sendBtn) sendBtn.disabled = !enabled;
    }

    if (connectBtn && (typeof window.SockJS === 'undefined' || (typeof window.StompJs === 'undefined' && typeof window.Stomp === 'undefined'))) {
        connectBtn.disabled = true;
        console.error('Nie załadowano wymaganych bibliotek SockJS i StompJS.');
        setTimeout(() => {
            if (typeof window.SockJS !== 'undefined' && (typeof window.StompJs !== 'undefined' || typeof window.Stomp !== 'undefined')) {
                connectBtn.disabled = false;
            }
        }, 1500);
    }

    // pre-fill username if present
    if (senderInput && myUsername) {
        senderInput.value = myUsername;
    }

    if (connectBtn && senderInput) {
        connectBtn.addEventListener('click', () => {
            myUsername = (senderInput.value || '').trim() || "Anonim";
            // save user info to local storage
            localStorage.setItem('myUsername', myUsername);
            localStorage.setItem('mySessionId', mySessionId);

            // if already connected, disconnect first
            if (stompClient && stompClient.active && typeof stompClient.deactivate === 'function') {
                stompClient.deactivate();
            }

            connect();

            if (myUsername === "Anonim") {
                showAnonymousWarning();
                senderInput.disabled = false;
                connectBtn.disabled = false;
                setChatInputState(false, "Tylko podgląd (anonim nie może pisać)");
            } else {
                removeAnonymousWarning();
                senderInput.disabled = true;
                connectBtn.disabled = true;
                setChatInputState(true, "Napisz wiadomość...");
            }
        });
    } else {
        console.error('Nie znaleziono elementów connectBtn lub senderInput w DOM.');
    }

    if (messageInput) {
        messageInput.addEventListener('keydown', function(event) {
            if (event.key === 'Enter') {
                event.preventDefault();
                sendMessage();
            }
        });
    }

    if (sendBtn) {
        sendBtn.addEventListener('click', sendMessage);
    }

    myColor = getColorForSession(mySessionId);

    // auto connect if session and username are provided
    if (myUsername && myUsername !== "Anonim") {
        const senderInput = document.getElementById('senderInput');
        const connectBtn = document.getElementById('connectBtn');
        if (senderInput) senderInput.value = myUsername;
        if (senderInput) senderInput.disabled = true;
        if (connectBtn) connectBtn.disabled = true;
        setTimeout(connect, 0); // connect after DOM is ready
    }

});

// create STOMP client supporting multiple UMD shapes
function createStompClient() {
    const StompGlobal = window.StompJs || window.Stomp;
    if (typeof SockJS === 'undefined' || !StompGlobal) {
        return null;
    }

    if (StompGlobal.Client) {
        try {
            const client = new StompGlobal.Client({
                webSocketFactory: () => new SockJS('/chat'),
                reconnectDelay: 3000,
                connectHeaders: { username: myUsername },
                onConnect: () => stompOnConnect(),
                onStompError: (frame) => console.error('STOMP error:', frame)
            });
            return client;
        } catch (e) {
            console.warn('Nie udało się utworzyć STOMP klienta. Przechodzenie do starszej wersji...', e);
        }
    }

    // older STOMP ver (v5)
    if (typeof StompGlobal.over === 'function') {
        const raw = StompGlobal.over(new SockJS('/chat'));
        raw.reconnect_delay = 3000;

        const wrapper = {
            active: false,
            activate: function() {
                try {
                    raw.connect(
                        { username: myUsername },
                        (frame) => {
                            this.active = true;
                            try { stompOnConnect(); } catch (e) { console.error('stompOnConnect error', e); }
                        },
                        (err) => {
                            console.error('Błąd połączenia STOMP (v5):', err);
                        }
                    );
                } catch (e) {
                    console.error('Nie udało się połączyć STOMP (v5):', e);
                }
            },
            subscribe: function(destination, callback) {
                return raw.subscribe(destination, callback);
            },
            publish: function({ destination, body, headers }) {
                raw.send(destination, headers || {}, body);
            },
            deactivate: function() {
                try {
                    raw.disconnect(() => { this.active = false; });
                } catch (e) {
                    console.warn('Błąd podczas rozłączania STOMP (v5):', e);
                    this.active = false;
                }
            }
        };
        return wrapper;
    }

    console.error('Nieznany wariant STOMPJS wykryty. Nie można utworzyć klienta.');
    return null;
}

function connect() {
    if (!myUsername) {
        alert("Najpierw wprowadź nazwę!");
        return;
    }
    if (stompClient && stompClient.active) return;

    stompClient = createStompClient();
    if (!stompClient) return;

    try {
        if (typeof stompClient.activate === 'function') {
            stompClient.activate();
        } else if (typeof stompClient.connect === 'function') {
            stompClient.connect({ username: myUsername }, stompOnConnect, (err) => console.error('Błąd połączenia STOMP:', err));
            stompClient.active = true;
        } else {
            console.warn('Klient STOMP nie ma metody activate ani connect. Próba ręcznego ustawienia aktywności.');
            stompClient.active = true;
            stompOnConnect();
        }
    } catch (e) {
        console.error('Nie udało się połączyć z serwerem STOMP:', e);
    }
}

// fetch helpers to populate active users
async function fetchActiveUsers() {
    try {
        const res = await fetch('/activeUsers', { cache: 'no-store' });
        if (!res.ok) {
            console.warn('Nie udało się pobrać aktywnych użytkowników:', res.status);
            return;
        }
        const users = await res.json();
        updateActiveUsers(users);
    } catch (e) {
        console.warn('Nie udało się pobrać aktywnych użytkowników:', e);
    }
}

async function fetchHistory() {
    try {
        // see messages from last hour
        const since = Date.now() - 60 * 60 * 1000;
        const res = await fetch('/api/messages/recent?since=' + since, { cache: 'no-store' });
        if (!res.ok) {
            console.warn('Nie udało się pobrać historii wiadomości:', res.status);
            return;
        }
        const messages = await res.json();
        if (!Array.isArray(messages)) return;

        const chat = document.getElementById('chat');
        if (chat) chat.innerHTML = '';

        messages.sort((a, b) => new Date(a.dateTime) - new Date(b.dateTime));
        messages.forEach(msg => showMessage(msg));
    } catch (e) {
        console.warn('Nie udało się pobrać historii wiadomości:', e);
    }
}

// get assigned color for this session from server
async function fetchSessionColor() {
    if (!mySessionId) return;
    try {
        const res = await fetch('/sessionColor/' + encodeURIComponent(mySessionId), { cache: 'no-store' });
        if (!res.ok) {
            console.debug('Serwer nie przypisał koloru sesji:', res.status);
            return;
        }
        let body = null;
        try {
            body = await res.json();
        } catch (e) {
            try {
                body = await res.text();
            } catch (e2) {
                body = null;
            }
        }
        if (!body) return;
        if (typeof body === 'string') {
            myColor = body;
        } else if (typeof body === 'object' && (body.color || body.colorValue)) {
            myColor = body.color || body.colorValue;
        }
        if (myColor) {
            console.debug('Przydzielono kolor sesji:', myColor);
            applyAssignedColorToPendingLocalMessages();
        }
    } catch (e) {
        console.debug('Błąd podczas pobierania koloru sesji:', e);
    }
}

function applyAssignedColorToPendingLocalMessages() {
    const chat = document.getElementById('chat');
    if (!chat || !myColor) return;
    Array.from(chat.children).forEach(el => {
        if (el.dataset.sessionId === mySessionId && el.dataset.awaitingColor === 'true') {
            const userSpan = el.querySelector('.user');
            if (userSpan) userSpan.style.color = myColor;
            delete el.dataset.awaitingColor;
        }
    });
}

function stompOnConnect() {
    fetchSessionColor().finally(() => {
        fetchActiveUsers();
        fetchHistory();
    });

    // subscribe to active user broadcasts
    if (stompClient && typeof stompClient.subscribe === 'function') {
        stompClient.subscribe('/topic/activeUsers', function(message) {
            try {
                const body = safeParse(message.body) ?? JSON.parse(message.body || 'null');
                if (Array.isArray(body)) {
                    updateActiveUsers(body);
                } else {
                    console.warn('/topic/activeUsers: nieoczekiwany format danych:', message.body);
                }
            } catch (e) {
                try {
                    if (Array.isArray(message.body)) updateActiveUsers(message.body);
                } catch (_) {
                    console.warn('Nie udało się przetworzyć /topic/activeUsers:', e);
                }
            }
        });
    }

    // handle creating/updating/deleting messages
    if (stompClient && typeof stompClient.subscribe === 'function') {
        stompClient.subscribe('/topic/messages', function(message) {
            const msg = safeParse(message.body);
            if (!msg) return;
            const existing = findExistingElementForIncoming(msg);
            if (existing) {
                if (msg.id) existing.dataset.id = msg.id;
                const contentEl = existing.querySelector('.content');
                if (contentEl) contentEl.textContent = msg.content;
                const timeSpan = existing.querySelector('.time');
                if (timeSpan && msg.dateTime) {
                    const time = new Date(msg.dateTime).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
                    timeSpan.textContent = `[${time}] `;
                }
                const userSpan = existing.querySelector('.user');
                if (userSpan && msg.color) userSpan.style.color = msg.color;
                // if server provided color for a previously-pending local message, clear awaiting flag
                if (existing.dataset.awaitingColor === 'true' && msg.color) {
                    existing.dataset.awaitingColor = '';
                    delete existing.dataset.awaitingColor;
                }
            } else {
                showMessage(msg);
            }
        });

        stompClient.subscribe('/topic/messageEdited', function(message) {
            const msg = safeParse(message.body);
            if (!msg) return;
            updateMessage(msg);
        });

        stompClient.subscribe('/topic/messageDeleted', function(message) {
            const deleted = safeParse(message.body);
            if (!deleted) return;
            const identifier = deleted.id ?? deleted.clientMessageId;
            if (!identifier) {
                console.warn('Otrzymano żądanie usunięcia wiadomości bez id:', message.body);
                return;
            }
            deleteMessage(identifier);
        });
    }
}

function sendMessage() {
    const senderEl = document.getElementById('senderInput');
    const messageInputEl = document.getElementById('messageInput');
    const sender = senderEl ? senderEl.value : myUsername;
    const content = messageInputEl ? messageInputEl.value : '';

    if (!sender || !content) return;

    if (!stompClient || !stompClient.active) {
        alert("Połączenie nie zostało jeszcze nawiązane!");
        return;
    }

    const clientMessageId = crypto.randomUUID();
    const chatMessage = {
        sender,
        content,
        sessionId: mySessionId,
        clientMessageId,
        dateTime: new Date().toISOString()
    };

    showMessage(chatMessage, { local: true });

    // unify publish API
    if (typeof stompClient.publish === 'function') {
        stompClient.publish({
            destination: "/app/sendMessage",
            body: JSON.stringify(chatMessage)
        });
    } else if (typeof stompClient.send === 'function') {
        stompClient.send("/app/sendMessage", {}, JSON.stringify(chatMessage));
    } else {
        console.error('Klient STOMP nie obsługuje ani publish, ani send.');
    }

    if (messageInputEl) {
        messageInputEl.value = '';
        messageInputEl.focus(); // return focus
    }
}

function updateMessage(updated) {
    const chat = document.getElementById('chat');
    if (!chat) return;
    const msgEl = Array.from(chat.children).find(el =>
        (updated.id && el.dataset.id === updated.id) ||
        (updated.clientMessageId && el.dataset.clientId === updated.clientMessageId)
    );
    if (msgEl) {
        const contentEl = msgEl.querySelector('.content');
        if (contentEl) contentEl.textContent = updated.content;
        if (updated.id) msgEl.dataset.id = updated.id;
    }
}

function deleteMessage(id) {
    const chat = document.getElementById('chat');
    if (!chat) return;
    let msgEl = Array.from(chat.children).find(el => el.dataset.id === id);
    if (!msgEl) {
        msgEl = Array.from(chat.children).find(el => el.dataset.clientId === id);
    }
    if (msgEl) msgEl.remove();
}

function showMessage(message, options = {}) {
    const chat = document.getElementById('chat');
    if (!chat) return;
    const el = document.createElement('div');
    el.className = "message";

    if (message.id) el.dataset.id = message.id;
    if (message.clientMessageId) el.dataset.clientId = message.clientMessageId;

    el.dataset.sessionId = message.sessionId || '';
    const isoTime = message.dateTime ? new Date(message.dateTime).toISOString() : new Date().toISOString();
    el.dataset.time = isoTime;

    const left = document.createElement('div');
    left.className = "message-left";

    const timeSpan = document.createElement('span');
    timeSpan.className = 'time';
    const time = new Date(isoTime).toLocaleTimeString([], {hour: '2-digit', minute: '2-digit'});
    timeSpan.textContent = `[${time}] `;
    timeSpan.style.fontWeight = 'normal';
    timeSpan.style.marginRight = '3px';

    const userSpan = document.createElement('span');
    userSpan.className = 'user';
    userSpan.textContent = (message.sender || 'Anonim') + ': ';

    const isOwnMessage = message.sessionId === mySessionId;

    const colorValue =
        message.color ||
        (isOwnMessage ? myColor : null) ||
        'black';
    userSpan.style.color = colorValue;
    userSpan.style.fontWeight = 'bold';

    const contentSpan = document.createElement('span');
    contentSpan.className = 'content';

    // clickable links for any sender
    const urlRegex = /^https?:\/\/[^\s]+$/;
    if (urlRegex.test(message.content)) {
        const link = document.createElement('a');
        link.href = message.content;
        link.textContent = message.content;
        link.target = "_blank";
        link.rel = "noopener noreferrer";
        contentSpan.appendChild(link);
    } else {
        contentSpan.textContent = message.content || '';
    }

    left.appendChild(timeSpan);
    left.appendChild(userSpan);
    left.appendChild(contentSpan);
    el.appendChild(left);

    if (options.local && !myColor) {
        el.dataset.awaitingColor = 'true';
    }

    if (message.sessionId === mySessionId) {
        const right = document.createElement('div');
        right.className = "message-buttons";

        const editBtn = document.createElement('button');
        editBtn.textContent = '✏️';
        editBtn.className = 'btn btn-sm btn-warning ms-2';

        const deleteBtn = document.createElement('button');
        deleteBtn.textContent = '❌';
        deleteBtn.className = 'btn btn-sm btn-danger ms-2';

        editBtn.onclick = () => {
            const currentContent = contentSpan.textContent || '';
            const newContent = prompt("Wprowadź nową wiadomość: ", currentContent);
            if (!newContent || newContent === currentContent) return;

            const payload = {
                id: el.dataset.id,
                clientMessageId: el.dataset.clientId,
                content: newContent
            };

            if (typeof stompClient.publish === 'function') {
                stompClient.publish({ destination: "/app/editMessage", body: JSON.stringify(payload) });
            } else if (typeof stompClient.send === 'function') {
                stompClient.send("/app/editMessage", {}, JSON.stringify(payload));
            }
        };

        deleteBtn.onclick = () => {
            if (!confirm("Na pewno chcesz usunąć tą wiadomość?")) return;

            const payload = {
                id: el.dataset.id,
                clientMessageId: el.dataset.clientId
            };

            if (typeof stompClient.publish === 'function') {
                stompClient.publish({ destination: "/app/deleteMessage", body: JSON.stringify(payload) });
            } else if (typeof stompClient.send === 'function') {
                stompClient.send("/app/deleteMessage", {}, JSON.stringify(payload));
            }
        };

        right.appendChild(editBtn);
        right.appendChild(deleteBtn);
        el.appendChild(right);
    }

    chat.appendChild(el);
    chat.scrollTop = chat.scrollHeight;
}

function findExistingElementForIncoming(msg) {
    const chat = document.getElementById('chat');
    if (!chat) return null;

    if (msg.clientMessageId) {
        const byClient = Array.from(chat.children).find(el => el.dataset.clientId === msg.clientMessageId);
        if (byClient) return byClient;
    }
    if (msg.id) {
        const byId = Array.from(chat.children).find(el => el.dataset.id === msg.id);
        if (byId) return byId;
    }

    const incomingTimeIso = msg.dateTime ? new Date(msg.dateTime).toISOString() : null;
    if (incomingTimeIso) {
        const byTime = Array.from(chat.children).find(el =>
            el.dataset.sessionId === msg.sessionId &&
            el.dataset.time === incomingTimeIso &&
            el.querySelector('.content') &&
            el.querySelector('.content').textContent.includes(msg.content)
        );
        if (byTime) return byTime;
    }
    return null;
}

function updateActiveUsers(users) {
    const usersList = document.getElementById('usersList');
    if (!usersList) return;
    usersList.textContent = Array.isArray(users) ? users.join(', ') : '';
}

function safeParse(str) {
    try {
        if (typeof str === 'object') return str; // already parsed
        return JSON.parse(str);
    } catch (e) {
        console.warn('Failed to parse message body', e);
        return null;
    }
}

function showAnonymousWarning() {
    let notif = document.getElementById('anonymousNotif');
    if (!notif) {
        notif = document.createElement('div');
        notif.id = 'anonymousNotif';
        notif.className = 'alert alert-warning mt-2';
        notif.textContent = "Jako anonim możesz tylko oglądać czat. Aby pisać podaj nazwę użytkownika.";
        const container = document.querySelector('.card-body');
        if (container) container.insertBefore(notif, container.firstChild);
    }
}

function removeAnonymousWarning() {
    const notif = document.getElementById('anonymousNotif');
    if (notif && notif.parentNode) notif.parentNode.removeChild(notif);
}

function getColorForSession(sessionId) {
    let hash = 0;
    for (let i = 0; i < sessionId.length; i++) {
        hash = ((hash << 5) - hash) + sessionId.charCodeAt(i);
        hash |= 0;
    }
    const idx = Math.abs(hash) % COLOR_PALETTE.length;
    return COLOR_PALETTE[idx];
}

window.sendMessage = sendMessage;
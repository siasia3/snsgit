let modalStompClient = null;
let modalCurrentChatroomId = null;

// 모달 열기
function openMessageModal() {
    document.getElementById('messageModal').style.display = 'flex';
    const memberId = sessionStorage.getItem('userId');
    loadChatroomList(memberId);
}

// 모달 닫기
function closeMessageModal() {
    document.getElementById('messageModal').style.display = 'none';
    disconnectModalSocket();
    modalCurrentChatroomId = null;
}

// 채팅방 목록 로드
function loadChatroomList(memberId) {
    fetchWithAuth(`/api/chatrooms/me?memberId=${memberId}`, { method: 'GET' })
        .then(res => res.json())
        .then(chatrooms => {
            renderChatroomList(chatrooms);
        });
}

// 채팅방 목록 렌더링
function renderChatroomList(chatrooms) {
    const list = document.getElementById('chatroomList');
    list.innerHTML = '';

    if (chatrooms.length === 0) {
        list.innerHTML = '<div style="padding:1rem; color:#aaa; text-align:center;">채팅방이 없습니다</div>';
        return;
    }

    chatrooms.forEach(room => {
        const item = document.createElement('div');
        item.className = 'chatroom-item';
        item.setAttribute('data-chatroom-id', room.chatroomId);
        item.setAttribute('data-opponent-id', room.opponentId);
        item.setAttribute('data-opponent-nickname', room.opponentNickname);

        const profileSrc = room.opponentProfileImage || '/image/basicProfile.jpg';
        const lastMsg = room.lastMessage || '대화를 시작해보세요';

        item.innerHTML = `
            <div class="profile-div">
                <img src="${profileSrc}" class="receiver-img">
            </div>
            <div class="chatroom-item-info">
                <span class="chatroom-item-name customBoldFont">${room.opponentNickname}</span>
                <span class="chatroom-item-last-msg">${lastMsg}</span>
            </div>
        `;

        item.addEventListener('click', () => {
            // 활성화 표시
            document.querySelectorAll('.chatroom-item').forEach(el => el.classList.remove('active'));
            item.classList.add('active');

            openModalChat(room.chatroomId, room.opponentNickname, profileSrc);
        });

        list.appendChild(item);
    });
}

// 모달 채팅 오픈
function openModalChat(chatroomId, opponentNickname, opponentProfileImg) {
    // 이전 소켓 끊기
    if (modalCurrentChatroomId !== chatroomId) {
        disconnectModalSocket();
    }
    modalCurrentChatroomId = chatroomId;

    // 헤더 세팅
    document.getElementById('modalChatEmpty').style.display = 'none';
    document.getElementById('modalChatHeader').style.display = 'flex';
    document.getElementById('modalChatMessages').style.display = 'flex';
    document.getElementById('modalInputArea').style.display = 'block';

    document.getElementById('modalReceiverName').textContent = opponentNickname;
    document.getElementById('modalReceiverImg').src = opponentProfileImg || '/image/basicProfile.jpg';

    // 채팅 기록 로드
    const chatMessages = document.getElementById('modalChatMessages');
    chatMessages.innerHTML = '';
    fetchWithAuth(`/api/chat/${chatroomId}`, { method: 'GET' })
        .then(res => res.json())
        .then(chats => {
            chats.forEach(chat => renderModalChat(chat));
            chatMessages.scrollTop = chatMessages.scrollHeight;
        });

    // 소켓 연결
    connectModalSocket(chatroomId);
}

// 소켓 연결
function connectModalSocket(chatroomId) {
    if (modalStompClient && modalStompClient.connected) return;

    const socket = new SockJS("/ws-chat");
    modalStompClient = Stomp.over(socket);
    modalStompClient.connect({}, () => {
        modalStompClient.subscribe(`/sub/chat/room/${chatroomId}`, function (msg) {
            const chat = JSON.parse(msg.body);
            renderModalChat(chat);
            const chatMessages = document.getElementById('modalChatMessages');
            chatMessages.scrollTop = chatMessages.scrollHeight;
        });
    });
}

// 소켓 끊기
function disconnectModalSocket() {
    if (modalStompClient && modalStompClient.connected) {
        modalStompClient.disconnect(() => {});
        modalStompClient = null;
    }
}

// 메시지 전송
function sendModalMessage() {
    const input = document.getElementById('modalMessageInput');
    const content = input.value.trim();
    if (!content || !modalCurrentChatroomId) return;

    const message = JSON.stringify({
        type: "TALK",
        chatRoomId: modalCurrentChatroomId,
        senderId: sessionStorage.getItem('userId'),
        message: content
    });
    modalStompClient.send("/pub/chat/send", {}, message);
    input.value = '';
}

// 채팅 렌더링 (기존 renderChat 참고)
function renderModalChat(chat) {
    const myId = sessionStorage.getItem('userId');
    const chatMessages = document.getElementById('modalChatMessages');
    const lastEl = chatMessages.lastElementChild;

    if (lastEl) {
        const lastTime = lastEl.querySelector('.message') && lastEl.querySelector('.message').getAttribute('data-chat-time');
        if (lastTime && isDifferentDate(chat.createdAt, lastTime)) {
            renderModalChatTime(chat.createdAt);
        }
    } else {
        renderModalChatTime(chat.createdAt);
    }

    if (chat.senderId == myId) {
        const tmpl = document.getElementById('modalSenderMessage').content.cloneNode(true);
        tmpl.querySelector('.sender-message').textContent = chat.content;
        tmpl.querySelector('.sender-message').setAttribute('data-chat-time', chat.createdAt);
        chatMessages.appendChild(tmpl);
    } else {
        const tmpl = document.getElementById('modalReceiverMessage').content.cloneNode(true);
        tmpl.querySelector('.receiver-message').textContent = chat.content;
        tmpl.querySelector('.receiver-message').setAttribute('data-chat-time', chat.createdAt);
        if (chat.profilePath) tmpl.querySelector('.receiver-img').src = chat.profilePath;
        chatMessages.appendChild(tmpl);
    }
}

function renderModalChatTime(createdAt) {
    const chatMessages = document.getElementById('modalChatMessages');
    const tmpl = document.getElementById('modalChatTimeTemplate').content.cloneNode(true);
    tmpl.querySelector('#timeArea').textContent = formatDate(createdAt);
    chatMessages.appendChild(tmpl);
}

// 이벤트 바인딩
document.addEventListener('DOMContentLoaded', function () {
    document.getElementById('messageModalClose').addEventListener('click', closeMessageModal);

    document.getElementById('messageModal').addEventListener('click', function (e) {
        if (e.target === this) closeMessageModal();
    });

    document.getElementById('modalMessagePostBtn').addEventListener('click', sendModalMessage);

    document.getElementById('modalMessageInput').addEventListener('keydown', function (e) {
        if (e.key === 'Enter' && !e.shiftKey && this.value.trim() !== '') {
            e.preventDefault();
            sendModalMessage();
        }
    });
});

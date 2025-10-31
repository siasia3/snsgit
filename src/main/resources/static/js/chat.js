function connect(chatroomId) {

    let socket = new SockJS("/ws-chat");
    stompClient = Stomp.over(socket);
    stompClient.connect({}, () => onConnected(chatroomId), onError(chatroomId));

    socket.onclose = (event) => {
        if (!event.wasClean) {
            console.log("비정상 종료 발생, 재연결 시도");
            setTimeout(() => connect(chatroomId), 2000);
        }
    };

}

let reconnectDelay = 2000;
function onConnected(chatroomId) {
    // Subscribe to the Public Topic
    stompClient.subscribe(`/sub/chat/room/${chatroomId}`, function(onMessageReceived){
        renderChat(JSON.parse(onMessageReceived.body));
    });

    stompClient.subscribe("/user/queue/errors", function (errorMessage) {
        const error = errorMessage.body;
        alert("잠시 후 다시 시도해주세요.");
    });



}

function sendMessage(message){
    stompClient.send("/pub/chat/send", {}, message);
}

function onError(chatroomId) {
    console.error('❌ 연결 실패 또는 끊김: 재연결 시도중');
    setTimeout(() => connect(chatroomId), reconnectDelay);
    /*connectingElement.textContent = 'Could not connect to WebSocket server. Please refresh this page to try again!';
    connectingElement.style.color = 'red';*/
}

//수신자 정보 api
function getReceiverInfo(nickname){
    const options = {
        method: 'GET'
    };
    return fetchWithAuth(`/api/user/${nickname}`,options)
        .then(response => response.json());
}

//수신자 정보 렌더링
function renderReceiverInfo(receiverData){
    if(receiverData.profilePath){
        document.querySelector('.receiver-img').src = receiverData.profilePath;
    }
    document.getElementById('receiverInfo').setAttribute('data-member-id',receiverData.memberId);
    document.querySelector('.receiverName').textContent = receiverData.nickname;
}

//채팅방 정보 api
function getChatroom(sender,receiver){
    const options = {
        method: 'GET'
    };
    return fetchWithAuth(`/api/chatroom/member?senderId=${sender}&receiverId=${receiver}`,options)
        .then(response => response.json());
}

//채팅 기록 api
function getChats(chatroomId){
    const options = {
        method: 'GET'
    };
    return fetchWithAuth(`/api/chat/${chatroomId}`,options)
        .then(response => response.json());
}

//입력한 채팅 추가
function renderChat(chat){
    let myId = sessionStorage.getItem('userId');
    let chatArea = document.getElementById('chatArea');
    let lastChat = chatArea.lastElementChild;
    if(lastChat){
        let lastChatTime = lastChat.querySelector('.message').getAttribute('data-chat-time');
        let firstChatTime = chat.createdAt;
        if(isDifferentDate(firstChatTime,lastChatTime)){
            renderChatTime(chat.createdAt);
        }
    }
    if(!lastChat){
        renderChatTime(chat.createdAt);
    }
    if(chat.senderId == myId){
        let senderTemplate = document.getElementById('senderMessage').content.cloneNode(true);
        senderTemplate.querySelector('.sender-message-div').setAttribute('data-chat-id',chat.chatId);
        senderTemplate.querySelector('.sender-message').textContent = chat.content;
        senderTemplate.querySelector('.sender-message').setAttribute('data-chat-time',chat.createdAt);
        chatArea.appendChild(senderTemplate);
    }
    if(chat.senderId != myId){
        let receiverTemplate = document.getElementById('receiverMessage').content.cloneNode(true);
        receiverTemplate.querySelector('.receiver-message-div').setAttribute('data-chat-id',chat.chatId);
        receiverTemplate.querySelector('.receiver-message').textContent = chat.content;
        receiverTemplate.querySelector('.receiver-message').setAttribute('data-chat-time',chat.createdAt);
        if(chat.profilePath) {
            receiverTemplate.querySelector('.receiver-img').src = chat.profilePath;
        }
        chatArea.appendChild(receiverTemplate);
    }
}

//채팅 시간 포맷
function formatDate(dateString) {
    const date = new Date(dateString);

    const year = date.getFullYear();
    const month = date.getMonth() + 1; // 0부터 시작
    const day = date.getDate();

    let hours = date.getHours();
    const minutes = String(date.getMinutes()).padStart(2, '0');

    const isPM = hours >= 12;
    const period = isPM ? '오후' : '오전';

    if (hours === 0) {
        hours = 12;
    }
    if (hours > 12) {
        hours -= 12;
    }

    return `${year}. ${month}. ${day}. ${period} ${hours}:${minutes}`;
}

//채팅 시간 비교
function isDifferentDate(firstChatTime,lastChatTime){
    const firstChatDate = new Date(firstChatTime);
    const lastChatDate = new Date(lastChatTime);

    return(
        lastChatDate.getFullYear() !== firstChatDate.getFullYear() ||
        lastChatDate.getMonth() !== firstChatDate.getMonth() ||
        lastChatDate.getDate() !== firstChatDate.getDate()
    );

}


//채팅 시간 렌더링
function renderChatTime(chatCreatedAt){
    let chatTimeTemplate = document.getElementById('chatTimeTemplate').content.cloneNode(true);
    let chatArea = document.getElementById('chatArea');
    chatTimeTemplate.getElementById('timeArea').textContent = formatDate(chatCreatedAt);
    chatArea.appendChild(chatTimeTemplate);
}





//친구 요청 상태 확인 api
async function friendRequestCheck(myId,userId){
    const options = {
        method: 'GET'
    };
    try{
        const response = await fetchWithAuth(`/api/friend-request/state?myId=${myId}&userId=${userId}`,options);

        if (response.status === 200) {
            const data = await response.json();
            return { status: 'OK', data };
        }

        if (response.status === 204) {
            return { status: 'EMPTY', data: null };
        }

    }catch (error) {

    }
}


//회원페이지 친구요청 렌더링
function renderFriendRequestByUser(friendRequest){
    //친구요청을 받거나 보낸 적이 없는 경우
    if(friendRequest.status=='EMPTY'){
        document.getElementById('friendRequest-btn').style.display = 'block';
    }
    if(friendRequest.status=='OK'){
        let data = friendRequest.data;
        let state = data.state;

        document.getElementById('friendReq-btns').setAttribute('data-friendReq-id',data.friendRequestId);

        if(state == 'ACCEPTED'){
            document.getElementById('friend-btn').style.display = 'block';
        }
        if(state == 'REQUESTED'){
            let id = sessionStorage.getItem('userId');
            if(data.senderId == id){
                document.getElementById('friendRequest-btn-cancel').style.display = 'block';
            }
            if(data.receiverId == id){
                document.getElementById('friendRequestIng-btn').style.display = 'block';
                document.getElementById('friendRequestIng').style.display = 'flex';
            }
        }
    }
}

//친구 요청 수락 api(친구 추가)
async function friendRequestAccept(friendRequestId){

    const options = {
        method: 'PATCH'
    };
    try{
        let response = await fetchWithAuth(`/api/friend-request/${friendRequestId}/accept`,options);

        if(response.status==200){
            return true;
        }

    }catch(error){
        alert("잠시 후 다시 시도해주시길 바랍니다.");
        return false;
    }

}

//친구 요청 거절 api
async function friendRequestRefuse(friendRequestId){

    const options = {
        method: 'Delete'
    };

    try{
        await fetchWithAuth(`/api/friend-request/${friendRequestId}`,options);
        return true;
    }catch(error){
        alert("잠시 후 다시 시도해주시길 바랍니다.");
        return false;
    }

}

//친구 상태 확인 api
async function getFriend(myId,userId){
    const options = {
        method: 'GET'
    };

    const response = await fetchWithAuth(``,options);
        if (response.status === 200) {
            const data = await response.json();
            return { status: 'OK', data };
        }

        if (response.status === 204) {
            return { status: 'EMPTY', data: null };
        }

}
//친구목록 get api
async function getFriends(){

    const options = {
        method: 'GET'
    };

    try{
        let myId = sessionStorage.getItem('userId');
        let response= await fetchWithAuth(`/api/friends?memberId=${myId}`,options);
        return response.json();
    } catch(error){
        alert("잠시 후 다시 시도해주시길 바랍니다.");
        console.log(error);
    }

}

document.getElementById("friendSidebar").addEventListener('click',function (event){
    if (event.target && event.target.closest('.friendDiv')) {
        let nickname = event.target.closest('.friendDiv').querySelector('.customBoldFont').textContent;
        window.location.href = `${BASE_URL}/user/${nickname}`
    }
})


//친구목록 렌더링
function renderFriendList(friendList){
    let friendSidebar = document.getElementById('friendMain');
    friendList.forEach(function (friend){
        let friTemplate = document.getElementById('friendTemplate').content.cloneNode(true);
        friTemplate.querySelector('.friendDiv').setAttribute('data-friend-id',friend.friendId);
        if(friend.friendProfilePath){
            friTemplate.querySelector('.friProfile').src = friend.friendProfilePath;
        }
        let friNickname = friTemplate.querySelector('.friNickName');
        friNickname.textContent = friend.friendNickname;
        friNickname.setAttribute('data-user-id',friend.friendMemId);
        friendSidebar.appendChild(friTemplate);
    });
}

//친구목록 친구 삭제 api
async function deleteFriend(friendId,friendMemberId){

    const options = {
        method: 'DELETE'
    };

    let myId = sessionStorage.getItem('userId');

    await fetchWithAuth(`/api/friend/${friendId}?myMemberId=${myId}&friendMemberId=${friendMemberId}`,options);
    return true;
}

//userDetail페이지 친구삭제
async function deleteFriend(friendMemberId){

    const options = {
        method: 'DELETE'
    };

    try{
        let myId = sessionStorage.getItem('userId');
        await fetchWithAuth(`/api/friend?myMemberId=${myId}&friendMemberId=${friendMemberId}`,options);
        return true;

    } catch(error) {
        alert("잠시 후 다시 시도해주세요.");
        return false;
    }
}



//친구요청 보내기 api
async function sendFriendRequest(myId,userId) {
    const data = {
        senderId: myId,
        receiverId: userId
    }

    const options = {
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        },
        body: JSON.stringify(data)
    }

    try{
        const response = await fetchWithAuth(`/api/friend-request`, options)
        if (response.status == 200) {
            return response.json();
        }
    } catch(error) {
        alert("친구 요청 수락 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.");
        return null;
    }

}
//받은 친구요청 조회 api
async function receivedRequestAPI(){
    const options = {
        method: 'GET'
    };

    try{
        let myId = sessionStorage.getItem('userId');
        let response= await fetchWithAuth(`/api/friend-request/received?receiverId=${myId}`,options);
        return response.json();
    } catch(error){
        alert("잠시 후 다시 시도해주시길 바랍니다.");
        console.log(error);
    }

}

//보낸 친구요청 조회 api
async function sentRequestAPI(){
    const options = {
        method: 'GET'
    };

    try{
        let myId = sessionStorage.getItem('userId');
        let response= await fetchWithAuth(`/api/friend-request/sent?senderId=${myId}`,options);
        return response.json();
    } catch(error){
        alert("잠시 후 다시 시도해주시길 바랍니다.");
        console.log(error);
    }

}


//받은 친구요청 렌더링
function renderReceivedRequest(receivedRequests){
    let receivedArea = document.getElementById('received');
    receivedArea.style.removeProperty("height");
    receivedArea.querySelector('.noFriendRequest').style.display = 'none';
    let receivedDiv = receivedArea.querySelector('.receivedRequestArea');
    receivedRequests.forEach(function(receivedRequest){
        let friendRequestTemplate = document.getElementById('received-friendRequest-template').content.cloneNode(true);
        friendRequestTemplate.querySelector('.friendReq').setAttribute('data-friendRequest-id',receivedRequest.friendRequestId);
        friendRequestTemplate.querySelector('.friendProfile').setAttribute('data-sender-id',receivedRequest.senderId);
        friendRequestTemplate.querySelector('.nickname').textContent = receivedRequest.nickname;
        if(receivedRequest.profilePath){
            friendRequestTemplate.querySelector('.profileImg').src =  receivedRequest.profilePath;
        }
        receivedDiv.appendChild(friendRequestTemplate);
    });
}

//보낸 친구요청 렌더링
function renderSentRequest(sentRequests){
    let sentArea = document.getElementById('sent');
    sentArea.style.removeProperty("height");
    sentArea.querySelector('.noFriendRequest').style.display = 'none';
    let sentDiv = sentArea.querySelector('.sentRequestArea');
    sentRequests.forEach(function(sentRequest){
        let friendRequestTemplate = document.getElementById('sent-friendRequest-template').content.cloneNode(true);
        friendRequestTemplate.querySelector('.friendReq').setAttribute('data-friendRequest-id',sentRequest.friendRequestId);
        friendRequestTemplate.querySelector('.friendProfile').setAttribute('data-receiver-id',sentRequest.receiverId);
        friendRequestTemplate.querySelector('.nickname').textContent = sentRequest.nickname;
        if(sentRequest.profilePath){
            friendRequestTemplate.querySelector('.profileImg').src =  sentRequest.profilePath;
        }
        sentDiv.appendChild(friendRequestTemplate);
    });
}

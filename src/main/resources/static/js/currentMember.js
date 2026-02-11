
//회원정보 api
async function getMemberInfo() {
    try {
        // 서버의 /memberId 엔드포인트로 GET 요청 보내기
        const response = await fetch(`/api/member/info`, {
            method: 'GET'
        });

        // 응답이 성공적이면
        if (response.ok) {
            const memberInfo = await response.json();
            //sessionStorage 저장
            sessionStorage.setItem('userId',memberInfo.memberId);
            sessionStorage.setItem('nickname',memberInfo.nickname);
            if(memberInfo.profilePath) {
                sessionStorage.setItem('profileImage', memberInfo.profilePath);
            }
        }
        if(response.status == 401) {
            const errorData = await response.json();
            console.error('에러 발생:', errorData.message); // 실패 메시지 출력
            alert("로그인을 다시 해주시길 바랍니다.");
            window.location.href = `/`;
        }
    } catch (error) {
        console.error('서버 요청 중 에러:', error);
        alert("잠시 후 다시 로그인 해주세요");
        window.location.href = `/`;
    }
}

//헤더 오른쪽 회원정보 렌더링
function renderHeaderMemberInfo(){

    let profilePath = sessionStorage.getItem('profileImage');
    if(profilePath) {
        document.getElementById('headerProfileImage').src = profilePath;
    }
    document.getElementById('memberName').children[0].textContent = sessionStorage.getItem('nickname') + "님";
}

//프로필편집 회원정보 조회 api
async function getMemberProfileEditInfo(){
    const memberId = sessionStorage.getItem('userId');
    try{
        const response = await fetchWithAuth(`/api/member/${memberId}`, {
            method: 'GET'
        });

        return await response.json();

    } catch (error){
        window.location.href = `/error/500`;
    }
}

//프로필편집 회원정보 렌더링
function memberProfileRender(memberProfileInfo){

    document.getElementById("myName").textContent = memberProfileInfo.name;
    if(memberProfileInfo.memberProfilePath){
        document.getElementById("profileImagePreview").src = memberProfileInfo.memberProfilePath;
    }
    document.getElementById("myId").value = sessionStorage.getItem('userId');
    document.getElementById("nickname").value = memberProfileInfo.nickname;
    if(memberProfileInfo.birthdate){
        document.getElementById("birthdate").value = memberProfileInfo.birthdate;
    }
    if(memberProfileInfo.gender){
        document.getElementById("gender").value = memberProfileInfo.gender;
    }

}

//회원프로필 편집 저장
async function editMemberProfile(memberProfileInfo){

    const options = {
        method: 'PATCH',
        body: memberProfileInfo
    };
    try{
        let response = await fetchWithAuth(`/api/member`,options);

        if(response.status==200){
            return await response.json();
        }

    }catch(error){
        if(error.status == 400) {
            alert("입력 정보를 다시 확인해주세요.");
        }else{
            alert("잠시 후 다시 시도해주세요.");
        }
    }
}

//회원 닉네임 중복확인
async function checkNicknameDuplicated(nickname){
    try{
        const options = {
            method: 'GET'
        };
        const response = await fetchWithAuth(`/api/member/check-nickname?nickname=${nickname}`, options);
        if(response.ok){
            return {state:true,message:'사용하실 수 있는 닉네임입니다.'};
        }

    } catch (error){
        if(error.status === 409){
            return {state:false,message:'중복된 닉네임입니다.'};
        }else{
            return {state:false,message:'잠시 후 다시 시도해주시길 바랍니다'};
        }
    }
}





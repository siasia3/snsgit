
const BASE_URL = 'https://horizonsns.com';
//회원가입 닉네임 중복체크
async function checkNickname(nickname){
    try {
        let response = await fetch(`${BASE_URL}/api/member/check-nickname?nickname=${nickname}`);

        if (!response.ok) {
            if (response.status === 409) {
                return { state: false, message: '중복된 닉네임입니다.' };
            }
            return { state: false, message: '서버 오류가 발생했습니다.' };
        }

        if(response.ok){
            isNicknameChecked = true;
            return {state:true,message:'사용 가능한 닉네임입니다.'};
        }

        //올리가 없지만 만약 여기로 온다면 혹시 모를 예외처리
        return { state: false, message: '닉네임 확인에 실패했습니다.' };

    } catch (error){
        return {state:false,message:'잠시 후 다시 시도해주시길 바랍니다'};
    }
}

//회원가입 ID 중복체크

async function checkUserId(userId){
    try {
        let response = await fetch(`${BASE_URL}/api/member/check-userId?userId=${userId}`);

        if (!response.ok) {
            if (response.status === 409) {
                return { state: false, message: '중복된 회원 아이디입니다.' };
            }
            return { state: false, message: '서버 오류가 발생했습니다.' };
        }

        if(response.ok){
            isUserIdChecked = true;
            return {state:true,message:'사용 가능한 회원 아이디입니다.'};
        }

        //올리가 없지만 만약 여기로 온다면 혹시 모를 예외처리
        return { state: false, message: '회원 아이디 확인에 실패했습니다.' };

    } catch (error){
        return {state:false,message:'잠시 후 다시 시도해주시길 바랍니다'};
    }
}

async function signup(formData){
    try {
        const response = await fetch(`${BASE_URL}/api/member/signup`, {
            method: 'POST',
            body: formData
        });

        return response;

    } catch (error) {
        alert('네트워크 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
}
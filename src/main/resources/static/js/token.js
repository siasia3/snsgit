const BASE_URL = 'https://horizonsns.com';

async function refreshAccessToken() {
    try {
        const response = await fetch(`/auth/refresh`, {
            method: 'POST',
            credentials: 'include', // 쿠키 사용 시 필요
        });

        if (response.ok) {
            console.log(response);
            let refresh = await response.json();

            return true;
        } else {
            //console.error('Refresh Token 만료 또는 유효하지 않음');
            alert('로그인이 만료되었습니다. 다시 로그인해주세요.');
            window.location.href = `/`; // 로그인 페이지로 리다이렉트
            return false;
        }
    } catch (error) {

        console.log("야야야");
        //throw new Error(error);
    }
}

async function logout() {
    try {
        const response = await fetch(`/auth/logout`, {
            method: 'POST',
            credentials: 'include', // 쿠키 사용 시 필요
        });

        if (response.ok) {
            sessionStorage.removeItem("nickname");
            sessionStorage.removeItem("profileImage");
            sessionStorage.removeItem("userId");
            window.location.href = `/`;

        }else {
            alert('잠시 후 다시 시도해주세요.');
        }

    } catch (error) {
        alert('오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
    }
}

async function fetchWithAuth(endpoint, options = {}) {
    try {
        // 첫 번째 요청: 쿠키에 있는 토큰 자동 전송
        if(!options.noSpinner){
            showLoadingSpinner();
        }
        const response = await fetch(`${endpoint}`, {
            ...options,
            credentials: 'include', // 쿠키를 요청에 포함
        });

        if (response.ok) {
            hideLoadingSpinner();
            return response; // 성공적인 응답 반환
        } else if (response.status === 401) {
            // 401 Unauthorized: Access Token 갱신 후 재요청
            console.warn('Access Token 만료. 갱신 시도 중...');
            let refresh = await refreshAccessToken(); // 토큰 갱신

            if(refresh){
                // 쿠키에 새 Access Token이 갱신되었으므로, 다시 요청
                const retryResponse = await fetch(`${endpoint}`, {
                    ...options,
                    credentials: 'include', // 쿠키 포함
                });

                if (retryResponse.ok) {
                    hideLoadingSpinner();
                    return retryResponse;
                } else {
                    const errorBody = await response.json();
                    throw {
                        status: response.status, // 상태코드
                        body: errorBody           // 응답 메시지
                    };
                }
            }

        } else {
            // 기타 오류 401을 제외한 400~500번대
            const errorBody = await response.json();
            //console.error(`요청 실패: ${response.status} - ${errorBody}`);
            throw {
                status: response.status, // 상태코드
                body: errorBody           // 응답 메시지
            };
        }
    } catch (error) {
        if(error.status<500){
            throw new Error(error);
        }
        window.location.href = `/error/500`;
    } finally {
        hideLoadingSpinner();
    }
}

function handleApiError(error){
    if (error.status >= 500) {
        // 서버 에러
        window.location.href = `/error/500`;
    } else if (error.status === 401) {
        // 토큰 만료 또는 인증되지 않은 사용자
        window.location.href = `/login`;
    } else {
        // 그 외 400번대;
        alert("잘못된 요청을 보내셨습니다.");
    }
}
document.addEventListener('DOMContentLoaded', function() {
    const loginForm = document.getElementById('loginForm');
    const errorMessage = document.getElementById('errorMessage');

    loginForm.addEventListener('submit', async function(e) {
        e.preventDefault();

        // 에러 메시지 초기화
        if (errorMessage) {
            errorMessage.style.display = 'none';
            errorMessage.textContent = '';
        }

        // 폼 데이터 가져오기
        const userId = document.getElementById('username').value.trim();
        const password = document.getElementById('password').value.trim();

        // 유효성 검사
        if (!userId || !password) {
            showError('아이디와 비밀번호를 모두 입력해주세요.');
            return;
        }

        // 로그인 요청
        try {
            const response = await fetch('/api/login', {
                method: 'POST',
                headers: {
                    'Content-Type': 'application/json'
                },
                body: JSON.stringify({
                    userId: userId,
                    password: password
                })
            });

            if (response.ok) {
                // 로그인 성공 - 메인 페이지로 이동
                window.location.href = '/main';
            } else if (response.status === 401) {
                // 인증 실패
                showError('아이디 또는 비밀번호가 일치하지 않습니다.');
            } else if (response.status === 400) {
                // 잘못된 요청
                const errorData = await response.json().catch(() => ({}));
                showError(errorData.message || '잘못된 요청입니다.');
            } else {
                // 기타 에러
                showError('로그인 중 오류가 발생했습니다. 잠시 후 다시 시도해주세요.');
            }
        } catch (error) {
            console.error('로그인 에러:', error);
            showError('서버와의 연결에 실패했습니다. 네트워크 연결을 확인해주세요.');
        }
    });

    // 에러 메시지 표시 함수
    function showError(message) {
        if (errorMessage) {
            errorMessage.textContent = message;
            errorMessage.style.display = 'block';
        } else {
            alert(message);
        }
    }
});



// 모달 닫기
function closeCreateModal() {
    const modal = document.getElementById('writeModal');
    modal.classList.remove('show'); // show 클래스를 제거하여 모달 숨기기
    resetWriteModal();
}
function closeUpdateModal() {
    const modal = document.getElementById('modifyModal');
    modal.classList.remove('show');
}

// 페이지 로드가 완료되면 로딩 스피너를 숨김
window.addEventListener('load', function() {
    document.getElementById('loadingSpinner').style.setProperty('display', 'none', 'important');
});

// 로딩 스피너 보이기
function showLoadingSpinner() {
    document.getElementById('loadingSpinner').style.setProperty('display', 'flex', 'important');
}

// 로딩 스피너 숨기기
function hideLoadingSpinner() {
    document.getElementById('loadingSpinner').style.setProperty('display', 'none', 'important');
}


const writeBtn = document.getElementById("writing");
const modal = document.getElementById('writeModal');
writeBtn.addEventListener("click",function () {
    modal.classList.add('show');
});
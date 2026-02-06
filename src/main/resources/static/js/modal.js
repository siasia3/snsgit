const detailCloseBtn = document.getElementById('closeDetailModalBtn');
const detailModalOverlay = document.getElementById('detailModalOverlay');
const postDetailModal = document.getElementById('postDetailModal');
const postMedia = document.querySelector('.post-media');
const commentInner = document.getElementById('comment-inner');

//게시글 상세조회 모달 열기 이벤트 등록
document.querySelectorAll('.userPostArea-inner').forEach(post => {
    post.addEventListener("click", event =>{

        if (event.target.closest(".postOverlay")) {
            let postId = event.target.closest('.memberPost').getAttribute('data-post-id');
            getPostDetail(postId).then(postResponse => renderPostDetail(postResponse));
            getComments(postId,15,0).then(commentResponse => renderCommentElements(commentResponse));
            detailModalOverlay.classList.remove('hidden');
        }

    })
})
// 게시글 상세모달 X 버튼 클릭 시 닫기
detailCloseBtn.addEventListener('click', () => {

    detailModalOverlay.classList.add('hidden');

    //첨부파일 비우기
    postMedia.querySelectorAll(':scope > *').forEach(child => {
        if (child.tagName.toLowerCase() !== 'template') {
            child.remove();
        }
    });
    //댓글 비우기
    commentInner.querySelectorAll(':scope > *').forEach(child => {
        if (child.tagName.toLowerCase() !== 'template') {
            child.remove();
        }
    });

    //내용 비우기
    const content = document.getElementById('post-detail-content');

    if (content) {
        content.innerHTML = `<span class="customBoldFont small-pr-custom authorName"></span>`;
    }
});


// 게시글 상세모달 밖 클릭 시 닫기
detailModalOverlay.addEventListener('click', (e) => {

    if (e.target === postDetailModal) {

        detailModalOverlay.classList.add('hidden');

        postMedia.querySelectorAll(':scope > *').forEach(child => {
            if (child.tagName.toLowerCase() !== 'template') {
                child.remove();
            }
        });
        commentInner.querySelectorAll(':scope > *').forEach(child => {
            if (child.tagName.toLowerCase() !== 'template') {
                child.remove();
            }
        });

        //내용 비우기
        const content = document.getElementById('post-detail-content');

        if (content) {
            content.innerHTML = `<span class="customBoldFont small-pr-custom authorName"></span>`;
        }

    }
});
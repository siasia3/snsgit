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

    // 댓글 입력창 비우기 (추가)
    document.getElementById('detailCommentInput').value = '';
    document.getElementById('detailCommentInput').nextElementSibling.style.display = 'none';
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

// 게시글 상세 모달 내 수정/삭제 버튼 이벤트
document.getElementById('postDetailModal').addEventListener('click', async (event) => {

    // 수정 버튼
    if (event.target && event.target.classList.contains('postModifyBtn')) {
        const postId = document.querySelector('.postDetail-modal-content').getAttribute('data-post-id');
        let post = await getPost(postId);
        renderBeforePostUpdate(post);
        document.getElementById('modifyModal').classList.add('show');
    }

    // 삭제 버튼
    if (event.target && event.target.classList.contains('postDeleteBtn')) {
        const postId = document.querySelector('.postDetail-modal-content').getAttribute('data-post-id');

        const result = await Swal.fire({
            title: '정말 삭제하시겠습니까?',
            text: '삭제 후에는 복구할 수 없습니다.',
            icon: 'warning',
            showCancelButton: true,
            confirmButtonColor: '#d33',
            cancelButtonColor: '#3085d6',
            confirmButtonText: '삭제',
            cancelButtonText: '취소'
        });

        if (result.isConfirmed) {
            try {
                await deletePost(postId);
                await Swal.fire({
                    title: '삭제 완료!',
                    text: '게시글이 성공적으로 삭제되었습니다.',
                    icon: 'success',
                    confirmButtonText: '확인'
                });
                // 모달 닫기
                detailModalOverlay.classList.add('hidden');
                location.reload();
            } catch (error) {
                console.error(error);
                Swal.fire({
                    title: '오류 발생',
                    text: '게시글 삭제 중 문제가 발생했습니다.',
                    icon: 'error',
                    confirmButtonText: '확인'
                });
            }
        }
    }
});
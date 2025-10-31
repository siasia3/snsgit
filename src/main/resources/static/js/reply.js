document.getElementById('postDetailModal').addEventListener('click', async (event) => {

    //대댓글 달기 버튼 클릭
    if(event.target && event.target.classList.contains('replyInputBtn')){
        let targetComment = event.target.closest('.comment');
        let parentId = targetComment.getAttribute('data-comment-id');
        let commentAuthor = targetComment.querySelector('.commentAuthor').innerText;
        let commentInput = document.getElementById('detailCommentInput');
        commentInput.removeAttribute('data-parent-id');
        commentInput.value = '@'+commentAuthor+' ';
        commentInput.setAttribute('data-parent-id',parentId);
        commentInput.focus();
        commentInput.nextElementSibling.style.display = 'block';
    }

    //대댓글 삭제 클릭
    if (event.target && event.target.classList.contains('replyRemove')){
        const targetReply = event.target.closest('.reply');
        let replyId = targetReply.getAttribute("data-reply-id");

        // SweetAlert2로 확인창 표시
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

        // 확인 눌렀을 때만 실행
        if (result.isConfirmed) {
            try {
                let result = await deleteComment(replyId);
                if(result.message=="ok"){
                    let replyLength = targetReply.closest('.comment').querySelectorAll('.reply').length;
                    if(replyLength == 1){
                        //대댓글 한개인 경우 대댓글 area 삭제
                        targetReply.closest('.reply-area').remove();
                    }else{
                        //대댓글이 두개이상인 경우 선택된 대댓글 요소만 삭제
                        let replyCountElement = targetReply.closest(".reply-area").querySelector(".replyCount");
                        targetReply.remove();

                    }

                }

            } catch (error) {
                console.error(error);
                /* Swal.fire({
                     title: '오류 발생',
                     text: '댓글 삭제 중 문제가 발생했습니다.',
                     icon: 'error',
                     confirmButtonText: '확인'
                 });*/
            }
        }

    }

    //대댓글 더보기 클릭
    if(event.target && event.target.classList.contains('replySeeMore')){
        let replyMoreBtn = event.target;
        let replyArea = replyMoreBtn.closest('.reply-area').querySelector('.replyInner');
        let targetComment = replyMoreBtn.closest('.comment');
        let parentId = targetComment.getAttribute('data-comment-id');
        let replyCntSpan =replyMoreBtn.querySelector('.replyCount');
        let replyCnt =  parseInt(replyCntSpan.textContent, 10);
        let page = replyMoreBtn.getAttribute('data-reply-page');
        let replies = await getReplies(parentId,page,replyCnt);

        let writtenReply = replyArea.querySelectorAll('.reply');
        let writtenReplyIds = new Set([...writtenReply]
            .map(reply => reply.dataset.replyId));
        replies = replies.filter(
            reply => !writtenReplyIds.has(reply.commentId.toString())
        );

        if(replies.length > 0){
            renderReply(replies,replyArea);
            replyCntSpan.innerText = replyCnt - replies.length;
            replyMoreBtn.setAttribute('data-reply-page',page+1);
        }
        if((replyCnt - replies.length)==0){
            replyMoreBtn.style.display = "none";
            replyMoreBtn.classList.remove('replySeeMore');
            replyMoreBtn.classList.add('replySee');
            replyMoreBtn.parentElement.querySelector('.replyHide').style.display = "block";
        }
        return;
    }

    //대댓글 숨기기 클릭
    if(event.target && event.target.classList.contains('replyHide')){
        let replyInner = event.target.closest('.reply-area').querySelector('.replyInner');
        replyInner.style.display = "none";
        let replySeeBtn = event.target.parentElement.querySelector('.replySee');
        replySeeBtn.querySelector('.replyCount').innerText = replyInner.querySelectorAll('.reply').length;
        replySeeBtn.style.display = "block";
        event.target.style.display = "none";
        return;
    }

    //숨겨진 대댓글 보기
    if(event.target && event.target.classList.contains('replySee')){
        event.target.style.display = "none";
        let replyInner = event.target.closest('.reply-area').querySelector('.replyInner');
        replyInner.style.display = "block";
        event.target.parentElement.querySelector('.replyHide').style.display = "block";
        return;
    }

});


let isReplyFetching = false; // 중복 요청 방지
let hasMoreReplies = true;
const replySize = 10;
//게시글 페이징 api

async function getReplies(parentId,page,replyCnt){
    if(replyCnt == 0) return;
    if (!hasMoreReplies || isReplyFetching) return;
    isReplyFetching = true;
    const options = {
        noSpinner: true,
        method: 'GET'
    };
    const response = await fetchWithAuth(`/api/comment/${parentId}/replies?page=${page}&size=${replySize}`,options);
    const data = await response.json();
    hasMoreReplies = data.hasNext;
    isReplyFetching = false;
    return data.content;

}

//상세조회 모달 대댓글 등록시 대댓글 렌더링 or 게시글 상세조회시 대댓글 렌더링
function renderReply(replies,replyInner){

    const userId = sessionStorage.getItem('userId');

    replies.forEach((reply) => {
        let replyClone = document.getElementById('postReply-template').content.cloneNode(true);
        replyClone.querySelector('.reply').setAttribute('data-reply-id',reply.commentId);
        if(reply.authorProfileImage){
            replyClone.querySelector('.profile-img').src = reply.authorProfileImage;
        }
        replyClone.querySelector('.replyAuthor').innerText = reply.commentAuthor;
        replyClone.querySelector('.replyContent').insertAdjacentHTML('beforeend', reply.commentContent);
        if(reply.authorId != userId){
            replyClone.querySelector('.commentDeleteBtn').remove();
        }
        if(replies.length > 1){
            //대댓글 리스트 불러와서 렌더링 시켜주는 경우
            replyInner.appendChild(replyClone);
        }else{
            //대댓글 등록해서 렌더링 시켜주는 경우
            replyInner.prepend(replyClone);
        }


    });
}

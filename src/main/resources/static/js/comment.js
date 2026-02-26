



document.getElementById('postDetailModal').addEventListener('input', function (event){
    //게시판 상세조회 댓글 게시버튼 활성화
    if(event.target && event.target.classList.contains('comment-content')){
        let commentPostBtn = event.target.nextElementSibling;
        if (event.target.value.trim() !== "") {
            commentPostBtn.style.display = 'block';
        } else {
            commentPostBtn.style.display = 'none'; // 버튼 비활성화
        }
    }
});

document.getElementById('postDetailModal').addEventListener('click', async function (event){
    //게시판 상세조회 답글달기 클릭 이벤트
    if(event.target && event.target.classList.contains('comment-content')){
        let commentPostBtn = event.target.nextElementSibling;
        if (event.target.value.trim() !== "") {
            commentPostBtn.style.display = 'block';
        } else {
            commentPostBtn.style.display = 'none'; // 버튼 비활성화
        }
    }

    // 댓글 삭제 클릭
    if (event.target && event.target.classList.contains('commentRemove')){
        const targetComment = event.target.closest('.comment');
        let commentId = targetComment.getAttribute("data-comment-id");

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
                let result = await deleteComment(commentId);
                if(result.message=="ok"){
                    //댓글 요소 삭제
                    targetComment.remove();
                }


            } catch (error) {
                console.error(error);
                /*Swal.fire({
                    title: '오류 발생',
                    text: '댓글 삭제 중 문제가 발생했습니다.',
                    icon: 'error',
                    confirmButtonText: '확인'
                });*/
            }
        }
    }


    if (event.target && event.target.classList.contains('commentPostBtn')){
        //상세조회 댓글 등록
        const targetPost = event.target.closest('.postDetail-modal-content');
        let commentContent = targetPost.querySelector('.comment-content').value;
        let postId = targetPost.getAttribute('data-post-id');
        let data = {
            postId: postId,
            commentContent: commentContent
        }
        if (commentContent.startsWith('@')){
            let parentId = document.getElementById('detailCommentInput').getAttribute('data-parent-id');
            data = {
                postId: postId,
                commentContent: commentContent,
                parentId: parentId
            }
        }
        createComment(data).then(result => {
            if (result.parentId) {
                //대댓글 렌더링
                let commentInner = document.getElementById('comment-inner');
                let parentComment = commentInner.querySelector(`[data-comment-id="${result.parentId}"]`);
                let replyInner = parentComment.querySelector('.replyInner');

                if (!replyInner) {
                    let replyAreaTemplate = document.getElementById('postComment-template').content.querySelector('.reply-area').cloneNode(true);
                    let replySeeMore = replyAreaTemplate.querySelector('.replySeeMore');
                    replySeeMore.classList.remove('replySeeMore');
                    replySeeMore.classList.add('replySee');
                    replySeeMore.style.display = 'none';
                    replyAreaTemplate.querySelector('.replyCount').innerText = 1;
                    replyAreaTemplate.querySelector('.replyHide').style.display = 'block';
                    parentComment.querySelector('.post-comment-content').appendChild(replyAreaTemplate);
                    replyInner = parentComment.querySelector('.replyInner');
                }

                const reply = [result];
                renderReply(reply,replyInner);
                if(replyInner.style.display === 'none'){
                    let replyCnt = replyInner.closest('.reply-area').querySelector('.replyCount');
                    let cnt = parseInt(replyCnt.textContent, 10) || 0;
                    cnt++;
                    replyCnt.textContent = cnt;
                }

            } else {
                //댓글 렌더링
                renderComment(result);
            }
        });
        targetPost.querySelector('.comment-content').value = '';
    }

});

//게시글 상세페이지 댓글 엔터 등록
document.getElementById('postDetailModal').addEventListener('keydown', function(event) {
    if (event.key === 'Enter' && event.target.classList.contains('comment-content')) {
        if (event.target.value.trim() !== '') {
            event.target.nextElementSibling.click();
        }
    }
});


/*
function fetchDump(jsonData, optionsParam, renderFunc) {

    try {
        const options = {
            method: optionsParam.method ? optionsParam.method : 'post',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonData,
        };

        fetchWithAuth('/api/comment',options).then(rep =>
            renderFunc(rep)
        );
    } catch () {

    }



}*/

//댓글 등록 API
async function createComment(comment){

    let jsonData = JSON.stringify(comment);


        const options = {
            method: 'post',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonData,
        };

        const result = await fetchWithAuth('/api/comment',options);
        let responseData =  await result.json();

        return responseData;
}

//댓글 삭제 API
async function deleteComment(commentId){

    const options = {
        method: 'delete'
    };

    try {
        const response = await fetchWithAuth(`/api/comment/${commentId}`, options);

        if (response.status == 200) {
            const responseData = await response.json();
            return responseData;
        }
    }catch(error){
        if(error.status == 400){
            alert("삭제권한이 없습니다");
        }else{
            alert("잠시 후 다시 시도해주세요.");
        }
    }
}

//댓글 삭제 성공 시 해당 요소 삭제



//게시판 댓글 페이징 조회 API
async function getComments(postId,size,page){

    const options = {
        method: 'get'
    };

    const response = await fetchWithAuth(`/api/post/${postId}/comments?page=${page}&size=${size}`, options);
    return await response.json();
}
//상세조회 모달 댓글 등록시 댓글 렌더링
function renderComment(comment){
    let postCommentArea = document.getElementById('comment-inner');
    let siblingComment = postCommentArea.querySelector('.comment');
    let commentClone = document.getElementById('postComment-template').content.cloneNode(true);
    commentClone.querySelector('.comment').setAttribute('data-comment-id',comment.commentId);
    commentClone.querySelector('.reply-area').remove();
    if(comment.authorProfileImage){
        commentClone.querySelector('.profile-img').src = comment.authorProfileImage;
    }
    commentClone.querySelector('.commentAuthor').innerText = comment.commentAuthor;
    commentClone.querySelector('.commentContent').insertAdjacentHTML('beforeend', comment.commentContent);
    postCommentArea.insertBefore(commentClone,siblingComment);
}

//상세조회시 여러개의 댓글 렌더링
function renderCommentElements(commentDatas){

    if(commentDatas.content){
        let postCommentArea = document.getElementById('comment-inner');
        let userId = sessionStorage.getItem("userId");
        commentDatas.content.forEach((comment,index) => {
            let postComment = document.getElementById('postComment-template').content.cloneNode(true);
            postComment.querySelector('.comment').setAttribute('data-comment-id',comment.commentId);
            //작성자가 프사 있는 경우
            if(comment.authorProfileImage) {
                postComment.querySelector('.profile-img').src = comment.authorProfileImage;
            }
            postComment.querySelector('.commentAuthor').innerText = comment.commentAuthor;
            postComment.querySelector('.commentContent').insertAdjacentHTML('beforeend', comment.commentContent);

            //댓글 작성자인지 아닌지
            if(comment.memberId != userId){
                postComment.querySelector('.commentDeleteBtn').remove();
            }
            //대댓글 있는 경우, 없는 경우 
            if(comment.replyCount > 0){
                postComment.querySelector('.replyCount').innerText = comment.replyCount;
            }
            if(comment.replyCount == 0){
                postComment.querySelector('.reply-area').remove();
            }
            postCommentArea.appendChild(postComment);
        });
        if(commentDatas.hasNext==true){
            document.getElementById('commentGet').style.display = 'block';
        }
        if(commentDatas.hasNext==false){
            document.getElementById('commentGet').style.display = 'none';
        }
    }


}



//게시글 상세조회 좋아요 클릭이벤트
document.getElementById('postDetailModal').addEventListener('click', async(event) => {
    if (event.target && event.target.classList.contains('fa-heart')) {
        const targetPost = event.target.closest('.postDetail-modal-content');
        if(event.target.classList.contains('fa-regular')){
            let heartIcon = targetPost.querySelector('.fa-heart');
            let likeId = await likeIncreaseAPI(targetPost);
            if(likeId){
                likeIncreaseRender(targetPost,heartIcon,likeId);
            }
            return;
        }

        if(event.target.classList.contains('fa-solid')){
            let heartIcon = event.target;
            let likeId = await likeDecreaseAPI(heartIcon);
            if(likeId){
                likeDecreaseRender(targetPost,heartIcon);
            }
        }

    }
});

//좋아요한 게시글 조회
async function likesPosts(cursor,pageSize){
    const options = {
        method: 'GET'
    };

    if(cursor){
        const response = await fetchWithAuth(`/api/user/liked-posts?cursor=${cursor}&pageSize=${pageSize}`,options);
        let likePosts = await response.json();
        return likePosts;
    }else if(!cursor){
        const response = await fetchWithAuth(`/api/user/liked-posts?pageSize=${pageSize}`,options);
        let likePosts = await response.json();
        return likePosts;
    }



}

//좋아요 게시글 렌더링
function renderLikesPosts(likePosts){
    let likePostArea = document.getElementById('likePostInner');
    let thumbnailTemplate = document.getElementById('likePost-thumbnail-template');
    if(!likePosts.content || likePosts.content.length === 0){
        likePostArea.style.justifyContent = 'center';
        let noPost = document.getElementById('no-likePost-template').content.cloneNode(true);
        likePostArea.appendChild(noPost);
    }
    likePosts.content.forEach((likePost)=>{
        let postThumbnail = thumbnailTemplate.content.cloneNode(true);
        postThumbnail.querySelector('.memberPost').setAttribute('data-post-id',likePost.postId);
        postThumbnail.querySelector('.likePost').setAttribute('data-like-id',likePost.likeId);
        renderMediaElementByExtension(likePost.thumbnailPath,postThumbnail);
        postThumbnail.querySelector('.likeCnt').textContent = likePost.likeCount;
        postThumbnail.querySelector('.commentCnt').textContent = likePost.commentCount;
        likePostArea.appendChild(postThumbnail);
    });
}


//좋아요 Insert API
async function likeIncreaseAPI(targetPost) {
    let postId = targetPost.getAttribute('data-post-id');
    let data = {
        postId: postId
    }
    let jsonData = JSON.stringify(data);
    try {
        const options = {
            noSpinner: true,
            method: 'post',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonData,
            credentials: 'include'
        };
        const result = await fetchWithAuth('/api/like',options);
        let likeId = await result.json();
        return likeId;
    } catch (error) {
        console.error('오류 발생:', error);
        console.log(error.status);
        if(error.status!=401) {
            alert('잠시 후 다시 시도해주세요.');
        }
    }

}

//좋아요 Delete API
async function likeDecreaseAPI(targetLike){
    let likeId = targetLike.getAttribute('data-like-id');
    let data = {
        likeId: likeId
    }
    let jsonData = JSON.stringify(data);
    try {
        const options = {
            noSpinner: true,
            method: 'delete',
            headers: {
                'Content-Type': 'application/json'
            },
            body: jsonData,
            credentials: 'include'
        };
        const result = await fetchWithAuth('/api/like',options);
        let likeId = await result.json();
        return likeId;
    } catch (error) {
            console.error('오류 발생:', error);
            alert('오류가 발생했습니다.');
    }
}

//좋아요 insert UI 변경
function likeIncreaseRender(postDiv,targetHeart,likeId){
    targetHeart.setAttribute('data-like-id',likeId);
    targetHeart.classList.remove('fa-regular');
    targetHeart.classList.add('fa-solid');
    let heartCnt = postDiv.querySelector('.heartCount').textContent;
    postDiv.querySelector('.heartCount').textContent = parseInt(heartCnt)+1;

}

//좋아요 delete UI 변경

function likeDecreaseRender(postDiv,targetHeart){
    targetHeart.classList.remove('fa-solid');
    targetHeart.classList.add('fa-regular');
    targetHeart.removeAttribute('data-like-id');
    let heartCnt = postDiv.querySelector('.heartCount').textContent;
    postDiv.querySelector('.heartCount').textContent = parseInt(heartCnt)-1;

}
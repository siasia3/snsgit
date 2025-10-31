//게시글 등록 파일 미리보기
document.getElementById('fileInput').addEventListener('change', (event) => {
    let modal = document.getElementById('writeModal');
    const files = event.target.files;
    renderFilePreview(modal,files);
});

document.getElementById('modifyFileInput').addEventListener('change', (event) => {
    let modal = document.getElementById('modifyModal');
    const files = event.target.files;
    renderFilePreview(modal,files);
});

//파일 미리보기 렌더링
function renderFilePreview(modal,files){
    const carouselItems = modal.querySelector('.carouselItems');
    const prevButton = modal.querySelector('.prevButton');
    const nextButton = modal.querySelector('.nextButton');

    carouselItems.innerHTML = ''; // 기존 아이템 초기화

    if (files.length > 1) {
        // 파일 개수에 따라 버튼 활성화
        prevButton.style.display = 'block';
        nextButton.style.display = 'block';
    } else {
        // 파일이 없으면 버튼 숨김
        prevButton.style.display = 'none';
        nextButton.style.display = 'none';
    }
    Array.from(files).forEach((file, index) => {
        const fileType = file.type;
        const carouselItem = document.createElement('div');
        carouselItem.className = `carousel-item ${index === 0 ? 'active' : ''}`; // 첫 번째 파일은 active 클래스 추가

        if (fileType.startsWith('image/')) {
            // 이미지 파일 미리보기
            const img = document.createElement('img');
            const objectURL = URL.createObjectURL(file);

            img.src = objectURL
            img.className = 'd-block w-100';
            img.alt = file.name;
            carouselItem.appendChild(img);

            img.onload = () => {
                URL.revokeObjectURL(objectURL);
            };
        } else if (fileType.startsWith('video/')) {
            // 동영상 파일 미리보기
            const video = document.createElement('video');
            const objectURL = URL.createObjectURL(file);

            video.src = objectURL
            video.className = 'd-block w-100';
            video.controls = true;
            carouselItem.appendChild(video);

            video.canplaythrough = () => {
                URL.revokeObjectURL(objectURL);
            };
        }
        carouselItems.appendChild(carouselItem);
    });
}

//게시글 등록 클릭
document.getElementById('postBtn').addEventListener('click', async (event) => {
    const formData = new FormData();
    const postContent = document.getElementById('postContent').value;
    //const postContent = textareaContent.replace(/#[^\s#]+/g, '').trim();
    const files = document.getElementById('fileInput').files;
    const hashtags = postContent.match(/#[^\s#]+/g) || [];
    const hashtagObjects = hashtags.map(tag => ({ content: tag.slice(1) }));

    const data = {
        postContent: postContent,
        hashtags : hashtagObjects
    }

    formData.append('postContent',new Blob([JSON.stringify(data)],{type: "application/json"}));
    Array.from(files).forEach((file) => {
        formData.append('files', file);
    });

    try {
        const options = {
            method: 'POST',
            body: formData
        };

        const result = await fetchWithAuth('/api/post',options);
        console.log('게시글 등록 성공:', result);
        document.getElementById('writeModal').classList.remove('show');
        resetModifyModal();
        alert('게시물이 성공적으로 등록되었습니다.');


    } catch (error) {
        console.error('오류 발생:', error);
        alert('오류가 발생했습니다.');
    }
});

//게시글 수정 클릭
document.getElementById('updateBtn').addEventListener('click', async (event) => {
    const postId = document.getElementById('modifyPostId').getAttribute('data-post-id');
    const attachId = document.getElementById('attachmentId').getAttribute('data-attach-id');
    const formData = new FormData();
    const postContent = document.getElementById('modifyPostContent').value;
    const files = document.getElementById('modifyFileInput').files;
    const hashtags = postContent.match(/#[^\s#]+/g) || [];
    const hashtagObjects = hashtags.map(tag => ({ content: tag.slice(1) }));

    const data = {
        postId: postId,
        attachmentId: attachId,
        postContent: postContent,
        hashtags : hashtagObjects
    }

    formData.append('post',new Blob([JSON.stringify(data)],{type: "application/json"}));
    Array.from(files).forEach((file) => {
        formData.append('files', file);
    });

    let result = await updatePost(formData,postId);
    if(result.success) {
        document.getElementById('modifyModal').classList.remove('show');
        resetModifyModal();
        alert('게시물이 성공적으로 수정되었습니다.');
    }
});

//게시글 삭제 버튼 클릭



//게시글 수정 API
async function updatePost(formData,postId){

    try {
        const options = {
            method: 'PATCH',
            body: formData
        };

        const result = await fetchWithAuth(`/api/post/${postId}`,options);
        return await result.json();

    } catch (error) {
        console.error('오류 발생:', error);
        alert('잠시 후 다시 시도해주세요.');
    }

}

//게시글 삭제 API
async function deletePost(postId){
    try {
        const options = {
            method: 'DELETE'
        };

        const result = await fetchWithAuth(`/api/post/${postId}`,options);
        return await result.json();

    } catch (error) {
        console.error('오류 발생:', error);
        alert('잠시 후 다시 시도해주세요.');
    }
}



let page = 0;
let isPostFetching = false; // 중복 요청 방지
let hasMorePosts = true;
const postSize = 10;
//게시글 페이징 api
async function pagingPosts(){
    if (!hasMorePosts || isPostFetching) return;
    isPostFetching = true;

    const options = {
        method: 'GET'
    };
    const response = await fetchWithAuth(`/api/posts?page=${page}&size=${postSize}`,options);
    const data = await response.json();
    if(data.content.length > 0){
        renderPosts(data);
    }
    page += 1;
    hasMorePosts = data.hasNext;
    isPostFetching = false;
}

//게시글 세부사항 API
async function getPostDetail(postId){
    const options = {
        method: 'GET'
    };
    let response = await fetchWithAuth('/api/post/'+postId,options);
    return await response.json();
}

//수정할 게시글 데이터 조회 API

async function getPost(postId){
    const options = {
        method: 'GET'
    };
    let response = await fetchWithAuth('/api/user/post/'+postId,options);
    return await response.json();
}


//게시글 수정 페이지 렌더링
function renderBeforePostUpdate(post){
    let modal = document.getElementById('modifyModal');
    document.getElementById('modifyPostId').setAttribute('data-post-id',post.postId);
    document.getElementById('modifyPostContent').value = post.postContent;
    const carouselItems = modal.querySelector('.carouselItems');
    const prevButton = modal.querySelector('.prevButton');
    const nextButton = modal.querySelector('.nextButton');

    carouselItems.innerHTML = ''; // 기존 아이템 초기화

    if (post.attachments.length > 1) {
        // 파일 개수에 따라 버튼 활성화
        prevButton.style.display = 'block';
        nextButton.style.display = 'block';
    } else {
        // 파일이 없으면 버튼 숨김
        prevButton.style.display = 'none';
        nextButton.style.display = 'none';
    }
    post.attachments.forEach((file, index) => {
        if(index == 0){
            document.getElementById('attachmentId').setAttribute('data-attach-id',file.attachmentId);
        }
        const fileType = file.type;
        const carouselItem = document.createElement('div');
        carouselItem.className = `carousel-item ${index === 0 ? 'active' : ''}`; // 첫 번째 파일은 active 클래스 추가

        if (fileType.startsWith('image/')) {
            // 이미지 파일 미리보기
            const img = document.createElement('img');

            img.src = file.path;
            img.className = 'd-block w-100';
            img.style.objectFit = 'cover';
            carouselItem.appendChild(img);

        } else if (fileType.startsWith('video/')) {
            // 동영상 파일 미리보기
            const video = document.createElement('video');

            video.src = file.path;
            video.className = 'd-block w-100';
            video.style.objectFit = 'cover';
            video.controls = true;
            carouselItem.appendChild(video);
        }
        carouselItems.appendChild(carouselItem);
    });

}

//게시판 세부사항 생성
function renderPostDetail(post){
    let detailModal = document.getElementById('postDetailModal');
    if(post.attachments.length > 1){
        let multiFileDetail = document.getElementById('postDetail-media-carousel');
        let cloneMedia = multiFileDetail.content.cloneNode(true);
        let carouselItems = cloneMedia.querySelector('.carousel-inner');
        carouselItems.replaceChildren();
        post.attachments.forEach((attach,index) => {
            if(attach.type.includes('video')){
                const videoTag = multiFileDetail.content.querySelector('.video-detail-container').cloneNode(true);
                videoTag.querySelector('source').src = attach.path;
                videoTag.querySelector('source').type = attach.type;
                carouselItems.appendChild(videoTag);
            }
            if(attach.type.includes('image')){
                const ImageTag = multiFileDetail.content.querySelector('.img-detail-container').cloneNode(true);
                ImageTag.querySelector('img').src = attach.path;
                carouselItems.appendChild(ImageTag);
            }
            if(index == 0){
                carouselItems.children[0].classList.add('active');
            }
            if(index > 1){
                let indicatorClone = cloneMedia.querySelector('.carousel-indicators').children[1].cloneNode(true);
                indicatorClone.setAttribute('data-bs-slide-to',index);
                cloneMedia.querySelector('.carousel-indicators').appendChild(indicatorClone);
            }
        });
        detailModal.querySelector('.post-media').appendChild(cloneMedia);
    }
    if(post.attachments.length == 1){
        let singleFileDetail = document.getElementById('postDetail-media');
        let cloneMedia = singleFileDetail.content.cloneNode(true);
        let mediaItem = cloneMedia.querySelector('.media-inner');
        mediaItem.replaceChildren();
        post.attachments.forEach((attach,index) => {
            if(attach.type.includes('video')){
                const videoTag = singleFileDetail.content.querySelector('.video-detail-container').cloneNode(true);
                videoTag.querySelector('source').src = attach.path;
                videoTag.querySelector('source').type = attach.type;
                mediaItem.appendChild(videoTag);
            }
            if(attach.type.includes('image')){
                const ImageTag = singleFileDetail.content.querySelector('.img-detail-container').cloneNode(true);
                ImageTag.querySelector('img').src = attach.path;
                mediaItem.appendChild(ImageTag);
            }
        });
        detailModal.querySelector('.post-media').appendChild(cloneMedia);
    }
    createPostDetailInfo(post);

}


//동적 페이징게시판 생성
function renderPosts(data){
    const mainDiv = document.getElementById('mainDiv-inner');
    const multiFileTemplate = document.getElementById('multi-file-template');
    const singleFileTemplate = document.getElementById('single-file-template');
    const userId = sessionStorage.getItem('userId');
    data.content.forEach(post => {
        if(post.attachments.length > 1) {
            const cloneMultiPost = multiFileTemplate.content.cloneNode(true);
            let targetPostId = 'carouselIndicators'+post.postId;

            if(userId == post.memberId) {
                cloneMultiPost.querySelector('.moreBtn').style.display = 'flex';
            }

            //캐러셀별로 고유값인 게시판 키값을 이용해서 작동하게 id값 세팅
            cloneMultiPost.querySelector('.slide').setAttribute('id',targetPostId);
            //캐러셀 item div
            const carouselItem = cloneMultiPost.querySelector('.postCarouselItem');
            carouselItem.replaceChildren();
            //indicators div
            const indicatorDiv = cloneMultiPost.querySelector('.carousel-indicators');
            //indicators 버튼 클론
            const indicatorClone = indicatorDiv.children[1].cloneNode(true);

            //CarouselItem들을 첨부파일 개수만큼 비디오,이미지 생성
            post.attachments.forEach((attach,index) => {

                if(attach.type.includes('video')){
                    const videoTag = multiFileTemplate.content.querySelector('.video-container').cloneNode(true);
                    videoTag.querySelector('source').src = attach.path;
                    videoTag.querySelector('source').type = attach.type;
                    carouselItem.appendChild(videoTag);
                }
                if(attach.type.includes('image')){
                    const ImageTag = multiFileTemplate.content.querySelector('.img-container').cloneNode(true);
                    ImageTag.querySelector('img').src = attach.path;
                    carouselItem.appendChild(ImageTag);
                }
                if(index == 0){
                    carouselItem.children[0].classList.add('active');
                }

                if(index > 1){
                    indicatorClone.setAttribute('data-bs-slide-to',index);
                    indicatorDiv.appendChild(indicatorClone);
                }
            });

            //캐러셀 target 지정
            cloneMultiPost.querySelectorAll("[data-bs-target]").forEach((element) => {
                element.setAttribute("data-bs-target", "#"+targetPostId);
            });

            createPostInfo(cloneMultiPost,post);
            mainDiv.appendChild(cloneMultiPost);

        }else{
            //단일파일 게시물
            const cloneSinglePost = singleFileTemplate.content.cloneNode(true);
            const mediaItem = cloneSinglePost.querySelector('.mediaDiv');
            mediaItem.replaceChildren();  // 자식 요소가 있으면 제거

            if(userId == post.memberId) {
                cloneSinglePost.querySelector('.moreBtn').style.display = 'flex';
            }

            post.attachments.forEach(attach => {
                if(attach.type.includes('video')){
                    const videoTag = singleFileTemplate.content.querySelector('.video-container').cloneNode(true);
                    videoTag.querySelector('source').src = attach.path;
                    videoTag.querySelector('source').type = attach.type;
                    mediaItem.appendChild(videoTag);
                }
                if(attach.type.includes('image')){
                    const ImageTag = singleFileTemplate.content.querySelector('.img-container').cloneNode(true);
                    ImageTag.querySelector('img').src = attach.path;

                    mediaItem.appendChild(ImageTag);
                }
            });
            createPostInfo(cloneSinglePost,post);
            mainDiv.appendChild(cloneSinglePost);
        }

    });
};

//게시판 세부사항 정보 세팅
function createPostDetailInfo(post){
    let postDetail = document.querySelector('.postDetail-modal-content');
    postDetail.setAttribute('data-post-id',post.postId);
    const authorElements = postDetail.querySelectorAll('.authorName');
    authorElements.forEach(element => {
        element.innerText = post.author;
    });
    if(post.profileImage){
        /*이미지 파일이 있는 경우*/
        const profileElements = postDetail.querySelectorAll('.profile-img');
        profileElements.forEach(element => {
            element.src = post.profileImage;
        });
    }
    //좋아요 유무
    if(post.likeId != null){
        let heartIcon = postDetail.querySelector('.fa-heart');
        heartIcon.setAttribute('data-like-id',post.likeId);
        heartIcon.classList.remove('fa-regular');
        heartIcon.classList.add('fa-solid');
    }
    //좋아요 개수
    if(post.likeCount != null && post.likeCount > 0){
        postDetail.querySelector('.heartCount').innerText= post.likeCount;
    }
    //게시글 내용
    if(post.content){
        let postText = document.getElementById('post-detail-content');
        postText.insertAdjacentHTML('beforeend', post.content.replace(/\n/g, '<br>'));
    }
    if(!post.content){
        postDetail.getElementById('modalPostContent').replaceChildren();
    }


}

//동적으로 게시판 정보 세팅
function createPostInfo(template,post){
    //게시판별 키값 세팅
    template.querySelector('.post').setAttribute('data-post-id',post.postId);
    //저자표시 span
    let postInfoSpan = template.querySelector('.authorInfo');
    postInfoSpan.innerText = post.author;
    if(post.profileImage){
        /*이미지 파일이 있는 경우*/
        template.querySelector('.profile-img').src = post.profileImage;
    }
    //좋아요 유무
    if(post.likeId != null){
        let heartIcon = template.querySelector('.fa-heart');
        heartIcon.setAttribute('data-like-id',post.likeId);
        heartIcon.classList.remove('fa-regular');
        heartIcon.classList.add('fa-solid');
    }
    //좋아요 개수
    if(post.likeCount != null && post.likeCount > 0){
        template.querySelector('.heartCount').innerText= post.likeCount;
    }
    //게시글 내용

    let postText = template.querySelector('.post-text');
    let moreBtn = template.querySelector('.show-more-btn');
    //더보기 체크
    const hasLineBreak = post.content.includes("\n");

    if(post.content){
        postText.innerText = post.content;
        if (post.content.length >= 14 || hasLineBreak){
            moreBtn.style.display = 'block';
        }
    }
    if(!post.content){
        template.querySelector('.post-content').replaceChildren();
    }
    //댓글 개수
    let commentCount = template.querySelector('.commentCntText');
    if(post.commentCount > 0){
        commentCount.innerText = post.commentCount;
    }
    if(commentCount && post.commentCount == 0){
        commentCount.closest('.postDiv').remove();
    }
}

//게시글 등록시 글 등록 modal 비워주는 함수
function resetWriteModal(){
    let modal = document.getElementById('writeModal');
    document.getElementById('postContent').value = "";
    document.getElementById('fileInput').value = "";
    modal.querySelector('.carouselItems').replaceChildren();
    modal.querySelector('.prevButton').style.display = 'none';
    modal.querySelector('.nextButton').style.display = 'none';
}

//게시글 수정시 글 수정 modal 비워주는 함수
function resetModifyModal(){
    let modal = document.getElementById('modifyModal');
    document.getElementById('modifyPostId').removeAttribute('data-post-id');
    document.getElementById('modifyPostContent').value = "";
    document.getElementById('modifyFileInput').value = "";
    modal.querySelector('.carouselItems').replaceChildren();
    modal.querySelector('.prevButton').style.display = 'none';
    modal.querySelector('.nextButton').style.display = 'none';
}




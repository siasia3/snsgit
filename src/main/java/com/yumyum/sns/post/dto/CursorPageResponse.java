package com.yumyum.sns.post.dto;

import java.time.LocalDateTime;
import java.util.List;

public record CursorPageResponse<T>(
        List<T> content,
        LocalDateTime nextCursor
){}

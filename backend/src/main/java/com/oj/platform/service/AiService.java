package com.oj.platform.service;

import com.oj.platform.dto.AiChatRequest;
import com.oj.platform.dto.AiChatResponse;
import com.oj.platform.dto.UserAiContextDto;

public interface AiService {
    AiChatResponse chat(AiChatRequest request, Long userId, String username);
    UserAiContextDto getUserAiContext(Long userId);
}

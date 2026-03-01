package com.yumyum.sns.config.webSocket;


import org.springframework.messaging.Message;
import org.springframework.messaging.MessageChannel;
import org.springframework.messaging.simp.stomp.StompCommand;
import org.springframework.messaging.simp.stomp.StompHeaderAccessor;
import org.springframework.messaging.support.ChannelInterceptor;
import org.springframework.stereotype.Component;

import java.security.Principal;
import java.util.Map;

@Component
public class JwtChannelInterceptor implements ChannelInterceptor {

    @Override
    public Message<?> preSend(Message<?> message, MessageChannel channel) {
        StompHeaderAccessor accessor = StompHeaderAccessor.wrap(message);

        if (StompCommand.CONNECT.equals(accessor.getCommand())) {
            Map<String, Object> sessionAttributes = accessor.getSessionAttributes();

            if (sessionAttributes != null) {
                Object identifier = sessionAttributes.get("identifier");
                if (identifier != null) {
                    // Principal 구현 후 setUser
                    accessor.setUser(new Principal() {
                        @Override
                        public String getName() {
                            return identifier.toString();
                        }
                    });
                }
            }
        }

        return message;
    }
}

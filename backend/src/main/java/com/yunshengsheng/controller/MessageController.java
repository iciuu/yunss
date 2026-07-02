package com.yunshengsheng.controller;

import com.yunshengsheng.common.ApiResponse;
import com.yunshengsheng.domain.Models.Message;
import com.yunshengsheng.security.TenantContext;
import com.yunshengsheng.service.AppStore;
import org.springframework.web.bind.annotation.*;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/message")
public class MessageController {
    private final AppStore store;

    public MessageController(AppStore store) {
        this.store = store;
    }

    @GetMapping("/list")
    public ApiResponse<List<Message>> list() {
        Long receiver = TenantContext.carrierId() == null ? TenantContext.userId() : TenantContext.carrierId();
        return ApiResponse.success(store.messages.values().stream()
                .filter(message -> message.receiverId().equals(receiver))
                .sorted(Comparator.comparing(Message::createdAt, Comparator.reverseOrder()))
                .toList());
    }

    @PostMapping("/{id}/read")
    public ApiResponse<Void> read(@PathVariable Long id) {
        Message message = store.messages.get(id);
        if (message != null) {
            store.messages.put(id, new Message(message.id(), message.receiverId(), message.type(), message.title(),
                    message.content(), message.relatedId(), true, message.createdAt()));
        }
        return ApiResponse.success(null);
    }

    @GetMapping("/unread/count")
    public ApiResponse<Map<String, Long>> unread() {
        Long receiver = TenantContext.carrierId() == null ? TenantContext.userId() : TenantContext.carrierId();
        long count = store.messages.values().stream().filter(message -> message.receiverId().equals(receiver)).filter(message -> !message.read()).count();
        return ApiResponse.success(Map.of("count", count));
    }
}

package com.lekang.journal.message.application;

import com.lekang.journal.message.api.GuestMessageCreateRequest;
import com.lekang.journal.message.api.GuestMessageCreatedResponse;
import com.lekang.journal.message.infrastructure.GuestMessageEntity;
import com.lekang.journal.message.infrastructure.GuestMessageRepository;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class GuestMessageService {
    private final GuestMessageRepository repository;
    private final MessageRateLimiter rateLimiter;

    public GuestMessageService(GuestMessageRepository repository, MessageRateLimiter rateLimiter) {
        this.repository = repository;
        this.rateLimiter = rateLimiter;
    }

    @Transactional
    public GuestMessageCreatedResponse create(GuestMessageCreateRequest request, String clientKey) {
        rateLimiter.consume(clientKey);
        String contact = request.contact() == null || request.contact().isBlank()
            ? null
            : request.contact().trim();
        repository.save(new GuestMessageEntity(
            request.name().trim(),
            contact,
            request.message().trim(),
            OffsetDateTime.now(ZoneOffset.UTC)
        ));
        return new GuestMessageCreatedResponse("留言已经放进收件箱。谢谢你写下来。");
    }
}

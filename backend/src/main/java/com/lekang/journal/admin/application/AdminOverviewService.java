package com.lekang.journal.admin.application;

import com.lekang.journal.admin.api.AdminOverviewResponse;
import com.lekang.journal.article.infrastructure.ArticleRepository;
import java.time.LocalDate;
import java.time.ZoneId;
import java.util.List;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class AdminOverviewService {
    private static final ZoneId SITE_ZONE = ZoneId.of("Asia/Shanghai");
    private final ArticleRepository articleRepository;
    private final List<String> dailyNotes;

    public AdminOverviewService(
        ArticleRepository articleRepository,
        @Value("${journal.admin.daily-notes}") List<String> dailyNotes
    ) {
        this.articleRepository = articleRepository;
        this.dailyNotes = dailyNotes == null || dailyNotes.stream().allMatch(String::isBlank)
            ? List.of("先写下来，好文章不必一次完成。")
            : dailyNotes.stream().map(String::trim).filter(note -> !note.isBlank()).toList();
    }

    @Transactional(readOnly = true)
    public AdminOverviewResponse overview() {
        long day = LocalDate.now(SITE_ZONE).toEpochDay();
        String note = dailyNotes.get(Math.floorMod(day, dailyNotes.size()));
        return new AdminOverviewResponse(
            articleRepository.countByStatus("DRAFT"),
            articleRepository.countByStatus("PUBLISHED"),
            articleRepository.countByStatus("ARCHIVED"),
            note
        );
    }
}

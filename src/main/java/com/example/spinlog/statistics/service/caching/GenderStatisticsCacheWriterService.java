package com.example.spinlog.statistics.service.caching;

import com.example.spinlog.article.entity.Article;
import com.example.spinlog.user.entity.Gender;
import com.example.spinlog.user.entity.User;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
@Slf4j
public class GenderStatisticsCacheWriterService {
    private final RedisService redisService;

    private final int PERIOD_CRITERIA = 30;

    public void updateStatisticsCacheFromNewData(Article article, User user) {
        if(isNotInStatisticsPeriodCriteria(article.getSpendDate()))
            return;

        // todo MBTI statistics 추가하면 수정 필요
        if(user.getGender() == Gender.NONE)
            return;
        log.info("Update cached data, new article id: {}", article.getArticleId());

        // todo hashkey 구하는 로직 별도 util 클래스로 분리
        redisService.incrementDataInHash("GenderEmotionStatisticsSum::" + article.getRegisterType(),
                user.getGender() + "::" + article.getEmotion(), article.getAmount());
        redisService.incrementDataInHash("GenderEmotionStatisticsCount::" + article.getRegisterType(),
                user.getGender() + "::" + article.getEmotion(), 1L);

        redisService.incrementDataInHash("GenderDailyAmountStatisticsSum::" + article.getRegisterType(),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), article.getAmount());
        redisService.incrementDataInHash("GenderDailyAmountStatisticsCount::" + article.getRegisterType(),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), 1L);

        redisService.incrementDataInHash("GenderSatisfactionStatisticsSum::" + article.getRegisterType(),
                user.getGender().name(), article.getSatisfaction());
        redisService.incrementDataInHash("GenderSatisfactionStatisticsCount::" + article.getRegisterType(),
                user.getGender().name(), 1L);

    }

    private boolean isNotInStatisticsPeriodCriteria(LocalDateTime spendDate) {
        LocalDateTime endDate = LocalDateTime.now();
        LocalDateTime startDate = endDate.minusDays(PERIOD_CRITERIA);
        return !(spendDate.isAfter(startDate) && spendDate.isBefore(endDate));
    }

    public void updateStatisticsCacheFromModifiedData(Article originalArticle, Article updateArticle, User user) {
        if(user.getGender() == Gender.NONE)
            return;
        if(isNotChanged(originalArticle, updateArticle))
            return;
        log.info("Update cached data, modified originalArticle id: {}", updateArticle.getArticleId());

        updateStatisticsCacheFromRemovedData(originalArticle, user);
        updateStatisticsCacheFromNewData(updateArticle, user);

    }

    // todo word frequency cache 추가 시 수정 필요
    private boolean isNotChanged(Article originalArticle, Article updateArticle) {
        return originalArticle.getAmount().equals(updateArticle.getAmount()) &&
                originalArticle.getEmotion().equals(updateArticle.getEmotion()) &&
                originalArticle.getSpendDate().equals(updateArticle.getSpendDate()) &&
                originalArticle.getSatisfaction().equals(updateArticle.getSatisfaction());
    }

    public void updateStatisticsCacheFromRemovedData(Article article, User user) {
        if(isNotInStatisticsPeriodCriteria(article.getSpendDate()))
            return;
        if(user.getGender() == Gender.NONE)
            return;
        log.info("Remove cached data, removed article id: {}", article.getArticleId());

        redisService.decrementDataInHash("GenderEmotionStatisticsSum::" + article.getRegisterType(),
                user.getGender() + "::" + article.getEmotion(), article.getAmount());
        redisService.decrementDataInHash("GenderEmotionStatisticsCount::" + article.getRegisterType(),
                user.getGender() + "::" + article.getEmotion(), 1L);

        redisService.decrementDataInHash("GenderDailyAmountStatisticsSum::" + article.getRegisterType(),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), article.getAmount());
        redisService.decrementDataInHash("GenderDailyAmountStatisticsCount::" + article.getRegisterType(),
                user.getGender() + "::" + article.getSpendDate().toLocalDate(), 1L);

        redisService.decrementDataInHash("GenderSatisfactionStatisticsSum::" + article.getRegisterType(),
                user.getGender().name(), article.getSatisfaction());
        redisService.decrementDataInHash("GenderSatisfactionStatisticsCount::" + article.getRegisterType(),
                user.getGender().name(), 1L);

    }

}

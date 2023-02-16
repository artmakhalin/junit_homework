package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;

import static com.dmdev.entity.Provider.GOOGLE;
import static com.dmdev.entity.Status.CANCELED;
import static com.dmdev.entity.Status.EXPIRED;
import static com.dmdev.entity.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

public class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionDao subscriptionDao;
    private SubscriptionService subscriptionService;

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                Clock.systemDefaultZone()
        );
    }

    @Test
    void upsertSuccess() {
        var createSubscriptionDto = getCreateSubscriptionDto();

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult.getId()).isNotNull();
    }

    @Test
    void cancelSuccess() {
        var subscription = subscriptionDao.insert(getSubscription());

        subscriptionService.cancel(subscription.getId());
        var canceledSubscription = subscriptionDao.findById(subscription.getId());

        assertThat(canceledSubscription.get()
                                       .getStatus()).isSameAs(CANCELED);
    }

    @Test
    void expireSuccess() {
        var subscription = subscriptionDao.insert(getSubscription());

        subscriptionService.expire(subscription.getId());
        var expiredSubscription = subscriptionDao.findById(subscription.getId());

        assertThat(expiredSubscription.get()
                                      .getStatus()).isSameAs(EXPIRED);
    }

    private CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                                    .userId(1)
                                    .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                    .name("Alex")
                                    .provider("GOOGLE")
                                    .build();
    }

    private Subscription getSubscription() {
        return Subscription.builder()
                           .userId(1)
                           .name("Alex")
                           .provider(GOOGLE)
                           .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                           .status(ACTIVE)
                           .build();
    }
}

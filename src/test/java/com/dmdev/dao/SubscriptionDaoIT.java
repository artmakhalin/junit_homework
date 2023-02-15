package com.dmdev.dao;

import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Provider.GOOGLE;
import static com.dmdev.entity.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAllSuccess() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));
        var sub3 = subscriptionDao.insert(getSubscription(3, "Connor"));

        var actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        var subIds = actualResult.stream()
                                 .map(Subscription::getId)
                                 .toList();
        assertThat(subIds).contains(sub1.getId(), sub2.getId(), sub3.getId());
    }

    @Test
    void findAllFromEmptyDB() {
        var actualResult = subscriptionDao.findAll();

        assertThat(actualResult).isEmpty();
    }

    @Test
    void findByIdSuccess() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));

        var actualResult = subscriptionDao.findById(sub1.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get()).isEqualTo(sub1);
    }

    @Test
    void findByIdNotFound() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));

        var actualResult = subscriptionDao.findById(999);

        assertThat(actualResult).isEmpty();
    }

    @Test
    void deleteExistingEntity() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));

        var actualResult = subscriptionDao.delete(sub1.getId());

        assertThat(actualResult).isTrue();
    }

    @Test
    void deleteNotExistingEntity() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));

        var actualResult = subscriptionDao.delete(999);

        assertThat(actualResult).isFalse();
    }

    @Test
    void updateSuccess() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        sub1.setName("Alex-updated");
        sub1.setProvider(APPLE);

        var actualResult = subscriptionDao.update(sub1);

        assertThat(actualResult).isEqualTo(sub1);
    }

    @Test
    void shouldThrowExceptionWhileUpdateIfUniqueConstraintViolated() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));

        sub1.setName("Paul");
        sub1.setProvider(APPLE);
        sub1.setUserId(2);

        assertThatExceptionOfType(JdbcSQLIntegrityConstraintViolationException.class)
                .isThrownBy(() -> subscriptionDao.update(sub1));
    }

    @Test
    void insertSuccess() {
        var sub1 = getSubscription(1, "Alex");

        var actualResult = subscriptionDao.insert(sub1);

        assertThat(actualResult.getId()).isNotNull();
    }

    @Test
    void shouldThrowExceptionWhileInsertIfUniqueConstraintViolated() {
        subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = getSubscription(1, "Alex");

        assertThatExceptionOfType(JdbcSQLIntegrityConstraintViolationException.class)
                .isThrownBy(() -> subscriptionDao.insert(sub2));
    }

    @Test
    void findByUserIdSuccess() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));
        var sub3 = subscriptionDao.insert(getSubscription(1, "Jane"));

        var actualResult = subscriptionDao.findByUserId(sub1.getUserId());

        assertThat(actualResult).hasSize(2);
        var userIds = actualResult.stream()
                                  .map(Subscription::getUserId)
                                  .toList();
        assertThat(userIds).contains(sub1.getUserId(), sub3.getUserId());
    }

    @Test
    void findByUserIdNotFound() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        var sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));
        var sub3 = subscriptionDao.insert(getSubscription(3, "Jane"));

        var actualResult = subscriptionDao.findByUserId(999);

        assertThat(actualResult).isEmpty();
    }

    @Test
    void upsertSuccessNewSubscription() {
        var sub1 = getSubscription(1, "Alex");

        var actualResult = subscriptionDao.upsert(sub1);

        assertThat(actualResult.getId()).isNotNull();
    }

    @Test
    void upsertSuccessExistingSubscription() {
        var sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        sub1.setName("Alex-updated");

        var actualResult = subscriptionDao.upsert(sub1);

        assertThat(actualResult).isEqualTo(sub1);
    }

    private Subscription getSubscription(Integer userId, String name) {
        return Subscription.builder()
                           .userId(userId)
                           .name(name)
                           .provider(GOOGLE)
                           .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                           .status(ACTIVE)
                           .build();
    }
}
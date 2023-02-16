package com.dmdev.dao;

import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.h2.jdbc.JdbcSQLIntegrityConstraintViolationException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.dmdev.entity.Provider.APPLE;
import static com.dmdev.entity.Provider.GOOGLE;
import static com.dmdev.entity.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;

class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Nested
    class FindAll {

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
        void findAllShouldReturnEmptyListIfDBIsEmpty() {
            var actualResult = subscriptionDao.findAll();

            assertThat(actualResult).isEmpty();
        }
    }

    @Nested
    class FindById {

        Subscription sub1;
        Subscription sub2;

        @BeforeEach
        void prepare() {
            sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
            sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));
        }

        @Test
        void findByIdSuccess() {
            var actualResult = subscriptionDao.findById(sub1.getId());

            assertThat(actualResult).isPresent();
            assertThat(actualResult.get()).isEqualTo(sub1);
        }

        @Test
        void findByIdShouldReturnEmptyListIfNoSuitableEntities() {
            var actualResult = subscriptionDao.findById(999);

            assertThat(actualResult).isEmpty();
        }
    }

    @Nested
    class Delete {

        Subscription sub1;

        @BeforeEach
        void prepare() {
            sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        }

        @Test
        void deleteShouldReturnTrueIfExistingEntity() {
            var actualResult = subscriptionDao.delete(sub1.getId());

            assertThat(actualResult).isTrue();
        }

        @Test
        void deleteShouldReturnFalseIfNotExistingEntity() {
            var actualResult = subscriptionDao.delete(999);

            assertThat(actualResult).isFalse();
        }
    }

    @Nested
    class Update {

        Subscription sub1;

        @BeforeEach
        void prepare() {
            sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
        }

        @Test
        void updateSuccess() {
            sub1.setName("Alex-updated");
            sub1.setProvider(APPLE);

            var actualResult = subscriptionDao.update(sub1);

            assertThat(actualResult).isEqualTo(sub1);
        }

        @Test
        void updateShouldThrowExceptionIfUniqueConstraintViolated() {
            subscriptionDao.insert(getSubscription(2, "Paul"));

            sub1.setName("Paul");
            sub1.setProvider(APPLE);
            sub1.setUserId(2);

            assertThatExceptionOfType(JdbcSQLIntegrityConstraintViolationException.class)
                    .isThrownBy(() -> subscriptionDao.update(sub1));
        }
    }

    @Nested
    class Insert {

        Subscription sub1 = getSubscription(1, "Alex");

        @Test
        void insertSuccess() {
            var actualResult = subscriptionDao.insert(sub1);

            assertThat(actualResult.getId()).isNotNull();
        }

        @Test
        void insertShouldThrowExceptionIfUniqueConstraintViolated() {
            subscriptionDao.insert(sub1);
            var sub2 = getSubscription(1, "Alex");

            assertThatExceptionOfType(JdbcSQLIntegrityConstraintViolationException.class)
                    .isThrownBy(() -> subscriptionDao.insert(sub2));
        }
    }

    @Nested
    class FindByUserId {

        Subscription sub1;
        Subscription sub2;
        Subscription sub3;

        @BeforeEach
        void prepare() {
            sub1 = subscriptionDao.insert(getSubscription(1, "Alex"));
            sub2 = subscriptionDao.insert(getSubscription(2, "Paul"));
            sub3 = subscriptionDao.insert(getSubscription(1, "Jane"));
        }

        @Test
        void findByUserIdSuccess() {
            var actualResult = subscriptionDao.findByUserId(sub1.getUserId());

            assertThat(actualResult).hasSize(2);
            var userIds = actualResult.stream()
                                      .map(Subscription::getUserId)
                                      .toList();
            assertThat(userIds).contains(sub1.getUserId(), sub3.getUserId());
        }

        @Test
        void findByUserShouldReturnEmptyListIfNoSuitableEntities() {
            var actualResult = subscriptionDao.findByUserId(999);

            assertThat(actualResult).isEmpty();
        }
    }

    @Nested
    class Upsert {

        Subscription sub1 = getSubscription(1, "Alex");

        @Test
        void upsertSuccessIfNewSubscription() {
            var actualResult = subscriptionDao.upsert(sub1);

            assertThat(actualResult.getId()).isNotNull();
        }

        @Test
        void upsertSuccessIfExistingSubscription() {
            var insertedSub = subscriptionDao.insert(sub1);
            insertedSub.setName("Alex-updated");

            var actualResult = subscriptionDao.upsert(insertedSub);

            assertThat(actualResult).isEqualTo(insertedSub);
        }
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
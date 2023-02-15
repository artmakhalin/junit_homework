package com.dmdev.service;

import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

import static com.dmdev.entity.Provider.GOOGLE;
import static com.dmdev.entity.Status.*;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatExceptionOfType;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    @Mock
    private Clock clock;
    @InjectMocks
    private SubscriptionService subscriptionService;

    @Test
    void upsertSuccessNewSubscription() {
        var createSubscriptionDto = getCreateSubscriptionDto();
        var subscription = getSubscription(null, ACTIVE);
        doReturn(new ValidationResult()).when(createSubscriptionValidator)
                                        .validate(createSubscriptionDto);
        doReturn(new ArrayList<Subscription>()).when(subscriptionDao)
                                               .findByUserId(createSubscriptionDto.getUserId());
        doReturn(subscription).when(createSubscriptionMapper)
                              .map(createSubscriptionDto);
        doReturn(subscription).when(subscriptionDao)
                              .upsert(subscription);

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult).isEqualTo(subscription);
        verify(subscriptionDao).upsert(subscription);
        verify(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        verify(createSubscriptionValidator).validate(createSubscriptionDto);
        verify(createSubscriptionMapper).map(createSubscriptionDto);
    }

    @Test
    void upsertSuccessUpdateExistingSubscription() {
        var createSubscriptionDto = getCreateSubscriptionDto();
        var subscription = getSubscription(null, ACTIVE);
        var existingSubscription = getSubscription(null, CANCELED);
        doReturn(new ValidationResult()).when(createSubscriptionValidator)
                                        .validate(createSubscriptionDto);
        doReturn(List.of(existingSubscription)).when(subscriptionDao)
                                               .findByUserId(createSubscriptionDto.getUserId());
        doReturn(subscription).when(subscriptionDao)
                              .upsert(subscription);

        var actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult).isEqualTo(subscription);
        verify(subscriptionDao).upsert(subscription);
        verify(subscriptionDao).findByUserId(createSubscriptionDto.getUserId());
        verify(createSubscriptionValidator).validate(createSubscriptionDto);
    }

    @Test
    void shouldThrowExceptionIfDtoInvalid() {
        var createSubscriptionDto = getCreateSubscriptionDto();
        var validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "userId is invalid"));
        doReturn(validationResult).when(createSubscriptionValidator)
                                  .validate(createSubscriptionDto);

        assertThatExceptionOfType(ValidationException.class)
                .isThrownBy(() -> subscriptionService.upsert(createSubscriptionDto));
        verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
    }

    @Test
    void cancelSuccess() {
        var subscription = getSubscription(1, ACTIVE);
        doReturn(Optional.of(subscription)).when(subscriptionDao)
                                           .findById(subscription.getId());

        subscriptionService.cancel(subscription.getId());

        assertThat(subscription.getStatus()).isSameAs(CANCELED);
        verify(subscriptionDao).findById(subscription.getId());
        verify(subscriptionDao).update(subscription);
    }

    @Test
    void shouldThrowExceptionIfSubscriptionIsNotActive() {
        var cancelledSubscription = getSubscription(1, CANCELED);
        doReturn(Optional.of(cancelledSubscription)).when(subscriptionDao)
                                                    .findById(cancelledSubscription.getId());

        assertThatExceptionOfType(SubscriptionException.class)
                .isThrownBy(() -> subscriptionService.cancel(cancelledSubscription.getId()));
        verify(subscriptionDao).findById(cancelledSubscription.getId());
    }

    @Test
    void expireSuccess() {
        var subscription = getSubscription(1, ACTIVE);
        doReturn(Optional.of(subscription)).when(subscriptionDao)
                                           .findById(subscription.getId());

        subscriptionService.expire(subscription.getId());

        assertThat(subscription.getStatus()).isSameAs(EXPIRED);
        verify(subscriptionDao).findById(subscription.getId());
        verify(subscriptionDao).update(subscription);
    }

    @Test
    void shouldThrowExceptionIfSubscriptionIsExpired() {
        var expiredSubscription = getSubscription(1, EXPIRED);
        doReturn(Optional.of(expiredSubscription)).when(subscriptionDao)
                                                  .findById(expiredSubscription.getId());

        assertThatExceptionOfType(SubscriptionException.class)
                .isThrownBy(() -> subscriptionService.expire(expiredSubscription.getId()));
        verify(subscriptionDao).findById(expiredSubscription.getId());
    }

    private CreateSubscriptionDto getCreateSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                                    .userId(1)
                                    .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                    .name("Alex")
                                    .provider("GOOGLE")
                                    .build();
    }

    private Subscription getSubscription(Integer id, Status status) {
        return Subscription.builder()
                           .id(id)
                           .userId(1)
                           .name("Alex")
                           .provider(GOOGLE)
                           .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                           .status(status)
                           .build();
    }
}
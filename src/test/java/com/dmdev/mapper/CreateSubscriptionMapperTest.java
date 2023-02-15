package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static com.dmdev.entity.Provider.GOOGLE;
import static com.dmdev.entity.Status.ACTIVE;
import static org.assertj.core.api.Assertions.assertThat;

class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        var dto = CreateSubscriptionDto.builder()
                                       .userId(1)
                                       .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                       .name("Alex")
                                       .provider("GOOGLE")
                                       .build();

        var actualResult = mapper.map(dto);

        var expectedResult = Subscription.builder()
                                         .userId(1)
                                         .name("Alex")
                                         .provider(GOOGLE)
                                         .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                         .status(ACTIVE)
                                         .build();

        assertThat(actualResult).isEqualTo(expectedResult);
    }
}
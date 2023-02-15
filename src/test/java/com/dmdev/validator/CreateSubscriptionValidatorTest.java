package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void validateSuccess() {
        var dto = CreateSubscriptionDto.builder()
                                       .userId(1)
                                       .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                       .name("some_name")
                                       .provider("GOOGLE")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isFalse();
    }

    @Test
    void invalidUserId() {
        var dto = CreateSubscriptionDto.builder()
                                       .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                       .name("Alex")
                                       .provider("GOOGLE")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getCode()).isEqualTo(100);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getMessage()).isEqualTo("userId is invalid");
    }

    @Test
    void invalidName() {
        var dto = CreateSubscriptionDto.builder()
                                       .userId(1)
                                       .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                       .name("")
                                       .provider("GOOGLE")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getCode()).isEqualTo(101);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getMessage()).isEqualTo("name is invalid");
    }

    @Test
    void invalidProvider() {
        var dto = CreateSubscriptionDto.builder()
                                       .userId(1)
                                       .expirationDate(Instant.parse("2025-12-03T10:15:30.00Z"))
                                       .name("Alex")
                                       .provider("fake_provider")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getCode()).isEqualTo(102);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getMessage()).isEqualTo("provider is invalid");
    }

    @Test
    void expirationDateIsNull() {
        var dto = CreateSubscriptionDto.builder()
                                       .userId(1)
                                       .name("Alex")
                                       .provider("GOOGLE")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getCode()).isEqualTo(103);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getMessage()).isEqualTo("expirationDate is invalid");
    }

    @Test
    void invalidExpirationDate() {
        var dto = CreateSubscriptionDto.builder()
                                       .userId(1)
                                       .name("Alex")
                                       .expirationDate(Instant.parse("2020-12-03T10:15:30.00Z"))
                                       .provider("GOOGLE")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getCode()).isEqualTo(103);
        assertThat(actualResult.getErrors()
                               .get(0)
                               .getMessage()).isEqualTo("expirationDate is invalid");
    }

    @Test
    void invalidUserIdNameProviderExpirationDate() {
        var dto = CreateSubscriptionDto.builder()
                                       .name("")
                                       .expirationDate(Instant.parse("2020-12-03T10:15:30.00Z"))
                                       .provider("fake_provider")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(4);
        var errorCodes = actualResult.getErrors()
                                     .stream()
                                     .map(Error::getCode)
                                     .toList();
        assertThat(errorCodes).contains(100, 101, 102, 103);
        var errorMessages = actualResult.getErrors()
                                        .stream()
                                        .map(Error::getMessage)
                                        .toList();
        assertThat(errorMessages).contains("userId is invalid", "expirationDate is invalid", "name is invalid", "provider is invalid");
    }
}
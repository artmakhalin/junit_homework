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
    void validateFailedIfInvalidUserId() {
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
    void validateFailedIfInvalidName() {
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
    void validateFailedIfInvalidProvider() {
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
    void validateFailedIfExpirationDateIsNull() {
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
    void validateFailedIfInvalidExpirationDate() {
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
    void validateFailedIfInvalidUserIdNameProviderExpirationDate() {
        var dto = CreateSubscriptionDto.builder()
                                       .name("")
                                       .expirationDate(Instant.parse("2020-12-03T10:15:30.00Z"))
                                       .provider("fake_provider")
                                       .build();

        var actualResult = validator.validate(dto);

        assertThat(actualResult.hasErrors()).isTrue();
        assertThat(actualResult.getErrors()).hasSize(4);
        assertThat(actualResult.getErrors()).containsExactly(
                Error.of(100, "userId is invalid"),
                Error.of(101, "name is invalid"),
                Error.of(102, "provider is invalid"),
                Error.of(103, "expirationDate is invalid")
        );
    }
}
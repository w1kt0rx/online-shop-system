package discount.service;

import discount.dto.CreateDiscountRequest;
import discount.dto.DiscountDto;
import discount.model.Discount;
import discount.model.DiscountType;
import discount.repository.DiscountRepository;
import exception.DiscountNotFoundException;
import exception.InvalidProductException;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.ZonedDateTime;
import java.util.List;
import java.util.Optional;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class DiscountServiceTest {

    @Mock
    DiscountRepository discountRepository;
    @InjectMocks
    DiscountService discountService;

    private Discount activeDiscount(String code, DiscountType type, BigDecimal value) {
        return new Discount(1L, code, "Test", type, value, BigDecimal.ZERO,
                ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30));
    }

    private Discount expiredDiscount(String code) {
        return new Discount(2L, code, "Expired", DiscountType.PERCENTAGE, new BigDecimal("10"),
                BigDecimal.ZERO,
                ZonedDateTime.now().minusDays(30),
                ZonedDateTime.now().minusDays(1));
    }

    // ── createDiscount ────────────────────────────────────────────────

    @Test
    void shouldCreateDiscountSuccessfully() {
        when(discountRepository.findByCode("SAVE10")).thenReturn(Optional.empty());
        when(discountRepository.getNextId()).thenReturn(1L);
        when(discountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        DiscountDto result = discountService.createDiscount(new CreateDiscountRequest(
                "SAVE10", "10% off", DiscountType.PERCENTAGE, new BigDecimal("10"),
                BigDecimal.ZERO, ZonedDateTime.now(), ZonedDateTime.now().plusDays(30)));

        assertThat(result.code()).isEqualTo("SAVE10");
        verify(discountRepository).save(any());
    }

    @Test
    void shouldThrowWhenCreatingDuplicateCode() {
        when(discountRepository.findByCode("SAVE10"))
                .thenReturn(Optional.of(activeDiscount("SAVE10", DiscountType.PERCENTAGE, new BigDecimal("10"))));

        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> discountService.createDiscount(new CreateDiscountRequest(
                        "SAVE10", "dup", DiscountType.PERCENTAGE, new BigDecimal("5"),
                        BigDecimal.ZERO, ZonedDateTime.now(), ZonedDateTime.now().plusDays(1))))
                .withMessageContaining("already exists");
    }

    @ParameterizedTest
    @MethodSource("provideDiscountScenarios")
    void shouldApplyDiscountCorrectly(DiscountType type, BigDecimal value,
                                      BigDecimal original, BigDecimal expected) {
        when(discountRepository.findByCode("CODE"))
                .thenReturn(Optional.of(activeDiscount("CODE", type, value)));

        BigDecimal result = discountService.applyDiscount("CODE", original);

        assertThat(result).isEqualByComparingTo(expected);
    }

    private static Stream<Arguments> provideDiscountScenarios() {
        return Stream.of(
                Arguments.of(DiscountType.PERCENTAGE, new BigDecimal("10"), new BigDecimal("1000"), new BigDecimal("900.00")),
                Arguments.of(DiscountType.PERCENTAGE, new BigDecimal("25"), new BigDecimal("1000"), new BigDecimal("750.00")),
                Arguments.of(DiscountType.PERCENTAGE, new BigDecimal("100"), new BigDecimal("500"), new BigDecimal("0.00")),
                Arguments.of(DiscountType.FIXED_AMOUNT, new BigDecimal("200"), new BigDecimal("1500"), new BigDecimal("1300")),
                Arguments.of(DiscountType.FIXED_AMOUNT, new BigDecimal("500"), new BigDecimal("300"), BigDecimal.ZERO)
        );
    }

    @Test
    void shouldThrowForUnknownCode() {
        when(discountRepository.findByCode("NOPE")).thenReturn(Optional.empty());

        assertThatExceptionOfType(DiscountNotFoundException.class)
                .isThrownBy(() -> discountService.applyDiscount("NOPE", new BigDecimal("1000")));
    }

    @Test
    void shouldThrowForExpiredCode() {
        when(discountRepository.findByCode("OLD")).thenReturn(Optional.of(expiredDiscount("OLD")));

        assertThatExceptionOfType(DiscountNotFoundException.class)
                .isThrownBy(() -> discountService.applyDiscount("OLD", new BigDecimal("1000")))
                .withMessageContaining("expired or inactive");
    }

    @Test
    void shouldThrowWhenMinOrderValueNotMet() {
        Discount d = new Discount(1L, "BIG", "Min 2000", DiscountType.FIXED_AMOUNT,
                new BigDecimal("200"), new BigDecimal("2000"),
                ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30));
        when(discountRepository.findByCode("BIG")).thenReturn(Optional.of(d));

        assertThatExceptionOfType(InvalidProductException.class)
                .isThrownBy(() -> discountService.applyDiscount("BIG", new BigDecimal("500")))
                .withMessageContaining("minimum");
    }

    @Test
    void shouldReturnOnlyActiveDiscounts() {
        when(discountRepository.getAll()).thenReturn(List.of(
                activeDiscount("ACTIVE", DiscountType.PERCENTAGE, new BigDecimal("10")),
                expiredDiscount("EXPIRED")
        ));

        List<DiscountDto> result = discountService.getAllActive();

        assertThat(result).hasSize(1);
        assertThat(result.get(0).code()).isEqualTo("ACTIVE");
    }

    @Test
    void shouldReturnEmptyListWhenNoActiveDiscounts() {
        when(discountRepository.getAll()).thenReturn(List.of(expiredDiscount("OLD")));
        assertThat(discountService.getAllActive()).isEmpty();
    }

    // ── describeDiscount ──────────────────────────────────────────────

    @Test
    void shouldDescribePercentageDiscount() {
        when(discountRepository.findByCode("SAVE10"))
                .thenReturn(Optional.of(activeDiscount("SAVE10", DiscountType.PERCENTAGE, new BigDecimal("10"))));

        Optional<String> desc = discountService.describeDiscount("SAVE10");

        assertThat(desc).isPresent().get().asString().contains("10");
    }

    @Test
    void shouldReturnEmptyDescriptionForExpiredCode() {
        when(discountRepository.findByCode("OLD")).thenReturn(Optional.of(expiredDiscount("OLD")));

        assertThat(discountService.describeDiscount("OLD")).isEmpty();
    }

    @Test
    void shouldReturnEmptyDescriptionForUnknownCode() {
        when(discountRepository.findByCode("X")).thenReturn(Optional.empty());

        assertThat(discountService.describeDiscount("X")).isEmpty();
    }

    // ── deActivate ────────────────────────────────────────────────────

    @Test
    void shouldDeactivateDiscount() {
        Discount d = activeDiscount("CODE", DiscountType.PERCENTAGE, new BigDecimal("10"));
        when(discountRepository.findById(1L)).thenReturn(Optional.of(d));
        when(discountRepository.save(any())).thenAnswer(inv -> inv.getArgument(0));

        discountService.deActivate(1L);

        assertThat(d.isValid()).isFalse();
        verify(discountRepository).save(d);
    }

    @Test
    void shouldThrowWhenDeactivatingNonExistentDiscount() {
        when(discountRepository.findById(99L)).thenReturn(Optional.empty());

        assertThatExceptionOfType(DiscountNotFoundException.class)
                .isThrownBy(() -> discountService.deActivate(99L));
    }

    // ── previewDiscountedTotal ───────────────────────────────────────

    @Test
    void shouldReturnDiscountedTotalOnValidCode() {
        when(discountRepository.findByCode("SAVE10"))
                .thenReturn(Optional.of(activeDiscount("SAVE10", DiscountType.PERCENTAGE, new BigDecimal("10"))));

        BigDecimal result = discountService.previewDiscountedTotal("SAVE10", new BigDecimal("1000"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("900"));
    }

    @Test
    void shouldReturnOriginalTotalWhenDiscountCodeUnknown() {
        when(discountRepository.findByCode("BAD")).thenReturn(Optional.empty());

        BigDecimal result = discountService.previewDiscountedTotal("BAD", new BigDecimal("1000"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void shouldReturnOriginalTotalWhenDiscountCodeExpired() {
        when(discountRepository.findByCode("OLD")).thenReturn(Optional.of(expiredDiscount("OLD")));

        BigDecimal result = discountService.previewDiscountedTotal("OLD", new BigDecimal("1000"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void shouldReturnOriginalTotalWhenMinOrderValueNotMet() {
        Discount discount = new Discount(3L, "SAVE200", "Test", DiscountType.FIXED_AMOUNT, new BigDecimal("200"),
                new BigDecimal("2000"), ZonedDateTime.now().minusDays(1), ZonedDateTime.now().plusDays(30));
        when(discountRepository.findByCode("SAVE200")).thenReturn(Optional.of(discount));

        BigDecimal result = discountService.previewDiscountedTotal("SAVE200", new BigDecimal("1000"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("1000"));
    }
}

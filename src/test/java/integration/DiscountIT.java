package integration;

import static org.assertj.core.api.Assertions.*;

import discount.dto.CreateDiscountRequest;
import discount.dto.DiscountDto;
import discount.model.DiscountType;
import discount.repository.impl.InMemoryDiscountRepository;
import discount.service.DiscountService;
import exception.DiscountNotFoundException;
import exception.InvalidProductException;
import java.math.BigDecimal;
import java.time.ZonedDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;

class DiscountIT {

    private DiscountService discountService;
    private ZonedDateTime now;

    @BeforeEach
    void setUp() {
        discountService = new DiscountService(new InMemoryDiscountRepository());
        now = ZonedDateTime.now();
    }

    private CreateDiscountRequest percentage(String code, BigDecimal value, BigDecimal minOrder) {
        return CreateDiscountRequest.of(
            code,
            "desc",
            DiscountType.PERCENTAGE,
            value,
            minOrder,
            now.minusDays(1),
            now.plusDays(30)
        );
    }

    private CreateDiscountRequest fixed(String code, BigDecimal value, BigDecimal minOrder) {
        return CreateDiscountRequest.of(
            code,
            "desc",
            DiscountType.FIXED_AMOUNT,
            value,
            minOrder,
            now.minusDays(1),
            now.plusDays(30)
        );
    }

    @Test
    void createDiscount_persistsAllFields() {
        DiscountDto dto = discountService.createDiscount(
            CreateDiscountRequest.of(
                "WELCOME10",
                "Welcome deal",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                now.minusDays(1),
                now.plusYears(1)
            )
        );

        assertThat(dto.id()).isPositive();
        assertThat(dto.code()).isEqualTo("WELCOME10");
        assertThat(dto.description()).isEqualTo("Welcome deal");
        assertThat(dto.type()).isEqualTo(DiscountType.PERCENTAGE);
        assertThat(dto.value()).isEqualByComparingTo(new BigDecimal("10"));
        assertThat(dto.minOrderValue()).isEqualByComparingTo(BigDecimal.ZERO);
        assertThat(dto.active()).isTrue();
        assertThat(dto.validFrom()).isNotNull();
        assertThat(dto.validTo()).isNotNull();
    }

    @Test
    void createDiscount_duplicateCodeThrows_firstOneUnchanged() {
        discountService.createDiscount(percentage("SALE10", new BigDecimal("10"), BigDecimal.ZERO));

        assertThatExceptionOfType(InvalidProductException.class)
            .isThrownBy(() ->
                discountService.createDiscount(percentage("SALE10", new BigDecimal("20"), BigDecimal.ZERO))
            )
            .withMessageContaining("SALE10");

        DiscountDto original = discountService.getByCode("SALE10");
        assertThat(original.value()).isEqualByComparingTo(new BigDecimal("10"));
    }

    @Test
    void createDiscount_validToBeforeValidFrom_throws() {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            discountService.createDiscount(
                CreateDiscountRequest.of(
                    "BAD",
                    "bad",
                    DiscountType.PERCENTAGE,
                    new BigDecimal("10"),
                    BigDecimal.ZERO,
                    now.plusDays(5),
                    now.minusDays(1)
                )
            )
        );
    }

    @ParameterizedTest
    @ValueSource(strings = { "", "   " })
    void createDiscount_blankCodeThrows(String code) {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            discountService.createDiscount(
                CreateDiscountRequest.of(
                    code,
                    "desc",
                    DiscountType.PERCENTAGE,
                    new BigDecimal("10"),
                    BigDecimal.ZERO,
                    now.minusDays(1),
                    now.plusDays(10)
                )
            )
        );
    }

    @Test
    void createDiscount_negativeValueThrows() {
        assertThatExceptionOfType(InvalidProductException.class).isThrownBy(() ->
            discountService.createDiscount(percentage("NEG", new BigDecimal("-5"), BigDecimal.ZERO))
        );
    }

    @ParameterizedTest(name = "{0}% off {1} = {2}")
    @CsvSource(
        { "10,  1000, 900.00", "25,  1000, 750.00", "50,   200, 100.00", "100,  500,   0.00", "15,  3000, 2550.00" }
    )
    void applyDiscount_percentage_correctResult(BigDecimal pct, BigDecimal base, BigDecimal expected) {
        discountService.createDiscount(percentage("PCT", pct, BigDecimal.ZERO));

        BigDecimal result = discountService.applyDiscount("PCT", base);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @ParameterizedTest(name = "{0} off {1} = {2}")
    @CsvSource({ "200, 1500, 1300", "500, 3000, 2500", "100,  100,    0", "999,  100,    0" })
    void applyDiscount_fixedAmount_correctResultWithFloor(BigDecimal amount, BigDecimal base, BigDecimal expected) {
        discountService.createDiscount(fixed("FIX", amount, BigDecimal.ZERO));

        BigDecimal result = discountService.applyDiscount("FIX", base);

        assertThat(result).isEqualByComparingTo(expected);
    }

    @Test
    void applyDiscount_orderBelowMinValue_throws() {
        discountService.createDiscount(fixed("MIN2K", new BigDecimal("200"), new BigDecimal("2000")));

        assertThatExceptionOfType(InvalidProductException.class)
            .isThrownBy(() -> discountService.applyDiscount("MIN2K", new BigDecimal("1999")))
            .withMessageContaining("minimum");
    }

    @Test
    void applyDiscount_orderExactlyAtMinValue_succeeds() {
        discountService.createDiscount(fixed("MIN2K", new BigDecimal("200"), new BigDecimal("2000")));

        BigDecimal result = discountService.applyDiscount("MIN2K", new BigDecimal("2000"));

        assertThat(result).isEqualByComparingTo(new BigDecimal("1800"));
    }

    @Test
    void applyDiscount_expiredCode_throws() {
        discountService.createDiscount(
            CreateDiscountRequest.of(
                "EXPIRED",
                "old",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                now.minusDays(30),
                now.minusDays(1)
            )
        );

        assertThatExceptionOfType(DiscountNotFoundException.class).isThrownBy(() ->
            discountService.applyDiscount("EXPIRED", new BigDecimal("1000"))
        );
    }

    @Test
    void applyDiscount_futureCode_throws() {
        discountService.createDiscount(
            CreateDiscountRequest.of(
                "FUTURE",
                "not yet",
                DiscountType.PERCENTAGE,
                new BigDecimal("10"),
                BigDecimal.ZERO,
                now.plusDays(5),
                now.plusDays(30)
            )
        );

        assertThatExceptionOfType(DiscountNotFoundException.class).isThrownBy(() ->
            discountService.applyDiscount("FUTURE", new BigDecimal("1000"))
        );
    }

    @Test
    void deactivate_discountNoLongerApplicableOrVisibleAsActive() {
        DiscountDto dto = discountService.createDiscount(percentage("ACTIVE", new BigDecimal("10"), BigDecimal.ZERO));
        assertThat(discountService.getAllActive()).hasSize(1);

        discountService.deactivate(dto.id());

        assertThat(discountService.getAllActive()).isEmpty();
        assertThatExceptionOfType(DiscountNotFoundException.class).isThrownBy(() ->
            discountService.applyDiscount("ACTIVE", new BigDecimal("1000"))
        );
    }

    @Test
    void getAllActive_returnsOnlyValidDiscounts() {
        discountService.createDiscount(percentage("VALID1", new BigDecimal("5"), BigDecimal.ZERO));
        discountService.createDiscount(percentage("VALID2", new BigDecimal("10"), BigDecimal.ZERO));
        discountService.createDiscount(
            CreateDiscountRequest.of(
                "EXPIRED2",
                "old",
                DiscountType.PERCENTAGE,
                new BigDecimal("15"),
                BigDecimal.ZERO,
                now.minusDays(10),
                now.minusDays(1)
            )
        );

        List<DiscountDto> active = discountService.getAllActive();

        assertThat(active).hasSize(2);
        assertThat(active).extracting(DiscountDto::code).containsExactlyInAnyOrder("VALID1", "VALID2");
        assertThat(active).allSatisfy(d -> assertThat(d.active()).isTrue());
    }

    @Test
    void getByCode_returnsCorrectDiscount() {
        discountService.createDiscount(fixed("FIXED200", new BigDecimal("200"), new BigDecimal("1000")));

        DiscountDto dto = discountService.getByCode("FIXED200");

        assertThat(dto.code()).isEqualTo("FIXED200");
        assertThat(dto.type()).isEqualTo(DiscountType.FIXED_AMOUNT);
        assertThat(dto.value()).isEqualByComparingTo(new BigDecimal("200"));
        assertThat(dto.minOrderValue()).isEqualByComparingTo(new BigDecimal("1000"));
    }

    @Test
    void getByCode_unknownCode_throws() {
        assertThatExceptionOfType(DiscountNotFoundException.class).isThrownBy(() -> discountService.getByCode("GHOST"));
    }

    @Test
    void previewDiscountedTotal_returnsOriginalWhenCodeInvalid() {
        BigDecimal original = new BigDecimal("1000");

        BigDecimal preview = discountService.previewDiscountedTotal("GHOST", original);

        assertThat(preview).isEqualByComparingTo(original);
    }

    @Test
    void previewDiscountedTotal_validCode_returnsDiscountedAmount() {
        discountService.createDiscount(percentage("PREVIEW10", new BigDecimal("10"), BigDecimal.ZERO));

        BigDecimal preview = discountService.previewDiscountedTotal("PREVIEW10", new BigDecimal("2000"));

        assertThat(preview).isEqualByComparingTo(new BigDecimal("1800.00"));
    }
}

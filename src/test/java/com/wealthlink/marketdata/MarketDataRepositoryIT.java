package com.wealthlink.marketdata;

import com.wealthlink.fund.entity.Fund;
import com.wealthlink.fund.entity.FundShareClass;
import com.wealthlink.fund.entity.Provider;
import com.wealthlink.fund.repository.FundRepository;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.marketdata.entity.*;
import com.wealthlink.marketdata.repository.FundPriceRepository;
import com.wealthlink.marketdata.repository.FxRateRepository;
import com.wealthlink.marketdata.repository.FxRateSourceRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import java.math.BigDecimal;
import java.time.LocalDate;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-layer test (DEV2-D2 / DEV2-D3) for FX rates and fund prices.
 * {@link #duplicateFundPriceSameShareClassDatePriceTypeAndProviderIsRejected()}
 * is the critical acceptance-criteria test called out explicitly in the
 * Jira doc's DAY 3 story: same (share class, date, type, provider) inserted
 * twice - first succeeds, second must be rejected.
 */
class MarketDataRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private FxRateSourceRepository fxRateSourceRepository;

    @Autowired
    private FxRateRepository fxRateRepository;

    @Autowired
    private FundPriceRepository fundPriceRepository;

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private FundShareClassRepository fundShareClassRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    private FundShareClass shareClassFixture(Currency currency, Country country, String isin) {
        Fund fund = fundRepository.saveAndFlush(Fund.builder()
                .isin(isin).name("Fixture Fund").baseCurrency(currency).domicileCountry(country).build());
        return fundShareClassRepository.saveAndFlush(FundShareClass.builder()
                .fund(fund).classCode("A").name("Class A").currency(currency).build());
    }

    @Test
    void seedFxRateSourcesAreLoadedByFlywayMigration() {
        FxRateSource ecb = fxRateSourceRepository.findByCode("ECB").orElseThrow();
        assertThat(ecb.getName()).isEqualTo("European Central Bank");
    }

    @Test
    void fxRateBusinessKeyIsEnforced() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Currency eur = currencyRepository.findByIsoCode("EUR").orElseThrow();
        FxRateSource ecb = fxRateSourceRepository.findByCode("ECB").orElseThrow();
        LocalDate rateDate = LocalDate.of(2026, 8, 14);

        fxRateRepository.saveAndFlush(FxRate.builder()
                .baseCurrency(eur).quoteCurrency(nok).rateDate(rateDate)
                .rateType(RateType.SPOT).source(ecb).rate(new BigDecimal("11.50000000")).build());

        FxRate duplicate = FxRate.builder()
                .baseCurrency(eur).quoteCurrency(nok).rateDate(rateDate)
                .rateType(RateType.SPOT).source(ecb).rate(new BigDecimal("11.60000000")).build();

        assertThatThrownBy(() -> fxRateRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void differentRateTypeOnSameDayIsAllowed() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Currency eur = currencyRepository.findByIsoCode("EUR").orElseThrow();
        FxRateSource ecb = fxRateSourceRepository.findByCode("ECB").orElseThrow();
        LocalDate rateDate = LocalDate.of(2026, 8, 15);

        fxRateRepository.saveAndFlush(FxRate.builder()
                .baseCurrency(eur).quoteCurrency(nok).rateDate(rateDate)
                .rateType(RateType.SPOT).source(ecb).rate(new BigDecimal("11.50000000")).build());

        FxRate closeRate = FxRate.builder()
                .baseCurrency(eur).quoteCurrency(nok).rateDate(rateDate)
                .rateType(RateType.CLOSE).source(ecb).rate(new BigDecimal("11.55000000")).build();

        assertThat(fxRateRepository.saveAndFlush(closeRate).getId()).isNotNull();
    }

    @Test
    void duplicateFundPriceSameShareClassDatePriceTypeAndProviderIsRejected() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        Provider morningstar = providerRepository.findByCode("MORNINGSTAR").orElseThrow();
        FundShareClass shareClass = shareClassFixture(nok, norway, "NO0010000010");
        LocalDate priceDate = LocalDate.of(2026, 8, 14);

        FundPrice first = FundPrice.builder()
                .fundShareClass(shareClass).priceDate(priceDate).priceType(PriceType.NAV)
                .provider(morningstar).currency(nok).price(new BigDecimal("102.50000000")).build();

        // FIRST -> SUCCESS
        assertThat(fundPriceRepository.saveAndFlush(first).getId()).isNotNull();

        FundPrice second = FundPrice.builder()
                .fundShareClass(shareClass).priceDate(priceDate).priceType(PriceType.NAV)
                .provider(morningstar).currency(nok).price(new BigDecimal("102.55000000")).build();

        // SECOND (same share class + date + type + provider, retry-style) -> REJECTED
        assertThatThrownBy(() -> fundPriceRepository.saveAndFlush(second))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void samePriceOnSameDayFromDifferentProviderIsAllowed() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        Provider morningstar = providerRepository.findByCode("MORNINGSTAR").orElseThrow();
        Provider bloomberg = providerRepository.findByCode("BLOOMBERG").orElseThrow();
        FundShareClass shareClass = shareClassFixture(nok, norway, "NO0010000011");
        LocalDate priceDate = LocalDate.of(2026, 8, 14);

        fundPriceRepository.saveAndFlush(FundPrice.builder()
                .fundShareClass(shareClass).priceDate(priceDate).priceType(PriceType.NAV)
                .provider(morningstar).currency(nok).price(new BigDecimal("102.50000000")).build());

        FundPrice fromOtherProvider = FundPrice.builder()
                .fundShareClass(shareClass).priceDate(priceDate).priceType(PriceType.NAV)
                .provider(bloomberg).currency(nok).price(new BigDecimal("102.50000000")).build();

        assertThat(fundPriceRepository.saveAndFlush(fromOtherProvider).getId()).isNotNull();
    }
}

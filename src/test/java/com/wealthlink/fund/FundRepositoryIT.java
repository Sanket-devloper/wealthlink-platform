package com.wealthlink.fund;

import com.wealthlink.fund.entity.*;
import com.wealthlink.fund.repository.FundProviderMappingRepository;
import com.wealthlink.fund.repository.FundRepository;
import com.wealthlink.fund.repository.FundShareClassRepository;
import com.wealthlink.fund.repository.ProviderRepository;
import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Currency;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.CurrencyRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Repository-layer test (DEV2-D2) for the Funds &amp; Providers module.
 * Wires real FKs into Dev 1's already-merged CURRENCY / COUNTRY tables.
 */
class FundRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private FundRepository fundRepository;

    @Autowired
    private FundShareClassRepository fundShareClassRepository;

    @Autowired
    private ProviderRepository providerRepository;

    @Autowired
    private FundProviderMappingRepository fundProviderMappingRepository;

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void seedProvidersAreLoadedByFlywayMigration() {
        Provider morningstar = providerRepository.findByCode("MORNINGSTAR").orElseThrow();
        assertThat(morningstar.getStatus()).isEqualTo(ProviderStatus.ACTIVE);
    }

    @Test
    void fundPersistsWithRealCurrencyAndCountryForeignKeys() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        Fund fund = Fund.builder()
                .isin("NO0010000001")
                .name("Nordic Equity Fund")
                .baseCurrency(nok)
                .domicileCountry(norway)
                .build();

        Fund saved = fundRepository.saveAndFlush(fund);

        assertThat(saved.getId()).isNotNull();
        assertThat(saved.getStatus()).isEqualTo(FundStatus.ACTIVE); // @PrePersist default
        assertThat(fundRepository.findByIsin("NO0010000001")).isPresent();
    }

    @Test
    void isinUniqueConstraintIsEnforced() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        fundRepository.saveAndFlush(Fund.builder()
                .isin("NO0010000002")
                .name("Original Fund")
                .baseCurrency(nok)
                .domicileCountry(norway)
                .build());

        Fund duplicate = Fund.builder()
                .isin("NO0010000002")
                .name("Duplicate ISIN Fund")
                .baseCurrency(nok)
                .domicileCountry(norway)
                .build();

        assertThatThrownBy(() -> fundRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void shareClassCodeIsUniquePerFundNotGlobally() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        Fund fundA = fundRepository.saveAndFlush(Fund.builder()
                .isin("NO0010000003").name("Fund A").baseCurrency(nok).domicileCountry(norway).build());
        Fund fundB = fundRepository.saveAndFlush(Fund.builder()
                .isin("NO0010000004").name("Fund B").baseCurrency(nok).domicileCountry(norway).build());

        // Same class code "A" on two different funds must be allowed.
        fundShareClassRepository.saveAndFlush(FundShareClass.builder()
                .fund(fundA).classCode("A").name("Class A").currency(nok).build());
        FundShareClass shareClassB = fundShareClassRepository.saveAndFlush(FundShareClass.builder()
                .fund(fundB).classCode("A").name("Class A").currency(nok).build());

        assertThat(shareClassB.getId()).isNotNull();

        // But the same class code twice on the SAME fund must be rejected.
        FundShareClass duplicateOnFundA = FundShareClass.builder()
                .fund(fundA).classCode("A").name("Class A duplicate").currency(nok).build();

        assertThatThrownBy(() -> fundShareClassRepository.saveAndFlush(duplicateOnFundA))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void fundProviderMappingBusinessKeyIsEnforced() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        Provider morningstar = providerRepository.findByCode("MORNINGSTAR").orElseThrow();

        Fund fund = fundRepository.saveAndFlush(Fund.builder()
                .isin("NO0010000005").name("Fund C").baseCurrency(nok).domicileCountry(norway).build());
        FundShareClass shareClass = fundShareClassRepository.saveAndFlush(FundShareClass.builder()
                .fund(fund).classCode("A").name("Class A").currency(nok).build());

        fundProviderMappingRepository.saveAndFlush(FundProviderMapping.builder()
                .fundShareClass(shareClass).provider(morningstar).externalFundId("MSTAR-EXT-1").build());

        FundProviderMapping duplicate = FundProviderMapping.builder()
                .fundShareClass(shareClass).provider(morningstar).externalFundId("MSTAR-EXT-1").build();

        assertThatThrownBy(() -> fundProviderMappingRepository.saveAndFlush(duplicate))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}

package com.wealthlink.reference;

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
 * Repository-layer test for the Country / Market Config module.
 */
class CurrencyRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private CurrencyRepository currencyRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void seedDataIsLoadedByFlywayMigration() {
        Currency nok = currencyRepository.findByIsoCode("NOK").orElseThrow();
        assertThat(nok.getMinorUnitDigits()).isEqualTo((short) 2);

        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();
        assertThat(norway.getDefaultCurrency().getIsoCode()).isEqualTo("NOK");
    }

    @Test
    void fourSeedCurrenciesExist() {
        assertThat(currencyRepository.count()).isEqualTo(4); // NOK, SEK, DKK, EUR
    }

    @Test
    void isoCodeUniqueConstraintIsEnforced() {
        Currency duplicateNok = Currency.builder()
                .isoCode("NOK")
                .name("Duplicate Norwegian Krone")
                .minorUnitDigits((short) 2)
                .build();

        assertThatThrownBy(() -> {
            currencyRepository.saveAndFlush(duplicateNok);
        }).isInstanceOf(DataIntegrityViolationException.class);
    }
}

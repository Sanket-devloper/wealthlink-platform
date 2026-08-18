package com.wealthlink.reference;

import com.wealthlink.reference.entity.Country;
import com.wealthlink.reference.entity.Market;
import com.wealthlink.reference.entity.MarketStatus;
import com.wealthlink.reference.repository.CountryRepository;
import com.wealthlink.reference.repository.MarketRepository;
import com.wealthlink.support.AbstractIntegrationTest;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Repository-layer test for Market, which sits inside Country / Market Config.
 */
class MarketRepositoryIT extends AbstractIntegrationTest {

    @Autowired
    private MarketRepository marketRepository;

    @Autowired
    private CountryRepository countryRepository;

    @Test
    void marketCanBeCreatedForASeededCountry() {
        Country norway = countryRepository.findByIsoCode("NO").orElseThrow();

        Market oslo = Market.builder()
                .country(norway)
                .name("Oslo Bors")
                .micCode("XOSL")
                .timezone("Europe/Oslo")
                .status(MarketStatus.ACTIVE)
                .build();

        Market saved = marketRepository.saveAndFlush(oslo);

        assertThat(saved.getId()).isNotNull();
        assertThat(marketRepository.findByCountryId(norway.getId())).contains(saved);
    }
}

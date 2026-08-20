package com.wealthlink.importdata.service;

import java.util.UUID;

public interface ImportItemProcessorService {

    void processItem(UUID importItemId);

}
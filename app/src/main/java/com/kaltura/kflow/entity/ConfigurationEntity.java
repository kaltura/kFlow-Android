package com.kaltura.kflow.entity;

import java.util.ArrayList;
import java.util.List;

/**
 * Created by alex_lytvynenko on 2019-06-24.
 */
public class ConfigurationEntity {
    private List<AccountEntity> accounts = new ArrayList<>();

    public List<AccountEntity> getAccounts() {
        return accounts;
    }
}

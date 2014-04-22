package com.subgraph.sgmail.accounts.impl;

import com.google.common.collect.ImmutableList;

import java.util.List;

public class AccountUtils {
    private final static List<String> GMAIL_DOMAINS = ImmutableList.of("gmail.com", "googlemail.com");

    static boolean isGmailServerAddress(String address) {
        for(String domain: GMAIL_DOMAINS) {
            if(address.toLowerCase().endsWith(domain)) {
                return true;
            }
        }
        return false;
    }
}

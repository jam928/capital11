package com.capital11.web;

public final class SessionKeys {

    /** cid of the logged-in customer. */
    public static final String CUSTOMER_ID = "cid";

    /** cid of the customer whose username was verified on the forgot-password page. */
    public static final String RESET_CUSTOMER_ID = "resetCid";

    private SessionKeys() {
    }
}

package com.example.spinlog.global.security.filter;

import com.example.spinlog.global.security.oauth2.user.OAuth2Response;

public class OAuth2ResponseImpl implements OAuth2Response {

    @Override
    public String getProvider() {
        return "google";
    }

    @Override
    public String getProviderId() {
        return "randomId";
    }

    @Override
    public String getEmail() {
        return "hhh@kkk";
    }

    @Override
    public String getAuthenticationName() {
        return "google_randomId";
    }
}

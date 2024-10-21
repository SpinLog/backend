package com.example.spinlog.global.security.customFilter;

import com.example.spinlog.global.security.oauth2.user.OAuth2Response;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;

@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OAuth2ResponseImpl implements OAuth2Response {
    @Builder.Default
    private String provider = "google";
    @Builder.Default
    private String providerId = "randomId";
    @Builder.Default
    private String email = "hhh@kkk";

    @Override
    public String getProvider() {
        return this.provider;
    }

    @Override
    public String getProviderId() {
        return this.providerId;
    }

    @Override
    public String getEmail() {
        return this.email;
    }

    @Override
    public String getAuthenticationName() {
        return this.provider + "_" + this.providerId;
    }
}

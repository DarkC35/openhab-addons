/**
 * Copyright (c) 2010-2021 Contributors to the openHAB project
 *
 * See the NOTICE file(s) distributed with this work for additional
 * information.
 *
 * This program and the accompanying materials are made available under the
 * terms of the Eclipse Public License 2.0 which is available at
 * http://www.eclipse.org/legal/epl-2.0
 *
 * SPDX-License-Identifier: EPL-2.0
 */
package org.openhab.binding.microsofttodo.internal.api;

import java.io.IOException;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.auth.AuthConstants;
import com.microsoft.graph.auth.BaseAuthentication;
import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.authentication.IAuthenticationProvider;
import com.microsoft.graph.http.IHttpRequest;
import com.microsoft.graph.httpcore.ICoreAuthenticationProvider;

import okhttp3.Request;

/**
 * The {@link MicrosoftToDoAuthProvider} wraps the {@link OAuthClientService} with an {@link IAuthenticationProvider}
 * and {@link ICoreAuthenticationProvider} that is required for making calls with the GraphServiceClient.
 * Overrides the {@link getAccessTokenSilent} method to use already managed access token from the OAuthClientService.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
public class MicrosoftToDoAuthProvider extends BaseAuthentication
        // using deprecated IAuthenticationProvider here since it is required for microsoft-graph 2.x
        implements IAuthenticationProvider, ICoreAuthenticationProvider {

    private final Logger logger = LoggerFactory.getLogger(MicrosoftToDoAuthProvider.class);

    private final OAuthClientService oAuthService;

    public MicrosoftToDoAuthProvider(String clientId, List<String> scopes, String redirectUri,
            @Nullable NationalCloud nationalCloud, @Nullable String tenant, String clientSecret,
            OAuthClientService oAuthService) {
        super(scopes, clientId,
                GetAuthority(nationalCloud == null ? NationalCloud.Global : nationalCloud,
                        tenant == null ? AuthConstants.Tenants.Common : tenant),
                redirectUri, nationalCloud == null ? NationalCloud.Global : nationalCloud,
                tenant == null ? AuthConstants.Tenants.Common : tenant, clientSecret);
        this.oAuthService = oAuthService;
    }

    @Override
    protected @Nullable String getAccessTokenSilent() {
        try {
            AccessTokenResponse accessTokenResponse = oAuthService.getAccessTokenResponse();
            if (accessTokenResponse != null) {
                return accessTokenResponse.getAccessToken();
            }
        } catch (OAuthException | IOException | OAuthResponseException e) {
            logger.debug("Exception during access of token response: ", e);
        }
        return null;
    }

    @Override
    public @Nullable Request authenticateRequest(@Nullable Request request) {
        if (request == null) {
            return null;
        }
        String tokenParameter = AuthConstants.BEARER + getAccessTokenSilent();
        return request.newBuilder().addHeader(AuthConstants.AUTHORIZATION_HEADER, tokenParameter).build();
    }

    @Override
    public void authenticateRequest(@Nullable IHttpRequest request) {
        if (request != null) {
            String tokenParameter = AuthConstants.BEARER + getAccessTokenSilent();
            request.addHeader(AuthConstants.AUTHORIZATION_HEADER, tokenParameter);
        }
    }
}

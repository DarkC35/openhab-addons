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
package org.openhab.binding.microsofttodo.internal.handler;

import static org.openhab.binding.microsofttodo.internal.MicrosoftToDoBindingConstants.*;

import java.io.IOException;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.microsofttodo.internal.MicrosoftToDoAccountHandler;
import org.openhab.binding.microsofttodo.internal.MicrosoftToDoConfiguration;
import org.openhab.binding.microsofttodo.internal.api.MicrosoftToDoApi;
import org.openhab.binding.microsofttodo.internal.api.exception.MicrosoftToDoException;
import org.openhab.core.auth.client.oauth2.AccessTokenRefreshListener;
import org.openhab.core.auth.client.oauth2.AccessTokenResponse;
import org.openhab.core.auth.client.oauth2.OAuthClientService;
import org.openhab.core.auth.client.oauth2.OAuthException;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.auth.client.oauth2.OAuthResponseException;
import org.openhab.core.cache.ExpiringCache;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.ThingStatus;
import org.openhab.core.thing.ThingStatusDetail;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.BaseBridgeHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.RefreshType;
import org.openhab.core.types.State;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.models.extensions.Todo;
import com.microsoft.graph.models.extensions.User;

@NonNullByDefault
public class MicrosoftToDoBridgeHandler extends BaseBridgeHandler
        implements MicrosoftToDoAccountHandler, AccessTokenRefreshListener {

    private final Logger logger = LoggerFactory.getLogger(MicrosoftToDoBridgeHandler.class);

    private final OAuthFactory oAuthFactory;

    private @NonNullByDefault({}) OAuthClientService oAuthService;
    private @NonNullByDefault({}) MicrosoftToDoConfiguration configuration;
    private @NonNullByDefault({}) MicrosoftToDoApi microsoftToDoApi;
    private @NonNullByDefault({}) ExpiringCache<List<Todo>> todoTaskListsCache;

    public MicrosoftToDoBridgeHandler(Bridge bridge, OAuthFactory oAuthFactory) {
        super(bridge);
        this.oAuthFactory = oAuthFactory;
    }

    @Override
    public void initialize() {
        updateStatus(ThingStatus.UNKNOWN);
        configuration = getConfigAs(MicrosoftToDoConfiguration.class);
        oAuthService = oAuthFactory.createOAuthClientService(thing.getUID().getAsString(), MICROSOFT_TOKEN_URL,
                MICROSOFT_AUTHORIZE_URL, configuration.clientId, configuration.clientSecret, MICROSOFT_SCOPES, true);
        oAuthService.addAccessTokenRefreshListener(MicrosoftToDoBridgeHandler.this);
        // TODO: make NationalCloud and tenant configurable with advanced configurations
        microsoftToDoApi = new MicrosoftToDoApi(configuration.clientId, MICROSOFT_SCOPES_LIST, "", null, null,
                configuration.clientSecret, oAuthService);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
        if (command instanceof RefreshType) {
            switch (channelUID.getId()) {
                case CHANNEL_ACCESSTOKEN:
                    onAccessTokenResponse(getAccessTokenResponse());
                    break;
            }
        }
    }

    private @Nullable AccessTokenResponse getAccessTokenResponse() {
        try {
            return oAuthService == null ? null : oAuthService.getAccessTokenResponse();
        } catch (OAuthException | IOException | OAuthResponseException | RuntimeException e) {
            logger.debug("Exception checking authorization: ", e);
            return null;
        }
    }

    @Override
    public void dispose() {
        if (oAuthService != null) {
            oAuthService.removeAccessTokenRefreshListener(this);
        }
        oAuthFactory.ungetOAuthService(thing.getUID().getAsString());
        // cancelSchedulers();
    }

    @Override
    public void onAccessTokenResponse(@Nullable AccessTokenResponse tokenResponse) {
        updateChannelState(CHANNEL_ACCESSTOKEN,
                new StringType(tokenResponse == null ? null : tokenResponse.getAccessToken()));
    }

    @Override
    public ThingUID getUID() {
        return thing.getUID();
    }

    @Override
    public String getName() {
        return thing.getProperties().getOrDefault(PROPERTY_MICROSOFT_USER, "");
    }

    @Override
    public String getEmail() {
        return thing.getProperties().getOrDefault(PROPERTY_MICROSOFT_EMAIL, "");
    }

    @Override
    public boolean isAuthorized() {
        final AccessTokenResponse accessTokenResponse = getAccessTokenResponse();

        return accessTokenResponse != null && accessTokenResponse.getAccessToken() != null
                && accessTokenResponse.getRefreshToken() != null;
    }

    @Override
    public String authorize(String redirectUrl, String reqCode) {
        try {
            logger.debug("Make call to Microsoft to get access token.");
            final AccessTokenResponse credentials = oAuthService.getAccessTokenResponseByAuthorizationCode(reqCode,
                    redirectUrl);
            onAccessTokenResponse(credentials);
            final String user = updateProperties();
            logger.debug("Authorized for user: {}", user);
            // startPolling();
            // TODO: implement polling and consider webhooks to support/replace polling
            // see: https://docs.microsoft.com/en-us/graph/webhooks
            return user;
        } catch (RuntimeException | OAuthException | IOException e) {
            updateStatus(ThingStatus.OFFLINE, ThingStatusDetail.CONFIGURATION_ERROR, e.getMessage());
            throw new MicrosoftToDoException(e.getMessage(), e);
        } catch (OAuthResponseException e) {
            throw new MicrosoftToDoException(e.getMessage(), e); // TODO: make a MSAuthorizationException
        }
    }

    private String updateProperties() {
        if (microsoftToDoApi != null) {
            final User user = microsoftToDoApi.getCurrentUser();
            final String userName = user.displayName;
            final String email = user.userPrincipalName;
            final Map<String, String> props = editProperties();

            props.put(PROPERTY_MICROSOFT_USER, userName);
            props.put(PROPERTY_MICROSOFT_EMAIL, email);
            updateProperties(props);
            return userName;
        }
        return "";
    }

    @Override
    public boolean equalsThingUID(String thingUID) {
        return getThing().getUID().getAsString().equals(thingUID);
    }

    @Override
    public String formatAuthorizationUrl(String redirectUri) {
        try {
            return oAuthService.getAuthorizationUrl(redirectUri, null, thing.getUID().getAsString());
        } catch (OAuthException e) {
            logger.debug("Error constructing AuthorizationUrl: ", e);
            return "";
        }
    }

    /**
     * Convenience method to update the channel state but only if the channel is linked.
     *
     * @param channelId id of the channel to update
     * @param state State to set on the channel
     */
    private void updateChannelState(String channelId, State state) {
        final Channel channel = thing.getChannel(channelId);

        if (channel != null && isLinked(channel.getUID())) {
            updateState(channel.getUID(), state);
        }
    }

    @Override
    public List<Todo> getTodoTaskLists() {
        final List<Todo> todoTaskLists = todoTaskListsCache.getValue();

        return todoTaskLists == null ? Collections.emptyList() : todoTaskLists;
    }
}

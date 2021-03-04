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
package org.openhab.binding.microsofttodo.internal;

import java.util.Set;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.microsofttodo.internal.handler.MicrosoftToDoBridgeHandler;
import org.openhab.core.auth.client.oauth2.OAuthFactory;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.ThingTypeUID;
import org.openhab.core.thing.binding.BaseThingHandlerFactory;
import org.openhab.core.thing.binding.ThingHandler;
import org.openhab.core.thing.binding.ThingHandlerFactory;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Reference;

/**
 * The {@link MicrosoftToDoHandlerFactory} is responsible for creating things and thing
 * handlers.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
@Component(configurationPid = "binding.microsofttodo", service = ThingHandlerFactory.class)
public class MicrosoftToDoHandlerFactory extends BaseThingHandlerFactory {

    private static final Set<ThingTypeUID> SUPPORTED_THING_TYPES_UIDS = Set.of(
            MicrosoftToDoBindingConstants.THING_TYPE_ACCOUNT, MicrosoftToDoBindingConstants.THING_TYPE_TODO_TASK_LIST);

    private final OAuthFactory oAuthFactory;
    private final MicrosoftToDoAuthService authService;

    @Activate
    public MicrosoftToDoHandlerFactory(@Reference OAuthFactory oAuthFactory,
            @Reference MicrosoftToDoAuthService authService) {
        this.oAuthFactory = oAuthFactory;
        this.authService = authService;
    }

    @Override
    public boolean supportsThingType(ThingTypeUID thingTypeUID) {
        return SUPPORTED_THING_TYPES_UIDS.contains(thingTypeUID);
    }

    @Override
    protected @Nullable ThingHandler createHandler(Thing thing) {
        ThingTypeUID thingTypeUID = thing.getThingTypeUID();

        if (MicrosoftToDoBindingConstants.THING_TYPE_ACCOUNT.equals(thingTypeUID)) {
            final MicrosoftToDoBridgeHandler handler = new MicrosoftToDoBridgeHandler((Bridge) thing, oAuthFactory);
            authService.addMicrosoftToDoAccountHandler(handler);
            return handler;
        }

        return null;
    }

    @Override
    protected synchronized void removeHandler(ThingHandler thingHandler) {
        if (thingHandler instanceof MicrosoftToDoAccountHandler) {
            authService.removeMicrosoftToDoAccountHandler((MicrosoftToDoAccountHandler) thingHandler);
        }
    }
}

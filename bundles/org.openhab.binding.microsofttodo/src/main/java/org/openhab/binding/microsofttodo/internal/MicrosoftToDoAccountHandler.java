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

import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingUID;
import org.openhab.core.thing.binding.ThingHandler;

import com.microsoft.graph.models.extensions.Todo;

/**
 * Interface to decouple MicrosoftToDo Bridge Handler implementation from other code.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
public interface MicrosoftToDoAccountHandler extends ThingHandler {

    /**
     * @return The {@link ThingUID} associated with this MicrosoftToDo Account Handler
     */
    ThingUID getUID();

    /**
     * @return The Microsoft user name associated with this MicrosoftToDo Account Handler
     */
    String getName();

    /**
     * @return The Microsoft email address associated with this MicrosoftToDo Account Handler
     */
    String getEmail();

    /**
     * @return List of todo task lists associated with this Microsoft Account Handler
     */
    List<Todo> getTodoTaskLists();

    /**
     * @return Returns true if the MicrosoftToDo Bridge is authorized.
     */
    boolean isAuthorized();

    /**
     * Calls Microsoft Graph Endpoint to obtain refresh and access tokens and persist data with Thing.
     *
     * @param redirectUrl The redirect url Microsoft calls back to
     * @param reqCode The unique code passed by Microsoft to obtain the refresh and access tokens
     * @return returns the name of the Microsoft user that is authorized
     */
    String authorize(String redirectUrl, String reqCode);

    /**
     * Returns true if the given Thing UID relates to this {@link MicrosoftToDoAccountHandler} instance.
     *
     * @param thingUID The Thing UID to check
     * @return true if it relates to the given Thing UID
     */
    boolean equalsThingUID(String thingUID);

    /**
     * Formats the Url to use to call Microsoft to authorize the application.
     *
     * @param redirectUri The uri Microsoft will redirect back to
     * @return the formatted url that should be used to call Microsoft Graph with
     */
    String formatAuthorizationUrl(String redirectUri);
}

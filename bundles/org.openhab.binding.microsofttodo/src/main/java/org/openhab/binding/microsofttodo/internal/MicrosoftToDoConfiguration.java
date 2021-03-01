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

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;

/**
 * The {@link MicrosoftToDoConfiguration} class contains fields mapping thing configuration parameters.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
public class MicrosoftToDoConfiguration {

    /**
     * Sample configuration parameter. Replace with your own.
     */
    private @Nullable String todoTaskListId;
    private @Nullable String clientId;

    public @Nullable String getTodoTaskListId() {
        return todoTaskListId;
    }

    public @Nullable String getClientId() {
        return clientId;
    }
}

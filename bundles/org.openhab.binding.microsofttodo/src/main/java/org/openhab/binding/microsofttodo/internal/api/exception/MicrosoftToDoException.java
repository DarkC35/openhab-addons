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
package org.openhab.binding.microsofttodo.internal.api.exception;

/**
 * Generic MicrosoftToDo exception class.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
public class MicrosoftToDoException extends RuntimeException {

    private static final long serialVersionUID = 4430428416887944560L;

    /**
     * Constructor.
     *
     * @param message MicrosoftToDo error message
     */
    public MicrosoftToDoException(String message) {
        super(message);
    }

    /**
     * Constructor.
     *
     * @param message MicrosoftToDo error message
     * @param cause Original cause of this exception
     */
    public MicrosoftToDoException(String message, Throwable cause) {
        super(message, cause);
    }
}

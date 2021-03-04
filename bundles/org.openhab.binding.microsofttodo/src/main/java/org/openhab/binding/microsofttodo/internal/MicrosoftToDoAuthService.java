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

import static org.openhab.binding.microsofttodo.internal.MicrosoftToDoBindingConstants.*;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Hashtable;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.microsofttodo.internal.api.exception.MicrosoftToDoException;
import org.osgi.framework.BundleContext;
import org.osgi.service.component.ComponentContext;
import org.osgi.service.component.annotations.Activate;
import org.osgi.service.component.annotations.Component;
import org.osgi.service.component.annotations.Deactivate;
import org.osgi.service.component.annotations.Reference;
import org.osgi.service.http.HttpService;
import org.osgi.service.http.NamespaceException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 * The {@link MicrosoftToDoAuthService} class to manage the servlets and bind authorization servlet to bridges.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@Component(service = MicrosoftToDoAuthService.class, configurationPid = "binding.microsofttodo.authService")
@NonNullByDefault
public class MicrosoftToDoAuthService {
    private static final String TEMPLATE_PATH = "templates/";
    private static final String TEMPLATE_ACCOUNT = TEMPLATE_PATH + "account.html";
    private static final String TEMPLATE_INDEX = TEMPLATE_PATH + "index.html";
    private static final String ERROR_UNKNOWN_BRIDGE = "Returned 'state' doesn't match any Bridges. Has the bridge been removed?";

    private final Logger logger = LoggerFactory.getLogger(MicrosoftToDoAuthService.class);

    private final List<MicrosoftToDoAccountHandler> handlers = new ArrayList<>();

    private @NonNullByDefault({}) HttpService httpService;
    private @NonNullByDefault({}) BundleContext bundleContext;

    @Activate
    protected void activate(ComponentContext componentContext, Map<String, Object> properties) {
        try {
            bundleContext = componentContext.getBundleContext();
            httpService.registerServlet(MICROSOFT_TODO_ALIAS, createServlet(), new Hashtable<>(),
                    httpService.createDefaultHttpContext());
            httpService.registerResources(MICROSOFT_TODO_ALIAS + MICROSOFT_TODO_IMG_ALIAS, "web", null);
        } catch (NamespaceException | ServletException | IOException e) {
            logger.warn("Error during MicrosoftToDo servlet startup", e);
        }
    }

    @Deactivate
    protected void deactivate(ComponentContext componentContext) {
        httpService.unregister(MICROSOFT_TODO_ALIAS);
        httpService.unregister(MICROSOFT_TODO_ALIAS + MICROSOFT_TODO_IMG_ALIAS);
    }

    private HttpServlet createServlet() throws IOException {
        return new MicrosoftToDoAuthServlet(this, readTemplate(TEMPLATE_INDEX), readTemplate(TEMPLATE_ACCOUNT));
    }

    private String readTemplate(String templateName) throws IOException {
        final URL index = bundleContext.getBundle().getEntry(templateName);

        if (index == null) {
            throw new FileNotFoundException(
                    String.format("Cannot find '{}' - failed to initialize MicrosoftToDo servlet", templateName));
        } else {
            try (InputStream inputStream = index.openStream()) {
                return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
            }
        }
    }

    /**
     * Call with Microsoft redirect uri returned State and Code values to get the refresh and access tokens and persist
     * these values
     *
     * @param servletBaseURL The servlet base, which will be the Microsoft redirect url
     * @param state The Microsoft returned state value
     * @param code The Microsoft returned code value
     * @return Returns the name of the Microsoft user that is authorized
     */
    public String authorize(String servletBaseURL, String state, String code) {
        final MicrosoftToDoAccountHandler listener = getMicrosoftToDoAuthListener(state);

        if (listener == null) {
            logger.debug(
                    "Microsoft Graph redirected with state '{}' but no matching bridge was found. Possible bridge has been removed.",
                    state);
            throw new MicrosoftToDoException(ERROR_UNKNOWN_BRIDGE);
        } else {
            return listener.authorize(servletBaseURL, code);
        }
    }

    /**
     * @param listener Adds the given handler
     */
    public void addMicrosoftToDoAccountHandler(MicrosoftToDoAccountHandler listener) {
        if (!handlers.contains(listener)) {
            handlers.add(listener);
        }
    }

    /**
     * @param handler Removes the given handler
     */
    public void removeMicrosoftToDoAccountHandler(MicrosoftToDoAccountHandler handler) {
        handlers.remove(handler);
    }

    /**
     * @return Returns all {@link MicrosoftToDoAccountHandler}s.
     */
    public List<MicrosoftToDoAccountHandler> getMicrosoftToDoAccountHandlers() {
        return handlers;
    }

    /**
     * Get the {@link MicrosoftToDoAccountHandler} that matches the given thing UID.
     *
     * @param thingUID UID of the thing to match the handler with
     * @return The {@link MicrosoftToDoAccountHandler} matching the thing UID or null
     */
    private @Nullable MicrosoftToDoAccountHandler getMicrosoftToDoAuthListener(String thingUID) {
        final Optional<MicrosoftToDoAccountHandler> maybeListener = handlers.stream()
                .filter(l -> l.equalsThingUID(thingUID)).findFirst();
        return maybeListener.isPresent() ? maybeListener.get() : null;
    }

    @Reference
    protected void setHttpService(HttpService httpService) {
        this.httpService = httpService;
    }

    protected void unsetHttpService(HttpService httpService) {
        this.httpService = null;
    }
}

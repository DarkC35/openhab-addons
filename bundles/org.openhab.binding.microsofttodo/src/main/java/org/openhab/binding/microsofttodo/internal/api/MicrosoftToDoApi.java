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

import java.util.ArrayList;
import java.util.List;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.core.auth.client.oauth2.OAuthClientService;

import com.microsoft.graph.auth.enums.NationalCloud;
import com.microsoft.graph.models.extensions.IGraphServiceClient;
import com.microsoft.graph.models.extensions.TodoTask;
import com.microsoft.graph.models.extensions.TodoTaskList;
import com.microsoft.graph.models.extensions.User;
import com.microsoft.graph.requests.extensions.GraphServiceClient;
import com.microsoft.graph.requests.extensions.ITodoTaskCollectionPage;
import com.microsoft.graph.requests.extensions.ITodoTaskCollectionRequestBuilder;
import com.microsoft.graph.requests.extensions.ITodoTaskListCollectionPage;
import com.microsoft.graph.requests.extensions.ITodoTaskListCollectionRequestBuilder;

/**
 * The {@link MicrosoftToDoApi} // TODO: add description
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
public class MicrosoftToDoApi {

    private final OAuthClientService oAuthService;
    private final MicrosoftToDoAuthProvider authProvider;
    private final IGraphServiceClient graphClient;

    public MicrosoftToDoApi(String clientId, List<String> scopes, String redirectUri,
            @Nullable NationalCloud nationalCloud, @Nullable String tenant, String clientSecret,
            OAuthClientService oAuthService) {
        this.oAuthService = oAuthService;
        authProvider = new MicrosoftToDoAuthProvider(clientId, scopes, redirectUri, nationalCloud, tenant, clientSecret,
                this.oAuthService);
        graphClient = GraphServiceClient.builder().authenticationProvider(authProvider).buildClient();
    }

    public User getCurrentUser() {
        return graphClient.me().buildRequest().get();
    }

    public List<TodoTaskList> getTodoTaskLists() {
        List<TodoTaskList> todoTaskLists = new ArrayList<>();
        ITodoTaskListCollectionPage page = graphClient.me().todo().lists().buildRequest().get();
        while (page != null) {
            todoTaskLists.addAll(page.getCurrentPage());
            ITodoTaskListCollectionRequestBuilder builder = page.getNextPage();
            if (builder == null) {
                break;
            }
            page = builder.buildRequest().get();
        }
        return todoTaskLists;
    }

    public List<TodoTask> getTodoTasks(String todoTaskListId) {
        List<TodoTask> todoTasks = new ArrayList<>();
        ITodoTaskCollectionPage page = graphClient.me().todo().lists(todoTaskListId).tasks().buildRequest().get();
        while (page != null) {
            todoTasks.addAll(page.getCurrentPage());
            ITodoTaskCollectionRequestBuilder builder = page.getNextPage();
            if (builder == null) {
                break;
            }
            page = builder.buildRequest().get();
        }
        return todoTasks;
    }
}

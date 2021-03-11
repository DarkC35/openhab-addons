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

import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ThingTypeUID;

/**
 * The {@link MicrosoftToDoBindingConstants} class defines common constants, which are
 * used across the whole binding.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
public class MicrosoftToDoBindingConstants {

    private static final String BINDING_ID = "microsofttodo";

    public static final String MICROSOFT_OAUTH_URL = "https://login.microsoftonline.com/common/oauth2/v2.0";
    public static final String MICROSOFT_AUTHORIZE_URL = MICROSOFT_OAUTH_URL + "/authorize";
    public static final String MICROSOFT_TOKEN_URL = MICROSOFT_OAUTH_URL + "/token";

    public static final List<String> MICROSOFT_SCOPES_LIST = Arrays.asList("offline_access",
            "https://graph.microsoft.com/user.read", "https://graph.microsoft.com/Tasks.Read");
    public static final String MICROSOFT_SCOPES = MICROSOFT_SCOPES_LIST.stream().collect(Collectors.joining(" "));

    // Authorization related Servlet and resources aliases.
    public static final String MICROSOFT_TODO_ALIAS = "/connectmicrosofttodo";
    public static final String MICROSOFT_TODO_IMG_ALIAS = "/img";

    // List of all Thing Type UIDs

    public static final ThingTypeUID THING_TYPE_ACCOUNT = new ThingTypeUID(BINDING_ID, "account");
    public static final ThingTypeUID THING_TYPE_TODO_TASK_LIST = new ThingTypeUID(BINDING_ID, "todoTaskList");

    // List of all Channel ids
    public static final String CHANNEL_ACCESSTOKEN = "accessToken";
    public static final String CHANNEL_TODO_TASK_LISTS = "todoTaskLists";

    public static final String CHANNEL_TODO_TASK_LIST_ID = "id";
    public static final String CHANNEL_TODO_TASK_LIST_DISPLAY_NAME = "displayName";
    public static final String CHANNEL_TODO_TASK_LIST_IS_OWNER = "isOwner";
    public static final String CHANNEL_TODO_TASK_LIST_IS_SHARED = "isShared";
    public static final String CHANNEL_TODO_TASK_LIST_WELLKNOWN_LIST_NAME = "wellknownListName";
    public static final String CHANNEL_TODO_TASK_LIST_NEXT_DUE_DATE_TIME = "nextDueDateTime";
    public static final String CHANNEL_TODO_TASK_LIST_TODO_TASKS = "todoTasks";
    public static final String CHANNEL_TODO_TASK_LIST_COMPLETED_TODO_TASKS = "completedTodoTasks";
    public static final String CHANNEL_TODO_TASK_LIST_OPEN_TODO_TASKS = "openTodoTasks";
    public static final String CHANNEL_TODO_TASK_LIST_TODO_TASKS_STRING = "todoTasksString";
    public static final String CHANNEL_TODO_TASK_LIST_COMPLETED_TODO_TASKS_STRING = "completedTodoTasksString";
    public static final String CHANNEL_TODO_TASK_LIST_OPEN_TODO_TASKS_STRING = "openTodoTasksString";
    public static final String CHANNEL_TODO_TASK_LIST_NO_TODO_TASKS = "numberOfTodoTasks";
    public static final String CHANNEL_TODO_TASK_LIST_NO_COMPLETED_TODO_TASKS = "numberOfCompletedTodoTasks";
    public static final String CHANNEL_TODO_TASK_LIST_NO_OPEN_TODO_TASKS = "numberOfOpenTodoTasks";

    // List of Bridge/Thing properties
    public static final String PROPERTY_MICROSOFT_USER = "user";
    public static final String PROPERTY_MICROSOFT_EMAIL = "email";
    public static final String PROPERTY_TODO_TASK_LIST_ID = "todoTaskListId";
}

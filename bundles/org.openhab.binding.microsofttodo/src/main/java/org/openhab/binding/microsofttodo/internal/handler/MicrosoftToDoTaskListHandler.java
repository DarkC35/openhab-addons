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

import java.util.List;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.eclipse.jdt.annotation.Nullable;
import org.openhab.binding.microsofttodo.internal.api.MicrosoftToDoApi;
import org.openhab.binding.microsofttodo.internal.config.MicrosoftToDoTaskListConfiguration;
import org.openhab.core.library.types.DateTimeType;
import org.openhab.core.library.types.DecimalType;
import org.openhab.core.library.types.OnOffType;
import org.openhab.core.library.types.StringType;
import org.openhab.core.thing.Bridge;
import org.openhab.core.thing.Channel;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.Thing;
import org.openhab.core.thing.binding.BaseThingHandler;
import org.openhab.core.types.Command;
import org.openhab.core.types.State;
import org.openhab.core.types.UnDefType;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.microsoft.graph.models.extensions.TodoTask;
import com.microsoft.graph.models.extensions.TodoTaskList;
import com.microsoft.graph.models.generated.TaskStatus;

/**
 * The {@link MicrosoftToDoTaskListHandler} is responsible for handling commands, which are sent to one of the channels.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@NonNullByDefault
public class MicrosoftToDoTaskListHandler extends BaseThingHandler {

    private final Logger logger = LoggerFactory.getLogger(MicrosoftToDoTaskListHandler.class);

    private @NonNullByDefault({}) MicrosoftToDoApi microsoftToDoApi;
    private @NonNullByDefault({}) MicrosoftToDoTaskListConfiguration configuration;

    private final MicrosoftToDoDynamicStateDescriptionProvider dynamicStateDescriptionProvider;
    private final ChannelUID todoTasksChannelUID;
    private final ChannelUID completedTodoTasksChannelUID;
    private final ChannelUID openTodoTasksChannelUID;

    /**
     * Constructor.
     *
     * @param thing Thing representing this device.
     */
    public MicrosoftToDoTaskListHandler(Thing thing,
            MicrosoftToDoDynamicStateDescriptionProvider dynamicStateDescriptionProvider) {
        super(thing);
        this.dynamicStateDescriptionProvider = dynamicStateDescriptionProvider;
        configuration = getConfigAs(MicrosoftToDoTaskListConfiguration.class);
        todoTasksChannelUID = new ChannelUID(thing.getUID(), CHANNEL_TODO_TASK_LIST_TODO_TASKS);
        completedTodoTasksChannelUID = new ChannelUID(thing.getUID(), CHANNEL_TODO_TASK_LIST_COMPLETED_TODO_TASKS);
        openTodoTasksChannelUID = new ChannelUID(thing.getUID(), CHANNEL_TODO_TASK_LIST_OPEN_TODO_TASKS);
    }

    @Override
    public void handleCommand(ChannelUID channelUID, Command command) {
    }

    @Override
    public void initialize() {
        final Bridge bridge = getBridge();
        final MicrosoftToDoBridgeHandler bridgeHandler = bridge == null ? null
                : (MicrosoftToDoBridgeHandler) bridge.getHandler();
        if (bridgeHandler != null) {
            microsoftToDoApi = bridgeHandler.getMicrosoftToDoApi();
        }
        // TODO: error handling
    }

    public boolean updateTodoTaskListStatus(TodoTaskList todoTaskList) {
        final String todoTaskListId = configuration.todoTaskListId;
        if (todoTaskListId.equals(todoTaskList.id)) {
            // TODO: improve logging
            logger.debug("updateTodoTaskListStatus of list id {}", todoTaskListId);
            List<TodoTask> todoTasks = microsoftToDoApi.getTodoTasks(todoTaskListId);

            updateChannelState(CHANNEL_TODO_TASK_LIST_ID, new StringType(todoTaskListId));
            updateChannelState(CHANNEL_TODO_TASK_LIST_DISPLAY_NAME, new StringType(todoTaskList.displayName));
            updateChannelState(CHANNEL_TODO_TASK_LIST_IS_OWNER, OnOffType.from(todoTaskList.isOwner));
            updateChannelState(CHANNEL_TODO_TASK_LIST_IS_SHARED, OnOffType.from(todoTaskList.isShared));
            updateChannelState(CHANNEL_TODO_TASK_LIST_WELLKNOWN_LIST_NAME,
                    new StringType(todoTaskList.wellknownListName.toString()));

            final List<TodoTask> completedTodoTasks = todoTasks.stream()
                    .filter(task -> task.status == TaskStatus.COMPLETED).collect(Collectors.toList());
            final List<TodoTask> openTodoTasks = todoTasks.stream().filter(task -> task.status != TaskStatus.COMPLETED)
                    .collect(Collectors.toList());
            final int noTodoTasks = todoTasks.size();
            final int noCompletedTodoTasks = completedTodoTasks.size();
            final int noOpenTodoTasks = openTodoTasks.size();
            final @Nullable DateTimeType nextDueDateTime = openTodoTasks.stream().map(task -> {
                @Nullable
                DateTimeType dueDateTime = task.dueDateTime == null ? null
                        : DateTimeType.valueOf(task.dueDateTime.dateTime);
                return dueDateTime;
            }).reduce(null, (prev, curr) -> {
                if (prev == null && curr != null) {
                    return curr;
                } else if (prev != null && curr != null) {
                    return curr.getZonedDateTime().compareTo(prev.getZonedDateTime()) < 0 ? curr : prev;
                } else {
                    return prev;
                }
            });
            final String delimiter = configuration.listSeparationDelimiter;
            final String todoTasksString = todoTasks.stream().map(task -> task.title)
                    .collect(Collectors.joining(delimiter));
            final String completedTodoTasksString = completedTodoTasks.stream().map(task -> task.title)
                    .collect(Collectors.joining(delimiter));
            final String openTodoTasksString = openTodoTasks.stream().map(task -> task.title)
                    .collect(Collectors.joining(delimiter));

            updateChannelState(CHANNEL_TODO_TASK_LIST_NEXT_DUE_DATE_TIME,
                    nextDueDateTime == null ? UnDefType.UNDEF : nextDueDateTime);
            updateChannelState(CHANNEL_TODO_TASK_LIST_NO_TODO_TASKS, new DecimalType(noTodoTasks));
            updateChannelState(CHANNEL_TODO_TASK_LIST_NO_COMPLETED_TODO_TASKS, new DecimalType(noCompletedTodoTasks));
            updateChannelState(CHANNEL_TODO_TASK_LIST_NO_OPEN_TODO_TASKS, new DecimalType(noOpenTodoTasks));
            updateChannelState(CHANNEL_TODO_TASK_LIST_TODO_TASKS_STRING, new StringType(todoTasksString));
            updateChannelState(CHANNEL_TODO_TASK_LIST_COMPLETED_TODO_TASKS_STRING,
                    new StringType(completedTodoTasksString));
            updateChannelState(CHANNEL_TODO_TASK_LIST_OPEN_TODO_TASKS_STRING, new StringType(openTodoTasksString));
            dynamicStateDescriptionProvider.setTodoTasks(todoTasksChannelUID, todoTasks);
            dynamicStateDescriptionProvider.setTodoTasks(completedTodoTasksChannelUID, completedTodoTasks);
            dynamicStateDescriptionProvider.setTodoTasks(openTodoTasksChannelUID, openTodoTasks);

            return true;
        }
        return false;
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
}

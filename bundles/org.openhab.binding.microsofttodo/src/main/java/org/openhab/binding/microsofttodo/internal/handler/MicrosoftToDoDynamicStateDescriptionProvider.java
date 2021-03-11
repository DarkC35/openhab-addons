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

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.eclipse.jdt.annotation.NonNullByDefault;
import org.openhab.core.thing.ChannelUID;
import org.openhab.core.thing.binding.BaseDynamicStateDescriptionProvider;
import org.openhab.core.thing.type.DynamicStateDescriptionProvider;
import org.openhab.core.types.StateOption;
import org.osgi.service.component.annotations.Component;

import com.microsoft.graph.models.extensions.TodoTask;
import com.microsoft.graph.models.extensions.TodoTaskList;

/**
 * Dynamically create the users list of todoTaskLists.
 *
 * @author Kevin Haunschmied - Initial contribution
 */
@Component(service = { DynamicStateDescriptionProvider.class, MicrosoftToDoDynamicStateDescriptionProvider.class })
@NonNullByDefault
public class MicrosoftToDoDynamicStateDescriptionProvider extends BaseDynamicStateDescriptionProvider {
    private final Map<ChannelUID, List<TodoTaskList>> todoTaskListsByChannel = new HashMap<>();
    private final Map<ChannelUID, List<TodoTask>> todoTasksByChannel = new HashMap<>();

    public void setTodoTaskLists(ChannelUID channelUID, List<TodoTaskList> newTodoTaskLists) {
        final List<TodoTaskList> todoTaskLists = todoTaskListsByChannel.get(channelUID);

        if (todoTaskLists == null || (newTodoTaskLists.size() != todoTaskLists.size())
                || !newTodoTaskLists.stream().allMatch(newList -> todoTaskLists.stream()
                        .anyMatch(list -> newList.id == list.id && newList.displayName.equals(list.displayName)))) {
            todoTaskListsByChannel.put(channelUID, newTodoTaskLists);
            setStateOptions(channelUID, newTodoTaskLists.stream()
                    .map(list -> new StateOption(list.id, list.displayName)).collect(Collectors.toList()));
        }
    }

    public void setTodoTasks(ChannelUID channelUID, List<TodoTask> newTodoTasks) {
        final List<TodoTask> todoTasks = todoTasksByChannel.get(channelUID);

        if (todoTasks == null || (newTodoTasks.size() != todoTasks.size()) || !newTodoTasks.stream().allMatch(
                newTask -> todoTasks.stream().anyMatch(task -> newTask.id == task.id && newTask.title == task.title))) {
            todoTasksByChannel.put(channelUID, newTodoTasks);
            setStateOptions(channelUID, newTodoTasks.stream().map(task -> new StateOption(task.id, task.title))
                    .collect(Collectors.toList()));
        }
    }
}

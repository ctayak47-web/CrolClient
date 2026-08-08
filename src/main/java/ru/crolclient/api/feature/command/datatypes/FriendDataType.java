package ru.crolclient.api.feature.command.datatypes;

import ru.crolclient.api.feature.command.exception.CommandException;
import ru.crolclient.api.feature.command.helpers.TabCompleteHelper;
import ru.crolclient.api.repository.friend.Friend;
import ru.crolclient.api.repository.friend.FriendRepository;

import java.util.List;
import java.util.stream.Stream;

public enum FriendDataType implements IDatatypeFor<Friend> {
    INSTANCE;

    @Override
    public Stream<String> tabComplete(IDatatypeContext datatypeContext) throws CommandException {
        Stream<String> friends = getFriends()
                .stream()
                .map(Friend::getName);

        String context = datatypeContext
                .getConsumer()
                .getString();

        return new TabCompleteHelper()
                .append(friends)
                .filterPrefix(context)
                .sortAlphabetically()
                .stream();
    }

    @Override
    public Friend get(IDatatypeContext datatypeContext) throws CommandException {
        String username = datatypeContext
                .getConsumer()
                .getString();

        return getFriends().stream()
                .filter(s -> s.getName().equalsIgnoreCase(username))
                .findFirst()
                .orElse(null);
    }

    private List<? extends Friend> getFriends() {
        return FriendRepository.getFriends();
    }
}

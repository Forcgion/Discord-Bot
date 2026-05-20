package org.example.listener;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.entities.Message;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

public class NumberGameListener extends ListenerAdapter {

    private final Map<String, Integer> nextNumber = new HashMap<>();

    @Override
    public void onMessageReceived(@NotNull MessageReceivedEvent event) {
        if (event.getAuthor().isBot()) return;
        if (!event.getChannel().getId().equals(ChannelConfig.COUNTING_CHANNEL_ID)) return;

        String content = event.getMessage().getContentRaw().trim();
        if (!content.matches("\\d+")) return;

        String channelId = event.getChannel().getId();
        int expected = nextNumber.getOrDefault(channelId, 1);
        int given;

        try {
            given = Integer.parseInt(content);
        } catch (NumberFormatException e) {
            return;
        }

        if (given == expected) {
            event.getMessage().addReaction(net.dv8tion.jda.api.entities.emoji.Emoji.fromUnicode("✅")).queue();
            nextNumber.put(channelId, expected + 1);
        } else {
            // Reset counter
            nextNumber.put(channelId, 1);

            TextChannel channel = event.getChannel().asTextChannel();
            final int wrongNumber = expected;

            // Bulk delete last 100 messages, then send the reset notice
            channel.getHistory().retrievePast(100).queue((List<Message> messages) -> {
                if (messages.isEmpty()) {
                    sendResetMessage(channel, wrongNumber);
                    return;
                }

                if (messages.size() == 1) {
                    messages.get(0).delete().queue(v -> sendResetMessage(channel, wrongNumber));
                } else {
                    channel.deleteMessages(messages).queue(v -> sendResetMessage(channel, wrongNumber));
                }
            });
        }
    }

    private void sendResetMessage(TextChannel channel, int expected) {
        channel.sendMessage(
                "❌ **See on vale number!** Järgmine õige number oli **" + expected + "**. Alustame otsast! \uD83D\uDD04"
        ).queue(botMsg -> botMsg.delete().queueAfter(5, TimeUnit.SECONDS));
    }
}
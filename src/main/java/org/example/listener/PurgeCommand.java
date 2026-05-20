package org.example.listener;

import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import org.jetbrains.annotations.NotNull;

import java.util.concurrent.TimeUnit;

public class PurgeCommand extends ListenerAdapter {

    private static final String STAFF_ROLE_ID = "";

    @Override
    public void onSlashCommandInteraction(@NotNull SlashCommandInteractionEvent event) {
        if (!event.getName().equals("purge")) return;

        boolean hasMeeskond = event.getMember().getRoles().stream()
                .anyMatch(role -> role.getId().equals(STAFF_ROLE_ID));

        if (!hasMeeskond) {
            event.reply("❌ See käsk on ainult **Meeskond** liikmetele!").setEphemeral(true).queue();
            return;
        }

        int amount = (int) event.getOption("kogus").getAsLong();

        if (amount < 1 || amount > 100) {
            event.reply("❌ Kogus peab olema 1 ja 100 vahel!").setEphemeral(true).queue();
            return;
        }

        event.deferReply(true).queue();

        event.getChannel().getHistory().retrievePast(amount).queue(messages -> {
            if (messages.isEmpty()) {
                event.getHook().sendMessage("❌ Ei leidnud sõnumeid!").queue();
                return;
            }

            if (messages.size() == 1) {
                messages.get(0).delete().queue();
            } else {
                event.getChannel().asTextChannel().deleteMessages(messages).queue();
            }

            event.getHook().sendMessage("✅ Kustutasin **" + messages.size() + "** sõnumit!")
                    .queue(reply -> reply.delete().queueAfter(5, TimeUnit.SECONDS, null, e -> {}));
        });
    }
}
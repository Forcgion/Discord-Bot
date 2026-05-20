package org.example.tickets;

import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Guild;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.Role;
import net.dv8tion.jda.api.entities.channel.concrete.Category;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.interactions.components.buttons.Button;
import org.example.storage.DefaultStorage;

import java.awt.Color;
import java.time.Instant;
import java.util.EnumSet;
import java.util.HashMap;
import java.util.Map;

public class TicketManager {

    private static final Map<Long, Long> openTickets = new HashMap<>();

    public static void openTicket(StringSelectInteractionEvent event, String type) {
        final StringSelectInteractionEvent finalEvent = event;
        event.deferReply(true).queue();

        Member member = event.getMember();
        long userId = member.getIdLong();
        Guild guild = event.getGuild();

        if (openTickets.containsKey(userId)) {
            finalEvent.getHook().sendMessage("❌ Teil on juba üks ticket olemas: <#" + openTickets.get(userId) + ">").queue();
            return;
        }

        String slug        = type.toLowerCase().replace(" ", "-");
        String channelName = slug + "-" + member.getUser().getName().toLowerCase();

        System.out.println("Channel name: " + channelName);
        System.out.println("Category ID: " + DefaultStorage.TICKET_CATEGORY_ID);

        Category category = guild.getCategoryById(DefaultStorage.TICKET_CATEGORY_ID);

        var action = (category != null)
                ? category.createTextChannel(channelName)
                : guild.createTextChannel(channelName);

        action.addPermissionOverride(
                guild.getPublicRole(),
                null,
                EnumSet.of(Permission.VIEW_CHANNEL)
        );

        action.addMemberPermissionOverride(
                userId,
                EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                null
        );

        for (String roleId : DefaultStorage.STAFF_ROLE_IDS) {
            Role role = guild.getRoleById(roleId);
            if (role != null) {
                action.addRolePermissionOverride(
                        role.getIdLong(),
                        EnumSet.of(Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY),
                        null
                );
            }
        }

        action.queue(channel -> {
            openTickets.put(userId, channel.getIdLong());
            System.out.println("✅ Kanal loodud: " + channel.getName());

            String emoji = switch (type) {
                case "Tehiline viga" -> "🔧";
                case "Report" -> "🚨";
                default -> "🎫";
            };

            Color color = switch (type) {
                case "Tehiline viga" -> Color.ORANGE;
                case "Report" -> Color.RED;
                default -> Color.BLUE;
            };

            EmbedBuilder embed = new EmbedBuilder()
                    .setTitle(emoji + " " + type + " Pilet")
                    .setDescription(
                            "Tere " + member.getAsMention() + "!\n\n" +
                                    "Palun kirjeldage oma mure meeskond tuleb peagi."
                    )
                    .addField("Type", emoji + " " + type, true)
                    .addField("Lahti tehtud", member.getUser().getAsTag(), true)
                    .setColor(color)
                    .setFooter("Üks pilet liikme kohta")
                    .setTimestamp(Instant.now());

            channel.sendMessageEmbeds(embed.build())
                    .setActionRow(
                            Button.danger("close_ticket",   "🔒 Sule"),
                            Button.primary("claim_ticket",  "🙋 Võtta"),
                            Button.secondary("add_user",    "➕ Lisa liige"),
                            Button.secondary("remove_user", "➖ Eemalda liige")
                    ).queue(
                            success -> {
                                System.out.println("✅ Pileti paneel saadetud!");
                                finalEvent.getHook().sendMessage("✅ Sinu **" + type + "** pilet: " + channel.getAsMention()).queue();
                            },
                            embedError -> {
                                System.err.println("❌ Error ei saatnud embedi: " + embedError.getMessage());
                                embedError.printStackTrace();
                                finalEvent.getHook().sendMessage("❌ Pilet tehtud aga paneel ei saadetud: " + channel.getAsMention()).queue();
                            }
                    );

        }, channelError -> {
            System.err.println("❌ ERROR ei loonud kanali: " + channelError.getMessage());
            channelError.printStackTrace();
            finalEvent.getHook().sendMessage("❌ Viga kanali loomisel: " + channelError.getMessage()).queue();
        });
    }

    public static void removeTicket(long channelId) {
        openTickets.values().remove(channelId);
    }

    public static boolean isStaff(Member member) {
        if (member.hasPermission(Permission.ADMINISTRATOR)) return true;
        return member.getRoles().stream()
                .anyMatch(r -> DefaultStorage.STAFF_ROLE_IDS.contains(r.getId()));
    }
}
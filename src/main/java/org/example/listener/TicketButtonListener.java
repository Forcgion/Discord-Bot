package org.example.listener;

import java.awt.Color;
import java.time.Instant;
import java.util.List;
import java.util.concurrent.TimeUnit;

import org.example.tickets.TicketManager;
import org.example.tickets.TranscriptService;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.entities.Member;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.entities.channel.concrete.TextChannel;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.events.message.MessageReceivedEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

public class TicketButtonListener extends ListenerAdapter {
    public void onButtonInteraction(ButtonInteractionEvent event) {
        switch (event.getComponentId()) {
            case "close_ticket" -> this.handleClose(event);
            case "claim_ticket" -> this.handleClaim(event);
            case "add_user" -> this.handleAddUser(event);
            case "remove_user" -> this.handleRemoveUser(event);
        }

    }

    private void handleClose(ButtonInteractionEvent event) {
        if (!TicketManager.isStaff(event.getMember())) {
            event.reply("❌ Ainult meeskond saab suleda pileteid.").setEphemeral(true).queue();
        } else {
            TextChannel channel = event.getChannel().asTextChannel();
            String closedBy = event.getMember().getUser().getAsTag();
            event.deferEdit().queue();
            TranscriptService.saveAndPost(channel, closedBy, (done) -> {
                EmbedBuilder embed = (new EmbedBuilder()).setTitle("\ud83d\udd12 Ticket Closed").setDescription("Check salvestab log kanalisse. Kustutab 10 seconid pärast.").setColor(Color.RED).setTimestamp(Instant.now());
                channel.sendMessageEmbeds(embed.build(), new MessageEmbed[0]).queue();
                TicketManager.removeTicket(channel.getIdLong());
                channel.delete().queueAfter(10L, TimeUnit.SECONDS);
            });
        }
    }

    private void handleClaim(ButtonInteractionEvent event) {
        if (!TicketManager.isStaff(event.getMember())) {
            event.reply("❌ Ainult meeskond saab võtta pileteid.").setEphemeral(true).queue();
        } else {
            event.getChannel().asTextChannel().sendMessage("✅ Pilet võetud " + event.getMember().getAsMention()).queue();
            event.editButton(event.getButton().asDisabled()).queue();
        }
    }

    private void handleAddUser(final ButtonInteractionEvent event) {
        if (!TicketManager.isStaff(event.getMember())) {
            event.reply("❌ Ainult meeskond saab lisada liikmeid.").setEphemeral(true).queue();
        } else {
            event.reply("\ud83d\udcdd Mention the user you want to add:").setEphemeral(true).queue();
            event.getJDA().addEventListener(new Object[]{new ListenerAdapter() {
                public void onMessageReceived(MessageReceivedEvent msgEvent) {
                    if (msgEvent.getChannel().getId().equals(event.getChannel().getId())) {
                        if (msgEvent.getAuthor().getId().equals(event.getUser().getId())) {
                            List<Member> mentioned = msgEvent.getMessage().getMentions().getMembers();
                            if (mentioned.isEmpty()) {
                                msgEvent.getChannel().sendMessage("❌ Mitte ühegi liiget pole pingitud, proovi uuesti.").queue();
                            } else {
                                Member target = (Member)mentioned.get(0);
                                event.getChannel().asTextChannel().upsertPermissionOverride(target).grant(new Permission[]{Permission.VIEW_CHANNEL, Permission.MESSAGE_SEND, Permission.MESSAGE_HISTORY}).queue();
                                msgEvent.getChannel().sendMessage("✅ Lisatud " + target.getAsMention()).queue();
                            }

                            event.getJDA().removeEventListener(new Object[]{this});
                        }
                    }
                }
            }});
        }
    }

    private void handleRemoveUser(final ButtonInteractionEvent event) {
        if (!TicketManager.isStaff(event.getMember())) {
            event.reply("❌ Ainult Meeskond saab eemaldada liikmeid.").setEphemeral(true).queue();
        } else {
            event.reply("\ud83d\udcdd Mention the user you want to remove:").setEphemeral(true).queue();
            event.getJDA().addEventListener(new Object[]{new ListenerAdapter() {
                public void onMessageReceived(MessageReceivedEvent msgEvent) {
                    if (msgEvent.getChannel().getId().equals(event.getChannel().getId())) {
                        if (msgEvent.getAuthor().getId().equals(event.getUser().getId())) {
                            List<Member> mentioned = msgEvent.getMessage().getMentions().getMembers();
                            if (!mentioned.isEmpty()) {
                                Member target = (Member)mentioned.get(0);
                                event.getChannel().asTextChannel().upsertPermissionOverride(target).deny(new Permission[]{Permission.VIEW_CHANNEL}).queue();
                                msgEvent.getChannel().sendMessage("✅ Eemaldatud " + target.getAsMention()).queue();
                            } else {
                                msgEvent.getChannel().sendMessage("❌ Mitte ühegi liiget pole pingitud, proovi uuesti.").queue();
                            }

                            event.getJDA().removeEventListener(new Object[]{this});
                        }
                    }
                }
            }});
        }
    }
}
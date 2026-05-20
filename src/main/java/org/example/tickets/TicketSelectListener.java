package org.example.tickets;

import net.dv8tion.jda.api.interactions.components.selections.StringSelectMenu;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.Permission;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.StringSelectInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;

import java.awt.Color;
import java.util.Objects;

public class TicketSelectListener extends ListenerAdapter {

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("setup-tickets")) return;

        String allowedUserId = "510577805168476171"; // ← paste your Discord user ID here

        if (!event.getUser().getId().equals(allowedUserId)) {
            event.reply("❌ Sul pole õigust seda käsku kasutada.").setEphemeral(true).queue();
            return;
        }

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle("🎫 Abi Piletid")
                .setDescription(
                        "Vajad abi? Vali kategooria ja allpool saad valida pileti.\n" +
                                "Meie meeskond püüab teid nii võimalikult kiiresti aidata kui võimalik."
                )
                .addField("🔧 Tehiline viga", "Viga serveris või on tehiline viga", false)
                .addField("🎫 Abi",      "Tavalised vead ja küsimused", false)
                .addField("🚨 Report",       "Reporti liiget", false)
                .setColor(Color.BLUE)
                .setFooter("Üks pilet liikme kohta");

        StringSelectMenu menu = StringSelectMenu.create("ticket_type")
                .setPlaceholder("📂 Vali pileti kategooria...")
                .addOption("🔧 Tehiline viga", "Tehiline viga", "Viga serveris või on tehiline viga")
                .addOption("🎫 Abi",          "Abi",          "Tavalised vead ja küsimused")
                .addOption("🚨 Report",        "Report",       "Reporti liiget")
                .build();

        event.getChannel().asTextChannel()
                .sendMessageEmbeds(embed.build())
                .setActionRow(menu)
                .queue();

        event.reply("✅ Pileti paneel saadetud!").setEphemeral(true).queue();
    }

    // ── Dropdown selected ───────────────────────────────────
    @Override
    public void onStringSelectInteraction(StringSelectInteractionEvent event) {
        if (!event.getComponentId().equals("ticket_type")) return;

        String selected = event.getValues().get(0);
        TicketManager.openTicket(event, selected);
    }
}
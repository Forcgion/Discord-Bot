package org.example.island;

import org.example.ChannelConfig;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.commands.OptionMapping;
import java.awt.Color;
import java.util.Map;

public class BuyBotCommand extends ListenerAdapter {

    public static final Map<String, long[]> BOTS = Map.of(
            "kartul",     new long[]{100,   20,  5},
            "porgand",    new long[]{250,   45,  10},
            "hernes",     new long[]{500,   90,  20},
            "vilj",       new long[]{1000,  180, 35},
            "suhkruroog", new long[]{2500,  400, 70},
            "bambus",     new long[]{5000,  900, 150}
    );

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("buybot")) return;

        if (!event.getChannel().getId().equals(ChannelConfig.ISLAND_CHANNEL_ID)) {
            event.reply("❌ Seda käsku saab kasutada ainult <#" + ChannelConfig.ISLAND_CHANNEL_ID + "> kanalil!")
                    .setEphemeral(true).queue();
            return;
        }

        String userId = event.getUser().getId();

        if (!IslandStorage.hasIsland(userId)) {
            event.reply("🏝️ Sinul pole veel saart! Kasuta **/buyisland** saare ostmiseks.")
                    .setEphemeral(true).queue();
            return;
        }

        OptionMapping option = event.getOption("type");
        if (option == null) {
            event.reply(buildShopList()).setEphemeral(true).queue();
            return;
        }

        String botType = option.getAsString().toLowerCase();

        if (!BOTS.containsKey(botType)) {
            event.reply("❌ Tundamatu boti tüüp. Vali siit: kartul, porgand, hernes, vilj, suhkruroog, bambus.")
                    .setEphemeral(true).queue();
            return;
        }

        if (IslandStorage.hasBot(userId, botType)) {
            event.reply("❌ Sul on juba **" + botType + "** bot!")
                    .setEphemeral(true).queue();
            return;
        }

        long price          = BOTS.get(botType)[0];
        long coinsPerCollect = BOTS.get(botType)[1];

        if (!IslandStorage.removeCoins(userId, price)) {
            event.reply("❌ Sul pole piisavalt saare münte! Vajad **" + price + " münti** " + botType + " boti jaoks.")
                    .setEphemeral(true).queue();
            return;
        }

        IslandStorage.addBot(userId, botType);

        EmbedBuilder embed = new EmbedBuilder()
                .setTitle(IslandCommand.botEmoji(botType) + " Bot Ostetud!")
                .setColor(Color.decode("#2ECC71"))
                .setDescription("Sa ostsid **" + IslandCommand.capitalize(botType) + " Boti** hinnaga **" + price + " münti**!")
                .addField("💰 Teenib /collect käsuga", coinsPerCollect + " münti", true)
                .addField("💳 Ülejäänud mündid", IslandStorage.getCoins(userId) + " münti", true)
                .setFooter("Kasuta /collect müntide kogumiseks!");

        event.replyEmbeds(embed.build()).queue();
    }

    private String buildShopList() {
        StringBuilder sb = new StringBuilder("**🤖 Boti pood** — kasuta `/buybot type:<nimi>` ostmiseks\n\n");
        for (var entry : BOTS.entrySet()) {
            String name = entry.getKey();
            long price  = entry.getValue()[0];
            long earn   = entry.getValue()[1];
            sb.append(IslandCommand.botEmoji(name))
                    .append(" **").append(IslandCommand.capitalize(name)).append("** — ")
                    .append(price).append(" münti | teenib ").append(earn).append(" münti /collect kohta\n");
        }
        return sb.toString();
    }
}
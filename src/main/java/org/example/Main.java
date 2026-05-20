package org.example;

import org.example.gamble.*;
import org.example.island.*;
import org.example.listener.HelpCommand;
import org.example.listener.EventHandler;
import org.example.listener.NumberGameListener;
import org.example.listener.PurgeCommand;
import org.example.listener.TicketButtonListener;
import org.example.listener.WelcomeListener;
import org.example.storage.DefaultStorage;
import org.example.tickets.TicketSelectListener;
import org.example.gamble.CoinFlipCommand;
import lombok.Generated;
import net.dv8tion.jda.api.JDA;
import net.dv8tion.jda.api.JDABuilder;
import net.dv8tion.jda.api.entities.Activity;
import net.dv8tion.jda.api.interactions.commands.OptionType;
import net.dv8tion.jda.api.interactions.commands.build.Commands;
import net.dv8tion.jda.api.interactions.commands.build.OptionData;
import net.dv8tion.jda.api.requests.GatewayIntent;
import net.dv8tion.jda.api.utils.ChunkingFilter;
import net.dv8tion.jda.api.utils.MemberCachePolicy;

public class Main {

    private static JDA jda;

    public static void main(String[] args) {
        try {
            Database.init();

            jda = JDABuilder.createDefault(DefaultStorage.token)
                    .setChunkingFilter(ChunkingFilter.ALL)
                    .setMemberCachePolicy(MemberCachePolicy.ALL)
                    .enableIntents(
                            GatewayIntent.GUILD_MEMBERS,
                            GatewayIntent.GUILD_MESSAGES,
                            GatewayIntent.MESSAGE_CONTENT
                    )
                    .setActivity(Activity.customStatus("Vaatan serveri üle!"))
                    .addEventListeners(
                            // ── Existing listeners ───────────────────────────
                            new EventHandler(),
                            new TicketButtonListener(),
                            new TicketSelectListener(),
                            new BlackjackCommand(),
                            new DailyCommand(),
                            new BalanceCommand(),
                            new NumberGameListener(),
                            new PurgeCommand(),
                            new WelcomeListener(),
                            new CoinFlipCommand(),
                            new HelpCommand(),

                            // ── Island listeners ─────────────────────────────
                            new IslandCommand(),
                            new BuyIslandCommand(),
                            new BuyBotCommand(),
                            new CollectCommand(),
                            new IsAfkCommand(),
                            new IsCoinflipCommand(),
                            new IsLevelCommand(),
                            new IsBalanceCommand(),
                            new BuyAfkCommand(),
                            new IsDailyCommand(),
                            new IslandDailyCommand(),

                            // ── Earning listeners for Island ────────────────────────────
                            new WorkCommand(),
                            new NewDailyCommand()
                    )
                    .build();

            jda.awaitReady();

            String guildId = "1503902438552240249";
            jda.getGuildById(guildId).updateCommands().addCommands(

                    // ── Existing commands ─────────────────────────────────────
                    Commands.slash("setup-tickets", "Send the ticket panel to this channel"),
                    Commands.slash("blackjack", "Mängi blackjacki")
                            .addOption(OptionType.INTEGER, "panus", "Panuse summa", true),
                    Commands.slash("daily",   "Võta oma päevane saldo"),
                    Commands.slash("balance", "Vaata oma saldot"),
                    Commands.slash("purge",   "Kustuta mitu sõnumit korraga")
                            .addOption(OptionType.INTEGER, "kogus", "Mitu sõnumit kustutada (max 100)", true),
                    Commands.slash("coinflip", "Viska münti ja vaata, kas tuleb pea või kiri")
                            .addOption(OptionType.INTEGER, "panus", "Kui palju paned peale", true)
                            .addOptions(
                                    new OptionData(OptionType.STRING, "valik", "Vali pea või kiri", true)
                                            .addChoice("🪙 Pea", "pea")
                                            .addChoice("🎴 Kiri", "kiri")
                            ),

                    // ── Earning commands ──────────────────────────────────────
                    Commands.slash("work",     "Tee tööd ja teeni 150 münti (kord tunnis)"),
                    Commands.slash("newdaily", "Võta päevane preemia 1-500 münti (kord päevas)"),

                    // ── Island commands ───────────────────────────────────────
                    Commands.slash("buyisland",   "Osta oma saar Discord müntidega"),
                    Commands.slash("island",      "Vaata oma saare statistikat"),
                    Commands.slash("isbalance",   "Vaata oma saare müntide saldot"),
                    Commands.slash("islevel",     "Vaata oma saare taset ja XP-d"),
                    Commands.slash("isdaily",     "Vaata oma saare päevast saldot"),
                    Commands.slash("islanddaily", "Võta saare päevane auhind (kord päevas)"),
                    Commands.slash("collect",     "Kogu saarele münte oma farmibottidelt"),
                    Commands.slash("isafk",       "Võta AFK preemia (kord iga 10 päeva tagant)"),

                    Commands.slash("buybot", "Osta farmibott oma saarele")
                            .addOptions(new OptionData(OptionType.STRING, "type",
                                    "Botti tüüp", true)
                                    .addChoice("🥔 Kartul",    "potato")
                                    .addChoice("🥕 Porgand",   "carrot")
                                    .addChoice("🫛 Punapeet",  "beetroot")
                                    .addChoice("🌾 Nisu",      "wheat")
                                    .addChoice("🎋 Suhkruroo", "sugarcane")
                                    .addChoice("🪵 Bambus",    "bamboo")),

                    Commands.slash("buyafk", "Osta 10 AFK päeva 250,000 müntide eest"),

                    Commands.slash("iscoinflip", "Pane saarele mündid mündiviskele")
                            .addOptions(new OptionData(OptionType.INTEGER, "amount",
                                    "Kui palju IC paned peale", true))
                            .addOptions(new OptionData(OptionType.STRING, "valik",
                                    "Vali kull või kiri", true)
                                    .addChoice("🪙 Kull", "pea")
                                    .addChoice("🎴 Kiri", "kiri")),

                    // ── Help command ───────────────────────────────────────
                    Commands.slash("help", "Vaata kõiki käske")

            ).queue();

            System.out.println("bot online");

        } catch (Exception e) {
            System.out.println("Error: " + e.getMessage());
        }
    }

    @Generated
    public static JDA getJda() {
        return jda;
    }
}
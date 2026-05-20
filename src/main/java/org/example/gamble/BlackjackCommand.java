package org.example.gamble;

import org.example.Database;
import org.example.util.ChannelGuard;
import net.dv8tion.jda.api.EmbedBuilder;
import net.dv8tion.jda.api.entities.MessageEmbed;
import net.dv8tion.jda.api.events.interaction.command.SlashCommandInteractionEvent;
import net.dv8tion.jda.api.events.interaction.component.ButtonInteractionEvent;
import net.dv8tion.jda.api.hooks.ListenerAdapter;
import net.dv8tion.jda.api.interactions.components.buttons.Button;

import java.awt.Color;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

public class BlackjackCommand extends ListenerAdapter {

    private static final String HIT_PREFIX   = "bj_hit:";
    private static final String STAND_PREFIX = "bj_stand:";

    private static final Map<String, String> CARD_EMOJIS = new HashMap<>();
    static {
        String[] values   = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};
        String[] spades   = {"🂡","🂢","🂣","🂤","🂥","🂦","🂧","🂨","🂩","🂪","🂫","🂭","🂮"};
        String[] hearts   = {"🂱","🂲","🂳","🂴","🂵","🂶","🂷","🂸","🂹","🂺","🂻","🂽","🂾"};
        String[] diamonds = {"🃁","🃂","🃃","🃄","🃅","🃆","🃇","🃈","🃉","🃊","🃋","🃍","🃎"};
        String[] clubs    = {"🃑","🃒","🃓","🃔","🃕","🃖","🃗","🃘","🃙","🃚","🃛","🃝","🃞"};

        for (int i = 0; i < values.length; i++) {
            CARD_EMOJIS.put(values[i] + "♠", spades[i]);
            CARD_EMOJIS.put(values[i] + "♥", hearts[i]);
            CARD_EMOJIS.put(values[i] + "♦", diamonds[i]);
            CARD_EMOJIS.put(values[i] + "♣", clubs[i]);
        }
    }

    private final Map<String, BlackjackGame> games = new ConcurrentHashMap<>();

    @Override
    public void onSlashCommandInteraction(SlashCommandInteractionEvent event) {
        if (!event.getName().equals("blackjack")) return;
        if (!ChannelGuard.requireBotChannel(event)) return;

        String userId = event.getUser().getId();

        if (games.containsKey(userId)) {
            event.reply("❌ Sul on juba aktiivne mäng! Kasuta nuppe et lõpetada.").setEphemeral(true).queue();
            return;
        }

        long bet = event.getOption("panus").getAsLong();
        if (bet <= 0) {
            event.reply("❌ Panus peab olema kõrgem kui 0.").setEphemeral(true).queue();
            return;
        }

        long balance = Database.getBalance(userId);
        if (bet > balance) {
            event.reply("❌ Sul pole piisavalt raha! Sinu saldo: **" + balance + "** €.").setEphemeral(true).queue();
            return;
        }

        Database.removeBalance(userId, bet);

        BlackjackGame game = new BlackjackGame(bet);
        games.put(userId, game);

        if (game.playerValue() == 21) {
            games.remove(userId);
            Database.addBalance(userId, bet * 2);
            event.replyEmbeds(buildEmbed(game, event.getUser().getName(),
                            "🎉 Blackjack! Sa võitsid **" + (bet * 2) + "** €!", Color.GREEN))
                    .queue();
            return;
        }

        event.replyEmbeds(buildEmbed(game, event.getUser().getName(),
                        "Sinu kord — Hit või Stand?", Color.YELLOW))
                .addActionRow(
                        Button.success(HIT_PREFIX + userId, "👊 Hit"),
                        Button.danger(STAND_PREFIX + userId, "✋ Stand")
                ).queue();
    }

    @Override
    public void onButtonInteraction(ButtonInteractionEvent event) {
        String componentId = event.getComponentId();

        boolean isHit   = componentId.startsWith(HIT_PREFIX);
        boolean isStand = componentId.startsWith(STAND_PREFIX);
        if (!isHit && !isStand) return;

        String gameOwner = componentId.substring(componentId.indexOf(':') + 1);

        if (!event.getUser().getId().equals(gameOwner)) {
            event.reply("❌ See pole sinu mäng!").setEphemeral(true).queue();
            return;
        }

        BlackjackGame game = games.get(gameOwner);
        if (game == null) {
            event.reply("❌ Ei leitud ühtegi aktiivset mängu.").setEphemeral(true).queue();
            return;
        }

        String username = event.getUser().getName();

        if (isHit) {
            game.playerHit();

            if (game.playerValue() > 21) {
                games.remove(gameOwner);
                event.editMessageEmbeds(buildEmbed(game, username,
                                "💥 Bust! Sa kaotasid **" + game.getBet() + "** €.", Color.RED))
                        .setComponents().queue();
                return;
            }

            if (game.playerValue() == 21) {
                resolveGame(event, game, gameOwner, username);
                return;
            }

            event.editMessageEmbeds(buildEmbed(game, username,
                            "Sinu kord — Hit või Stand?", Color.YELLOW))
                    .setActionRow(
                            Button.success(HIT_PREFIX + gameOwner, "👊 Hit"),
                            Button.danger(STAND_PREFIX + gameOwner, "✋ Stand")
                    ).queue();

        } else {
            resolveGame(event, game, gameOwner, username);
        }
    }

    private void resolveGame(ButtonInteractionEvent event, BlackjackGame game,
                             String gameOwner, String username) {
        game.dealerPlay();
        games.remove(gameOwner);

        int  pv  = game.playerValue();
        int  dv  = game.dealerValue();
        long bet = game.getBet();

        String result;
        Color  color;

        if (dv > 21 || pv > dv) {
            Database.addBalance(gameOwner, bet * 2);
            result = "🎉 Sa võitsid **" + (bet * 2) + "** €!";
            color  = Color.GREEN;
        } else if (pv == dv) {
            Database.addBalance(gameOwner, bet);
            result = "🤝 Push! Sa saad oma **" + bet + "** € tagasi.";
            color  = Color.YELLOW;
        } else {
            result = "😔 Diiler võitis. Sa kaotasid **" + bet + "** €.";
            color  = Color.RED;
        }

        event.editMessageEmbeds(buildEmbed(game, username, result, color))
                .setComponents().queue();
    }

    private String cardEmoji(String card) {
        return CARD_EMOJIS.getOrDefault(card, card);
    }

    private String formatHand(List<String> hand) {
        StringBuilder sb = new StringBuilder();
        for (String card : hand) sb.append(cardEmoji(card)).append(" ");
        return sb.toString().trim();
    }

    private MessageEmbed buildEmbed(BlackjackGame game, String username,
                                    String result, Color color) {
        String dealerDisplay;
        String dealerHeader;

        if (game.isDealing()) {
            dealerDisplay = cardEmoji(game.getDealerHand().get(0)) + " 🂠";
            dealerHeader  = "Diileri käsi (?)";
        } else {
            dealerDisplay = formatHand(game.getDealerHand());
            dealerHeader  = "Diileri käsi (" + game.dealerValue() + ")";
        }

        return new EmbedBuilder()
                .setTitle("🃏 Blackjack — " + username)
                .setColor(color)
                .setDescription(result)
                .addField("Sinu käsi (" + game.playerValue() + ")",
                        formatHand(game.getPlayerHand()), false)
                .addField(dealerHeader, dealerDisplay, false)
                .addField("Panus", "💰 " + game.getBet() + " €", true)
                .build();
    }

    public static class BlackjackGame {
        private final List<String> deck;
        private final List<String> playerHand = new ArrayList<>();
        private final List<String> dealerHand = new ArrayList<>();
        private final long         bet;
        private       boolean      dealing = true;

        private static final String[] SUITS  = {"♠", "♥", "♦", "♣"};
        private static final String[] VALUES = {"A","2","3","4","5","6","7","8","9","10","J","Q","K"};

        public BlackjackGame(long bet) {
            this.bet  = bet;
            this.deck = buildDeck();
            Collections.shuffle(deck);
            playerHand.add(draw());
            dealerHand.add(draw());
            playerHand.add(draw());
            dealerHand.add(draw());
        }

        private List<String> buildDeck() {
            List<String> d = new ArrayList<>();
            for (String suit : SUITS)
                for (String value : VALUES)
                    d.add(value + suit);
            return d;
        }

        private String draw() { return deck.remove(0); }

        public void playerHit()  { playerHand.add(draw()); }

        public void dealerPlay() {
            dealing = false;
            while (dealerValue() < 17) dealerHand.add(draw());
        }

        public int playerValue() { return handValue(playerHand); }
        public int dealerValue() { return handValue(dealerHand); }

        private int handValue(List<String> hand) {
            int total = 0, aces = 0;
            for (String card : hand) {
                String v = card.replaceAll("[♠♥♦♣]", "");
                switch (v) {
                    case "A"           -> { total += 11; aces++; }
                    case "J", "Q", "K" -> total += 10;
                    default            -> total += Integer.parseInt(v);
                }
            }
            while (total > 21 && aces > 0) { total -= 10; aces--; }
            return total;
        }

        public List<String> getPlayerHand() { return playerHand; }
        public List<String> getDealerHand() { return dealerHand; }
        public long         getBet()        { return bet; }
        public boolean      isDealing()     { return dealing; }
    }
}
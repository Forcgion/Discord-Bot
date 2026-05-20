package org.example;

import java.sql.Connection;
import java.sql.DriverManager;

public class Database {
    private static Connection connection;

    public static void init() {
        try {
            connection = DriverManager.getConnection("jdbc:sqlite:tickets.db");
            connection.setAutoCommit(true);

            try (var st = connection.createStatement()) {
                st.execute("PRAGMA journal_mode=WAL");
                st.execute("PRAGMA synchronous=NORMAL");

                st.execute("""
                    CREATE TABLE IF NOT EXISTS user_balance (
                        user_id    TEXT PRIMARY KEY,
                        balance    BIGINT DEFAULT 0,
                        last_daily BIGINT DEFAULT 0
                    )
                """);
                st.execute("""
                    CREATE TABLE IF NOT EXISTS tickets (
                        id              INTEGER PRIMARY KEY AUTOINCREMENT,
                        channel_id      TEXT NOT NULL,
                        channel_name    TEXT NOT NULL,
                        type            TEXT NOT NULL,
                        opened_by_id    TEXT NOT NULL,
                        opened_by_tag   TEXT NOT NULL,
                        claimed_by      TEXT DEFAULT 'Unclaimed',
                        message_count   INTEGER DEFAULT 0,
                        status          TEXT DEFAULT 'open',
                        opened_at       TEXT NOT NULL,
                        closed_at       TEXT DEFAULT NULL
                    )
                """);
            }

            System.out.println("Database connected.");
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static Connection get() {
        return connection;
    }

    public static long getBalance(String userId) {
        try (var ps = connection.prepareStatement(
                "SELECT balance FROM user_balance WHERE user_id = ?")) {
            ps.setString(1, userId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("balance") : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void addBalance(String userId, long amount) {
        try (var ps = connection.prepareStatement("""
                INSERT INTO user_balance (user_id, balance, last_daily)
                VALUES (?, ?, 0)
                ON CONFLICT(user_id) DO UPDATE SET balance = balance + excluded.balance
                """)) {
            ps.setString(1, userId);
            ps.setLong(2, amount);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public static void removeBalance(String userId, long amount) {
        addBalance(userId, -amount);
    }

    public static long getLastDaily(String userId) {
        try (var ps = connection.prepareStatement(
                "SELECT last_daily FROM user_balance WHERE user_id = ?")) {
            ps.setString(1, userId);
            try (var rs = ps.executeQuery()) {
                return rs.next() ? rs.getLong("last_daily") : 0;
            }
        } catch (Exception e) {
            e.printStackTrace();
            return 0;
        }
    }

    public static void setLastDaily(String userId, long timestamp) {
        try (var ps = connection.prepareStatement("""
                INSERT INTO user_balance (user_id, balance, last_daily)
                VALUES (?, 0, ?)
                ON CONFLICT(user_id) DO UPDATE SET last_daily = excluded.last_daily
                """)) {
            ps.setString(1, userId);
            ps.setLong(2, timestamp);
            ps.executeUpdate();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
}

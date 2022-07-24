package com.zpedroo.voltzauctions.mysql;

import com.zpedroo.multieconomy.api.CurrencyAPI;
import com.zpedroo.multieconomy.objects.general.Currency;
import com.zpedroo.voltzauctions.objects.Auction;
import com.zpedroo.voltzauctions.objects.Bid;
import com.zpedroo.voltzauctions.objects.PlayerData;
import com.zpedroo.voltzauctions.utils.encoder.Base64Encoder;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.math.BigInteger;
import java.sql.*;
import java.util.*;

public class DBManager {

    public void saveAuctionData(Auction auction) {
        executeUpdate("REPLACE INTO `" + DBConnection.AUCTIONS_TABLE + "` (`id`, `seller_uuid`, `serialized_item`, `expiration_date_in_millis`, `currency`," +
                " `minimum_bid`, `last_bid_author_uuid`, `last_bid_amount`, `last_bid_timestamp_in_millis`) VALUES " +
                "('" + auction.getId() + "', " +
                "'" + auction.getSellerUniqueId() + "', " +
                "'" + Base64Encoder.itemStackArrayToBase64(new ItemStack[] { auction.getItem() }) + "', " +
                "'" + auction.getExpirationDateInMillis() + "', " +
                "'" + auction.getCurrency().getFileName() + "', " +
                "'" + auction.getMinimumBid() + "', " +
                "'" + (auction.getLastBid() == null ? "" : auction.getLastBid().getAuthorUniqueId()) + "', " +
                "'" + (auction.getLastBid() == null ? 0 : auction.getLastBid().getBidAmount()) + "', " +
                "'" + (auction.getLastBid() == null ? 0 : auction.getLastBid().getTimestampInMillis()) + "');");
    }

    public void savePlayerData(PlayerData data) {
        String query = "REPLACE INTO `" + DBConnection.PLAYERS_TABLE + "` (`uuid`, `items_to_collect`) VALUES " +
                "('" + data.getUniqueId() + "', " +
                "'" + Base64Encoder.itemStackArrayToBase64(data.getItemsToCollect().toArray(new ItemStack[0])) + "');";
        executeUpdate(query);
    }

    public Map<Long, Auction> getAuctionsFromDatabase() {
        Map<Long, Auction> auctions = new HashMap<>(32);

        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.AUCTIONS_TABLE + "`;";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            while (result.next()) {
                long auctionId = result.getLong(1);
                UUID sellerUniqueId = UUID.fromString(result.getString(2));
                ItemStack item = Base64Encoder.itemStackArrayFromBase64(result.getString(3))[0];
                long expirationDateInMillis = result.getLong(4);
                Currency currency = CurrencyAPI.getCurrency(result.getString(5));
                BigInteger minimumBid = result.getBigDecimal(6).toBigInteger();

                Bid lastBid = null;
                if (!result.getString(7).isEmpty()) {
                    UUID lastBidAuthorUniqueId = UUID.fromString(result.getString(7));
                    BigInteger lastBidAmount = result.getBigDecimal(8).toBigInteger();
                    long lastBidTimestampInMillis = result.getLong(9);

                    lastBid = new Bid(lastBidAuthorUniqueId, lastBidAmount, lastBidTimestampInMillis);
                }

                auctions.put(auctionId, new Auction(auctionId, sellerUniqueId, item, currency, minimumBid, lastBid, expirationDateInMillis));
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return auctions;
    }

    public PlayerData getPlayerDataFromDatabase(Player player) {
        Connection connection = null;
        PreparedStatement preparedStatement = null;
        ResultSet result = null;
        String query = "SELECT * FROM `" + DBConnection.PLAYERS_TABLE + "` WHERE `uuid`='" + player.getUniqueId() + "';";

        try {
            connection = getConnection();
            preparedStatement = connection.prepareStatement(query);
            result = preparedStatement.executeQuery();

            if (result.next()) {
                List<ItemStack> itemsToCollect = new LinkedList<>(Arrays.asList(Base64Encoder.itemStackArrayFromBase64(result.getString(2))));

                return new PlayerData(player.getUniqueId(), itemsToCollect);
            }
        } catch (Exception ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, result, preparedStatement, null);
        }

        return new PlayerData(player.getUniqueId(), new ArrayList<>(4));
    }

    public void deleteAuction(long auctionId) {
        executeUpdate("DELETE FROM `" + DBConnection.AUCTIONS_TABLE + "` WHERE `id`='" + auctionId + "';");
    }

    private void executeUpdate(String query) {
        Connection connection = null;
        Statement statement = null;
        try {
            connection = getConnection();
            statement = connection.createStatement();
            statement.executeUpdate(query);
        } catch (SQLException ex) {
            ex.printStackTrace();
        } finally {
            closeConnection(connection, null, null, statement);
        }
    }

    private void closeConnection(Connection connection, ResultSet resultSet, PreparedStatement preparedStatement, Statement statement) {
        try {
            if (connection != null) connection.close();
            if (resultSet != null) resultSet.close();
            if (preparedStatement != null) preparedStatement.close();
            if (statement != null) statement.close();
        } catch (SQLException ex) {
            ex.printStackTrace();
        }
    }

    protected void createTable() {
        executeUpdate("CREATE TABLE IF NOT EXISTS `" + DBConnection.AUCTIONS_TABLE + "` (`id` BIGINT, `seller_uuid` VARCHAR(255)," +
                "`serialized_item` LONGTEXT, `expiration_date_in_millis` BIGINT, `currency` LONGTEXT, `minimum_bid` DECIMAL(100,0)," +
                "`last_bid_author_uuid` VARCHAR(255), `last_bid_amount` DECIMAL(100,0), `last_bid_timestamp_in_millis` BIGINT, PRIMARY KEY(`id`));");
        executeUpdate("CREATE TABLE IF NOT EXISTS `" + DBConnection.PLAYERS_TABLE + "` (`uuid` VARCHAR(255), `items_to_collect` LONGTEXT, PRIMARY KEY(`uuid`));");
    }

    private Connection getConnection() throws SQLException {
        return DBConnection.getInstance().getConnection();
    }
}
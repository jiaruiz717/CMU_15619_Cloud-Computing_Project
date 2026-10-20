package app;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.*;

public class Hashtag {

    public static double computeHashScore(Connection con, Set<String> popularTags, long user1, long user2) throws SQLException {
        if (user1 == user2) {
            return 1;
        }
        Map<String, Integer> user1Map = new HashMap<>();
        String query = "SELECT hashtag_text FROM user_hashtag WHERE user_id = " + user1;
        Statement stmt = con.createStatement();
        ResultSet rs = stmt.executeQuery(query);
        while (rs.next()) {
            String[] hashtags = rs.getString("hashtag_text").split("\u001F");
            for (String tag : hashtags) {
                String s = tag.toLowerCase(Locale.ENGLISH);
                if (!popularTags.contains(s)) {
                    user1Map.put(s, 1 + user1Map.getOrDefault(s, 0));
                }
            }
        }

        int match = 0;
        Set<String> user2Set = new HashSet<>();
        query = "SELECT hashtag_text FROM user_hashtag WHERE user_id = " + user2;
        stmt = con.createStatement();
        rs = stmt.executeQuery(query);
        while (rs.next()) {
            String[] hashtags = rs.getString("hashtag_text").split("\u001F");
            for (String tag : hashtags) {
                String s = tag.toLowerCase(Locale.ENGLISH);
                if (popularTags.contains(s)) {
                    continue;
                }
                if (!user1Map.containsKey(s)) {
                    continue;
                }
                if (user2Set.contains(s)) {
                    match++;
                } else {
                    match += user1Map.get(s) + 1;
                    user2Set.add(s);
                }
            }
        }
        return match > 10 ? 1 + Math.log(1 + match - 10) : 1;
    }
}

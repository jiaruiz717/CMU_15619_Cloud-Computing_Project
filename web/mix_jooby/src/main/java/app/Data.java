package app;

import org.json.JSONArray;
import org.json.JSONObject;

import java.sql.*;
import java.util.*;

public class Data {

    static class User implements Comparable<User> {
        long id;
        double interScore;
        double hashScore;
        double keyScore;
        double score;
        StringBuilder text = new StringBuilder();
        StringBuilder hashtags = new StringBuilder();
        long latestTid;
        String latestText;
        String screenName;
        String description;

        public User(long id, long latestT, String latestTx, String tex, String tags,
                    String screen, String desc, double iScore, double hScore) {
            this.id = id;
            latestTid = latestT;
            latestText = latestTx;
            screenName = screen;
            description = desc;
            interScore = iScore;
            hashScore = hScore;
            add(tex, tags);
        }

        public void add(String tex, String tags) {
            text.append(tex).append("\u001F");
            hashtags.append(tags).append("\u001F");
        }

        @Override
        public boolean equals(Object o) {
            if (o == this) {
                return true;
            }
            if (!(o instanceof User)) {
                return false;
            }
            User u = (User) o;
            return id == u.id;
        }

        @Override
        public int hashCode() {
            return Long.valueOf(id).hashCode();
        }

        @Override
        public int compareTo(User u) {
            long diff = id - u.id;
            if (diff > 0) {
                return 1;
            } else if (diff < 0) {
                return -1;
            } else {
                return 0;
            }
        }

        @Override
        public String toString() {
            return "User{" +
                    "id=" + id +
                    ", interScore=" + interScore +
                    ", hashScore=" + hashScore +
                    ", keyScore=" + keyScore +
                    ", score=" + score +
                    '}';
        }
    }

    static class SortByScore implements Comparator<User> {
        @Override
        public int compare(User u1, User u2) {
            double diff = u1.score - u2.score;
            if (diff > 0) {
                return -1;
            } else if (diff < 0) {
                return 1;
            } else { // break tie by user ID in descending numerical order
                long dif = u1.id - u2.id;
                if (dif > 0) {
                    return -1;
                } else if (dif < 0) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
    }

    private static final String mysqlHost = System.getenv("mysqlHost");
    private static final String mysqlUser = System.getenv("mysqlUser");
    private static final String mysqlPwd = System.getenv("mysqlPassword");
    private static final String url = "jdbc:mysql://" + mysqlHost + ":3306/twitter_db?useSSL=false&serverTimezone=UTC&allowPublicKeyRetrieval=true&characterEncoding=utf8&useUnicode=true";
    static Connection con;

    static {
        try {
            con = DriverManager.getConnection(url, mysqlUser, mysqlPwd);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public static String serve(long user_id, String type, String phrase, String hashtag) {
        Statement stmt = null;
        try {
            // Get all replied users
            Map<Long, User> replyUsers = new HashMap<>();
            Map<Long, User> retweetUsers = new HashMap<>();
            String query = String.format("SELECT * FROM contact_tweets WHERE user_id = %s", user_id);
            stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery(query);
            while (rs.next()) {
                int isReply = rs.getInt("is_reply");
                int isRetweet = rs.getInt("is_retweet");
                String contactTweets = rs.getString("contact_tweet");
                JSONArray jsonArray = new JSONArray(contactTweets);
                for (int i = 0; i < jsonArray.length(); i++) {
                    JSONObject contactUser = jsonArray.getJSONObject(i);
                    long contactUserId = contactUser.getLong("other_user_id");
                    String allTexts = contactUser.getString("all_texts");
                    long maxTid = contactUser.getLong("maxtid");
                    String maxTidText = contactUser.getString("maxtid_text");
                    String allHashTags = contactUser.getString("all_hashtags");
                    int hashTagCount;
                    try {
                        hashTagCount = contactUser.getInt("hashtag_count");
                    } catch (Exception e) {
                        hashTagCount = 0;
                    }
                    double hashTagScore;
                    if (hashTagCount <= 10 || contactUserId == user_id) {
                        hashTagScore = 1.0;
                    } else {
                        hashTagScore = 1 + Math.log(1 + hashTagCount - 10);
                    }
                    double interScore = contactUser.getDouble("interaction_score");
                    String description;
                    try {
                        description = contactUser.getString("other_description");
                    } catch (Exception e){
                        description = "";
                    }
                    String screenName;
                    try {
                        screenName = contactUser.getString("other_screen_name");
                    } catch (Exception e) {
                        screenName = "";
                    }
                    if (isReply == 1 && isRetweet == 0) {
                        replyUsers.put(contactUserId, new User(contactUserId, maxTid, maxTidText, allTexts,
                                allHashTags, screenName, description, interScore, hashTagScore));
                    } else {
                        retweetUsers.put(contactUserId, new User(contactUserId, maxTid, maxTidText, allTexts,
                                allHashTags, screenName, description, interScore, hashTagScore));
                    }
                }
            }

            // Invalid if the user has no contact tweets
            if (replyUsers.isEmpty() && retweetUsers.isEmpty()) {
                return "dwendwen,144331934093\nINVALID";
            }

            // Only consider tweets of specified type
            List<User> contactUsers;
            if (type.equals("reply")) {
                contactUsers = new ArrayList<>(replyUsers.values());
            } else if (type.equals("retweet")) {
                contactUsers = new ArrayList<>(retweetUsers.values());
            } else {
                contactUsers = new ArrayList<>();
                for (Long id : replyUsers.keySet()) {
                    User u = replyUsers.get(id);
                    if (retweetUsers.containsKey(id)) {
                        User u2 = retweetUsers.get(id);
                        u.text.append(u2.text);
                        u.hashtags.append(u2.hashtags);
                        if (u2.latestTid > u.latestTid) {
                            u.latestText = u2.latestText;
                        }
                    }
                    contactUsers.add(u);
                }
                for (Long id : retweetUsers.keySet()) {
                    User u = retweetUsers.get(id);
                    if (!replyUsers.containsKey(id)) {
                        contactUsers.add(u);
                    }
                }
            }

            // Get keyword score
            for (User u : contactUsers) {
                u.keyScore = 1 + Math.log(computeKeyTextMatches(hashtag, phrase, u) + 1);
                u.score = u.interScore * u.hashScore * u.keyScore;
            }

            contactUsers.sort(new SortByScore());

            // Generate output
            StringBuilder sb = new StringBuilder();
            sb.append("dwendwen,144331934093\n");
            for (int i = 0; i < contactUsers.size(); i++) {
                User u = contactUsers.get(i);
                if (u.score == 0) {
                    break;
                }
                sb.append(u.id).append("\t");
                sb.append(u.screenName).append("\t").append(u.description).append("\t");
                sb.append(u.latestText);
                if (i != contactUsers.size() - 1 && contactUsers.get(i + 1).score != 0) {
                    sb.append("\n");
                }
            }
            //System.out.println(sb);
            return sb.toString();
        } catch (SQLException e) {
            return e.getMessage();
        } finally {
            if (stmt != null) {
                try {
                    stmt.close();
                } catch (SQLException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    public static double computeKeyTextMatches(String targetHashtag, String targetPhrase, User contanctUser) throws SQLException {
        String[] contactUserHashtagList = contanctUser.hashtags.toString().split("\u001F");
        String[] contactUserTextList = contanctUser.text.toString().split("\u001F");
        int hashtagMatch = 0, textMatch = 0;
        for (String s : contactUserHashtagList) {
            if (targetHashtag.compareToIgnoreCase(s) == 0) {
                hashtagMatch++;
            }
        }
        for (String s : contactUserTextList) {
            textMatch += KMP.findOccurance(targetPhrase, s);
        }
        return hashtagMatch + textMatch;
    }

}
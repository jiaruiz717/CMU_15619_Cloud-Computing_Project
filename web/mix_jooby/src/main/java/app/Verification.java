package app;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;

import java.math.BigInteger;
import java.util.HashMap;
import java.util.Map;

public class Verification {


    public static boolean verify(JSONObject request) {
        if (!(request.length() == 3 && request.has("chain")
                && request.has("new_tx") && request.has("new_target"))) {
            return false;
        }
        JSONArray blocks = request.getJSONArray("chain");
        JSONArray newTxs = request.getJSONArray("new_tx");

        String prevHash = "00000000";
        BigInteger prevTime = BigInteger.ZERO;
        Map<String, BigInteger> balances = new HashMap<>();
        for (int i = 0; i < blocks.length(); i++) {
            JSONObject block = blocks.getJSONObject(i);
            if (!checkBlock(block, i, prevHash)) {
                return false;
            }
            JSONArray txs = block.getJSONArray("all_tx");
            for (int j = 0; j < txs.length(); j++) {
                JSONObject tx = txs.getJSONObject(j);
                if (!checkTx(tx, i, prevTime, balances, j == txs.length() - 1)) {
                    return false;
                }
                prevTime = new BigInteger(tx.getString("time"));
            }
            prevHash = block.getString("hash");
        }

        // Check new_tx
        for (int i = 0; i < newTxs.length(); i++) {
            JSONObject tx = newTxs.getJSONObject(i);
            if (tx.length() == 3) {
                if (!checkTxNew(tx, prevTime, balances))
                    return false;
            } else if (tx.length() == 7) {
                if (!checkTx(tx, 0, prevTime, balances, false))
                    return false;
            } else {
                return false;
            }
            prevTime = new BigInteger(tx.getString("time"));
        }

        return true;
    }

    // Check "hash", "id", "pow", "target" of a block
    public static boolean checkBlock(JSONObject block, int id, String prevHash) {
        if (!(block.has("all_tx") && block.has("hash") && block.has("id")
                && block.has("pow") && block.has("target"))) {
            return false;
        }

        if (block.getInt("id") != id) {
            return false;
        }

        StringBuilder sb = new StringBuilder();
        sb.append(id).append("|").append(prevHash);

        JSONArray transactions = block.getJSONArray("all_tx");
        for (int i = 0; i < transactions.length(); i++) {
            JSONObject tx = transactions.getJSONObject(i);
            sb.append("|").append(tx.getString("hash"));
        }

        String pow = block.getString("pow");
        String hash = DigestUtils.sha256Hex(
                DigestUtils.sha256Hex(sb.toString()) + pow).substring(0, 8);
        return hash.compareTo(block.getString("target")) < 0;
    }

    // Check "amt", "fee", "hash", "sig", "time" of a transaction
    public static boolean checkTx(JSONObject tx, int id, BigInteger prevTime,
                                  Map<String, BigInteger> balances, boolean last) {
        if (last) { // Reward transaction
            if (!(tx.length() == 4 && tx.has("amt") && tx.has("hash")
                    && tx.has("recv") && tx.has("time"))) {
                return false;
            }
            // "amt" should match
            BigInteger amt = new BigInteger("500000000").divide(BigInteger.TWO.pow(id / 2));
            if (!(new BigInteger(tx.get("amt").toString()).equals(amt))) {
                return false;
            }
        } else {
            if (!(tx.length() == 7 && tx.has("amt") && tx.has("hash")
                    && tx.has("fee") && tx.has("recv") && tx.has("send")
                    && tx.has("sig") && tx.has("time"))) {
                return false;
            }
            // "fee" should be non-negative
            BigInteger fee = new BigInteger(tx.get("fee").toString());
            if (fee.compareTo(BigInteger.ZERO) < 0) {
                return false;
            }
            // "amt" should be non-negative
            BigInteger amt = new BigInteger(tx.get("amt").toString());
            if (amt.compareTo(BigInteger.ZERO) < 0) {
                return false;
            }
            // Sender should have enough money to send
            String sender = tx.get("send").toString();
            BigInteger balance = balances.getOrDefault(sender, BigInteger.ZERO);
            BigInteger newBalance = balance.subtract(amt).subtract(fee);
            if (newBalance.compareTo(BigInteger.ZERO) < 0) {
                return false;
            }
            balances.put(sender, newBalance);
        }
        // "time" should increase
        if (new BigInteger(tx.getString("time")).compareTo(prevTime) <= 0) {
            return false;
        }
        // "hash" should match
        if (!BlockChain.getTxHash(tx).equals(tx.getString("hash"))) {
            return false;
        }
        // Receiver receives money
        String receiver = tx.get("recv").toString();
        BigInteger oldAmt = balances.getOrDefault(receiver, BigInteger.ZERO);
        balances.put(receiver, oldAmt.add(new BigInteger(tx.get("amt").toString())));
        return true;
    }

    // A new transaction created by the miner with only "time", "recv" and "amt
    public static boolean checkTxNew(JSONObject tx, BigInteger prevTime, Map<String, BigInteger> balances) {
        if (!(tx.has("amt") && tx.has("recv") && tx.has("time"))) {
            return false;
        }
        BigInteger amt = new BigInteger(tx.get("amt").toString());
        if (amt.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        if (new BigInteger(tx.getString("time")).compareTo(prevTime) <= 0) {
            return false;
        }

        // Sender is our team "1097844002039" when not specified
        String sender = "1097844002039";
        BigInteger balance = balances.getOrDefault(sender, BigInteger.ZERO);
        BigInteger newBalance = balance.subtract(amt);
        if (newBalance.compareTo(BigInteger.ZERO) < 0) {
            return false;
        }
        balances.put(sender, newBalance);

        String receiver = tx.get("recv").toString();
        BigInteger oldAmt = balances.getOrDefault(receiver, BigInteger.ZERO);
        balances.put(receiver, oldAmt.add(new BigInteger(tx.get("amt").toString())));
        return true;
    }
}

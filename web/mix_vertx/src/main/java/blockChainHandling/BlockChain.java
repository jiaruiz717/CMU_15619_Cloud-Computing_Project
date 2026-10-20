package blockChainHandling;

import org.apache.commons.codec.digest.DigestUtils;
import org.json.JSONArray;
import org.json.JSONObject;
import java.math.BigInteger;

public class BlockChain {
    private static final BigInteger TEAM_ACCOUNT = new BigInteger("1097844002039");

    public static JSONObject mineNewBlock(JSONObject request) {
        JSONObject newBlock = new JSONObject();

        // Fill in required fields for new transactions with only "time", "recv" and "amt"
        JSONArray newTx = request.getJSONArray("new_tx");
        for (int i = 0; i < newTx.length(); i++) {
            JSONObject tx = newTx.getJSONObject(i);
            if (tx.length() == 3) {
                newTx.put(i, fillMissingTxFields(tx));
            }
        }

        // Append the reward transaction
        newTx.put(getRewardBlock(request));
        newBlock.put("all_tx", newTx);

        // Append "hash", "id", "pow", "target" to the new block
        JSONArray blocks = request.getJSONArray("chain");
        newBlock.put("id", blocks.length());

        String target = request.getString("new_target");
        newBlock.put("target", target);

        String prefix = getBlockHashPrefix(request.getJSONArray("chain"), newBlock);
        for (long i = 0; i < Long.MAX_VALUE; i++) {
            String hash = DigestUtils.sha256Hex(prefix + i).substring(0, 8);
            if (hash.compareTo(target) < 0) {
                newBlock.put("hash", hash);
                newBlock.put("pow", String.valueOf(i));
                break;
            }
        }

        return newBlock;
    }

    public static JSONObject fillMissingTxFields(JSONObject json) {
        if (json.length() != 3) {
            return json;
        }
        json.put("send", TEAM_ACCOUNT);
        json.put("fee", 0);
        String hash = getTxHash(json);
        json.put("hash", hash);
        json.put("sig", getSig(hash));
        return json;
    }

    public static String getTxHash(JSONObject tx) {
        String[] keys = new String[] {"time", "send", "recv", "amt", "fee"};
        StringBuilder sb = new StringBuilder();
        for (String s : keys) {
            if (tx.has(s)) {
                sb.append(tx.get(s));
            }
            if (!s.equals(keys[keys.length - 1])) {
                sb.append("|");
            }
        }
        return DigestUtils.sha256Hex(sb.toString()).substring(0, 8);
    }

    public static String getBlockHashPrefix(JSONArray blocks, JSONObject newBlock) {
        StringBuilder sb = new StringBuilder();
        sb.append(newBlock.getInt("id")).append("|");

        String prevBlockHash = "";
        if (blocks.length() == 0) {
            prevBlockHash = "00000000";
        } else {
            prevBlockHash = blocks.getJSONObject(blocks.length() - 1).getString("hash");
        }
        sb.append(prevBlockHash);

        JSONArray transactions = newBlock.getJSONArray("all_tx");
        for (int i = 0; i < transactions.length(); i++) {
            JSONObject tx = transactions.getJSONObject(i);
            sb.append("|").append(tx.getString("hash"));
        }
        return DigestUtils.sha256Hex(sb.toString());
    }

    public static BigInteger getSig(String hash) {
        BigInteger n = new BigInteger("1561906343821");
        BigInteger d = new BigInteger("343710770439");
        return new BigInteger(hash, 16).modPow(d, n);
    }

    public static JSONObject getRewardBlock(JSONObject request) {
        JSONObject reward = new JSONObject();

        JSONArray blocks = request.getJSONArray("chain");
        reward.put("amt", new BigInteger("500000000").divide(new BigInteger("2").pow(blocks.length() / 2)));
        reward.put("recv", TEAM_ACCOUNT);

        JSONArray txes = blocks.getJSONObject(blocks.length() - 1).getJSONArray("all_tx");
        JSONObject lastTx = txes.getJSONObject(txes.length() - 1);
        reward.put("time", new BigInteger(lastTx.getString("time"))
                .add(new BigInteger("600000000000")).toString());
        reward.put("hash", getTxHash(reward));

        return reward;
    }
}

package blockChainHandling;

import org.json.JSONArray;
import org.json.JSONObject;

public class Service {
    public static String serve(String cc) {
        String requestStr = IOFormatProcess.decode(cc);
        JSONObject requestJson = new JSONObject(requestStr);

        if (!Verification.verify(requestJson)) {
            return "dwendwen,144331934093\nINVALID";
        }

        JSONArray blocks = requestJson.getJSONArray("chain");
        JSONObject resultJson = new JSONObject();
        blocks.put(BlockChain.mineNewBlock(requestJson));
        resultJson.put("chain", blocks);

        String resultStr = IOFormatProcess.encode(resultJson.toString());
        return "dwendwen,144331934093\n" + resultStr;
    }
}

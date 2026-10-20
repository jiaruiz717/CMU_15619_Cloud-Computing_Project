package app;

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

//    public static void main(String[] args) {
//        String cc = "eJyNlNtu2zAMht9F17kQTyKZVxmKwsc2QNsNbbANKPruYwzFh6RN54sglmTrE7-ffk_dY3N4Sfsf76l5ero__p3-vg7d77SH7GrMOWMm36Xm-Zj2kuu1S8fD85D2CcRQMHNerrRLj83bY0yiD22n5Onjbpd-_fwTQ0wxfejTPs-rMriNokNMHJvXh-F4Gotndhuot8ND7K9qYKUwO-0qZxFAABUoEmPjEFScMZbRAl1IuJQraFYVQRUsJ-i34aW_OnZl5KEZ2bmdqCaUYoSFBZkkn1FUlCBeS0JQUdTJoYIUcBS7pJAsTmLBCDJTbA9VIciapgu4CaLu6IKn3QwVvnEEMSRoJbZzXxz1o8JI3K0cnRXBomhospDbWhF8auhUFMjm8Tsb2kJOZQHO7laJTzcGl3qikOhEmcyx3C5MR0MuQLDYsRxqUMQ1642gFNfKQMx4BeAMEMeOpNAMsHVcAbzY0HVgazObddMeKF-YCSMltpHI7Kp7ouZAjLYyg9UMLmZyr9AC_k_zBE4gFXGc1UghBJPgt7OafLaiEA_oZV6hZI2skkdtyzdd05TWSku4eIGwyeDKDOo32kZUjGcSYdRoniuSSBRLTJbsi59N2CpIO7RsY88rECZDQgJ3mPv34hQTCRHyElWUeL9eGywRtuiQqCbybZS-NeJowlVUPvvSfpUVhE-z0hfWsaCusmI1K0ubZAynY9tsshKftLuPfyJIf8s=";
//        System.out.println(serve(cc));
//    }

}

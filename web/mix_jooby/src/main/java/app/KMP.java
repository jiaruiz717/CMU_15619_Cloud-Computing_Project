package app;

public class KMP {

    static int KMPSearch(String pattern, String txt){
        int patternLength = pattern.length();
        int txtLength = txt.length();

        int[] lps = new int[patternLength];
        computeLPSArray(pattern, patternLength, lps);

        int txtIdx = 0; // index for txt
        int patIdx = 0; // index for pattern
        int matchFount = 0;

        while (txtIdx < txtLength){
            if (pattern.charAt(patIdx) == txt.charAt(txtIdx)){
                patIdx++;
                txtIdx++;
            }

            if (patIdx == patternLength){
                matchFount++;
                patIdx = lps[patIdx-1]; // in case there are other matches
            } else if (txtIdx<txtLength && pattern.charAt(patIdx) != txt.charAt(txtIdx)){
                if (patIdx != 0){
                    patIdx = lps[patIdx-1];
                } else {
                    txtIdx = txtIdx + 1;
                }
            }
        }
        return matchFount;
    }

    static void computeLPSArray(String pattern, int patternLength, int[] lps){
        int len = 0;
        int idx = 1;
        lps[0] = 0; // longest prefix suffix

        while (idx < patternLength){
            if (pattern.charAt(idx) == pattern.charAt(len)){
                len++;
                lps[idx] = len;
                idx++;
            } else {
                if (len != 0){
                    len = lps[len-1];
                } else {
                    lps[idx] = len;
                    idx++;
                }
            }
        }
    }

    public static int findOccurance(String substring, String text){
        int lastIndex = 0;
        int count = 0;
        while (lastIndex != -1) {

            lastIndex = text.indexOf(substring, lastIndex);

            if (lastIndex != -1) {
                count++;
                lastIndex += 1;
            }
        }
        return count;
    }

    public static void main(String[] args) {
        String txt = "RT @sahmati1970: @RT_Erdogan PRÄ°M DOLDU YIL DOLDU EMEKLÄ°LÄ°K HAYAL OLDU #emeklilikteyaÅŸatakÄ±lanlar torba yasa ile emekli olmalÄ±. Adalet yeriâ€¦";
        String pattern = "olmalÄ±";
        System.out.println(KMPSearch(pattern, txt));
        System.out.println(findOccurance(pattern, txt));
    }
}
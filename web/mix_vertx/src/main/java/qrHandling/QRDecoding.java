package qrHandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QRDecoding {
    String decodeText;
    List<String> binaryList;
    List<String> hexList;
    int[][] image;
    int[][] rawMatrix;
    static final int length = (32 * 32 / 8) + 1;
    static final LogisticMap logisticMap = new LogisticMap(length);

    public QRDecoding(String decodeText) {
        this.decodeText = decodeText;
    }

    public String hex2Binary(String hex, int padding) {
        long temp = Long.parseLong(hex, 16);
        String binary = Long.toBinaryString(temp);
        if (padding == 32) {
            char[] full = new char[32];
            for (int i = 0; i < 32; i++) {
                if (i < 32 - binary.length()) {
                    full[i] = '0';
                } else {
                    full[i] = binary.charAt(i - 32 + binary.length());
                }
            }
            return new String(full);
        }
        return binary;
    }

    public List<String> splitBy0x() {
        List<String> result = new ArrayList<>();
        String[] temp = this.decodeText.split("0x");
        for (int i = 0; i < temp.length; i++) {
            // if it's not empty, add it to list
            if (!temp[i].equals("")) {
                result.add(temp[i]);
            }
        }
        return result;
    }

    public List<String> hexList2BinaryList() {
        List<String> binaryList = new ArrayList<>();
        for (String hex : this.hexList) {
            binaryList.add(this.hex2Binary(hex, 32));
        }
        return binaryList;
    }

    public String xor(String binarySeq, String lmSeq) {
      StringBuilder resultSeq = new StringBuilder();
//        for (int i = binarySeq.length() - 1; i > -1; i--) {
//            String temp = String.valueOf((binarySeq.charAt(i) - '0') ^ (lmSeq.charAt(8 - i - 1) - '0'));
//            resultSeq.append(temp);
//        }
//        return resultSeq.reverse().toString();
      int binarySeq_length = binarySeq.length();
      String temp;
      for (int i = 0; i <binarySeq_length; i++) {
        temp = String.valueOf((binarySeq.charAt(i) - '0') ^ (lmSeq.charAt(8 - i - 1) - '0'));
        resultSeq.append(temp);
      }
      return resultSeq.toString();
    }

    public int[][] xorWithLm() {
        int lmIdx = 0;
        List<String> lmBinary8BitList = this.logisticMap.getBinaryMatrix();
        StringBuilder result = new StringBuilder();
        for (String binary32Bit : this.binaryList) {
            for (int i = 0; i < binary32Bit.length(); i += 8) {
                String binary8Bit = binary32Bit.substring(i, i + 8);
                // System.out.println(binary8Bit);
                String xoredBinaryString = this.xor(binary8Bit, lmBinary8BitList.get(lmIdx));
                // System.out.println(xoredBinaryString);
                result.append(xoredBinaryString);
                lmIdx++;
            }
        }
        int[][] matrix = new int[32][32];
        for (int[] ints : matrix) {
            Arrays.fill(ints, 2);
        }
        int k = 0;
        for (int i = 0; i < 32; i++) {
            for (int j = 0; j < 32; j++) {
                matrix[i][j] = result.toString().charAt(k++) - '0';
            }
        }
        return matrix;
    }

    public boolean isCorner(int x, int y) {
        int[][] corner = new int[][] { { 1, 1, 1, 1, 1, 1, 1 },
                { 1, 0, 0, 0, 0, 0, 1 },
                { 1, 0, 1, 1, 1, 0, 1 },
                { 1, 0, 1, 1, 1, 0, 1 },
                { 1, 0, 1, 1, 1, 0, 1 },
                { 1, 0, 0, 0, 0, 0, 1 },
                { 1, 1, 1, 1, 1, 1, 1 } };
        for (int i = 0; i < 7; i++) {
            for (int j = 0; j < 7; j++) {
                if (this.rawMatrix[x + i][y + j] != corner[i][j]) {
                    return false;
                }
            }
        }
        return true;
    }

    public String getText() {
        // get the length of the text
        int size = this.image.length;
        StringBuilder lengthBi = new StringBuilder();
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 2; j++) {
                lengthBi.append(this.image[size - 1 - i][size - 1 - j]);
            }
        }
        long length = Long.parseLong(lengthBi.toString(), 2);

        // get the original text
        StringBuilder result = new StringBuilder();
        int x = size - 5;
        int y = size - 1;
        boolean up = true;
        boolean left = true;
        for (int i = 0; i < 2 * length; i++) {
            StringBuilder ch = new StringBuilder();
            int j = 0;
            while (j < 8) {
                ch.append(this.image[x][y]);
                j++;
                if (up && left) {
                    if (x == 0 || (x == 8 && (y <= 8 || y >= size - 8))) {
                        if (y == 8) {
                            ch.append(this.image[x][y - 1]);
                            ch.append(this.image[x][y - 3]);
                            y -= 4;
                        } else {
                            ch.append(this.image[x][y - 1]);
                            ch.append(this.image[x][y - 2]);
                            y -= 3;
                        }
                        j += 2;
                        up = false;
                        left = false;
                    } else {
                        y--;
                        left = false;
                    }
                } else if (up && !left) {
                    if (size == 25 && x == 21 && (y == 15 || y == 19)) {
                        x -= 5;
                    }
                    if (x == 7) {
                        x--;
                    }
                    x--;
                    y++;
                    left = true;
                } else if (!up && !left) {
                    if (x == size - 1 && y == 9) {
                        x -= 8;
                        y--;
                        left = true;
                        up = true;
                        continue;
                    }
                    if (size == 25 && x == 15 && y == 17) {
                        x += 5;
                    }
                    if (x == 5) {
                        x++;
                    }
                    x++;
                    y++;
                    left = true;
                } else if (!up && left) {
                    if ((x == size - 1 && y >= 11) || (x == size - 9 && y == 5)) {
                        ch.append(this.image[x][y - 1]);
                        ch.append(this.image[x][y - 2]);
                        j += 2;
                        y -= 3;
                        up = true;
                        left = false;
                    } else {
                        y--;
                        left = false;
                    }
                }
            }
            if (i % 2 == 0) {
                long temp = Long.parseLong(ch.toString(), 2);
                result.append((char) temp);
            }
        }
        return result.toString();
    }

    public void locateImage() {
        List<List<Integer>> corners = new ArrayList<>();
        for (int i = 0; i < 32 - 6; i++) {
            for (int j = 0; j < 32 - 6; j++) {
                if (this.isCorner(i, j)) {
                    corners.add(new ArrayList<>(Arrays.asList(i, j)));
                }
            }
        }
//        System.out.println(corners);
        // four rotation cases
        if ((corners.get(0).get(0) == corners.get(1).get(0)) && (corners.get(0).get(1) == corners.get(2).get(1))) {
            // no need to rotate
            int size = corners.get(1).get(1) - corners.get(0).get(1) + 7;
            this.image = new int[size][size];
            for (int[] ints : this.image) {
                Arrays.fill(ints, 2);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    this.image[i][j] = this.rawMatrix[corners.get(0).get(0) + i][corners.get(0).get(1) + j];
                }
            }
        } else if ((corners.get(0).get(1) == corners.get(1).get(1))
                && (corners.get(1).get(0) == corners.get(2).get(0))) {
            // need to rotate 90 degrees clockwise
            int size = corners.get(1).get(0) - corners.get(0).get(0) + 7;
            this.image = new int[size][size];
            for (int[] ints : this.image) {
                Arrays.fill(ints, 2);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    this.image[j][size - 1 - i] = this.rawMatrix[corners.get(0).get(0) + i][corners.get(0).get(1) + j];
                }
            }
        } else if ((corners.get(0).get(1) == corners.get(2).get(1))
                && (corners.get(1).get(0) == corners.get(2).get(0))) {
            // need to rotate 180 degrees clockwise
            int size = corners.get(2).get(0) - corners.get(0).get(0) + 7;
            this.image = new int[size][size];
            for (int[] ints : this.image) {
                Arrays.fill(ints, 2);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    this.image[size - 1 - i][size - 1
                            - j] = this.rawMatrix[corners.get(0).get(0) + i][corners.get(1).get(1) + j];
                }
            }

        } else if ((corners.get(0).get(0) == corners.get(1).get(0))
                && (corners.get(1).get(1) == corners.get(2).get(1))) {
            // need to rotate 270 degrees clockwise
            int size = corners.get(1).get(1) - corners.get(0).get(1) + 7;
            this.image = new int[size][size];
            for (int[] ints : this.image) {
                Arrays.fill(ints, 2);
            }
            for (int i = 0; i < size; i++) {
                for (int j = 0; j < size; j++) {
                    this.image[size - 1 - j][i] = this.rawMatrix[corners.get(0).get(0) + i][corners.get(0).get(1) + j];
                }
            }

        }
    }

    public String processInputHex() {
        if (this.decodeText == null || this.decodeText.split("0x").length != 33) {
            return "";
        }
        // 1. split by '0x'
        this.hexList = this.splitBy0x();
        // 2. convert hex to binary with padding to 32 bit
        this.binaryList = this.hexList2BinaryList();
        // 3. xor binary string with logistic map
        this.rawMatrix = this.xorWithLm();
        // 4. correctly locate the hidden QR image
        this.locateImage();
        // 5. get the input text
        return this.getText();
    }

    // public static void main(String[] args) {
    // String plainText =
    // "0x98dad7920x8ba9ef380x6753fe7e0x7e0470c10xc18486f00x8a1d61590xfaaf2910xa4124a3a0x4fc2059b0x217fc4600x185b564a0x3a1d72470xd05e7f9a0x70ae93a40x379e694a0x7a095fb20x74696d430x4e5f92c50xc14041800xb01d05e70xaad286700xac64c7670xcf0b48c20xa8bab3030x7f9070050x7aa1649c0xa38f11be0x7f8020080x3cedb3f90x77bcdde30xb5bb2ebf0xe03826cf";
    // int length = (32*32/8)+1;
    // QRDecoding qrDecoding = new QRDecoding(new LogisticMap(length), plainText);
    // System.out.println(qrDecoding.processInputHex());
    // }
}

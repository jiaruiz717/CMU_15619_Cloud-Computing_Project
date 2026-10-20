package qrHandling;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class QREncoding {
    private static final int MAX_LENGTH = 25*25;
    private static final String PADDING_STRING = "1110110000010001";
    Matrix matrix_instance;
    public int[][] matrix;
    int version;
    String binaryString;
    static final LogisticMap logisticMap = new LogisticMap(79);

    public String toFullBinaryString(String element){
        char[] full = new char[8];
        Arrays.fill(full, '0');
        int j = 0;
        for(int i = 8-element.length(); i < 8;i++){
            full[i] = element.charAt(j++);
        }
        return new String(full);
    }

    public String zigzagMatrix(Matrix matrix, String plainText){
        this.matrix = matrix.matrix;
        this.matrix_instance = matrix;
        this.version = matrix.version;
        this.binaryString = this.plain2BinaryString(plainText);
        // fill out every empty cell (i.e. cell with value 2) to the RIGHT of timing pattern
        int value1 = this.fillPairsOfColumns(7,this.version-1,1,1);
        // fill out every empty cell (i.e. cell with value 2) to the LEFT of timing pattern
        int value2 = this.fillPairsOfColumns(0,5,0,value1);
        String matrixBinaryString = this.concatenate_matrix();
        return matrixBinaryString;
    }

    public int fillPairsOfColumns(int startCol, int endCol, int goUp, int startValue){
        int value = startValue;
        for(int col = endCol; col>startCol-1; col-=2){
            if(goUp == 1){
                this.fillColumn(value-1, col, this.version-1, 0, true);
                value = this.fillColumn(value, col-1, this.version-1, 0, true);
            }else {
                this.fillColumn(value-1, col, 0, this.version-1, false);
                value = this.fillColumn(value, col-1, 0, this.version-1, false);
            }
            goUp = (1+goUp) % 2;
        }
        return value;
    }

    public int fillColumn(int startValue, int col, int startRow, int endRow, boolean upDirection){
        int value = startValue;
        if(upDirection){
            for(int i = startRow; i>endRow-1; i--){
                if(this.matrix[i][col] != 2){
                    // boundary
                    continue;
                }
                this.matrix[i][col] = this.binaryString.charAt(value)-'0';
                value += 2;
            }
        }else {
            for(int i = startRow; i<endRow+1; i++){
                if(this.matrix[i][col] != 2){
                    // boundary
                    continue;
                }
                this.matrix[i][col] = this.binaryString.charAt(value)-'0';
                value += 2;
            }
        }
        return value;
    }

    public void printMatrix(){
        for (int[] ints : this.matrix) {
            System.out.println(Arrays.toString(ints));
        }
    }

    public String xorSequence(String sequence){
        int length = sequence.length();
        if(length == 0){
            return null;
        }
        int result = sequence.charAt(0)-'0';
        for(int i = 1; i<length; i++){
            result = result ^ (sequence.charAt(i) - '0');
        }
        return "0b"+Integer.toBinaryString(result);
    }

    public String plain2BinaryString(String plain){
        int length = plain.length();
        List<String> encodingList = new ArrayList<>();
        String byte_length = "0b"+Integer.toBinaryString(length);
        encodingList.add(byte_length);

        for(int i = 0; i<length;i++){
            String byteOrigin = "0b"+Integer.toBinaryString(plain.charAt(i));
            encodingList.add(byteOrigin);
            String byteError = xorSequence(Integer.toBinaryString(plain.charAt(i)));
            encodingList.add(byteError);
        }
        return this.encodingList2LongString(encodingList);
    }

    public String encodingList2LongString(List<String> encodingList){
        StringBuilder encodingString = new StringBuilder();
        int length = encodingList.size();
        for (String s : encodingList) {
            encodingString.append(this.toFullBinaryString(s.substring(2)));
        }
        int remain_length = MAX_LENGTH-encodingString.length();
        while(remain_length >= PADDING_STRING.length()){
            encodingString.append(PADDING_STRING);
            remain_length -= PADDING_STRING.length();
        }
        return encodingString.toString();
    }

    public String concatenate_matrix(){
        StringBuilder matrixString = new StringBuilder();
        for(int i = 0;i < this.matrix.length;i++){
            for(int j = 0; j< this.matrix[0].length;j++){
                char c = (char)(this.matrix[i][j]+'0');
                matrixString.append(c);
            }
        }
        return matrixString.toString();
    }

    public String binary2Hex(String binary){
        long target = Long.parseLong(binary,2);
        return Long.toHexString(target);
    }

    public String xorWithLogisticMap(String matrixBinaryString){
        List<String> qrBinary = new ArrayList<>();
        for(int i = 0;i<matrixBinaryString.length();i+=8){
            if(matrixBinaryString.length()-i<8){
                qrBinary.add(matrixBinaryString.substring(i));
            }else {
                qrBinary.add(matrixBinaryString.substring(i,i+8));
            }
        }
        List<String> logisticMatrix = logisticMap.getBinaryMatrix();
        StringBuilder binaryResult = new StringBuilder();
        StringBuilder xorBinaryString;
        String temp,qr8bit, lm8bit;
        for(int i = 0; i<qrBinary.size(); i++){
            qr8bit = qrBinary.get(i);
            lm8bit = logisticMatrix.get(i);
            xorBinaryString = new StringBuilder();
//            for(int j = qr8bit.length()-1; j>-1; j--){
//                String temp = String.valueOf((qr8bit.charAt(j) - '0') ^ (lm8bit.charAt(8-j-1) - '0'));
//                xorBinaryString.append(temp);
//            }
//            binaryResult.append(xorBinaryString.reverse());
          int qr8bit_length = qr8bit.length();
          for (int j=0; j<qr8bit_length; j++){
            temp = String.valueOf( (qr8bit.charAt(j)-'0') ^ (lm8bit.charAt(8-j-1)-'0') );
            xorBinaryString.append(temp);
          }
          binaryResult.append(xorBinaryString);
        }
        return this.splitStringTo32BitHex(binaryResult.toString());
    }

    public String splitStringTo32BitHex(String binaryString){
        StringBuilder hexString = new StringBuilder();
        for(int i = 0;i<binaryString.length();i+=32){
            String subString = null;
            if(binaryString.length()-i<32){
                subString = binaryString.substring(i);
            }else {
                subString = binaryString.substring(i,i+32);
            }
            String hex = binary2Hex(subString);
            hexString.append("0x");
            hexString.append(hex);
        }
        return hexString.toString();
    }

    public String getEncoding(String plainText){
        Matrix matrix;
        if(plainText == null || plainText.length() == 0){
            System.err.println("Input plainText should not be null or empty");
            return null;
        }else if(plainText.length() <= 13){
            matrix = new Matrix(21);
        }else if(plainText.length() <= 22){
            matrix = new Matrix(25);
        }else {
            System.err.println("The length of input plain text should not exceed 22");
            return null;
        }
        String matrixBinaryString= this.zigzagMatrix(matrix, plainText);
        return this.xorWithLogisticMap(matrixBinaryString);
    }
}

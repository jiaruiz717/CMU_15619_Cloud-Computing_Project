package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class LogisticMap {
    public int length;
    public int[] logistic_array;
    private static final double x_0 = 0.1;
    private static final double r = 4;
    public List<String> matrix;

    public LogisticMap(int length){
        this.length = length;
        this.logistic_array = new int[length];
        Arrays.fill(logistic_array, (int)(Math.floor(x_0 * 255)));
        calculate_lm(x_0, 1);
        this.matrix = convertArrayToMatrix();
    }

    public Double calculate_lm(double prev_x, int cur_index){
        if(cur_index == this.length){
            return null;
        }
        prev_x = r * prev_x * (1-prev_x);
        this.logistic_array[cur_index] = (int)(Math.floor(prev_x * 255));
        return this.calculate_lm(prev_x, cur_index+1);
    }

    public List<String> convertArrayToMatrix(){
        List<String> matrix = new ArrayList<>();
        for(int i = 0; i < this.length; i++){
            matrix.add(toFullBinaryString(this.logistic_array[i]));
        }
        return matrix;
    }

    public String toFullBinaryString(int element){
        String temp = Integer.toBinaryString(element);
        char[] full = new char[8];
        Arrays.fill(full, '0');
        int j = 0;
        for(int i = 8-temp.length(); i < 8;i++){
            full[i] = temp.charAt(j++);
        }
        return new String(full);
    }

    public List<String> getBinaryMatrix(){
        return this.matrix;
    }

}

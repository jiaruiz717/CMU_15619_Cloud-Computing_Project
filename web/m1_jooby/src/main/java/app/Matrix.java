package app;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class Matrix {
    public int version;
    public int[][] matrix;
    private static final int alignment_pattern = 18;

    public Matrix(int version){
        this.version = version;
        this.matrix = new int[version][version];
        matrixInit();
    }

    public void printMatrix(){
        for(int i = 0;i<version;i++){
            for(int j = 0;j<version;j++){
                System.out.print(this.matrix[i][j]);
            }
            System.out.print('\n');
        }
    }

    public void drawVertical(int col, int row1, int row2, int value, boolean alternating){
        for(int i = row1; i < row2+1;i++){
            if(alternating){
                value = (value+1)%2;
            }
            this.matrix[i][col] = value;
        }
    }

    public void drawHorizontal(int row, int col1, int col2, int value, boolean alternating){
        for(int i = col1; i < col2+1; i++){
            if(alternating){
                value = (value+1)%2;
            }
            this.matrix[row][i] = value;
        }
    }

    public void drawSquare(List<Integer> topLeft, List<Integer> rightBottom, int position){
//        assert position == 0;
        int r1 = topLeft.get(0);
        int c1 = topLeft.get(1);
        int r2 = rightBottom.get(0);
        int c2 = rightBottom.get(1);

        // four black lines
        drawVertical(c1,r1,r2,1,false);
        drawVertical(c2,r1,r2,1,false);
        drawHorizontal(r1,c1,c2,1,false);
        drawHorizontal(r2,c1,c2,1,false);

        // white lines within the square
        drawHorizontal(r1+1, c1+1, c2-1, 0, false);
        drawHorizontal(r2-1, c1+1, c2-1, 0, false);
        drawVertical(c1+1, r1+1, r2-1, 0, false);
        drawVertical(c2-1, r1+1, r2-1, 0, false);

        // black block at center
        for(int i = r1+2; i<r1+5; i++){
            drawHorizontal(i, c1+2, c1+4, 1, false);
        }

        // different positions
        switch (position){
            case 0:{
                drawHorizontal(r2+1, c1, c2+1, 0, false);
                drawVertical(c2+1, r1, r2+1, 0 ,false);
                drawVertical(c2+2, r1, r2+1, 0 ,false);
                break;
            }
            case 1:{
                drawHorizontal(r2+1, c1-1, c2, 0, false);
                drawVertical(c1-1, r1, r2+1, 0, false);
                break;
            }
            case 2:{
                drawHorizontal(r1-1, c1, c2, 0 ,false);
                drawVertical(c2+1, r1-1, r2, 0 ,false);
                drawVertical(c2+2, r1-1, r2, 0, false);
                break;
            }
            default:{
                break;
            }
        }
    }

    public void drawAlignment(){
        // the position of alignment pattern can be hard-coded

        // draw 4 black borders
        drawHorizontal(alignment_pattern-2, alignment_pattern-2, alignment_pattern+2,1, false);
        drawHorizontal(alignment_pattern+2, alignment_pattern-2, alignment_pattern+2, 1, false);
        drawVertical(alignment_pattern-2, alignment_pattern-2, alignment_pattern+2, 1, false);
        drawVertical(alignment_pattern+2, alignment_pattern-2, alignment_pattern+2, 1, false);

        // draw white cell within the square
        drawHorizontal(alignment_pattern-1, alignment_pattern-1, alignment_pattern+1, 0, false);
        drawHorizontal(alignment_pattern+1, alignment_pattern-1, alignment_pattern+1, 0, false);
        drawVertical(alignment_pattern-1, alignment_pattern-1, alignment_pattern+1, 0, false);
        drawVertical(alignment_pattern+1, alignment_pattern-1, alignment_pattern+1, 0, false);

        // draw the center black cell
        drawHorizontal(alignment_pattern, alignment_pattern, alignment_pattern, 1, false);

        // put 0 in the skipped cells
        drawVertical(alignment_pattern-3, alignment_pattern-2, alignment_pattern+2, 0, false);
    }

    public void matrixInit(){
        // fill the initial matrix with 2
        for(int i = 0; i<version; i++){
            Arrays.fill(matrix[i], 2);
        }

        // draw three squares
        drawSquare(new ArrayList<>(Arrays.asList(0,0)), new ArrayList<>(Arrays.asList(6,6)), 0);
        drawSquare(new ArrayList<>(Arrays.asList(0,this.version-7)), new ArrayList<>(Arrays.asList(6,this.version-1)), 1);
        drawSquare(new ArrayList<>(Arrays.asList(this.version-7, 0)), new ArrayList<>(Arrays.asList(this.version-1, 6)), 2);

        // draw time pattern
        drawHorizontal(6, 8, this.version-9, 0,true);
        drawVertical(6, 8, this.version-9, 0, true);

        // draw alignment pattern if it's 25*25
        if(this.version == 25){
            drawAlignment();
        }
    }
}

package com.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast; // Import the Toast class

public class SudokuBoardView extends View {

    private Paint thickLinePaint;
    private Paint thinLinePaint;
    private Paint textPaint;
    private Paint selectedCellPaint;
    private Paint conflictingCellPaint;

    // ✅ New Paint object for user-inputted numbers
    private Paint userTextPaint;

    private int selectedRow = -1;
    private int selectedCol = -1;

    // Simple placeholder for the Sudoku board
    private int[][] board = new int[9][9];
    private boolean[][] isStartingCell = new boolean[9][9];


    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        thickLinePaint = new Paint();
        thickLinePaint.setColor(Color.parseColor("#4A5568"));
        thickLinePaint.setStrokeWidth(6f);
        thickLinePaint.setStyle(Paint.Style.STROKE);

        thinLinePaint = new Paint();
        thinLinePaint.setColor(Color.parseColor("#2D3748"));
        thinLinePaint.setStrokeWidth(2f);
        thinLinePaint.setStyle(Paint.Style.STROKE);

        textPaint = new Paint();
        textPaint.setColor(Color.WHITE);
        textPaint.setTextSize(64f);
        textPaint.setTextAlign(Paint.Align.CENTER);

        // ✅ Initialize the new Paint object
        userTextPaint = new Paint();
        userTextPaint.setColor(Color.parseColor("#00FFD1")); // Bright Cyan color for user input
        userTextPaint.setTextSize(64f);
        userTextPaint.setTextAlign(Paint.Align.CENTER);

        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.parseColor("#00FFD1"));
        selectedCellPaint.setAlpha(80); // Semi-transparent
        selectedCellPaint.setStyle(Paint.Style.FILL);

        conflictingCellPaint = new Paint();
        conflictingCellPaint.setColor(Color.RED);
        conflictingCellPaint.setAlpha(80);
        conflictingCellPaint.setStyle(Paint.Style.FILL);

        // Example board setup
        generateNewBoard();
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawGrid(canvas);
        drawNumbers(canvas);
        drawSelectedCell(canvas);
    }

    private void drawGrid(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        // Draw thin lines
        for (int i = 0; i <= 9; i++) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, size, thinLinePaint);
            canvas.drawLine(0, i * cellSize, size, i * cellSize, thinLinePaint);
        }

        // Draw thick lines
        for (int i = 0; i <= 9; i += 3) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, size, thickLinePaint);
            canvas.drawLine(0, i * cellSize, size, i * cellSize, thickLinePaint);
        }
    }

    private void drawNumbers(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] != 0) {
                    String text = String.valueOf(board[row][col]);
                    float x = col * cellSize + cellSize / 2;
                    float y = row * cellSize + cellSize / 2 - (textPaint.descent() + textPaint.ascent()) / 2;

                    // ✅ Use the correct paint based on whether it's a starting number or user input
                    if (isStartingCell[row][col]) {
                        canvas.drawText(text, x, y, textPaint);
                    } else {
                        canvas.drawText(text, x, y, userTextPaint);
                    }
                }
            }
        }
    }

    private void drawSelectedCell(Canvas canvas) {
        if (selectedRow != -1 && selectedCol != -1) {
            float size = getWidth();
            float cellSize = size / 9f;
            float left = selectedCol * cellSize;
            float top = selectedRow * cellSize;

            canvas.drawRect(left, top, left + cellSize, top + cellSize, selectedCellPaint);
        }
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            float size = getWidth();
            float cellSize = size / 9f;

            selectedCol = (int) (x / cellSize);
            selectedRow = (int) (y / cellSize);

            invalidate(); // Redraw the board
            return true;
        }
        return super.onTouchEvent(event);
    }

    public void setNumber(int number) {
        if (selectedRow != -1 && selectedCol != -1 && !isStartingCell[selectedRow][selectedCol]) {
            board[selectedRow][selectedCol] = number;
            invalidate();
        }
    }

    public void eraseNumber() {
        if (selectedRow != -1 && selectedCol != -1) {
            if (isStartingCell[selectedRow][selectedCol]) {
                // If the user tries to erase a starting number, show a message
                Toast.makeText(getContext(), "Cannot erase starting numbers.", Toast.LENGTH_SHORT).show();
            } else {
                // Otherwise, erase the number
                board[selectedRow][selectedCol] = 0;
                invalidate();
            }
        }
    }

    // A very simple placeholder for generating a board
    private void generateNewBoard() {
        // This is a simple, non-random board for demonstration.
        // A real implementation would use a Sudoku generation algorithm.
        int[][] sampleBoard = {
                {5, 3, 0, 0, 7, 0, 0, 0, 0},
                {6, 0, 0, 1, 9, 5, 0, 0, 0},
                {0, 9, 8, 0, 0, 0, 0, 6, 0},
                {8, 0, 0, 0, 6, 0, 0, 0, 3},
                {4, 0, 0, 8, 0, 3, 0, 0, 1},
                {7, 0, 0, 0, 2, 0, 0, 0, 6},
                {0, 6, 0, 0, 0, 0, 2, 8, 0},
                {0, 0, 0, 4, 1, 9, 0, 0, 5},
                {0, 0, 0, 0, 8, 0, 0, 7, 9}
        };

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                board[i][j] = sampleBoard[i][j];
                if (board[i][j] != 0) {
                    isStartingCell[i][j] = true;
                }
            }
        }
    }
}


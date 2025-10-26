// Sudoku-App/app/src/main/java/com/example/sudoku/SudokuBoardView.java
package com.example.sudoku;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Rect;
import android.util.AttributeSet;
import android.view.MotionEvent;
import android.view.View;
import android.widget.Toast;
import android.util.Log;


public class SudokuBoardView extends View {

    private Paint thickLinePaint;
    private Paint thinLinePaint;
    private Paint textPaint; // For starting numbers
    private Paint userTextPaint; // For user input numbers
    private Paint selectedCellPaint;
    private Paint conflictingCellPaint; // For highlighting cell with conflict (optional)
    private Paint relatedCellHighlightPaint; // Highlight row/col/box
    private Paint sameNumberHighlightPaint; // Highlight cells with the same number
    private Paint errorTextPaint; // Paint for highlighting incorrect numbers

    private int selectedRow = -1;
    private int selectedCol = -1;

    private int[][] board = new int[9][9]; // Current state including user input
    private boolean[][] isStartingCell = new boolean[9][9];
    private String solutionString = null; // Store the solution for validation
    private final Rect textBounds = new Rect();

    private int errorCount = 0; // Error counter


    public SudokuBoardView(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        thickLinePaint = new Paint();
        thickLinePaint.setColor(Color.parseColor("#4A5568"));
        thickLinePaint.setStrokeWidth(6f);
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setAntiAlias(true);

        thinLinePaint = new Paint();
        thinLinePaint.setColor(Color.parseColor("#2D3748"));
        thinLinePaint.setStrokeWidth(2f);
        thinLinePaint.setStyle(Paint.Style.STROKE);
        thinLinePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#E2E8F0")); // Off-white
        textPaint.setTextSize(64f);
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        userTextPaint = new Paint();
        userTextPaint.setColor(Color.parseColor("#63B3ED")); // Sudoku blue
        userTextPaint.setTextSize(64f);
        userTextPaint.setTextAlign(Paint.Align.CENTER);
        userTextPaint.setAntiAlias(true);

        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.parseColor("#4A5568")); // Use thick line color
        selectedCellPaint.setAlpha(100);
        selectedCellPaint.setStyle(Paint.Style.FILL);

        relatedCellHighlightPaint = new Paint();
        relatedCellHighlightPaint.setColor(Color.parseColor("#2D3748")); // Use thin line color
        relatedCellHighlightPaint.setAlpha(80);
        relatedCellHighlightPaint.setStyle(Paint.Style.FILL);

        sameNumberHighlightPaint = new Paint();
        sameNumberHighlightPaint.setColor(Color.parseColor("#63B3ED")); // User input color
        sameNumberHighlightPaint.setAlpha(90);
        sameNumberHighlightPaint.setStyle(Paint.Style.FILL);

        conflictingCellPaint = new Paint(); // Used if highlighting the conflicting cell itself red
        conflictingCellPaint.setColor(Color.parseColor("#F56565"));
        conflictingCellPaint.setAlpha(100);
        conflictingCellPaint.setStyle(Paint.Style.FILL);

        errorTextPaint = new Paint(); // Used for drawing the number itself in red
        errorTextPaint.setColor(Color.parseColor("#F56565")); // Red for errors
        errorTextPaint.setTextSize(64f);
        errorTextPaint.setTextAlign(Paint.Align.CENTER);
        errorTextPaint.setAntiAlias(true);
        errorTextPaint.setFakeBoldText(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        // Adjust text size based on cell size dynamically
        float dynamicTextSize = size / 9f * 0.7f;
        textPaint.setTextSize(dynamicTextSize);
        userTextPaint.setTextSize(dynamicTextSize);
        errorTextPaint.setTextSize(dynamicTextSize);
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHighlights(canvas); // Draw background highlights first
        drawGrid(canvas);       // Draw grid lines
        drawNumbers(canvas);    // Draw numbers on top
    }

    // Method to set the board from string, called when a new game starts OR restarts
    public void setBoard(String boardString, String solution) {
        if (boardString == null || boardString.length() != 81 || solution == null || solution.length() != 81) {
            Log.e("SudokuBoardView", "Invalid board or solution string provided.");
            board = new int[9][9];
            isStartingCell = new boolean[9][9];
            solutionString = null;
            errorCount = 0;
            invalidate();
            return;
        }

        this.solutionString = solution;
        int k = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char c = boardString.charAt(k++);
                if (c >= '1' && c <= '9') {
                    board[i][j] = c - '0';
                    isStartingCell[i][j] = true;
                } else {
                    board[i][j] = 0;
                    isStartingCell[i][j] = false;
                }
            }
        }
        errorCount = 0;
        selectedRow = -1;
        selectedCol = -1;
        invalidate();
    }

    /**
     * *** ADDED METHOD ***
     * Loads a saved game state (current board) onto the view.
     * Assumes setBoard() was called previously with the initial puzzle to set isStartingCell correctly.
     * @param stateString The 81-character string representing the saved state ('0' for empty).
     */
    public void loadCurrentState(String stateString) {
        if (stateString == null || stateString.length() != 81) {
            Log.e("SudokuBoardView", "Invalid current state string provided for loading.");
            return; // Don't modify the board if state is invalid
        }
        if (solutionString == null) {
            Log.e("SudokuBoardView", "Cannot load state before solution is set. Call setBoard() first.");
            return; // Need the solution to validate loaded numbers immediately
        }

        Log.d("SudokuBoardView", "Loading state: " + stateString);
        int k = 0;
        errorCount = 0; // Reset error count when loading state
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char c = stateString.charAt(k++);
                if (!isStartingCell[i][j]) { // Only modify non-starting cells
                    if (c >= '1' && c <= '9') {
                        int num = c - '0';
                        board[i][j] = num;
                        // Recalculate errors based on loaded state
                        if (solutionString.charAt(i * 9 + j) - '0' != num) {
                            errorCount++;
                        }
                    } else {
                        board[i][j] = 0; // Set empty if '0'
                    }
                }
                // Ensure starting cells from setBoard() are preserved
                else if (board[i][j] == 0 && c >= '1' && c <= '9') {
                    // This case handles if setBoard wasn't called first, attempt to restore starting cell
                    Log.w("SudokuBoardView", "Attempting to restore starting cell during loadState.");
                    board[i][j] = c - '0';
                    isStartingCell[i][j] = true;
                }
            }
        }
        Log.d("SudokuBoardView", "State loaded. Recalculated error count: " + errorCount);
        invalidate(); // Redraw with loaded state
    }


    private void drawGrid(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        for (int i = 0; i <= 9; i++) {
            Paint paint = (i % 3 == 0) ? thickLinePaint : thinLinePaint;
            canvas.drawLine(i * cellSize, 0, i * cellSize, size, paint);
            canvas.drawLine(0, i * cellSize, size, i * cellSize, paint);
        }
    }


    private void drawNumbers(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] != 0) {
                    String text = String.valueOf(board[row][col]);
                    Paint currentPaint;

                    if (isStartingCell[row][col]) {
                        currentPaint = textPaint;
                    } else {
                        if (solutionString != null &&
                                (solutionString.charAt(row * 9 + col) - '0') != board[row][col]) {
                            currentPaint = errorTextPaint;
                        } else {
                            currentPaint = userTextPaint;
                        }
                    }

                    currentPaint.getTextBounds(text, 0, text.length(), textBounds);
                    float x = col * cellSize + cellSize / 2;
                    float y = row * cellSize + cellSize / 2 + textBounds.height() / 2f - textBounds.bottom;

                    canvas.drawText(text, x, y, currentPaint);
                }
            }
        }
    }


    private void drawHighlights(Canvas canvas) {
        if (selectedRow == -1 || selectedCol == -1) return;

        float size = getWidth();
        float cellSize = size / 9f;
        int selectedValue = board[selectedRow][selectedCol];

        int boxStartRow = selectedRow - selectedRow % 3;
        int boxStartCol = selectedCol - selectedCol % 3;
        for (int i = 0; i < 9; i++) {
            canvas.drawRect(i * cellSize, selectedRow * cellSize, (i + 1) * cellSize, (selectedRow + 1) * cellSize, relatedCellHighlightPaint);
            canvas.drawRect(selectedCol * cellSize, i * cellSize, (selectedCol + 1) * cellSize, (i + 1) * cellSize, relatedCellHighlightPaint);
        }
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, relatedCellHighlightPaint);
            }
        }

        if (selectedValue != 0) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board[r][c] == selectedValue) {
                        canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, sameNumberHighlightPaint);
                    }
                }
            }
        }

        float left = selectedCol * cellSize;
        float top = selectedRow * cellSize;
        canvas.drawRect(left, top, left + cellSize, top + cellSize, selectedCellPaint);

    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            float size = getWidth();
            float cellSize = size / 9f;

            int col = (int) (x / cellSize);
            int row = (int) (y / cellSize);

            if (col >= 0 && col < 9 && row >= 0 && row < 9) {
                selectedCol = col;
                selectedRow = row;
                invalidate();
                return true;
            } else {
                if (selectedRow != -1 || selectedCol != -1) {
                    selectedRow = -1;
                    selectedCol = -1;
                    invalidate();
                }
                return false;
            }
        }
        return super.onTouchEvent(event);
    }


    public void setNumber(int number) {
        if (selectedRow != -1 && selectedCol != -1) {
            if (isStartingCell[selectedRow][selectedCol]) {
                Toast.makeText(getContext(), "Cannot change starting numbers.", Toast.LENGTH_SHORT).show();
            } else {
                int previousValue = board[selectedRow][selectedCol];
                if (previousValue == number) return;

                // Error Tracking
                if (solutionString != null && number != 0) {
                    int correctValue = solutionString.charAt(selectedRow * 9 + selectedCol) - '0';
                    boolean wasCorrect = (previousValue != 0 && previousValue == correctValue);
                    boolean isNowCorrect = (number == correctValue);

                    // Increment error count only if placing an incorrect number
                    // Do not increment if replacing one incorrect number with another
                    // Do not increment if erasing an incorrect number (handled in eraseNumber if needed)
                    if (!isNowCorrect) {
                        // Increment only if the cell was previously empty or correct
                        if (previousValue == 0 || wasCorrect) {
                            errorCount++;
                            Log.d("SudokuError", "Incorrect number placed. Error count: " + errorCount);
                        } else {
                            // Replacing one error with another - don't increment again
                            Log.d("SudokuError", "Replaced incorrect number with another incorrect number.");
                        }
                    }
                }

                board[selectedRow][selectedCol] = number;
                invalidate();
            }
        } else {
            Toast.makeText(getContext(), "Select a cell first.", Toast.LENGTH_SHORT).show();
        }
    }


    public void eraseNumber() {
        if (selectedRow != -1 && selectedCol != -1) {
            if (isStartingCell[selectedRow][selectedCol]) {
                Toast.makeText(getContext(), "Cannot erase starting numbers.", Toast.LENGTH_SHORT).show();
            } else {
                int previousValue = board[selectedRow][selectedCol];
                if (previousValue != 0) {
                    // Note: We are not decrementing errorCount here.
                    // An error made persists in the count until the game ends.
                    board[selectedRow][selectedCol] = 0;
                    invalidate();
                }
            }
        } else {
            Toast.makeText(getContext(), "Select a cell first.", Toast.LENGTH_SHORT).show();
        }
    }


    // Returns the current state of the board as a 2D array
    public int[][] getBoardState() {
        int[][] copy = new int[9][9];
        for (int i = 0; i < 9; i++) {
            System.arraycopy(board[i], 0, copy[i], 0, 9);
        }
        return copy;
    }

    // Returns the current board state as an 81-character string
    public String getBoardString() {
        StringBuilder sb = new StringBuilder(81);
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                sb.append(board[i][j]); // Append number (0-9)
            }
        }
        return sb.toString();
    }

    // Checks if the current board state matches the solution string
    public boolean isSolvedCorrectly() {
        if (solutionString == null) {
            Log.w("SudokuBoardView", "Solution string is null, cannot validate.");
            return false; // Cannot validate if solution is unknown
        }
        // Check if board is full AND matches solution
        return isBoardFull() && getBoardString().equals(solutionString);
    }

    // Checks if all cells on the board are filled (non-zero)
    public boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    return false;
                }
            }
        }
        return true;
    }


    public int getErrorCount() {
        return errorCount;
    }

}


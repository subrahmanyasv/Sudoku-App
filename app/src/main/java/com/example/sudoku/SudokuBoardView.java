// Relative Path: Sudoku-App/app/src/main/java/com/example/sudoku/SudokuBoardView.java
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
        setMeasuredDimension(size, size);
    }

    @Override
    protected void onDraw(Canvas canvas) {
        super.onDraw(canvas);
        drawHighlights(canvas); // Draw background highlights first
        drawGrid(canvas);       // Draw grid lines
        drawNumbers(canvas);    // Draw numbers on top
    }

    // Method to set the board from string, called when a new game starts
    public void setBoard(String boardString, String solution) {
        // Basic validation of input strings
        if (boardString == null || boardString.length() != 81 || solution == null || solution.length() != 81) {
            Log.e("SudokuBoardView", "Invalid board or solution string provided. Lengths: board=" + (boardString != null ? boardString.length() : "null") + ", solution=" + (solution != null ? solution.length() : "null"));
            // Clear the board to an empty state
            board = new int[9][9];
            isStartingCell = new boolean[9][9];
            solutionString = null;
            errorCount = 0; // Reset errors
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
                    isStartingCell[i][j] = true; // Mark as a pre-filled cell
                } else {
                    board[i][j] = 0; // Treat anything else ('.', '0') as empty
                    isStartingCell[i][j] = false;
                }
            }
        }
        errorCount = 0; // Reset errors for the new board
        selectedRow = -1; // Reset selection
        selectedCol = -1;
        invalidate(); // Request a redraw
    }


    // Draws the grid lines
    private void drawGrid(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        // Draw thin lines
        for (int i = 0; i <= 9; i++) {
            float start = (i % 3 == 0) ? 1f : 0; // Slight offset to avoid drawing over thick lines
            float end = (i % 3 == 0) ? size -1f : size;
            if (i != 0 && i != 9) { // Don't adjust outer borders
                canvas.drawLine(i * cellSize, start, i * cellSize, end, thinLinePaint);
                canvas.drawLine(start, i * cellSize, end, i * cellSize, thinLinePaint);
            } else { // Draw outer borders normally
                canvas.drawLine(i * cellSize, 0, i * cellSize, size, thinLinePaint);
                canvas.drawLine(0, i * cellSize, size, i * cellSize, thinLinePaint);
            }
        }

        // Draw thick lines (every 3rd line)
        for (int i = 0; i <= 9; i += 3) {
            canvas.drawLine(i * cellSize, 0, i * cellSize, size, thickLinePaint);
            canvas.drawLine(0, i * cellSize, size, i * cellSize, thickLinePaint);
        }
    }

    // Draws the numbers in the cells
    private void drawNumbers(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] != 0) { // Only draw non-empty cells
                    String text = String.valueOf(board[row][col]);
                    Paint currentPaint;

                    // Determine the correct paint style based on cell type and correctness
                    if (isStartingCell[row][col]) {
                        currentPaint = textPaint; // Use standard text paint for pre-filled numbers
                    } else {
                        // Check user input against the solution
                        if (solutionString != null &&
                                (solutionString.charAt(row * 9 + col) - '0') != board[row][col]) {
                            currentPaint = errorTextPaint; // Use error paint if incorrect
                        } else {
                            currentPaint = userTextPaint; // Use user input paint if correct or no solution available
                        }
                    }

                    // Calculate text bounds for precise vertical centering
                    currentPaint.getTextBounds(text, 0, text.length(), textBounds);
                    float x = col * cellSize + cellSize / 2; // Center horizontally
                    // Center vertically using text bounds height
                    float y = row * cellSize + cellSize / 2 + textBounds.height() / 2f - textBounds.bottom;

                    canvas.drawText(text, x, y, currentPaint);
                }
            }
        }
    }


    // Draws background highlights for selected cell, related cells, and same numbers
    private void drawHighlights(Canvas canvas) {
        if (selectedRow == -1 || selectedCol == -1) return; // No highlights if no cell is selected

        float size = getWidth();
        float cellSize = size / 9f;
        int selectedValue = board[selectedRow][selectedCol];

        // 1. Highlight related cells (row, column, and 3x3 box)
        int boxStartRow = selectedRow - selectedRow % 3;
        int boxStartCol = selectedCol - selectedCol % 3;
        for (int i = 0; i < 9; i++) {
            // Highlight row
            canvas.drawRect(i * cellSize, selectedRow * cellSize, (i + 1) * cellSize, (selectedRow + 1) * cellSize, relatedCellHighlightPaint);
            // Highlight column
            canvas.drawRect(selectedCol * cellSize, i * cellSize, (selectedCol + 1) * cellSize, (i + 1) * cellSize, relatedCellHighlightPaint);
        }
        // Highlight 3x3 box (redraws over some cells, but simpler than excluding)
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, relatedCellHighlightPaint);
            }
        }

        // 2. Highlight cells with the same number as the selected cell (if selected cell is not empty)
        if (selectedValue != 0) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board[r][c] == selectedValue) {
                        canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, sameNumberHighlightPaint);
                    }
                }
            }
        }

        // 3. Highlight the selected cell itself (drawn on top of other highlights)
        float left = selectedCol * cellSize;
        float top = selectedRow * cellSize;
        canvas.drawRect(left, top, left + cellSize, top + cellSize, selectedCellPaint);

        // Optional: Conflict Highlighting for the selected cell
        // if (!isStartingCell[selectedRow][selectedCol] && board[selectedRow][selectedCol] != 0 &&
        //     solutionString != null && (solutionString.charAt(selectedRow * 9 + selectedCol) - '0') != board[selectedRow][selectedCol]) {
        //     canvas.drawRect(left, top, left + cellSize, top + cellSize, conflictingCellPaint); // Draw red overlay on cell
        // }
    }


    // Handles touch events to select cells
    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            float size = getWidth();
            float cellSize = size / 9f;

            // Calculate row and column based on touch coordinates
            int col = (int) (x / cellSize);
            int row = (int) (y / cellSize);

            // Check if touch is within the grid bounds
            if (col >= 0 && col < 9 && row >= 0 && row < 9) {
                // If the same cell is tapped again, deselect (optional)
                // if (selectedRow == row && selectedCol == col) {
                //    selectedRow = -1;
                //    selectedCol = -1;
                // } else {
                selectedCol = col;
                selectedRow = row;
                // }
                invalidate(); // Request redraw with new selection highlights
                return true; // Indicate event was handled
            } else {
                // Clicked outside the grid, deselect
                if (selectedRow != -1 || selectedCol != -1) { // Only invalidate if something was selected
                    selectedRow = -1;
                    selectedCol = -1;
                    invalidate();
                }
                return false; // Event not handled (or handled by deselecting)
            }
        }
        return super.onTouchEvent(event);
    }

    // Sets the number for the currently selected cell
    public void setNumber(int number) {
        if (selectedRow != -1 && selectedCol != -1) { // Check if a cell is selected
            if (isStartingCell[selectedRow][selectedCol]) {
                // Prevent changing pre-filled numbers
                Toast.makeText(getContext(), "Cannot change starting numbers.", Toast.LENGTH_SHORT).show();
            } else {
                int previousValue = board[selectedRow][selectedCol];
                if (previousValue == number) return; // No change if number is the same

                // Error Tracking Logic: Increment error count if the new number is incorrect
                if (solutionString != null && number != 0) { // Only check errors for non-empty inputs
                    int correctValue = solutionString.charAt(selectedRow * 9 + selectedCol) - '0';
                    boolean wasCorrect = (previousValue == correctValue); // Was the previous value correct?
                    boolean isNowCorrect = (number == correctValue);    // Is the new number correct?

                    // We primarily care about INCREMENTING errors when a WRONG number is placed.
                    // A simple way is to count errors only when a non-starting cell is filled INCORRECTLY.
                    // We don't decrement if they fix it, to penalize guessing.
                    if (!isNowCorrect) {
                        // Only increment if the *previous* state wasn't already this specific incorrect number
                        // OR if it was empty before.
                        // A simpler logic: just check if the placed number is wrong.
                        // But the requirements might be to count each mistake *instance*.
                        // Let's stick to the simple: increment if the new number is wrong.
                        // (More complex logic could track *which* cells are wrong)

                        // Simpler check: If the number placed is WRONG, increment error.
                        // This means correcting an error doesn't reduce the count.
                        errorCount++;
                        Log.d("SudokuError", "Incorrect number placed. Error count: " + errorCount);

                    } else {
                        // Number placed is correct. Check if it was previously wrong.
                        // if (previousValue != 0 && !wasCorrect) {
                        // Optional: Log correction, but don't decrement count for penalty.
                        // Log.d("SudokuCorrection", "Cell corrected.");
                        // }
                    }
                }
                // --- End Error Tracking ---

                board[selectedRow][selectedCol] = number; // Update the board state
                invalidate(); // Redraw to show the new number and highlights
            }
        } else {
            // No cell selected
            Toast.makeText(getContext(), "Select a cell first.", Toast.LENGTH_SHORT).show();
        }
    }

    // Clears the number from the currently selected cell (if not pre-filled)
    public void eraseNumber() {
        if (selectedRow != -1 && selectedCol != -1) { // Check if a cell is selected
            if (isStartingCell[selectedRow][selectedCol]) {
                // Prevent erasing pre-filled numbers
                Toast.makeText(getContext(), "Cannot erase starting numbers.", Toast.LENGTH_SHORT).show();
            } else {
                int previousValue = board[selectedRow][selectedCol];
                if (previousValue != 0) { // Only erase if the cell is not already empty
                    // Error Tracking on Erase:
                    // If erasing an incorrect number, the error technically remains until corrected.
                    // For simplicity, we don't modify errorCount on erase.
                    board[selectedRow][selectedCol] = 0; // Set cell to empty
                    invalidate(); // Redraw
                }
            }
        } else {
            // No cell selected
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
        return getBoardString().equals(solutionString);
    }

    // Checks if all cells on the board are filled (non-zero)
    public boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) {
                    return false; // Found an empty cell
                }
            }
        }
        return true; // No empty cells found
    }

    // Getter for the accumulated error count
    public int getErrorCount() {
        return errorCount;
    }

}


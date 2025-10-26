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
        thickLinePaint.setColor(Color.parseColor("#4A5568")); // Use color from theme if possible
        thickLinePaint.setStrokeWidth(6f);
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setAntiAlias(true);

        thinLinePaint = new Paint();
        thinLinePaint.setColor(Color.parseColor("#2D3748")); // Use color from theme if possible
        thinLinePaint.setStrokeWidth(2f);
        thinLinePaint.setStyle(Paint.Style.STROKE);
        thinLinePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#E2E8F0")); // Off-white (text_primary)
        textPaint.setTextSize(64f); // Will be adjusted in onMeasure
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);

        userTextPaint = new Paint();
        userTextPaint.setColor(Color.parseColor("#63B3ED")); // Sudoku blue (board_text_user?)
        userTextPaint.setTextSize(64f); // Will be adjusted in onMeasure
        userTextPaint.setTextAlign(Paint.Align.CENTER);
        userTextPaint.setAntiAlias(true);

        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.parseColor("#0E7490")); // Dark Cyan (board_cell_selected)
        selectedCellPaint.setAlpha(100); // Adjust alpha as needed
        selectedCellPaint.setStyle(Paint.Style.FILL);

        relatedCellHighlightPaint = new Paint();
        relatedCellHighlightPaint.setColor(Color.parseColor("#475569")); // Secondary button color (board_cell_related)
        relatedCellHighlightPaint.setAlpha(80); // Adjust alpha as needed
        relatedCellHighlightPaint.setStyle(Paint.Style.FILL);

        sameNumberHighlightPaint = new Paint();
        sameNumberHighlightPaint.setColor(Color.parseColor("#38BDF8")); // Light Blue (board_cell_same_number)
        sameNumberHighlightPaint.setAlpha(90); // Adjust alpha as needed
        sameNumberHighlightPaint.setStyle(Paint.Style.FILL);

        // Optional paint if highlighting the cell background for conflict
        conflictingCellPaint = new Paint();
        conflictingCellPaint.setColor(Color.parseColor("#F87171")); // Red (text_error)
        conflictingCellPaint.setAlpha(100);
        conflictingCellPaint.setStyle(Paint.Style.FILL);

        errorTextPaint = new Paint(); // Used for drawing the number itself in red
        errorTextPaint.setColor(Color.parseColor("#FB7185")); // Rose (board_text_error)
        errorTextPaint.setTextSize(64f); // Will be adjusted in onMeasure
        errorTextPaint.setTextAlign(Paint.Align.CENTER);
        errorTextPaint.setAntiAlias(true);
        errorTextPaint.setFakeBoldText(true);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        // Adjust text size based on cell size dynamically
        float dynamicTextSize = size / 9f * 0.7f; // Make text slightly smaller than cell height
        textPaint.setTextSize(dynamicTextSize);
        userTextPaint.setTextSize(dynamicTextSize);
        errorTextPaint.setTextSize(dynamicTextSize);
        setMeasuredDimension(size, size); // Make the view square
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
        // Basic validation
        if (boardString == null || boardString.length() != 81) {
            Log.e("SudokuBoardView", "Invalid board string provided.");
            // Reset board state
            board = new int[9][9];
            isStartingCell = new boolean[9][9];
            solutionString = null;
            errorCount = 0; // Reset error count
            selectedRow = -1;
            selectedCol = -1;
            invalidate(); // Redraw with empty board
            return;
        }

        // Validate solution string (can be null for challenges after modification)
        if (solution != null && solution.length() != 81) {
            Log.w("SudokuBoardView", "Invalid solution string provided. Proceeding without solution validation.");
            this.solutionString = null; // Treat invalid solution as null
        } else {
            this.solutionString = solution;
            Log.d("SudokuBoardView", "Board set. Solution is " + (this.solutionString != null ? "available." : "not available."));
        }


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
        // Reset state for a new/restarted board
        errorCount = 0;
        selectedRow = -1;
        selectedCol = -1;
        invalidate(); // Trigger redraw
    }

    /**
     * Loads a saved game state (current board) onto the view.
     * Assumes setBoard() was called previously with the initial puzzle.
     * @param stateString The 81-character string representing the saved state ('0' for empty).
     */
    public void loadCurrentState(String stateString) {
        if (stateString == null || stateString.length() != 81) {
            Log.e("SudokuBoardView", "Invalid current state string provided for loading.");
            return; // Don't modify the board if state is invalid
        }
        // Solution should already be set via setBoard() before calling this

        Log.d("SudokuBoardView", "Loading state: " + stateString);
        int k = 0;
        // *** DO NOT reset errorCount here - it will be set externally ***
        // errorCount = 0;

        // Recalculate errors purely based on loaded state (for logging/comparison if needed)
        int recalculatedErrors = 0;

        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char c = stateString.charAt(k++);
                if (!isStartingCell[i][j]) { // Only modify non-starting cells
                    if (c >= '1' && c <= '9') {
                        int num = c - '0';
                        board[i][j] = num;
                        // Recalculate errors based on loaded state if solution is available
                        if (hasSolution()) {
                            if (solutionString.charAt(i * 9 + j) - '0' != num) {
                                recalculatedErrors++;
                            }
                        }
                    } else {
                        board[i][j] = 0; // Set empty if '0'
                    }
                }
            }
        }
        // Log the recalculated count, but don't overwrite the actual errorCount
        Log.d("SudokuBoardView", "State loaded. Recalculated error count based *only* on loaded state: " + recalculatedErrors);
        invalidate(); // Redraw with loaded state
    }


    private void drawGrid(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        // Draw vertical lines
        for (int i = 0; i <= 9; i++) {
            Paint paint = (i % 3 == 0) ? thickLinePaint : thinLinePaint;
            canvas.drawLine(i * cellSize, 0, i * cellSize, size, paint);
        }
        // Draw horizontal lines
        for (int i = 0; i <= 9; i++) {
            Paint paint = (i % 3 == 0) ? thickLinePaint : thinLinePaint;
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

                    // Determine which paint to use
                    if (isStartingCell[row][col]) {
                        currentPaint = textPaint; // Original puzzle numbers
                    } else {
                        // Check against solution ONLY if solution is available
                        if (hasSolution() && solutionString.charAt(row * 9 + col) - '0' != board[row][col]) {
                            currentPaint = errorTextPaint; // Incorrect user number
                        } else {
                            currentPaint = userTextPaint; // Correct or unvalidated user number
                        }
                    }

                    // Calculate position to center text in the cell
                    currentPaint.getTextBounds(text, 0, text.length(), textBounds);
                    float x = col * cellSize + cellSize / 2;
                    // Adjust y-position for vertical centering
                    float y = row * cellSize + cellSize / 2 + textBounds.height() / 2f - textBounds.bottom; // Corrected calculation

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

        // Highlight row, column, and 3x3 box
        int boxStartRow = selectedRow - selectedRow % 3;
        int boxStartCol = selectedCol - selectedCol % 3;

        for (int i = 0; i < 9; i++) {
            // Highlight row i
            canvas.drawRect(i * cellSize, selectedRow * cellSize, (i + 1) * cellSize, (selectedRow + 1) * cellSize, relatedCellHighlightPaint);
            // Highlight column i
            canvas.drawRect(selectedCol * cellSize, i * cellSize, (selectedCol + 1) * cellSize, (i + 1) * cellSize, relatedCellHighlightPaint);
        }
        // Highlight 3x3 box (this will overlap row/col highlights, which is fine)
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, relatedCellHighlightPaint);
            }
        }

        // Highlight cells with the same number as the selected cell (if selected cell is not empty)
        if (selectedValue != 0) {
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    if (board[r][c] == selectedValue) {
                        canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, sameNumberHighlightPaint);
                    }
                }
            }
        }

        // Highlight the selected cell itself (drawn last to be on top)
        float left = selectedCol * cellSize;
        float top = selectedRow * cellSize;
        canvas.drawRect(left, top, left + cellSize, top + cellSize, selectedCellPaint);

        // Optionally, highlight conflicting cell background (if selected cell has an error)
        if (hasSolution() && !isStartingCell[selectedRow][selectedCol] && selectedValue != 0 &&
                solutionString.charAt(selectedRow * 9 + selectedCol) - '0' != selectedValue) {
            canvas.drawRect(left, top, left + cellSize, top + cellSize, conflictingCellPaint); // Red background tint
        }
    }


    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (event.getAction() == MotionEvent.ACTION_DOWN) {
            float x = event.getX();
            float y = event.getY();
            float size = getWidth();
            float cellSize = size / 9f;

            // Calculate column and row based on touch position
            int col = (int) (x / cellSize);
            int row = (int) (y / cellSize);

            // Ensure touch is within grid bounds
            if (col >= 0 && col < 9 && row >= 0 && row < 9) {
                // Check if the selected cell changed
                if (row != selectedRow || col != selectedCol) {
                    selectedCol = col;
                    selectedRow = row;
                    invalidate(); // Trigger redraw with new highlights
                }
                return true; // Consume the event
            } else {
                // If touch is outside the grid, deselect
                if (selectedRow != -1 || selectedCol != -1) {
                    selectedRow = -1;
                    selectedCol = -1;
                    invalidate(); // Trigger redraw without highlights
                }
                return false; // Don't consume if outside grid and nothing was selected
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
                // Only proceed if the number is actually different
                if (previousValue == number) return;

                // --- Error Tracking Logic ---
                // Only track errors if the solution is available
                if (hasSolution()) {
                    int correctValue = solutionString.charAt(selectedRow * 9 + selectedCol) - '0';
                    boolean wasCorrect = (previousValue != 0 && previousValue == correctValue);
                    boolean isNowCorrect = (number == correctValue);

                    // Increment error count only if placing an incorrect number
                    // where the cell was previously empty or correct.
                    if (number != 0 && !isNowCorrect) {
                        if (previousValue == 0 || wasCorrect) {
                            errorCount++;
                            Log.d("SudokuError", String.format("Incorrect number %d placed at (%d, %d). Correct: %d. Error count: %d",
                                    number, selectedRow, selectedCol, correctValue, errorCount));
                        } else {
                            // Replacing one error with another - no change in error count
                            Log.d("SudokuError", String.format("Replaced incorrect number %d with %d at (%d, %d). Correct: %d. Error count remains: %d",
                                    previousValue, number, selectedRow, selectedCol, correctValue, errorCount));
                        }
                    }
                    // If placing a correct number where it was previously incorrect,
                    // the error count remains (errors are cumulative for the game session).
                    else if (number != 0 && isNowCorrect && !wasCorrect && previousValue != 0) {
                        Log.d("SudokuError", String.format("Corrected cell (%d, %d) with %d. Error count remains: %d",
                                selectedRow, selectedCol, number, errorCount));
                    }
                }
                // --- End Error Tracking ---

                // Update the board and redraw
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
                    // Erasing a number doesn't reduce the historical error count for the game.
                    board[selectedRow][selectedCol] = 0; // Set to empty
                    invalidate(); // Redraw
                }
            }
        } else {
            Toast.makeText(getContext(), "Select a cell first.", Toast.LENGTH_SHORT).show();
        }
    }


    // Returns the current state of the board as a 2D array (defensive copy)
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
        boolean isFull = isBoardFull();
        if (!hasSolution()) {
            // If no solution available (challenge mode), "correct" means "full"
            Log.d("SudokuBoardView", "isSolvedCorrectly: Solution not available. Is full: " + isFull);
            return isFull;
        }
        // If solution is available, check if full AND matches solution
        boolean matches = getBoardString().equals(solutionString);
        Log.d("SudokuBoardView", "isSolvedCorrectly: Solution available. Is full: " + isFull + ", Matches solution: " + matches);
        return isFull && matches;
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

    /**
     * Fills the non-starting cells with the correct solution. (DEBUG METHOD)
     */
    public void fillWithSolution() {
        if (!hasSolution()) {
            Log.w("SudokuBoardView", "fillWithSolution called but no solution is available.");
            return;
        }
        Log.d("SudokuBoardView", "Filling board with solution (DEBUG).");
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isStartingCell[i][j]) { // Only fill user cells
                    board[i][j] = solutionString.charAt(i * 9 + j) - '0';
                }
            }
        }
        errorCount = 0; // Reset errors when explicitly solving
        invalidate(); // Redraw with solution filled
    }

    /**
     * Checks if a valid solution string is currently loaded.
     * @return true if a solution is available, false otherwise.
     */
    public boolean hasSolution() {
        return solutionString != null && solutionString.length() == 81;
    }

    /**
     * Provides internal access to the solution string (e.g., for restarting).
     * @return The solution string, or null if not available.
     */
    protected String getSolutionStringInternal() {
        return solutionString;
    }


    /**
     * Gets the current cumulative error count for this game session.
     * Returns 0 if the solution is not available (e.g., during challenges).
     * @return The number of incorrect placements made.
     */
    public int getErrorCount() {
        // Return 0 if we can't validate against a solution
        return hasSolution() ? errorCount : 0;
    }

    /**
     * *** ADDED: Method to externally set the error count (used when loading a game) ***
     * @param count The error count loaded from saved state.
     */
    public void setErrorCount(int count) {
        this.errorCount = Math.max(0, count); // Ensure count is not negative
        Log.d("SudokuBoardView", "Error count externally set to: " + this.errorCount);
    }

}


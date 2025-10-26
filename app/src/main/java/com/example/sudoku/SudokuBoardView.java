// Relative Path: app/src/main/java/com/example/sudoku/SudokuBoardView.java
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
        thickLinePaint.setColor(Color.parseColor("#64748b")); // board_line_thick from colors.xml
        thickLinePaint.setStrokeWidth(6f); // Slightly thicker for 3x3 box lines
        thickLinePaint.setStyle(Paint.Style.STROKE);
        thickLinePaint.setAntiAlias(true);

        thinLinePaint = new Paint();
        thinLinePaint.setColor(Color.parseColor("#475569")); // board_line_thin from colors.xml
        thinLinePaint.setStrokeWidth(2f); // Thinner for inner lines
        thinLinePaint.setStyle(Paint.Style.STROKE);
        thinLinePaint.setAntiAlias(true);

        textPaint = new Paint();
        textPaint.setColor(Color.parseColor("#e2e8f0")); // board_text_starting from colors.xml
        textPaint.setTextSize(64f); // Initial size, adjusted in onMeasure
        textPaint.setTextAlign(Paint.Align.CENTER);
        textPaint.setAntiAlias(true);
        textPaint.setFakeBoldText(true); // Make starting numbers bold

        userTextPaint = new Paint();
        userTextPaint.setColor(Color.parseColor("#67e8f9")); // board_text_user from colors.xml
        userTextPaint.setTextSize(64f); // Initial size, adjusted in onMeasure
        userTextPaint.setTextAlign(Paint.Align.CENTER);
        userTextPaint.setAntiAlias(true);

        selectedCellPaint = new Paint();
        selectedCellPaint.setColor(Color.parseColor("#0e7490")); // board_cell_selected from colors.xml
        // selectedCellPaint.setAlpha(100); // Alpha can be adjusted or removed
        selectedCellPaint.setStyle(Paint.Style.FILL);

        relatedCellHighlightPaint = new Paint();
        relatedCellHighlightPaint.setColor(Color.parseColor("#475569")); // board_cell_related from colors.xml
        // relatedCellHighlightPaint.setAlpha(80); // Alpha can be adjusted or removed
        relatedCellHighlightPaint.setStyle(Paint.Style.FILL);

        sameNumberHighlightPaint = new Paint();
        sameNumberHighlightPaint.setColor(Color.parseColor("#38bdf8")); // board_cell_same_number from colors.xml
        // sameNumberHighlightPaint.setAlpha(90); // Alpha can be adjusted or removed
        sameNumberHighlightPaint.setStyle(Paint.Style.FILL);

        // This paint is primarily for the text color of wrong numbers
        errorTextPaint = new Paint();
        errorTextPaint.setColor(Color.parseColor("#fb7185")); // board_text_error from colors.xml
        errorTextPaint.setTextSize(64f); // Initial size, adjusted in onMeasure
        errorTextPaint.setTextAlign(Paint.Align.CENTER);
        errorTextPaint.setAntiAlias(true);
        errorTextPaint.setFakeBoldText(true); // Make errors stand out

        // Optional: Paint for RED background on incorrect cell (conflictingCellPaint)
        // conflictingCellPaint = new Paint();
        // conflictingCellPaint.setColor(Color.parseColor("#fb7185")); // board_text_error color
        // conflictingCellPaint.setAlpha(100);
        // conflictingCellPaint.setStyle(Paint.Style.FILL);
    }

    @Override
    protected void onMeasure(int widthMeasureSpec, int heightMeasureSpec) {
        super.onMeasure(widthMeasureSpec, heightMeasureSpec);
        int size = Math.min(getMeasuredWidth(), getMeasuredHeight());
        // Adjust text size based on cell size dynamically
        float dynamicTextSize = size / 9f * 0.6f; // Reduced multiplier for better fit
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

    /**
     * Sets the initial board state and the solution string.
     * Clears previous state, errors, and selection.
     * @param boardString 81-char string ('0'-'9'), initial puzzle state.
     * @param solution    81-char string ('1'-'9'), the full solution. Can be null for challenges.
     */
    public void setBoard(String boardString, String solution) {
        // Allow solution to be null, but boardString must be valid
        if (boardString == null || boardString.length() != 81) {
            Log.e("SudokuBoardView", "Invalid board string provided.");
            // Initialize empty board
            board = new int[9][9];
            isStartingCell = new boolean[9][9];
            solutionString = null; // Clear solution
            errorCount = 0; // Reset errors
            selectedRow = -1; // Reset selection
            selectedCol = -1;
            invalidate(); // Redraw empty board
            return;
        }

        // Validate solution string if provided
        if (solution != null && solution.length() != 81) {
            Log.w("SudokuBoardView", "Invalid solution string provided. Proceeding without solution validation.");
            this.solutionString = null; // Treat invalid solution as null
        } else {
            this.solutionString = solution; // Store the valid (or null) solution
        }

        int k = 0;
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                char c = boardString.charAt(k++);
                if (c >= '1' && c <= '9') {
                    board[i][j] = c - '0';
                    isStartingCell[i][j] = true;
                } else {
                    board[i][j] = 0; // Use 0 for empty cells
                    isStartingCell[i][j] = false;
                }
            }
        }
        errorCount = 0; // Reset error count for new board
        selectedRow = -1; // Reset selection
        selectedCol = -1;
        invalidate(); // Redraw the board
        Log.d("SudokuBoardView", "Board set. Solution is " + (this.solutionString != null ? "available." : "NOT available (Challenge?)."));
    }

    /**
     * Loads a saved game state (current board) onto the view.
     * Assumes setBoard() was called previously with the initial puzzle to set isStartingCell correctly.
     * Requires the solution string to be available for error checking.
     * @param stateString The 81-character string representing the saved state ('0' for empty).
     */
    public void loadCurrentState(String stateString) {
        if (stateString == null || stateString.length() != 81) {
            Log.e("SudokuBoardView", "Invalid current state string provided for loading.");
            return; // Don't modify the board if state is invalid
        }
        // *** MODIFIED: Allow loading even if solutionString is null, skip error check ***
        if (solutionString == null) {
            Log.w("SudokuBoardView", "Loading state, but solution string is null. Error count will not be recalculated.");
        } else if (solutionString.length() != 81) {
            Log.e("SudokuBoardView", "Cannot load state: Invalid internal solution string length.");
            return;
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
                        // Recalculate errors based on loaded state ONLY IF solution is available
                        if (solutionString != null && (solutionString.charAt(i * 9 + j) - '0') != num) {
                            errorCount++;
                        }
                    } else {
                        board[i][j] = 0; // Set empty if '0'
                    }
                }
                // Ensure starting cells from setBoard() are preserved
                else if (board[i][j] == 0 && c >= '1' && c <= '9') {
                    Log.w("SudokuBoardView", "Attempting to restore starting cell at (" + i + "," + j + ") during loadState.");
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

        // Draw thin lines first
        for (int i = 0; i <= 9; i++) {
            if (i % 3 != 0) { // Only draw thin lines
                canvas.drawLine(i * cellSize, 0, i * cellSize, size, thinLinePaint); // Vertical
                canvas.drawLine(0, i * cellSize, size, i * cellSize, thinLinePaint); // Horizontal
            }
        }

        // Draw thick lines on top
        for (int i = 0; i <= 9; i++) {
            if (i % 3 == 0) { // Only draw thick lines
                canvas.drawLine(i * cellSize, 0, i * cellSize, size, thickLinePaint); // Vertical
                canvas.drawLine(0, i * cellSize, size, i * cellSize, thickLinePaint); // Horizontal
            }
        }
    }


    private void drawNumbers(Canvas canvas) {
        float size = getWidth();
        float cellSize = size / 9f;

        for (int row = 0; row < 9; row++) {
            for (int col = 0; col < 9; col++) {
                if (board[row][col] != 0) { // Check if cell is not empty
                    String text = String.valueOf(board[row][col]);
                    Paint currentPaint;

                    if (isStartingCell[row][col]) {
                        currentPaint = textPaint; // Bold, primary color for starting numbers
                    } else {
                        // Check against solution only if solutionString is available
                        if (solutionString != null &&
                                (solutionString.charAt(row * 9 + col) - '0') != board[row][col]) {
                            currentPaint = errorTextPaint; // Red, bold for incorrect user numbers
                        } else {
                            currentPaint = userTextPaint; // Accent color for correct user numbers (or if no solution)
                        }
                    }

                    // Calculate text position for centering
                    currentPaint.getTextBounds(text, 0, text.length(), textBounds);
                    float x = col * cellSize + cellSize / 2;
                    // Adjust y slightly higher for better vertical centering within cell
                    float y = row * cellSize + cellSize / 2 + textBounds.height() / 2f - textBounds.bottom;

                    canvas.drawText(text, x, y, currentPaint);
                }
            }
        }
    }


    private void drawHighlights(Canvas canvas) {
        if (selectedRow == -1 || selectedCol == -1) return; // No selection, no highlights

        float size = getWidth();
        float cellSize = size / 9f;
        int selectedValue = board[selectedRow][selectedCol]; // Value in the selected cell

        // 1. Highlight related row, column, and 3x3 box
        int boxStartRow = selectedRow - selectedRow % 3;
        int boxStartCol = selectedCol - selectedCol % 3;
        // Use a slightly lighter/different color for row/col/box highlights
        Paint relatedHighlight = relatedCellHighlightPaint; // You defined this in init()
        for (int i = 0; i < 9; i++) {
            // Highlight Row i (excluding selected cell itself later)
            canvas.drawRect(i * cellSize, selectedRow * cellSize, (i + 1) * cellSize, (selectedRow + 1) * cellSize, relatedHighlight);
            // Highlight Column i (excluding selected cell itself later)
            canvas.drawRect(selectedCol * cellSize, i * cellSize, (selectedCol + 1) * cellSize, (i + 1) * cellSize, relatedHighlight);
        }
        // Highlight 3x3 Box (Overlapping is okay)
        for (int r = boxStartRow; r < boxStartRow + 3; r++) {
            for (int c = boxStartCol; c < boxStartCol + 3; c++) {
                canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, relatedHighlight);
            }
        }

        // 2. Highlight cells with the same number as the selected cell (if selected cell is not empty)
        if (selectedValue != 0) {
            Paint sameNumHighlight = sameNumberHighlightPaint; // Defined in init()
            for (int r = 0; r < 9; r++) {
                for (int c = 0; c < 9; c++) {
                    // Don't highlight the selected cell itself with this color
                    if (board[r][c] == selectedValue && !(r == selectedRow && c == selectedCol)) {
                        canvas.drawRect(c * cellSize, r * cellSize, (c + 1) * cellSize, (r + 1) * cellSize, sameNumHighlight);
                    }
                }
            }
        }

        // 3. Draw the main selected cell highlight (drawn last to be distinctly on top)
        float left = selectedCol * cellSize;
        float top = selectedRow * cellSize;
        Paint selectHighlight = selectedCellPaint; // Defined in init()
        canvas.drawRect(left, top, left + cellSize, top + cellSize, selectHighlight);
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
                if (row != selectedRow || col != selectedCol) {
                    selectedCol = col;
                    selectedRow = row;
                    invalidate();
                }
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

                boolean errorStateChanged = false;
                // Only track errors if solution is available
                if (solutionString != null) {
                    int correctValue = solutionString.charAt(selectedRow * 9 + selectedCol) - '0';
                    boolean wasCorrectBefore = (previousValue != 0 && previousValue == correctValue);
                    boolean isCorrectNow = (number != 0 && number == correctValue);

                    if (!isCorrectNow && number != 0 && (previousValue == 0 || wasCorrectBefore)) {
                        errorCount++;
                        errorStateChanged = true;
                        Log.d("SudokuError", "Incorrect number (" + number + ") placed at (" + selectedRow + "," + selectedCol + "). Error count: " + errorCount);
                    }
                    else if (!wasCorrectBefore && previousValue != 0 && (isCorrectNow || number == 0)) {
                        Log.d("SudokuError", "Incorrect number ("+ previousValue +") corrected/removed at ("+ selectedRow + ","+ selectedCol +"). Error count remains: " + errorCount);
                        errorStateChanged = true; // Visual state changed
                    } else if (wasCorrectBefore && !isCorrectNow && number !=0 ){
                        Log.d("SudokuError", "Correct number ("+ previousValue +") replaced by incorrect ("+ number +") at ("+ selectedRow + ","+ selectedCol +"). Error count: " + errorCount);
                        // Need to increment here because a correct number became incorrect
                        errorCount++;
                        errorStateChanged = true;
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
                    // Log if erasing an incorrect number (only if solution available)
                    if (solutionString != null) {
                        int correctValue = solutionString.charAt(selectedRow * 9 + selectedCol) - '0';
                        if (previousValue != correctValue) {
                            Log.d("SudokuError", "Incorrect number ("+ previousValue +") erased at ("+ selectedRow + ","+ selectedCol +"). Error count remains: " + errorCount);
                        }
                    }
                    board[selectedRow][selectedCol] = 0; // Set cell to empty
                    invalidate(); // Redraw the board
                }
            }
        } else {
            Toast.makeText(getContext(), "Select a cell first.", Toast.LENGTH_SHORT).show();
        }
    }

    /**
     * Fills all non-starting cells with the correct solution numbers.
     * Intended for development/testing purposes only. Requires solutionString to be set.
     */
    public void fillWithSolution() {
        if (!hasSolution()) { // Use the new helper method
            Log.e("SudokuBoardView", "Cannot fill with solution: Solution string is missing or invalid.");
            Toast.makeText(getContext(), "Solution not available.", Toast.LENGTH_SHORT).show();
            return;
        }

        int k = 0;
        boolean changed = false; // Track if any cell was changed
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (!isStartingCell[i][j]) { // Only fill non-starting cells
                    char correctChar = solutionString.charAt(k);
                    int correctNum = 0;
                    if (correctChar >= '1' && correctChar <= '9') {
                        correctNum = correctChar - '0';
                    }
                    // Only update if the current value is different
                    if (board[i][j] != correctNum) {
                        board[i][j] = correctNum;
                        changed = true;
                    }
                }
                k++; // Increment index regardless
            }
        }
        if (changed) {
            errorCount = 0; // Reset error count as the board is now correct
            invalidate(); // Redraw the board with the solution filled in
        } else {
            Toast.makeText(getContext(), "Board already matches solution.", Toast.LENGTH_SHORT).show();
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

    /**
     * Checks if the current board state matches the solution string (if available)
     * AND if the board is completely filled.
     * If solutionString is null, it only checks if the board is full.
     * @return true if the board is correctly solved or full (if no solution available), false otherwise.
     */
    public boolean isSolvedCorrectly() {
        boolean isFull = isBoardFull();
        if (solutionString == null) {
            // No solution to check against (challenge mode), just check if full
            Log.d("SudokuBoardView", "isSolvedCorrectly: No solution available, checking if full: " + isFull);
            return isFull;
        }
        // Solution is available, check if full AND matches solution
        boolean matchesSolution = getBoardString().equals(solutionString);
        Log.d("SudokuBoardView", "isSolvedCorrectly: Solution available. Is full: " + isFull + ", Matches solution: " + matchesSolution);
        return isFull && matchesSolution;
    }

    // Checks if all cells on the board are filled (non-zero)
    public boolean isBoardFull() {
        for (int i = 0; i < 9; i++) {
            for (int j = 0; j < 9; j++) {
                if (board[i][j] == 0) { // 0 represents an empty cell
                    return false;
                }
            }
        }
        return true;
    }

    // *** ADDED HELPER METHOD ***
    /**
     * Checks if the board currently has a valid solution string loaded.
     * @return true if solutionString is not null and has the correct length, false otherwise.
     */
    public boolean hasSolution() {
        return this.solutionString != null && this.solutionString.length() == 81;
    }

    // *** ADDED HELPER METHOD ***
    /**
     * Internal getter for the solution string, used by GameActivity for restart.
     * @return The stored solution string (can be null).
     */
    protected String getSolutionStringInternal() {
        return this.solutionString;
    }


    public int getErrorCount() {
        // Return error count only if solution is available to calculate it accurately
        return (solutionString != null) ? errorCount : 0;
    }

}


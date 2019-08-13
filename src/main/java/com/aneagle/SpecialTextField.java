package com.aneagle;

/*
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.

 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */


import javafx.application.Platform;
import javafx.beans.NamedArg;
import javafx.scene.control.TextField;
import javafx.scene.input.KeyEvent;

import java.util.HashSet;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Text field with a constant text length which same as the mask length. Available symbols are set via the mask.
 * Mask symbols is same as in AWT except [*] where excluded an empty character symbol.
 */
public class SpecialTextField extends TextField {
    private static final String MASK_NUMBER = "#";
    private static final String MASK_CHARACTER = "?";
    private static final String MASK_HEXADECIMAL = "H";
    private static final String MASK_UPPER_CHARACTER = "U";
    private static final String MASK_LOWER_CHARACTER = "L";
    private static final String MASK_CHAR_OR_NUM = "A";
    private static final String MASK_ANYTHING = "*";

    private static final Pattern PATTERN_NUMBER = Pattern.compile("[0-9]");
    private static final Pattern PATTERN_CHARACTER = Pattern.compile("[A-z]");
    private static final Pattern PATTERN_HEXADECIMAL = Pattern.compile("[0-9A-Fa-f]");
    private static final Pattern PATTERN_UPPER_CHARACTER = Pattern.compile("[A-z]");
    private static final Pattern PATTERN_LOWER_CHARACTER = Pattern.compile("[A-z]");
    private static final Pattern PATTERN_CHAR_OR_NUM = Pattern.compile("[0-9A-z]");
    private static final char EMPTY_CHAR = "_".charAt(0);
    private final Pattern PATTERN_ANYTHING;
    private final String mask;
    private String text;
    private Set<Character> specialSymbols = new HashSet<>();


    /**
     * Constructor.
     *
     * @param mask - mask expression:
     *             # - any valid number [0-9];
     *             ? - any character [A-z];
     *             H - any hexadecimal character [0-9A-Fa-f];
     *             U - any character. All lowercase character are mapped to upper case;
     *             L - any character. All uppercase character are mapped to lower case;
     *             A - any character or number [0-9A-z];
     *             * - any symbol except a symbol of empty character, i.e. [_]. [^_]
     */
    public SpecialTextField(@NamedArg("mask") String mask) {
        super();
        this.mask = mask;
        this.text = textFromMask(specialSymbols);

        StringBuilder expression = new StringBuilder("[");
        for (int i = 1; i < specialSymbols.toString().length() - 1; i++) {
            expression.append("^").append(specialSymbols.toString().charAt(i));
        }
        expression.append("^").append(EMPTY_CHAR).append("]");

        PATTERN_ANYTHING = Pattern.compile(expression.toString());

        this.caretPositionProperty().addListener((observable1, oldValue, newValue) -> {
            int caretPosition = (int) newValue;
            if (caretPosition >= text.length()) {
                Platform.runLater(() -> positionCaret(text.length()));
                return;
            }
            if (caretPosition < 0) {
                Platform.runLater(() -> positionCaret(0));
            }
        });

        this.focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (newValue && (getText() == null || getText().equals(""))) {
                this.setText(text);
                Platform.runLater(() -> positionCaret(0));
            }
            if (!newValue && getText().equals(textFromMask(null))) {
                this.setText("");
            }
        });

        this.textProperty().addListener((observable, oldValue, newValue) -> {
            if (oldValue != null && newValue != null && !newValue.equals("") &&
                    (text == null || !text.equals(newValue))) {
                final int caretPosition = getCaretPosition();
                if (caretPosition >= oldValue.length() || isSpecial(oldValue.charAt(caretPosition))) {
                    text = oldValue;
                    setText(text);
                    return;
                }
                char challenger = newValue.charAt(caretPosition);
                String currentMask = String.valueOf(mask.charAt(caretPosition));
                Pattern currentPattern;

                switch (currentMask) {
                    case MASK_NUMBER:
                        currentPattern = PATTERN_NUMBER;
                        break;
                    case MASK_CHARACTER:
                        currentPattern = PATTERN_CHARACTER;
                        break;
                    case MASK_ANYTHING:
                        currentPattern = PATTERN_ANYTHING;
                        break;
                    case MASK_HEXADECIMAL:
                        currentPattern = PATTERN_HEXADECIMAL;
                        break;
                    case MASK_UPPER_CHARACTER:
                        currentPattern = PATTERN_UPPER_CHARACTER;
                        break;
                    case MASK_LOWER_CHARACTER:
                        currentPattern = PATTERN_LOWER_CHARACTER;
                        break;
                    case MASK_CHAR_OR_NUM:
                        currentPattern = PATTERN_CHAR_OR_NUM;
                        break;
                    default:
                        System.out.println("Error of Default");
                        throw new IllegalArgumentException();
                }

                Matcher matcher = currentPattern.matcher(String.valueOf(challenger));

                if (currentPattern == PATTERN_UPPER_CHARACTER)
                    challenger = String.valueOf(challenger).toUpperCase().charAt(0);
                if (currentPattern == PATTERN_LOWER_CHARACTER)
                    challenger = String.valueOf(challenger).toLowerCase().charAt(0);

                if (matcher.matches()) {
                    text = (replaceInsteadInsertion(newValue, challenger, caretPosition));
                    if (caretPosition + 1 < text.length() && isSpecial(text.charAt(caretPosition + 1))) {
                        Platform.runLater(() -> positionCaret(caretPosition + 2));
                    } else Platform.runLater(() -> positionCaret(caretPosition + 1));
                } else {
                    text = oldValue;
                }
                this.setText(text);

            } else if (newValue != null && newValue.length() == mask.length()) {
                text = newValue;
                textFromMask(specialSymbols);
                this.setText(text);
            }
        });

        this.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getEventType() == KeyEvent.KEY_PRESSED) {
                final int caretPosition = getCaretPosition();
                switch (event.getCode()) {
                    case BACK_SPACE:
                        if (caretPosition != 0) {
                            for (int i = 1; caretPosition - i >= 0; i++) {
                                if (!isSpecial(text.charAt(caretPosition - i))) {
                                    text = replaceCharAtPosition(text, EMPTY_CHAR, caretPosition - i);
                                    this.setText(text);
                                    final int j = i;
                                    Platform.runLater(() -> positionCaret(caretPosition - j));
                                    break;
                                }
                            }
                        }
                        event.consume();
                        break;
                    case DELETE:
                        for (int i = 0; caretPosition + i < text.length(); i++) {
                            if (!isSpecial(text.charAt(caretPosition + i))) {
                                text = replaceCharAtPosition(text, EMPTY_CHAR, caretPosition + i);
                                this.setText(text);
                                final int j = i + 1;
                                Platform.runLater(() -> positionCaret(caretPosition + j));
                                break;
                            }
                        }
                        event.consume();
                        break;
                }
            }
        });
    }

    private static String replaceInsteadInsertion(String str, char ch, int pos) {
        char[] buffer = new char[str.toCharArray().length];
        for (int i = 0, j = 0; i < buffer.length; i++, j++) {
            if (i != pos) {
                buffer[j] = str.toCharArray()[i];
            } else {
                buffer[j] = ch;
                ++i;
            }
        }
        String result = new String(buffer);
        result = result.substring(0, result.length() - 1);
        return result;
    }

    private static String replaceCharAtPosition(String str, char ch, int pos) {
        char[] buffer = new char[str.toCharArray().length];
        for (int i = 0; i < buffer.length; i++) {
            if (i != pos) buffer[i] = str.toCharArray()[i];
            else buffer[i] = ch;
        }
        str = new String(buffer);
        return str;
    }

    @Override
    public void replaceText(int start, int end, String text) {
        if (start == end && !text.equals("")) super.replaceText(start, end, text);
    }

    /**
     * Method used for check an empty characters.
     *
     * @return true - no empty characters
     */
    public boolean isFilled() {
        for (char ch : text.toCharArray()) {
            if (ch == EMPTY_CHAR) return false;
        }
        return true;
    }

    private String textFromMask(Set<Character> specialSymbols) {
        String tempText = mask.replace(MASK_ANYTHING, String.valueOf(EMPTY_CHAR));
        tempText = tempText.replace(MASK_CHARACTER, String.valueOf(EMPTY_CHAR));
        tempText = tempText.replace(MASK_NUMBER, String.valueOf(EMPTY_CHAR));
        tempText = tempText.replace(MASK_HEXADECIMAL, String.valueOf(EMPTY_CHAR));
        tempText = tempText.replace(MASK_UPPER_CHARACTER, String.valueOf(EMPTY_CHAR));
        tempText = tempText.replace(MASK_LOWER_CHARACTER, String.valueOf(EMPTY_CHAR));
        tempText = tempText.replace(MASK_CHAR_OR_NUM, String.valueOf(EMPTY_CHAR));
        if (specialSymbols != null) {
            for (int i = 0; i < tempText.length(); i++) {
                char ch = tempText.charAt(i);
                if (ch != EMPTY_CHAR) specialSymbols.add(ch);
            }
        }
        return tempText;
    }

    private boolean isSpecial(char character) {
        for (char ch : specialSymbols) {
            if (character == ch) return true;
        }
        return false;
    }

    public void forceSetText(String text) {
        this.text = text;
    }
}

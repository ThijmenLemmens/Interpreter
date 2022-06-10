import tokens.Token;
import tokens.TokenType;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Scanner {

    private final String src;
    private final List<Token> tokens = new ArrayList<>();
    private static final Map<String, TokenType> keywords = new HashMap<>();
    private int start = 0;
    private int current = 0;
    private int line = 1;

    static {
        keywords.put("&&", TokenType.AND);
        keywords.put("class", TokenType.CLASS);
        keywords.put("else", TokenType.ELSE);
        keywords.put("false", TokenType.FALSE);
        keywords.put("fnc", TokenType.FNC);
        keywords.put("for", TokenType.FOR);
        keywords.put("if", TokenType.IF);
        keywords.put("NULL", TokenType.NULL);
        keywords.put("||", TokenType.OR);
        keywords.put("println", TokenType.PRINTLN);
        keywords.put("return", TokenType.RETURN);
        keywords.put("super", TokenType.SUPER);
        keywords.put("this", TokenType.THIS);
        keywords.put("true", TokenType.TRUE);
        keywords.put("var", TokenType.VAR);
        keywords.put("while", TokenType.WHILE);
    }

    public Scanner(String src) {
        this.src = src;
    }

    public List<Token> scanTokens() {
        while (!isAtEnd()) {
            start = current;
            scanToken();
        }

        tokens.add(new Token(TokenType.EOF, "", null, line));
        return tokens;
    }

    private void scanToken() {
        char c = advance();
        switch (c) {
            case '(' -> addToken(TokenType.LEFT_PAREN);
            case ')' -> addToken(TokenType.RIGHT_PAREN);
            case '{' -> addToken(TokenType.LEFT_BRACE);
            case '}' -> addToken(TokenType.RIGHT_BRACE);
            case ',' -> addToken(TokenType.COMMA);
            case '.' -> addToken(TokenType.DOT);
            case '-' -> addToken(TokenType.MINUS);
            case '+' -> addToken(TokenType.PLUS);
            case ';' -> addToken(TokenType.SEMICOLON);
            case '*' -> addToken(TokenType.STAR);
            case '!' -> addToken(match('=') ? TokenType.BANG_EQUAL : TokenType.BANG);
            case '=' -> addToken(match('=') ? TokenType.EQUAL_EQUAL : TokenType.EQUAL);
            case '>' -> addToken(match('=') ? TokenType.GREATER_EQUAL : TokenType.GREATER);
            case '<' -> addToken(match('=') ? TokenType.LESS_EQUAL : TokenType.LESS);
            case '/' -> {
                if (match('/')) {
                    while(peek() != '\n' && !isAtEnd()) advance();
                } else if (match('*')) {
                    blockComment();
                } else {
                    addToken(TokenType.SLASH);
                }
            }
            case '|' -> {
                if (match('|')) {
                    addToken(TokenType.OR);
                }
            }
            case ' ', '\r', '\t' -> {}
            case '\n' -> line++;
            case '"' -> string();
            default ->  {
                if (isDigit(c)) {
                    number();
                } else if (isAlpha(c)) {
                    identifier();
                } else {
                    Main.error(line, "Unexpected character.");
                }
            }
        }
    }

    private char advance() {
        return src.charAt(current++);
    }

    private char peek() {
        if (isAtEnd()) return '\0';
        return src.charAt(current);
    }

    private char peekNext() {
        if (current + 1 >= src.length()) return '\0';
        return src.charAt(current + 1);
    }

    private boolean match(char expected) {
        if (isAtEnd()) return false;
        if (src.charAt(current) != expected) return false;

        current++;
        return true;
    }

    private void addToken(TokenType tokenType) {
        addToken(tokenType, null);
    }

    private void addToken(TokenType tokenType , Object literal) {
        String text = src.substring(start, current);
        tokens.add(new Token(tokenType, text, literal, line));
    }

    private void blockComment() {
        while (peek() != '*' && !isAtEnd()) {
            if (peek() == '/') {
                advance();
            }
            advance();
        }
        advance();
        advance();
    }

    private void string() {
        while (peek() != '"' && !isAtEnd()) {
            if (peek() == '\n') line++;
            advance();
        }

        if (isAtEnd()) {
            Main.error(line, "Unterminated string.");
            return;
        }

        advance();

        String string = src.substring(start + 1, current - 1);
        addToken(TokenType.STRING, string);
    }

    private void number() {
        while (isDigit(peek())) advance();

        if (peek() == '.' && !isAtEnd()) {
            advance();
            while (isDigit(peek())) advance();
        }
        addToken(TokenType.NUMBER, Double.parseDouble(src.substring(start, current)));
    }

    private void identifier() {
        while (isAlphaNumeric(peek())) advance();
        String string = src.substring(start, current);
        TokenType tokenType = keywords.get(string);
        if (tokenType == null) tokenType = TokenType.IDENTIFIER;
        addToken(tokenType);
    }

    private boolean isAlpha(char c) {
        return c >= 'a' && c <= 'z' || c >= 'A' && c <= 'Z' || c == '_';
    }

    private boolean isDigit(char c) {
        return c >= '0' && c <= '9';
    }

    private boolean isAlphaNumeric(char c) {
        return isAlpha(c) ||isDigit(c);
    }


    private boolean isAtEnd() {
        return current >= src.length();
    }
}

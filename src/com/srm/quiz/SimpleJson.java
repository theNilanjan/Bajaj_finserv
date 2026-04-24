package com.srm.quiz;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

public class SimpleJson {
    public static Object parse(String text) {
        return new Parser(text).parseValue();
    }

    public static String stringify(Object value) {
        if (value == null) {
            return "null";
        }
        if (value instanceof String stringValue) {
            return "\"" + escape(stringValue) + "\"";
        }
        if (value instanceof Number || value instanceof Boolean) {
            return String.valueOf(value);
        }
        if (value instanceof Map<?, ?> map) {
            StringBuilder builder = new StringBuilder("{");
            boolean first = true;
            for (Map.Entry<?, ?> entry : map.entrySet()) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(stringify(String.valueOf(entry.getKey())));
                builder.append(':');
                builder.append(stringify(entry.getValue()));
            }
            return builder.append('}').toString();
        }
        if (value instanceof List<?> list) {
            StringBuilder builder = new StringBuilder("[");
            boolean first = true;
            for (Object item : list) {
                if (!first) {
                    builder.append(',');
                }
                first = false;
                builder.append(stringify(item));
            }
            return builder.append(']').toString();
        }

        throw new IllegalArgumentException("Unsupported value type: " + value.getClass().getName());
    }

    @SuppressWarnings("unchecked")
    public static Map<String, Object> asObject(Object value) {
        return (Map<String, Object>) value;
    }

    @SuppressWarnings("unchecked")
    public static List<Object> asArray(Object value) {
        return (List<Object>) value;
    }

    private static String escape(String value) {
        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\b", "\\b")
                .replace("\f", "\\f")
                .replace("\n", "\\n")
                .replace("\r", "\\r")
                .replace("\t", "\\t");
    }

    private static final class Parser {
        private final String text;
        private int index;

        private Parser(String text) {
            this.text = text;
        }

        private Object parseValue() {
            skipWhitespace();
            if (index >= text.length()) {
                throw new IllegalArgumentException("Unexpected end of JSON input");
            }

            char current = text.charAt(index);
            return switch (current) {
                case '{' -> parseObject();
                case '[' -> parseArray();
                case '"' -> parseString();
                case 't' -> parseLiteral("true", Boolean.TRUE);
                case 'f' -> parseLiteral("false", Boolean.FALSE);
                case 'n' -> parseLiteral("null", null);
                default -> parseNumber();
            };
        }

        private Map<String, Object> parseObject() {
            Map<String, Object> object = new LinkedHashMap<>();
            index++;
            skipWhitespace();

            if (peek('}')) {
                index++;
                return object;
            }

            while (true) {
                skipWhitespace();
                String key = parseString();
                skipWhitespace();
                expect(':');
                Object value = parseValue();
                object.put(key, value);
                skipWhitespace();

                if (peek('}')) {
                    index++;
                    return object;
                }

                expect(',');
            }
        }

        private List<Object> parseArray() {
            List<Object> array = new ArrayList<>();
            index++;
            skipWhitespace();

            if (peek(']')) {
                index++;
                return array;
            }

            while (true) {
                array.add(parseValue());
                skipWhitespace();

                if (peek(']')) {
                    index++;
                    return array;
                }

                expect(',');
            }
        }

        private String parseString() {
            expect('"');
            StringBuilder builder = new StringBuilder();

            while (index < text.length()) {
                char current = text.charAt(index++);
                if (current == '"') {
                    return builder.toString();
                }
                if (current != '\\') {
                    builder.append(current);
                    continue;
                }

                if (index >= text.length()) {
                    throw new IllegalArgumentException("Unexpected end of escaped string");
                }

                char escaped = text.charAt(index++);
                switch (escaped) {
                    case '"', '\\', '/' -> builder.append(escaped);
                    case 'b' -> builder.append('\b');
                    case 'f' -> builder.append('\f');
                    case 'n' -> builder.append('\n');
                    case 'r' -> builder.append('\r');
                    case 't' -> builder.append('\t');
                    case 'u' -> builder.append(parseUnicode());
                    default -> throw new IllegalArgumentException("Unsupported escape sequence: \\" + escaped);
                }
            }

            throw new IllegalArgumentException("Unterminated string literal");
        }

        private char parseUnicode() {
            if (index + 4 > text.length()) {
                throw new IllegalArgumentException("Incomplete unicode escape");
            }

            String hex = text.substring(index, index + 4);
            index += 4;
            return (char) Integer.parseInt(hex, 16);
        }

        private Object parseLiteral(String literal, Object value) {
            if (!text.startsWith(literal, index)) {
                throw new IllegalArgumentException("Expected literal: " + literal);
            }
            index += literal.length();
            return value;
        }

        private Number parseNumber() {
            int start = index;

            if (peek('-')) {
                index++;
            }

            while (index < text.length() && Character.isDigit(text.charAt(index))) {
                index++;
            }

            if (peek('.')) {
                index++;
                while (index < text.length() && Character.isDigit(text.charAt(index))) {
                    index++;
                }
                return Double.parseDouble(text.substring(start, index));
            }

            return Long.parseLong(text.substring(start, index));
        }

        private boolean peek(char expected) {
            return index < text.length() && text.charAt(index) == expected;
        }

        private void expect(char expected) {
            skipWhitespace();
            if (!peek(expected)) {
                throw new IllegalArgumentException("Expected '" + expected + "' at index " + index);
            }
            index++;
        }

        private void skipWhitespace() {
            while (index < text.length() && Character.isWhitespace(text.charAt(index))) {
                index++;
            }
        }
    }
}

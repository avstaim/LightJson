package com.staim.lightjson.implementations.parsers;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonParser;
import com.staim.lightjson.JsonType;
import com.staim.lightjson.implementations.elements.JsonArrayElement;
import com.staim.lightjson.implementations.elements.JsonNullElement;
import com.staim.lightjson.implementations.elements.JsonObjectElement;
import com.staim.lightjson.implementations.elements.JsonPlainElement;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * New Parser, scalable for BIG JSONs
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public class ParserScalable implements JsonParser {
    @Override
    public JsonElement parse(String json) throws JsonException {
        final String jsonString = json.trim();

        // Try trivial variants
        if ("{}".equals(jsonString) || "{ }".equals(jsonString))  // Empty Object
            return new JsonObjectElement();
        if ("[]".equals(jsonString) || "[ ]".equals(jsonString)) // Empty Array
            return new JsonArrayElement();

        JsonReader reader = new JsonReader(jsonString);
        try {
            return parse(reader);
        } catch (ClosingSymbolException e) {
            throw new JsonException("Unexpected symbol: " + e.getMessage());
        }
    }

    private int checkChar(char c) {
        switch (c) {
            case '{': return 1;
            case '[': return 2;
            case '\"': return 3;
            case '-': return 4;
            case '}':
            case ']': return 6;
            default: {
                if (Character.isDigit(c)) return 4;
                if (Character.isLetter(c)) return 5;
                return 0;
            }
        }
    }

    private class ClosingSymbolException extends Exception {
        public ClosingSymbolException(String message) {
            super(message);
        }
    }

    private JsonElement parse(JsonReader reader) throws JsonException, ClosingSymbolException {
        final int cc[] = new int[1];
        final char cl[] = new char[1];

        reader.skip(new CharacterChecker() {
            @Override
            public boolean check(char c) {
                cl[0] = c;
                cc[0] = checkChar(c);
                return cc[0] == 0;
            }
        });

        switch (cc[0]) {
            case 1: return parseObject(reader);
            case 2: return parseArray(reader);
            case 3: return parseString(reader);
            case 4: return parseNumber(reader);
            case 5: return parseOther(reader);
            case 6: throw new ClosingSymbolException(Character.toString(cl[0]));
        }
        return null;
    }

    private JsonElement parseObject(JsonReader reader) throws JsonException {
        Map<String, JsonElement> elements = new HashMap<>();
        try {
            char c = reader.read();
            if (c != '{') throw new JsonException("Internal Error");

            do {
                reader.skip(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != '\"' && !Character.isDigit(c) && c != '}';
                    }
                });
                c = reader.read();
                String key;
                if (c == '}') break;
                else if (c == '\"') {
                    key = reader.read(new CharacterChecker() {
                        char lastChar = 0;
                        @Override
                        public boolean check(char c) {
                            boolean terminated = lastChar == '\\';
                            lastChar = c;
                            return terminated || c != '\"';
                        }
                    });
                } else {
                    reader.stepBack();
                    key = reader.read(new CharacterChecker() {
                        @Override
                        public boolean check(char c) {
                            return Character.isDigit(c) || c == '.';
                        }
                    });
                }

                reader.skipToCharNoBack(':');
                elements.put(key, parse(reader));
                reader.skip(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != ',' && c != '}';
                    }
                });
                c = reader.read();
            } while (c != '}');
        } catch (ClosingSymbolException ignored) {}
        return new JsonObjectElement(elements);
    }

    private JsonElement parseArray(JsonReader reader) throws JsonException {
        List<JsonElement> elementList = new ArrayList<>();
        try {
            char c = reader.read();
            if (c != '[') throw new JsonException("Internal Error");

            do {
                elementList.add(parse(reader));

                reader.skip(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != ',' && c != ']';
                    }
                });

                c = reader.read();
            } while (c != ']');
        } catch (ClosingSymbolException ignored) {}
        return new JsonArrayElement(elementList);
    }

    private JsonElement parseString(JsonReader reader) throws JsonException {
        reader.skipNoBack(new CharacterChecker() {
            @Override
            public boolean check(char c) {
                return c != '\"';
            }
        });

        String string = reader.read(new CharacterChecker() {
            char lastChar = 0;
            @Override
            public boolean check(char c) {
                boolean terminated = lastChar == '\\';
                lastChar = c;
                return terminated || c != '\"';
            }
        });

        if (string.startsWith("\""))
            string = string.substring(1, string.length() - 1);
        return new JsonPlainElement<>(string, JsonType.STRING);
    }

    private JsonElement parseNumber(JsonReader reader) throws JsonException {
        final boolean[] isSeparatorGlobal = new boolean[1];
        isSeparatorGlobal[0] = false;
        String string = reader.read(new CharacterChecker() {
            @Override
            public boolean check(char c) {
                boolean isSeparator = (c == '.');
                if (isSeparator) isSeparatorGlobal[0] = true;
                return Character.isDigit(c) || c == '-' || isSeparator || c == 'e';
            }
        });
        @SuppressWarnings("RedundantCast")
        Number number = isSeparatorGlobal[0] ? (Number)Double.parseDouble(string) : (Number)Long.parseLong(string);
        return new JsonPlainElement<>(number, JsonType.NUMBER);
    }

    private JsonElement parseOther(JsonReader reader) throws JsonException {
        String string = reader.read(new CharacterChecker() {
            @Override
            public boolean check(char c) {
                return Character.isLetter(c);
            }
        });
        if (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes"))
            return new JsonPlainElement<>(true, JsonType.BOOLEAN);
        if (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("no"))
            return new JsonPlainElement<>(false, JsonType.BOOLEAN);
        if (string.equalsIgnoreCase("null") || string.equalsIgnoreCase("nil"))
            return new JsonNullElement();

        if (string.equalsIgnoreCase("nan"))
            return new JsonPlainElement<>(Double.NaN, JsonType.NUMBER);
        if (string.equalsIgnoreCase("inf") || string.equalsIgnoreCase("+inf") || string.equalsIgnoreCase("infinity") || string.equalsIgnoreCase("+infinity"))
            return new JsonPlainElement<>(Double.POSITIVE_INFINITY, JsonType.NUMBER);
        if (string.equalsIgnoreCase("-inf") || string.equalsIgnoreCase("-infinity"))
            return new JsonPlainElement<>(Double.NEGATIVE_INFINITY, JsonType.NUMBER);

        return new JsonPlainElement<>(string, JsonType.STRING);
    }

    interface CharacterChecker {
        boolean check(char c);
    }

    public class JsonReader {
        private String string;
        private int length;
        private int next = 0;

        public JsonReader(String string)  {
            this.string = string;
            this.length = string.length();
        }

        public void stepBack() {
           if (next > 0) next--;
        }

        public char read() throws JsonException {
            if (next >= length)
                throw new JsonException("Unexpected End of Json");
            return string.charAt(next++);
        }

        public String read(CharacterChecker checker) throws JsonException {
            char c;
            StringBuilder stringBuilder = new StringBuilder();
            boolean check;
            do {
                c = read();
                check = checker.check(c);
                if (check) stringBuilder.append(c);
            } while (check);
            stepBack();
            return stringBuilder.toString();
        }

        public void skip(CharacterChecker checker) throws JsonException {
            char c;
            do { c = read(); } while (checker.check(c));
            stepBack();
        }

        public void skipNoBack(CharacterChecker checker) throws JsonException {
            char c;
            do { c = read(); } while (checker.check(c));
        }

        public void skipToCharNoBack(char ch) throws JsonException {
            int found = string.indexOf(ch, next);
            if (found == -1) throw new JsonException("Unexpected End of Json");
            next = found + 1;
        }
    }
}

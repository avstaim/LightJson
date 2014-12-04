package com.staim.lightjson.implementations.parsers;

import com.staim.lightjson.JsonElement;
import com.staim.lightjson.JsonException;
import com.staim.lightjson.JsonParser;
import com.staim.lightjson.implementations.elements.*;

import java.io.IOException;
import java.io.Reader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * New Parser
 *
 * Created by alexeyshcherbinin on 03.12.14.
 */
public class ParserScalable implements JsonParser {

    @Override
    public JsonElement parse(String json) throws JsonException {
        final String jsonString = json.trim();

        // Try trivial variants
        if ("{}".equals(jsonString) || "{ }".equals(jsonString))  // Empty Object
            return new JsonObject();
        if ("[]".equals(jsonString) || "[ ]".equals(jsonString)) // Empty Array
            return new JsonArray();

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
            char c = (char)reader.read();
            if (c != '{') throw new JsonException("Internal Error");

            do {
                reader.skip(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != '\"' && !Character.isDigit(c) && c != '}';
                    }
                });
                c = (char)reader.read();
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

                reader.skipNoBack(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != ':';
                    }
                });

                elements.put(key, parse(reader));

                reader.skip(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != ',' && c != '}';
                    }
                });

                c = (char) reader.read();
            } while (c != '}');
        } catch (ClosingSymbolException ignored) {
        } catch (IOException e) {
            throw new JsonException(e.getMessage());
        }

        return new JsonObject(elements);
    }

    private JsonElement parseArray(JsonReader reader) throws JsonException {
        List<JsonElement> elementList = new ArrayList<>();
        try {
            char c = (char)reader.read();
            if (c != '[') throw new JsonException("Internal Error");

            do {
                elementList.add(parse(reader));

                reader.skip(new CharacterChecker() {
                    @Override
                    public boolean check(char c) {
                        return c != ',' && c != ']';
                    }
                });

                c = (char) reader.read();
            } while (c != ']');
        } catch (ClosingSymbolException ignored) {
        } catch (IOException e) {
            throw new JsonException(e.getMessage());
        }

        return new JsonArray(elementList);
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

        if (string.startsWith("/"))
            string = string.substring(1, string.length() - 1);
        return new JsonString(string);
    }

    private JsonElement parseNumber(JsonReader reader) throws JsonException {
        final boolean[] isSeparatorGlobal = new boolean[1];
        String string = reader.read(new CharacterChecker() {
            @Override
            public boolean check(char c) {
                boolean isSeparator = (c == '.');
                if (isSeparator) isSeparatorGlobal[0] = true;
                return Character.isDigit(c) || c == '-' || isSeparator;
            }
        });
        Number number = isSeparatorGlobal[0] ? Double.parseDouble(string) : Long.parseLong(string);
        return new JsonNumber(number);
    }

    private JsonElement parseOther(JsonReader reader) throws JsonException {
        String string = reader.read(new CharacterChecker() {
            @Override
            public boolean check(char c) {
                return Character.isLetter(c);
            }
        });
        if (string.equalsIgnoreCase("true") || string.equalsIgnoreCase("yes"))
            return new JsonBoolean(true);
        if (string.equalsIgnoreCase("false") || string.equalsIgnoreCase("no"))
            return new JsonBoolean(false);
        if (string.equalsIgnoreCase("null") || string.equalsIgnoreCase("nil"))
            return new JsonNull();

        if (string.equalsIgnoreCase("nan"))
            return new JsonNumber(Double.NaN);
        if (string.equalsIgnoreCase("inf") || string.equalsIgnoreCase("+inf") || string.equalsIgnoreCase("infinity") || string.equalsIgnoreCase("+infinity"))
            return new JsonNumber(Double.POSITIVE_INFINITY);
        if (string.equalsIgnoreCase("-inf") || string.equalsIgnoreCase("-infinity"))
            return new JsonNumber(Double.NEGATIVE_INFINITY);

        return new JsonString(string);
    }

    interface CharacterChecker {
        boolean check(char c);
    }

    @SuppressWarnings("SynchronizeOnNonFinalField")
    public class JsonReader extends Reader {
        private String str;
        private int length;
        private int next = 0;

        public JsonReader(String s)  {
            this.str = s;
            this.length = s.length();
        }

        private void ensureOpen() throws IOException {
            if (str == null)
                throw new IOException("Stream closed");
        }

        @Override
        public int read() throws IOException {
            synchronized (lock) {
                ensureOpen();
                if (next >= length)
                    throw new IOException("Unexpected End of Stream");
                return str.charAt(next++);
            }
        }

        @SuppressWarnings("NullableProblems")
        @Override
        public int read(char[] buf, int off, int len) throws IOException {
            throw new IOException("Not supported");
        }

        @Override
        public long skip(long ns) throws IOException {
            synchronized (lock) {
                ensureOpen();
                if (next >= length)
                    return 0;
                // Bound skip by beginning and end of the source
                long n = Math.min(length - next, ns);
                n = Math.max(-next, n);
                next += n;
                return n;
            }
        }

        public void close() {
            str = null;
        }

        public void stepBack() {
           if (next > 0) next--;
        }

        public String read(CharacterChecker checker) throws JsonException {
            try {
                char c;
                StringBuilder stringBuilder = new StringBuilder();
                boolean check;
                do {
                    c = (char) read();
                    check = checker.check(c);
                    if (check) stringBuilder.append(c);
                } while (check);
                stepBack();
                return  stringBuilder.toString();
            } catch (IOException e) {
                throw new JsonException(e.getMessage());
            }
        }

        public void skip(CharacterChecker checker) throws JsonException {
            try {
                char c;
                do {
                    c = (char) read();
                } while (checker.check(c));
                stepBack();
            } catch (IOException e) {
                throw new JsonException(e.getMessage());
            }
        }

        public void skipNoBack(CharacterChecker checker) throws JsonException {
            try {
                char c;
                do {
                    c = (char) read();
                } while (checker.check(c));
            } catch (IOException e) {
                throw new JsonException(e.getMessage());
            }
        }
    }
}

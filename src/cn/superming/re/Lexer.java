package cn.superming.re;

/**
 * 词法解析器
 */
class Lexer {
    enum Tag{
        START,          // 词法解析器启动时的标签
        CARET,          // ^
        DOLLAR,         // $
        PAREN_LEFT,     // (
        PAREN_RIGHT,    // )
        SQUARE_LEFT,    // [
        SQUARE_RIGHT,   // ]
        DASH,           // -
        OR,             // |
        ANY,            // .
        OPT,            // ?
        CLOSURE,        // *
        PLUS,           // +
        L,              // 常规字符
        END             // 解析完成后的标签
    }

    private final int ASCII_COUNT = 128;
    private Tag[] tagMap = null;            // 标签表数组
    private int charIndex = 0;              // 指示当前解析到第几个字符
    private String currentRegex = "";       // 当前正在解析的正则表达式
    private Tag currentTag = Tag.START;     // 当前字符对应的标签
    private char currentChar = '\0';        // 当前解析的字符

    private boolean isEscape;               // 是否有转义符

    /**
     * 初始化标签表
     */
    private void initTagMap(){
        for (int i = 0; i < ASCII_COUNT; i++) {
            tagMap[i] = Tag.L;
        }

        tagMap['^'] = Tag.CARET;
        tagMap['$'] = Tag.DOLLAR;
        tagMap['('] = Tag.PAREN_LEFT;
        tagMap[')'] = Tag.PAREN_RIGHT;
        tagMap['['] = Tag.SQUARE_LEFT;
        tagMap[']'] = Tag.SQUARE_RIGHT;
        tagMap['-'] = Tag.DASH;
        tagMap['|'] = Tag.OR;
        tagMap['.'] = Tag.ANY;
        tagMap['?'] = Tag.OPT;
        tagMap['*'] = Tag.CLOSURE;
        tagMap['+'] = Tag.PLUS;
    }

    Lexer(){
        tagMap = new Tag[ASCII_COUNT];
        initTagMap();
    }

    Lexer(String regex){
        tagMap = new Tag[ASCII_COUNT];
        currentRegex = regex;
        initTagMap();
    }


    void setRegex(String regex) {
        currentRegex = regex;
    }

    boolean matchCurrentTag(Tag tag){ return currentTag == tag; }

    char getCurrentChar() { return currentChar; }
    Tag getCurrentTag() { return currentTag; }

    /**
     * 每调用一次将处理一个字符
     */
    void advance(){
        if (charIndex >= currentRegex.length()){ // 已经解析完成
            currentTag = Tag.END;                // 将标签设为END
            return;
        }

        currentChar = currentRegex.charAt(charIndex);
        isEscape = (currentChar == '\\');
        if (isEscape){           // 如果遇到了转移字符
            charIndex++;         // 跳过转义字符
            currentChar = currentRegex.charAt(charIndex);
            currentTag = Tag.L;  // 只能转义特殊字符，将它们当作字符常量
        }
        else {
            currentTag = tagMap[currentChar];
        }

        charIndex++;            // 当前字符索引结束，指向下一个
    }

    /**
     * 重启词法解析器
     */
    void restart(){
        charIndex = 0;
        currentRegex = "";
        currentChar = '\0';
        currentTag = Tag.START;
    }
}

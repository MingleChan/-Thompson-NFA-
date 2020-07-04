package cn.superming.re;

import java.util.Set;

class NFAConstructor {
    private Lexer lexer;
    private StateNodePool nodePool;

    NFAConstructor(){
        lexer = new Lexer();
        nodePool = new StateNodePool();
    }

    /**
     * 构建状态机的接口
     * @param pair
     */
    NodePair construct(String regex, NodePair pair){
        if (lexer.matchCurrentTag(Lexer.Tag.END)){  // 如果解析到头, 重启解析器.
            lexer.restart();
        }

        lexer.setRegex(regex);  // 设置要解析的正则表达式
        lexer.advance();        // 先处理一个字符

        nodePool = new StateNodePool();
        pair = new NodePair();
        expression(pair);   // 构建NFA.
        pair.endNode.setEdge(StateNode.EMPTY);  // 设置尾节点的edge为EMPTY.
        return pair;       // 返回头结点和尾节点
    }
    /**
     * 为单个字符构建状态机
     * @return
     */
    private boolean constructForSingleChar(NodePair pair){
        if (!lexer.matchCurrentTag(Lexer.Tag.L)){
            return false;
        }

        pair.startNode = nodePool.getStateNode();       // 左节点
        pair.endNode = nodePool.getStateNode();         // 右节点
        pair.startNode.out = pair.endNode;              // 左节点指向右节点
        pair.startNode.setEdge(lexer.getCurrentChar()); // 将边设置为当前解析完的字符

        lexer.advance();                            // 解析下一个字符
        return true;
    }

    /**
     * 为任意匹配符(.)构建状态机
     * @return
     */
    private boolean constructForAny(NodePair pair){
        if (!lexer.matchCurrentTag(Lexer.Tag.ANY)){
            return false;
        }

        pair.startNode = nodePool.getStateNode();       // 左节点
        pair.endNode = nodePool.getStateNode();         // 右节点
        pair.startNode.out = pair.endNode;              // 左节点指向右节点
        pair.startNode.setEdge(StateNode.CHARSET);      // 将边设置为字符集

        pair.startNode.addToSet((byte) '\n');           // 不解析回车和换行符
        pair.startNode.addToSet((byte) '\r');
        pair.startNode.setNegation();

        lexer.advance();                            // 解析下一个字符
        return true;
    }

    /**
     * 为 [...] | [.-.] | [^...] | [^.-.] 构建状态机
     * @param pair
     * @return
     */
    private boolean constructForCharSet(NodePair pair){
        if (!lexer.matchCurrentTag(Lexer.Tag.SQUARE_LEFT)){
            return false;
        }

        lexer.advance();        // 跳过 [ 符号, 解析下一个字符

        boolean isCaret = false;    // 判断是否 ^ 符号
        if (lexer.matchCurrentTag(Lexer.Tag.CARET)){
            isCaret = true;
            lexer.advance();
        }

        pair.startNode = nodePool.getStateNode();       // 左节点
        pair.endNode = nodePool.getStateNode();         // 右节点
        pair.startNode.out = pair.endNode;              // 左节点指向右节点
        pair.startNode.setEdge(StateNode.CHARSET);      // 将边设置为字符集

        if (lexer.matchCurrentTag(Lexer.Tag.SQUARE_RIGHT)){
            throw new RuntimeException("非法输入: 方括号内无内容.");
        }

        doInBrackets(pair.startNode.inputSet);       // 方括号中内容处理
        if (isCaret){                                // 如果有^符号, 将字符集中内容取反
            pair.startNode.setNegation();
        }

        lexer.advance();
        return true;
    }

    /**
     * 处理方括号内部的内容
     * @param set
     */
    private void doInBrackets(Set<Byte> set){
        int beforeDash = 0;
        while (!lexer.matchCurrentTag(Lexer.Tag.SQUARE_RIGHT)){     // 当解析到右方括号时结束
            if (lexer.matchCurrentTag(Lexer.Tag.END)){              // 没有遇到右方括号但正则表达式已经解析结束
                throw new RuntimeException("非法输入：没有右方括号.");
            }

            if (!lexer.matchCurrentTag(Lexer.Tag.DASH)){            // 如果不是 - 号, 就将字符加入字符集
                beforeDash = lexer.getCurrentChar();
                set.add((byte)beforeDash);
            } else {                                                // 如果是 - 号, 将 - 符号左右两边的字符之间的字符加入字符集和
                lexer.advance();  // 跳过 - 号
                if (beforeDash == 0){
                    throw new RuntimeException("非法输入: -号前没有字符.");
                }
                if (lexer.matchCurrentTag(Lexer.Tag.L)) {
                    for (int c = beforeDash + 1; c <= lexer.getCurrentChar(); c++) {
                        set.add((byte) c);
                    }
                } else {
                    throw new RuntimeException("非法输入: -号后是非法字符.");
                }
            }
            lexer.advance();
        }
    }

    /**
     * 为 * 构建状态机
     * @param pair
     * @return
     */
    private boolean constructForClosure(NodePair pair){
        /*                       ----------------ε--------------
         *                      \|/          ----------         |
         *    startNode--->pair.startNode--->|        |--->pair.endNode--->endNode
         *        |                          ----------                      /|\
         *        -------------------------------ε----------------------------
         */
        if (!lexer.matchCurrentTag(Lexer.Tag.CLOSURE)){
            return false;
        }

        StateNode startNode = nodePool.getStateNode();
        StateNode endNode = nodePool.getStateNode();

        pair.endNode.out = pair.startNode;

        startNode.out = pair.startNode;
        pair.endNode.out2 = endNode;

        startNode.out2 = endNode;

        pair.startNode = startNode;
        pair.endNode = endNode;

        lexer.advance();
        return true;
    }

    /**
     * 为 + 构建状态机
     * @param pair
     * @return
     */
    private boolean constructForPlus(NodePair pair){
        /*                       ----------------ε--------------
         *                      \|/          ----------         |
         *    startNode--->pair.startNode--->|        |--->pair.endNode--->endNode
         *                                   ----------
         */
        if (!lexer.matchCurrentTag(Lexer.Tag.PLUS)){
            return false;
        }

        StateNode startNode = nodePool.getStateNode();
        StateNode endNode = nodePool.getStateNode();

        pair.endNode.out = pair.startNode;

        startNode.out = pair.startNode;
        pair.endNode.out2 = endNode;

        pair.startNode = startNode;
        pair.endNode = endNode;

        lexer.advance();
        return true;
    }

    /**
     * 为 ? 构建状态机
     * @param pair
     * @return
     */
    private boolean constructForOpt(NodePair pair){
        /*
         *                                   ----------
         *    startNode--->pair.startNode--->|        |--->pair.endNode--->endNode
         *        |                          ----------                      /|\
         *        -------------------------------ε----------------------------
         */
        if (!lexer.matchCurrentTag(Lexer.Tag.OPT)){
            return false;
        }

        StateNode startNode = nodePool.getStateNode();
        StateNode endNode = nodePool.getStateNode();

        startNode.out = pair.startNode;
        pair.endNode.out = endNode;

        startNode.out2 = endNode;

        pair.startNode = startNode;
        pair.endNode = endNode;

        lexer.advance();
        return true;
    }

    private boolean constructForInParenExpression(NodePair pair){
        if (!lexer.matchCurrentTag(Lexer.Tag.PAREN_LEFT)){ // 判断是否是(符号
            return false;
        }
        lexer.advance();    // 解析下一个字符
        expression(pair);   // 为()中的内容构建NFA.
        if (lexer.matchCurrentTag(Lexer.Tag.PAREN_RIGHT)){  // 判断是否是)符号
            lexer.advance();
        } else {
            throw new RuntimeException("非法输入: 缺失右圆括号.");
        }
        return true;
    }

    /**
     * 最高优先级调用
     * @param pair
     */
    private void firstLevelConstruct(NodePair pair){
        /*
         * firstLevel -> singleChar | . | [...] | (expression)
         */
        boolean handled = constructForInParenExpression(pair);
        if (!handled){
            handled = constructForSingleChar(pair);
        }
        if (!handled){
            handled = constructForAny(pair);
        }
        if (!handled){
            handled = constructForCharSet(pair);
        }
    }

    /**
     * 次高优先级调用
     * @param pair
     */
    private void secondLevelConstruct(NodePair pair){
        /*
         *  secondLevel -> firstLevel* | firstLevel+ | firstLevel?
         */
        firstLevelConstruct(pair);

        boolean handled = constructForClosure(pair);
        if (!handled){
            handled = constructForPlus(pair);
        }
        if (!handled){
            handled = constructForOpt(pair);
        }
    }

    private void expression(NodePair pair) {
        /*
         * expression -> concat_expression
         * expression -> concat_expression | concat_expression
         */
        concatExpression(pair);

        while (lexer.matchCurrentTag(Lexer.Tag.OR)){
            lexer.advance();                        // 解析下一字符
            NodePair localPair = new NodePair();

            concatExpression(localPair);
            // 处理指针
            StateNode startNode = nodePool.getStateNode();
            StateNode endNode = nodePool.getStateNode();

            startNode.out = pair.startNode;
            startNode.out2 = localPair.startNode;

            pair.endNode.out = endNode;
            localPair.endNode.out = endNode;

            pair.startNode = startNode;
            pair.endNode = endNode;
        }
    }

    private void concatExpression(NodePair pair) {
        /*
         * concatExpression -> secondLevelConstruct·secondLevelConstruct
         * concatExpression -> concatExpression·secondLevelConstruct
         */
        if (isLegal(lexer.getCurrentTag())){    // 判断字符是否合法
            secondLevelConstruct(pair);
        }

        // 只要后面的字符与前面的可以直接连接,就继续
        while (isLegal(lexer.getCurrentTag())){
            NodePair localPair = new NodePair();
            secondLevelConstruct(localPair);
            pair.endNode.out = localPair.startNode;
            pair.endNode = localPair.endNode;
        }
    }

    private boolean isLegal(Lexer.Tag tag){
        switch (tag){
            case DOLLAR:
            case PAREN_RIGHT:
            case OR:
            case END:
                return false;
            case CLOSURE:
            case PLUS:
            case OPT:
                throw new RuntimeException("非法输入: " + lexer.getCurrentChar() + "应该放在表达式末尾.");
            case CARET:
                throw new RuntimeException(lexer.getCurrentChar() + "应该放在整个表达式开头.");
            case SQUARE_RIGHT:
                throw new RuntimeException("非法输入: 不能以" + lexer.getCurrentChar() + "开头.");
        }
        return true;
    }
}
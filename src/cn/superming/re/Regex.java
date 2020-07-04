package cn.superming.re;

import java.util.*;

public class Regex {
    private NFAConstructor nfaConstructor;
    private NodePair pair;
    private boolean caseSensitive = true;

    public Regex(){
        nfaConstructor = new NFAConstructor();
        pair = new NodePair();
    }

    public boolean isCaseSensitive() {
        return caseSensitive;
    }

    /**
     * 设置是否大小写敏感
     * @param caseSensitive
     */
    public void setCaseSensitive(boolean caseSensitive) {
        this.caseSensitive = caseSensitive;
    }

    /**
     * 判断当前字符串是否能被regex正则表达式所表示的有限状态机接收(贪心匹配)
     * @param regex 正则表达式
     * @param text 字符串
     * @return 是否被接受
     */
    public boolean matchAll(String regex, String text){
        if (regex == null || text == null)
            throw new RuntimeException("输入有误: 不能为空指针.");

        if (regex.length() == 0){
            throw new RuntimeException("输入有误: regex不能为空.");
        }

        if (text.length() == 0){
            throw new RuntimeException("输入有误: text不能为空.");
        }
        String regexForConstruct = null;
        String textForIdentify = null;

        if (!caseSensitive){ // 判断是否大小写敏感, 否就统一转成小写.
            regexForConstruct = regex.toLowerCase();
            textForIdentify = text.toLowerCase();
        } else {
            regexForConstruct = regex;
            textForIdentify = text;
        }

        pair = nfaConstructor.construct(regexForConstruct, pair); // 构建NFA

        Set<StateNode> set = new HashSet<StateNode>();          // 将开始结点加入当前状态集
        set.add(pair.startNode);
        set = computeEpsilonClosure(set);               // 计算epsilon闭包

        char[] chars = textForIdentify.toCharArray();   // 将待识别字符串转为字符数组进行识别
        for (char c : chars) {                          // 有限状态机逐个识别,直到字符串末尾
            set = move(set, c);
            set = computeEpsilonClosure(set);
            if (set == null || set.isEmpty()){
                break;
            }
        }
        if (hasAccepted(set)){                          // 判断字符串识别结束后的当前状态集合里是否包含接收状态
            return true;
        } else {
            return false;
        }
    }

    /**
     * 接口函数, 匹配字符串text中被regex正则表达式所表示的有限状态机接收的字串(全匹配)
     * @param regex 正则表达式
     * @param text 需要匹配的字符串
     * @return 能被接收的字串
     */
    public String[] match(String regex, String text){
        if (regex == null || text == null)
            throw new RuntimeException("输入有误: 不能为空指针.");

        if (regex.length() == 0){
            throw new RuntimeException("输入有误: regex不能为空.");
        }

        if (text.length() == 0){
            throw new RuntimeException("输入有误: text不能为空.");
        }
        String regexForConstruct = null;
        String textForIdentify = null;

        if (!caseSensitive){
            regexForConstruct = regex.toLowerCase();
            textForIdentify = text.toLowerCase();
        } else {
            regexForConstruct = regex;
            textForIdentify = text;
        }

        ArrayList<String> matchedStrs = new ArrayList<>();
        String[] strings = null;

        if (regexForConstruct.startsWith("^") && !regexForConstruct.endsWith("$")){
            strings = matchStartWithCaret(regexForConstruct, textForIdentify);
        } else if (!regexForConstruct.startsWith("^") && regexForConstruct.endsWith("$")){
            strings = matchEndWithDollar(regexForConstruct, textForIdentify);
        } else if (regexForConstruct.startsWith("^") && regexForConstruct.endsWith("$")){
            if (matchAll(regexForConstruct.substring(1, regexForConstruct.length()-1), textForIdentify)){
                matchedStrs.add(textForIdentify);
            }
        } else {
            strings = matchWithOutLimit(regexForConstruct, textForIdentify);
        }
        if (strings != null){
            Arrays.stream(strings).forEach(matchedStrs::add);
        }

        return matchedStrs.toArray(String[]::new);
    }

    /**
     * 匹配以 ^ 符号开始的串
     * @param regex
     * @param text
     * @return
     */
    private String[] matchStartWithCaret(String regex, String text){
        return matchFromHere(regex.substring(1), text);
    }

    /**
     * 匹配以 $ 符号结尾的串
     * @param regex
     * @param text
     * @return
     */
    private String[] matchEndWithDollar(String regex, String text){
        ArrayList<String> matchedStrs = new ArrayList<>();

        boolean isMatched;
        for (int i = 0; i < text.length(); i++) {
            isMatched = matchFromHereToEnd(regex.substring(0, regex.length() - 1), text.substring(i));
            if (isMatched){
                matchedStrs.add(text.substring(i));
            }
        }
        return matchedStrs.toArray(String[]::new);
    }

    /**
     * 没有开始和结束符合限制的匹配
     * @param regex
     * @param text
     * @return
     */
    private String[] matchWithOutLimit(String regex, String text){
        ArrayList<String> matchedStrs = new ArrayList<>();
        String matchedStr[] = null;
        for (int i = 0; i < text.length(); i++) {
            matchedStr = matchFromHere(regex, text.substring(i));
            if (matchedStr != null && matchedStr.length != 0){
                Arrays.stream(matchedStr).forEach(matchedStrs::add);
            }
        }
        return matchedStrs.toArray(String[]::new);
    }

    /**
     * 贪心匹配, 把text中所有符合正则表达式的字串都返回
     * @param regex
     * @param text
     * @return
     */
    private String[] matchFromHere(String regex, String text){
        ArrayList<String> matchedStrs = new ArrayList<>();
        StringBuffer lastMatchedStr = new StringBuffer("");

        pair = nfaConstructor.construct(regex, pair);

        Set<StateNode> set = new HashSet<StateNode>();
        set.add(this.pair.startNode);
        set = computeEpsilonClosure(set);

        char[] chars = text.toCharArray();
        for (char c : chars) {
            set = move(set, c);
            if (set.isEmpty()){
                return matchedStrs.toArray(String[]::new);
            }
            lastMatchedStr.append(c);
            set = computeEpsilonClosure(set);
            if (hasAccepted(set)){
                matchedStrs.add(lastMatchedStr.toString());
            }
        }
        return matchedStrs.toArray(String[]::new);
    }

    /**
     * 必须遍历完text后处于接收状态才返回真
     * @param regex
     * @param text
     * @return
     */
    private boolean matchFromHereToEnd(String regex, String text){
        pair = nfaConstructor.construct(regex, pair);

        Set<StateNode> set = new HashSet<StateNode>();
        set.add(this.pair.startNode);
        set = computeEpsilonClosure(set);

        char[] chars = text.toCharArray();
        for (char c : chars) {
            set = move(set, c);
            if (set.isEmpty()){
                return false;
            }
            set = computeEpsilonClosure(set);
        }
        if (hasAccepted(set)){
            return true;
        }
        return false;
    }

    /**
     * NFA的状态转移函数
     * @param set 当前所处状态集合
     * @param c 输入字符
     * @return 跳转状态集合
     */
    private Set<StateNode> move(Set<StateNode> set, char c){
        if (set == null || set.isEmpty()){  // 如果当前状态集为空则直接返回
            return null;
        }

        Set<StateNode> resultSet = new HashSet<StateNode>();    // 跳转状态集
        Iterator<StateNode> iterator = set.iterator();

        while (iterator.hasNext()){     // 对状态集中的状态逐个进行跳转操作
            StateNode stateNode = iterator.next();
            if ((char)stateNode.getEdge() == c){    // 单个字符
                resultSet.add(stateNode.out);
                continue;
            }
            if (stateNode.getEdge() == stateNode.CHARSET){  // 字符集
                if (stateNode.inputSet.contains((byte)c)){
                    resultSet.add(stateNode.out);
                    continue;
                }
            }
        }
        return resultSet;
    }

    /**
     * 计算epsilon闭包集合
     * @param set
     * @return
     */
    private Set<StateNode> computeEpsilonClosure(Set<StateNode> set){
        if (set == null || set.isEmpty()){
            return null;
        }

        Stack<StateNode> stack = new Stack<StateNode>();            // 使用stack是因为不能一边遍历迭代器,一边往迭代器里放东西
        Iterator<StateNode> iterator = set.iterator();      // 所以先将set里的节点全部放入stack后,再依次从stack里取出进行判断
        while (iterator.hasNext()){
            StateNode stateNode = iterator.next();
            stack.push(stateNode);
        }

        while (!stack.isEmpty()){
            StateNode node = stack.pop();
            if (node.out != null && node.getEdge() == StateNode.EPSILON){
                stack.push(node.out);
                set.add(node.out);
            }
            if (node.out2 != null && node.getEdge() == StateNode.EPSILON){
                stack.push(node.out2);
                set.add(node.out2);
            }
        }

        return set;
    }

    /**
     * 判断参数集合中是否有接收状态(默认没有出度的边为接收状态)
     * @param set 当前所处状态集合
     * @return
     */
    private boolean hasAccepted(Set<StateNode> set) {
        if (set == null || set.isEmpty())
            return false;

        boolean isAccepted = false;
        Iterator<StateNode> iterator = set.iterator();
        while (iterator.hasNext()){
            StateNode stateNode = iterator.next();
            if (stateNode.getEdge() == stateNode.EMPTY){   // 如果集合中有某一结点没有出度,则接收
                isAccepted = true;
            }
        }

        return isAccepted;
    }
}

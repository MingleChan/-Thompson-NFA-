package cn.superming.re;

import java.util.HashSet;
import java.util.Set;

class NodePair {
    StateNode startNode;
    StateNode endNode;
}

class StateNode {

    private static final int ASCII_COUNT = 127;

    private int edge;         // 边上字符
    // 关于边上字符的特殊定义
    static final int EPSILON = -1;  // 空转移ε
    static final int CHARSET = -2;  // []中的字符集
    static final int EMPTY = -3;    // 没有出去的边

    Set<Byte> inputSet;        // 解析到[...]时,将括号内的字符放入该集合中
    StateNode out;
    StateNode out2;

    private int nodeNum;      // 结点编号

    /**
     * 构建节点
     */
    StateNode(){
        inputSet = new HashSet<Byte>();
        initState(); // 如果是普通节点或者开始，将边初始化为 空转移
    }

    int getEdge() {
        return edge;
    }

    void setEdge(int edge) {
        this.edge = edge;
    }

    int getNodeNum() {
        return nodeNum;
    }

    void setNodeNum(int nodeNum) {
        this.nodeNum = nodeNum;
    }

    /**
     * 初始化结点信息，默认初始化边为空转移
     */
    void initState(){
        inputSet.clear();
        out = null;
        out2 = null;
        nodeNum = -1;
        edge = EPSILON;
    }

    /**
     * 添加字符到字符集中
     * @param b 需要加入的字符
     */
    void addToSet(byte b){
        inputSet.add(b);
    }
    /**
     * 对字符集进行取反操作
     */
    void setNegation(){
        Set<Byte> newSet = new HashSet<>();
        for (byte b = 0; b < ASCII_COUNT; b++) {
            if (!inputSet.contains(b)){
                newSet.add(b);
            }
        }
        inputSet = newSet;
    }
}

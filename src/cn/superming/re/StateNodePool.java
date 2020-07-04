package cn.superming.re;

class StateNodePool {
    private static final int MAX_NODE_NUM = 64;    // 最大节点个数
    private StateNode[] stateNodeArray = null;     // 节点数组和结点栈共同维护节点池
    private int arrayIndex = 0;  // 数组下标, 跳过开始节点
    private int stateNodeID = 0; // 结点编号

    StateNodePool() {
        stateNodeArray = new StateNode[MAX_NODE_NUM];   //初始化结点池

        for (int i = 0; i < MAX_NODE_NUM; i++) {
            stateNodeArray[i] = new StateNode();
        }
    }

    /**
     * 从节点池中获取节点
     * @return
     */
    StateNode getStateNode(){
        if (++stateNodeID >= MAX_NODE_NUM-1){
            throw new RuntimeException("普通节点数量超过限制.");
        }

        StateNode node = stateNodeArray[arrayIndex];
        arrayIndex++;

        node.initState();
        node.setNodeNum(stateNodeID);
        node.setEdge(StateNode.EPSILON);

        return node;
    }
}

## 源码树

​        src--cn--superming--re-----Regex.java                       正则表达式匹配类
​                                                       |
​                                                       | ---NFAConstructor.java      NFA构建器类
​                                                       |
​                                                       |---StateNodePool.java       状态节点池类
​                                                       |
​                                                       |---StateNode.java              状态结点类
​                                                       |
​                                                       |---Lexer.java                      词法解析器类

## 使用方法：

方法1：将cn文件夹放入工程的src目录下即可。

方法2：将re.jar文件放入工程的库文件夹中；或者将re.jar文件放入工程的src目录下的任意文件夹中，将其add as library即可。

开发环境：jdk11.0.7

## API：

### 核心类：Regex

1. 构造器(Regex)

   参数：void
   例：

   ```java
   Regex regex = new Regex();
   ```

2. 设置是否大小写敏感(setCaseSensitive)

   参数：boolen

   返回值：void

   例：

   ```java
   regex.setCaseSensitive(false);
   ```

3. 判断当前是否大小写敏感(isCaseSensitive)

   参数：void

   返回值：boolen

   例：

   ```java
   isCaseSensitive = regex.isCaseSensitive();
   ```

4. 判断字符串是否能够完全与正则表达式匹配(matchAll)

   参数：String regex, String text

   返回值：boolen

   例：

   ```java
   isMatched = regex.matchAll(regex, text);
   ```

5. 返回字符串中所有能够与正则表达式匹配的子字符串集合(match)
   参数：String regex, String text

   返回值：String[]

   例：

   ```java
   matchedArray = regex.match(regex, text);
   ```

   
# arch-design.md

该文档用来详细记录SpringBoot项目的架构设计方法

在本次项目开发中，前端页面作为静态资源已经给出，存放在[src/main/resources/static](../store/src/main/resources/static)文件夹下。数据库表的设计也已经完成，所以下面讨论的主要是后端设计思路

## 数据库连接配置

在新创建一个SpringBoot项目后，为保证数据库连接正常，需要修改[src/main/resources/application.properties](../store/src/main/resources/application.properties)文件，具体格式如下

```properties
# spring.application.name=store
spring.datasource.url=jdbc:mysql://localhost:3306/store?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
spring.datasource.username=root
spring.datasource.password=123456

mybatis.mapper-locations=classpath:mapper/*.xml
# user.address.max-count=20

# server.servlet.context-path=/store
# spring.servlet.multipart.maxFileSize=10MB
# spring.servlet.multipart.maxRequestSize=10MB
```

## 后端连接数据库

以用户注册功能为例

### 实体类设计

所谓**实体类**，就是用来对应数据库表的Java类

#### 实体类属性设计

1. 首先分析数据库表共有的字段，这样可以避免在不同表对应的实体类中反复设计这些重复字段  
可以利用Java中的继承特性来将共有字段设计成一个基类，如[BaseEntity.java](../store/src/main/java/com/cy/store/entity/BaseEntity.java)  
因为所有数据库表都有这样四个相同的字段

    ```sql
    created_user VARCHAR(20) COMMENT '日志-创建人',
    created_time DATETIME COMMENT '日志-创建时间',
    modified_user VARCHAR(20) COMMENT '日志-最后修改执行人',
    modified_time DATETIME COMMENT '日志-最后修改时间',
    ```

    所以BaseEntity.java也就只有这四个属性

    ```java
    private String createdUser;
    private Date createdTime;
    private String modifiedUser;
    private Date modifiedTime;
    ```

2. 每张表对应的实体类先继承该基类  

   ```java
   public class User extends BaseEntity {}
   ```

   然后按照每张表的字段分别进行设计

#### 实体类方法设计

实体类的方法并不需要去具体设计，只需要利用IDEA自带的Generate功能进行补全  
需要补全的方法有

- `get()`和`set()`方法  
  我们需要对实体类进行封装设计，属性均声明为`private`，所以需要设计对应的方法

- `equals()`和`hashCode()`方法  
  这两个方法通常成对出现，方便HashSet和HashMap的使用  
  在Java中，如果两个对象的`equals()`相等，则它们的`hashCode()`必须相同；如果`equals()`不相等，则`hashCode()`可以不同也可以相同

- `toString()`方法  
  方便观察对象的值

### 数据库映射

在本项目中，使用MyBatis来连接后端与数据库

#### 接口设计

对于一个实体类，需要设计相应的接口，接口中声明的方法即希望执行的SQL语句  
比如说，[UserMapper.java](../store/src/main/java/com/cy/store/mapper/UserMapper.java)中声明了查询与插入的SQL语句

```java
/**
 * 插入用户的数据
 * @param user 用户的数据
 * @return 受影响的行数(增、删、改，都会有受影响的行数作为返回值，可以根据返回值来判断是否执行成功)
 */
Integer insert(User user);

/**
 * 根据用户名来查询用户的数据
 * @param username 用户名
 * @return 如果找到对应的用户则返回这个用户的数据，如果没有找到则返回null值
 */
User findByUsername(String username);
```

#### XML文件设计

因为使用了MyBatis，可以很好地将Java代码与SQL代码区分开来，我们在[UserMapper.xml](../store/src/main/resources/mapper/UserMapper.xml)中编写相应的SQL语句

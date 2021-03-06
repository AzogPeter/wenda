# wenda
本项目是一个基于 SpringBoot 的问答平台。

- [项目的基本框架及配置](#项目的基本框架及配置)
- [AOP面向切面 和 IOC控制反转](#AOP面向切面-和-IOC控制反转)
- [MySQL 和 MyBatis](#MySQL-和-MyBatis)
- [注册与登录的实现](#注册与登录的实现)
- [发表问题和敏感词过滤](#发表问题和敏感词过滤)
- [发表评论和站内信](#发表评论和站内信)
- [Redis 实现点赞和点踩功能](#Redis-实现点赞和点踩功能)
- [邮件发送](#邮件发送)
- [异步消息机制](#异步消息机制)
- [关注和粉丝列表的实现](#关注和粉丝列表的实现)
- [推拉模式下的 Feed 流](#推拉模式下的-Feed-流)
- [使用爬虫对网站进行数据填充](#使用爬虫对网站进行数据填充)


## 项目的基本框架及配置 ##

创建远程 git 仓库（不选择默认README.MD），创建本地 git 仓库，配置 IDEA。

首先于 Version Control 中加载 Git 执行路径并测试环境与版本；

于 VCS 中选择 import into Version Control；

选择项目文件 Git-->Add；

选择 Git --> Commit Directory;

提交Commit Message，Author: Azog <<15XXXXXX@qq.com>>；

Error：Push to origin/master was rejected，问题的原因就是:本地仓库和远程仓库的代码不一样。本例中新建远程仓库时包含README文件，删除即可。


创建 SpringBoot 工程，导入 web，velocity 和 aop 的包（SpringBoot1.5 以上不支持 velocity ，需要手动引入 velocity 依赖，或者降低SpringBoot版本）。
    
生成 maven 项目，pom.xml 包含上述依赖。
    
Controller 中使用注解配置，requestmapping，responsebody 基本可以解决请求转发以及响应内容的渲染。responsebody 自动选择 viewresolver 进行解析。

使用 pathvariable 和 requestparam 传递参数，使用 velocity 编写页面模板。
Velocity常用语法： 
$!{ 变量/表达式 } 

会使用 velocity 自带工具类 DateTool
会出现XMLToolboxManage\ServletToolboxManager has been deprecated.Please use org.apache.velocity.tools.ToolboxFactory instead.
导致日期不显示的WARNNING(实为高版本SpringBoot不兼容Velocity)。
    
使用 HTTP 规范下的 httpservletrequest 和 httpservletresponse 来封装请求和相响应，使用封装好的 session 和 cookie 对象。
    
使用重定向的 redirectview 和统一异常处理器 exceptionhandler。

重定向思路：
客户浏览器发送 http 请求，web 服务器接受后发送 302（临时转移）状态码响应及对应 location 给客户浏览器，
客户浏览器发现是 302 响应，则自动再发送一个新的 http 请求。

## AOP面向切面 和 IOC控制反转 ##

IOC 意味着将你设计好的对象交给容器控制，而不是传统的在你的对象内部直接控制；
由容器帮我们查找及注入依赖对象，对象只是被动的接受依赖对象，所以是反转；
常见方式有依赖注入、依赖查找。
    
AOP 为面向切面编程，可以对业务逻辑的各个部分进行隔离；
从而使得业务逻辑各部分之间的耦合度降低，提高程序的可重用性；
主要解决日志，统计，权限等问题；
本例中使用 logger 来记录日志，用该切面的切面方法来监听 Controller。


## MySQL 和 MyBatis ##
本例使用 Mysql 作为数据库， 并采用 Workbench 简化操作；

MyBatis 本身就很小且简单，没有任何第三方依赖；
同时使用灵活，不会对应用程序或者数据库的现有设计强加任何影响；
相比传统数据库操作 MyBatis 把问题简单化，把关注点放在执行 SQL 语句获取数据上；

关于 mybatis 两种连接 mysql 的方式（注解和xml配置）：

mybatis 通过 xml 配置来连接数据库，其实主要是两个 xml，第一个是数据库配置的 xml，即 config.xml 文件，
它是告诉程序，要连哪个数据库，数据库的地址用户名密码等等；
第二个 xml 文件就是是我们自己配置的 mapper.xml 文件，
这里面就是写sql语句，然后说明它的传入参数的类型，返回参数的类型，在程序中调用这个sql的时候，调用的一个标识等等。
注意 xml 要求放在 resource 中并且与 DAO 接口在相同的包路径下。

mybatis 通过注解来连接数据库，即定义 sql 映射的接口，里面写好要执行的 sql 语句然后以一个方法的方式来返回结果；
同时在 config.xml 文件里mappers标签里加上相应映射。


## 注册与登录的实现 ##

新建数据表 login_ticket 用来存储 ticket 字段。该字段在用户登录成功时被生成并存入数据库，并被设置为 Cookie，下次用户登录时会带上这个 ticket，ticket 是随机的 UUID 字符串，有过期时间以及有效状态。

使用拦截器 interceptor 来拦截所有用户请求，判断请求中是否存在有效的 ticket，如果有就将用户信息写入 Threadlocal。所有线程的 threadlocal 都被存在一个叫做 hostholder 的实例中，根据该实例就可以在全局任意位置获取用户的信息。

该 ticket 的功能类似 Session，也是通过 Cookie 写回浏览器，浏览器请求时再通过 Cookie 传递，区别是该字段是存在数据库中的，并且可以用于移动端。

通过用户访问权限拦截器来拦截用户的越界访问，比如用户没有管理员权限就不能访问管理员页面。

本例在数据库中使用随机 salt 字段，加密用户密码，然后使用MD5加密实现二次加密。


## 漏洞与过滤 ##

发布问题时检查标题和内容，防止 HTML 注入 & XSS 攻击；

当用户在输入框输入内容，后台对输入内容不做处理直接添加入页面的时候，用户就可以刻意填写 HTML、JavaScript 脚本来作为文本输入，这样这个页面就会出现一些用户加入的东西了。

防止 xss 注入直接使用 HTMLutils 的方法即可实现。

过滤敏感词首先需要建立一个字典树，并且读取一份保存敏感词的文本文件，然后初始化字典树。最后将过滤器作为一个服务，让需要过滤敏感词的服务进行调用即可。


## 发表评论和站内信 ##

设计 Entity_Id 与 Entity_Type 字段，Entity可以封装为任何实体，例如问题、问题的评论、评论、评论的评论；

也便于任何实体都可实现点赞，发消息等功能。


## Redis 实现点赞和点踩功能 ##

首先了解一下 Redis 的基础知识，数据结构，Jedis 使用等。

开发点踩和点赞功能，在此之前根据业务封装好 Jedis 的增删改查操作，放在 util 包中。

根据需求确定 key 字段，格式是——like：实体类型：实体id 和 dislike：实体类型：实体 id。这样可以将喜欢一条新闻的人存在一个集合，不喜欢的存在另一个集合。通过统计数量可以获得点赞和点踩数。

一般点赞点踩操作是先修改 Redis 的值并获取返回值，然后再异步修改 MySQL 数据库的 likecount 数值。这样既可以保证点赞操作快速完成，也可保证数据一致性。


## 异步消息机制 ##

为了节省资源，在某些功能中有一些不需要实时执行的操作或者任务，可化为异步操作。

具体操作就是使用 Redis 来实现异步消息队列。代码中使用事件 Event 来包装一个事件，事件需要记录事件实体的各种信息：一个异步工具类（事件生产者 + 事件消费者 + EventHandler 接口），让以后各种事件的实现类来实现这个接口。

事件生产者一般作为一个服务，由 Controller 中的业务逻辑调用并产生一个事件，将事件序列化存入 Redis 队列中，事件消费者则通过一个线程循环获取队列里的事件，并且寻找对应的 handler 进行处理。

整个异步事件的框架开发完成，后面新加入的登录，点赞等事件都可以这么实现。


## 邮件发送 ##
注册、评论均可实现实时邮件发送。使用 JavaMail 包，它支持一些常用的邮件协议，如SMTP；
使用smtp.exmail@qq.com 专属邮件客户端，检验服务器地址，用户名、密码。


## 关注和粉丝列表的实现 ##

使用 Redis 实现每一个关注对象的粉丝列表以及每一个用户的关注对象列表。通过该列表的 CRUD 操作可以对应获取粉丝列表和关注列表，并且实现关注和取关功能。

由于关注成功和添加粉丝成功时同一个事务里的两个操作，可以使用 Redis 的事务 multi 来包装事务并进行提交。

除此之外，关注成功或者被关注还可以通过事件机制来生成发送邮件的事件，由异步的队列处理器来完成事件响应，同样是根据 Redis 来实现。

对于粉丝列表，除了显示粉丝的基本信息之外，还要显示当前用户是否关注了这个粉丝，以便前端显示。

对于关注列表来说，如果被关注对象是用户的话，除了显示用户的基本信息之外，还要显示当前用户是被这个用户关注，以便前端显示。
（本例中前端存在重大BUG）


## 推拉模式下的 Feed 流 ##

微博的新鲜事功能介绍：关注好友的动态（好友的点赞和发表的问题等），关注了某个问题，这些都是 feed 流的一部分。

在知乎中的 feed 流主要体现于：关注用户的评论行为，关注用户的关注问题行为。

feed 流主要分为两种，推模式和拉模式。推模式主要是把新鲜事推送给关注该用户的粉丝，本例使用 Redis 来存储某个用户接受的新鲜事 id 列表，这个信息流又称为 timeline，根据用户的唯一 key 来存储；拉模式主要是用户直接找寻自己所有关注的人，并且到数据库去查找这些关注对象的新鲜事，直接返回。

推模式主要适合粉丝较少的小用户，因为他们的粉丝量少，使用推模式产生的冗余副本也比较少，并且可以减少用户访问的压力。

拉模式主要适合大v，因为很多僵尸粉和非活跃用户根本不需要推送信息，用推模式发给这些僵尸粉或者非活跃用户就是浪费资源。所以让用户通过拉模式请求，只需要一个数据副本即可。同时如果是热点信息，这些信息也可以放在缓存，让用户首先拉取这些信息，提高查询效率。

使用 feedhandler 异步处理上述的两个事件，当事件发生时，根据事件实体进行重新包装，构造一个新鲜事，因为所有新鲜事的格式是一样的。需要包括：日期，新鲜事类型，发起者，新鲜事内容，然后把该数据存入数据库，以便用户使用 pull 模式拉出。

为了适配推送模式，此时也要把新鲜事放到该用户所有粉丝的 timeline 里，这样的话就同时实现了推和拉的操作了。


## 使用爬虫对网站进行数据填充 ##

安装 Python3.x 并且配置环境变量。同时安装 PyCharm ,安装 pip。

安装好以后，先熟悉 Python 的语法，写一些例子，比如数据类型，操作符，方法调用，以及面向对象的技术。

因为数据是要导入数据库的，所以这里安装 MySQLdb 的一个库，并且写一下连接数据库的代码，写一下简单的crud进行测试。

使用 requests 库作为解析 HTTP 请求的工具，使用 beautifulsoup 作为解析 html 代码的工具，请求之后直接使用 css 选择器匹配。即可获得内容。

当然现在我们有更方便的工具 pyspider，可以方便解析请求并且可以设置代理，伪装身份等，直接传入 url 并且写好多级的解析函数，程序便会迭代执行，直到把所有页面的内容解析出来。这里我们直接启动 pyspider 的 web 应用并且写好 Python 代码，就可以执行爬虫了。

知乎：先找到问题，再把问题下所有的回答进行爬取，最后把问题和评论一起处理。




























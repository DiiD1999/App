# Example-GithubAction

> 一个小Demo，通过使用Github的自动化CICD服务，部署运行SpringBoot项目在私人服务器上

### 环境准备

- 一台拥有公共IP的`1核1G`以上配置的Linux云服务器。笔者选用的`Linux`发行版为`CentOS Linux release 7.0.1406 (Core)`
- 已安装好`JDK8`与`Maven`。若是还未安装，可以参考以下笔者的安装方式。
  - [软件安装-Java - DiiD - 博客园 (cnblogs.com)](https://www.cnblogs.com/Di-iD/p/13792916.html)
  - [软件安装-Maven - DiiD - 博客园 (cnblogs.com)](https://www.cnblogs.com/Di-iD/p/17153583.html)

### 项目设置

#### Github Setting

进入Github仓库管理界面，依次点击`Setting`->`New Repository Sectet ` 。分别解释以下的字段和值

- `HOST`：私有服务器公网IP地址
- `USERNAME`:私有服务器公网用户名，笔者选择`root`
- `PASSWORD`:私有服务器`USERNAME`对应密码
- `PORT`:私有服务器公网端口，一般取22

![image-20230225005751733](https://cdn.jsdelivr.net/gh/docker200/PicGo-Resource@main/20230225005753.png)

#### Project Setting

在项目的根目录下新增`/.github/workflows/maven.yml`

并在`maven.yml`添加以下内容.

```yml
# This workflow will build a Java project with Maven, and cache/restore any dependencies to improve the workflow execution time
# For more information see: https://docs.github.com/en/actions/automating-builds-and-tests/building-and-testing-java-with-maven

# This workflow uses actions that are not certified by GitHub.
# They are provided by a third-party and are governed by
# separate terms of service, privacy policy, and support
# documentation.

name: Java CI with Maven

# 触发构建时机
on:
  push:
    branches: [ "main" ]
  pull_request:
    branches: [ "main" ]

# 任务
jobs:
  build:

    # Github Action CI/CD的机器选择。
    runs-on: ubuntu-latest

    steps:
      - uses: actions/checkout@v3
      - name: Set up JDK 8
        uses: actions/setup-java@v3
        with:
          java-version: '8.0.362'
          distribution: 'temurin'
          cache: maven
      # maven缓存，不加的话每次都会去重新拉取，会影响速度
      - name: Dependies Cache
        uses: actions/cache@v2
        with:
          path: ~/.m2/repository
          key: ${{ runner.os }}-maven-${{ hashFiles('**/pom.xml') }}
          restore-keys: |
            ${{ runner.os }}-maven-
      # Maven 打包
      - name: Build with Maven
        run: mvn clean package --file pom.xml
      # 将打包后的Jar包从Github Action服务器传输至私人服务器中
      - name: Transfer jar packets
        uses: garygrossgarten/github-action-scp@release
        with:
          # Github Action 的服务器文件路径
          local: target/demo-0.0.1-SNAPSHOT.jar
          # 私有服务器文件路径
          remote: /home/demo-0.0.1-SNAPSHOT.jar
          # 私有服务器公网IP地址
          host: ${{ secrets.HOST }}
          # 私有服务器用户名
          username: ${{ secrets.USERNAME }}
          # 私有服务器用户密码
          password: ${{ secrets.PASSWORD }}
      # 部署运行
      - name: Deploy
        uses: appleboy/ssh-action@master
        with:
          # 同上述
          host: ${{ secrets.HOST }}
          username: ${{ secrets.USERNAME }}
          password: ${{ secrets.PASSWORD }}
          port: ${{ secrets.PORT }}
          # ssh进入系统后执行什么样的操作。一般是关闭原有的服务在重启
          script: |
            cd /home
            ps -ef | grep  demo-0.0.1-SNAPSHOT.jar | grep -v grep | awk '{print $2}' | xargs kill -9
            # nohup /usr/local/java/jdk1.8.0_361/bin/java -jar /home/demo-0.0.1-SNAPSHOT.jar > nohup.out &
            nohup /usr/local/java/jdk1.8.0_361/bin/java -jar /home/demo-0.0.1-SNAPSHOT.jar > runtime.log 2>&1 &
            pwd


```
### 问题与解决

1问题. runtime.log日志显示

```
nohup: 无法运行命令“java“: 没有那个文件或目录  
```

1原因：无法识别$`$JAVAHOME`$的环境变量

1解决：用绝对路径`/usr/local/java/jdk1.8.0_361/bin/java`即可

### 参考链接

- [利用Github的Action实现Java项目自动化部署_github部署java项目_不想写代码的人的博客-CSDN博客](https://blog.csdn.net/weixin_44572376/article/details/128177708?spm=1001.2101.3001.6650.2&utm_medium=distribute.pc_relevant.none-task-blog-2~default~YuanLiJiHua~Position-2-128177708-blog-120467145.pc_relevant_aa&depth_1-utm_source=distribute.pc_relevant.none-task-blog-2~default~YuanLiJiHua~Position-2-128177708-blog-120467145.pc_relevant_aa&utm_relevant_index=3)- 
- [nohup: 无法运行命令“java“: 没有那个文件或目录_掰下一块月亮下酒的博客-CSDN博客_nohup: 无法运行命令'java': 没有那个文件或目录](https://blog.csdn.net/weixin_43345365/article/details/108615164)
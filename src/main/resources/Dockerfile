#基础镜像
FROM sgys.zq.virtual.com.cn:8082/jdk8-python3.12:latest

#工作目录
WORKDIR /app

# 安装 Python clouddrive依赖
RUN pip install --no-cache-dir -U clouddrive

#添加jar包
COPY sgys-util-strm-1.0.jar app.jar
#添加logback配置文件
COPY config/ config/
# 添加lib依赖
COPY lib/ lib/

# 将 Python 脚本复制到容器中（如果有多个脚本，可以调整路径）
COPY python/ python/

#提示映射端口
EXPOSE 8080
ENV APP_PORT=8080
ENV JAVA_OPTS="-Dloader.path=lib -Dfile.encoding=utf-8 -Xms256m -Xmx256m -Xss256k -XX:MetaspaceSize=128m -XX:+UseG1GC -XX:MaxGCPauseMillis=100 -XX:+AlwaysPreTouch -XX:-OmitStackTraceInFastThrow"
#容器内启动命令
CMD java $JAVA_OPTS -jar /app/app.jar ${SPRINGBOOT_OPTS}
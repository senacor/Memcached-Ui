FROM openjdk:11 AS BUILD_IMAGE

ARG MAVEN_VERSION=3.6.3
ENV APP_HOME=/root/dev/myapp/
ARG BASE_URL=https://apache.osuosl.org/maven/maven-3/${MAVEN_VERSION}/binaries

RUN mkdir -p /usr/share/maven /usr/share/maven/ref \
 && curl -fsSL -o /tmp/apache-maven.tar.gz ${BASE_URL}/apache-maven-${MAVEN_VERSION}-bin.tar.gz \
 && tar -xzf /tmp/apache-maven.tar.gz -C /usr/share/maven --strip-components=1 \
 && rm -f /tmp/apache-maven.tar.gz \
 && ln -s /usr/share/maven/bin/mvn /usr/bin/mvn

ENV MAVEN_HOME /usr/share/maven
ENV MAVEN_CONFIG "$USER_HOME_DIR/.m2"

WORKDIR $APP_HOME
COPY . .
RUN mvn clean package

FROM openjdk:11-jre
WORKDIR /root/
COPY --from=BUILD_IMAGE /root/dev/myapp/target/MemcachedUI.jar .
EXPOSE 8080
CMD ["java","-jar","MemcachedUI.jar"]
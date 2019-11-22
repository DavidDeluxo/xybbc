FROM registry.xyb2b.com.cn/base/jre:1.8.0_101

MAINTAINER Calia "liang.zehua@xyb2b.com"

ADD mall-service-1.0.jar /usr/local/release

WORKDIR /usr/local/release

EXPOSE 8597

CMD ["java", "-jar", "mall-service-1.0.jar"]
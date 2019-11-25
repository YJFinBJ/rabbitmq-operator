FROM openjdk:8-jdk-alpine
COPY target/rabbitmq-operator-core-1.0.0-SNAPSHOT.jar /app/
WORKDIR /app
RUN unzip rabbitmq-operator-core-1.0.0-SNAPSHOT.jar
CMD ["java", "-cp", "com.indeed.operators.rabbitmq.RabbitMQOperator"]

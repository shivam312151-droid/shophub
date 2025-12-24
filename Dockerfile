FROM eclipse-temurin:11-jdk
WORKDIR /app
COPY . .
RUN javac -d simple-server/bin simple-server/Main.java
ENV PORT=8080
EXPOSE 8080
CMD ["sh", "-c", "java -cp simple-server/bin Main"]

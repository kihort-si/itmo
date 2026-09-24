docker run -d --name java-target eclipse-temurin:17-jdk sh -c \
"echo 'public class Main { public static void main(String[] args) throws Exception { while(true) { Thread.sleep(1000); } } }' > Main.java && javac Main.java && java Main"
docker ps
docker exec java-target jps
docker exec java-target jstack 32 > thread_dump_container.txt
head -20 thread_dump_container.txt
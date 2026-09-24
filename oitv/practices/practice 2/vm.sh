sudo apt update
sudo apt install -y openjdk-17-jdk
java -version
javac -version
jstack --help
cat > Main.java <<'EOF'
public class Main {
    public static void main(String[] args) throws Exception {
        while (true) {
            Thread.sleep(1000);
        }
    }
}
EOF
javac Main.java
java Main &
jps
jstack 3358 > thread_dump_vm.txt
head -20 thread_dump_vm.txt
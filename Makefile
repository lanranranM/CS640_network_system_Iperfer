JC = javac

Iperfer.class: Iperfer.java
	$(JC) Iperfer.java

client:
	java Iperfer -c -h localhost -p 1024 -t 10
server:
	java Iperfer -s -p 1024

clean:
	rm -f *.class
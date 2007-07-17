mvn clean
mvn scm:update -Dmaven.test.skip=true -o
mvn release:prepare -Dmaven.test.skip=true -o 
mvn release:perform -Dmaven.test.skip=true -o

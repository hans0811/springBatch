CURRENT_DATE=`date "+%Y%m%d-%H%M%S"`
LESSON=$(basename $PWD)
mvn clean package -Dmaven.test.skip=true;
java -jar ./target/hans-batch-0.0.1-SNAPSHOT.jar "item=shoes" "run.date=$CURRENT_DATE" "lesson=$LESSON" type=$1;
read;
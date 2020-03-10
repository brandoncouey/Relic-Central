FROM openjdk:8

ADD build/libs/central-all-*.jar .

EXPOSE 18650:18650/tcp

CMD java -jar central-all-*.jar

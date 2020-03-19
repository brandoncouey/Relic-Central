FROM openjdk:8

ADD build/libs/Relic-Central-all*.jar .

EXPOSE 18650:18650/tcp

ENV live = true

CMD java -jar Relic-Central-all-*.jar $live

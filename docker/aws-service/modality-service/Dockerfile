FROM __ECR_ACCOUNT_ID__.dkr.ecr.__AWS_REGION__.amazonaws.com/oracle_java

RUN set -eux; \
        mkdir -p /opt/modality/conf

COPY modality-all-server-application-vertx-1.0.0-SNAPSHOT-fat.jar /opt/modality
COPY HttpOptions.json /opt/modality/conf
COPY ModalityDatabase.json /opt/modality/conf

WORKDIR /opt/modality

EXPOSE 80

CMD ["java", "-jar", "/opt/modality/modality-all-server-application-vertx-1.0.0-SNAPSHOT-fat.jar"]
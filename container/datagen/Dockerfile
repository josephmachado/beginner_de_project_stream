FROM python:3.7-slim

RUN pip install \
    psycopg2-binary==2.9.3 \
    faker==13.3.2 \
    confluent-kafka


WORKDIR /opt/datagen

COPY requirements.txt /opt/datagen/

RUN pip install --no-cache-dir -r /opt/datagen/requirements.txt

CMD ["tail" "-F" "anything"]
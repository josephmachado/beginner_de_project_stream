FROM python:3.7-slim

RUN pip install \
    psycopg2-binary==2.9.3 \
    faker==13.3.2 \
    confluent-kafka


WORKDIR /opt/datagen

COPY ../../datagen /opt/datagen

# CMD [ "python" "./gen_user_payment_data.py"]
CMD ["tail" "-F" "anything"]
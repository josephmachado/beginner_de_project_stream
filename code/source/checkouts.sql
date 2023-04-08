CREATE TABLE checkouts (
    checkout_id STRING,
    user_id INT,
    product_id STRING,
    payment_method STRING,
    total_amount DECIMAL(5, 2),
    shipping_address STRING,
    billing_address STRING,
    user_agent STRING,
    ip_address STRING,
    datetime_occured TIMESTAMP(3),
    processing_time AS PROCTIME(),
    WATERMARK FOR datetime_occured AS datetime_occured - INTERVAL '15' SECOND
) WITH (
    'connector' = 'kafka',
    'topic' = 'checkouts',
    'properties.bootstrap.servers' = 'kafka:9092',
    'properties.group.id' = 'flink-consumer-group-1',
    'scan.startup.mode' = 'earliest-offset',
    'format' = 'json'
);
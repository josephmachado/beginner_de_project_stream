INSERT INTO
    attributed_checkouts
SELECT
    checkout_id,
    user_name,
    click_id,
    product_id,
    payment_method,
    total_amount,
    shipping_address,
    billing_address,
    user_agent,
    ip_address,
    checkout_time,
    click_time
FROM
    (
        SELECT
            co.checkout_id,
            u.username AS user_name,
            cl.click_id,
            co.product_id,
            co.payment_method,
            co.total_amount,
            co.shipping_address,
            co.billing_address,
            co.user_agent,
            co.ip_address,
            co.datetime_occured AS checkout_time,
            cl.datetime_occured AS click_time,
            ROW_NUMBER() OVER (
                PARTITION BY cl.user_id,
                cl.product_id
                ORDER BY
                    cl.datetime_occured
            ) AS rn
        FROM
            checkouts AS co
            JOIN users FOR SYSTEM_TIME AS OF co.processing_time AS u ON co.user_id = u.id
            LEFT JOIN clicks AS cl ON co.user_id = cl.user_id
            AND co.product_id = cl.product_id
            AND co.datetime_occured BETWEEN cl.datetime_occured
            AND cl.datetime_occured + INTERVAL '1' HOUR
    )
WHERE
    rn = 1;
# Beginner Data Engineering Project - Stream Version

## WIP

Run `make run` 

## Check sink

```bash
pgcli -h localhost -p 5432 -U postgres -d postgres 
# password: postgres
```

Use the below query to check that the output updates every few seconds.

```sql
SELECT checkout_id, click_id, checkout_time, click_time, user_name FROM commerce.attributed_checkouts order by checkout_time desc limit 5;
```
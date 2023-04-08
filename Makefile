up:
	docker compose up --build -d

down:
	docker compose down

gen-user-data:
	docker exec datagen python3 gen_user_payment_data.py

gen-clicks-checkouts-data:
	docker exec datagen python3 get_clicks_purchases.py

gen-data: gen-user-data gen-clicks-checkouts-data

run-data-stream:
	docker exec jobmanager ./bin/flink run --python ./code/checkout_attribution.py

sleep:
	sleep 20 

re-up: down up sleep gen-user-data run-data-stream
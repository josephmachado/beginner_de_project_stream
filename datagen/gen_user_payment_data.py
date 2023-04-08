import psycopg2
import argparse
from time import sleep
import random
from faker import Faker

fake = Faker()


def gen_user_product_data(num_records: int) -> None:
    for id in range(num_records):
        # sleep(0.5)
        conn = psycopg2.connect(
            dbname="postgres",
            user="postgres",
            password="postgres",
            host="postgres",
        )
        curr = conn.cursor()
        curr.execute(
            "INSERT INTO commerce.users (id, username, password) VALUES (%s, %s, %s)",
            (id, fake.user_name(), fake.password()),
        )
        curr.execute(
            "INSERT INTO commerce.products (id, name, description, price) VALUES (%s, %s, %s, %s)",
            (id, fake.name(), fake.text(), fake.random_int(min=1, max=100)),
        )
        conn.commit()

        # sleep(0.5)
        # update 10 % of the time
        if random.randint(1, 100) >= 90:
            curr.execute(
                "UPDATE commerce.users SET username = %s WHERE id = %s",
                (fake.user_name(), id),
            )
            curr.execute(
                "UPDATE commerce.products SET name = %s WHERE id = %s",
                (fake.name(), id),
            )
        conn.commit()

        # sleep(0.5)
        # # delete 5 % of the time
        # if random.randint(1, 100) >= 95:
        #     curr.execute("DELETE FROM commerce.users WHERE id = %s", (id,))
        #     curr.execute("DELETE FROM commerce.products WHERE id = %s", (id,))

        conn.commit()
        curr.close()

    return


if __name__ == "__main__":
    parser = argparse.ArgumentParser()
    parser.add_argument(
        "-n",
        "--num_records",
        type=int,
        help="Number of records to generate",
        default=100,
    )
    args = parser.parse_args()
    num_records = args.num_records
    gen_user_product_data(num_records)

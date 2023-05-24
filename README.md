# Beginner Data Engineering Project - Stream Version

Code for blog at [Data Engineering Project Stream Edition](https://www.startdataengineering.com/post/data-engineering-project-for-beginners-stream-edition/).

## Project

Consider we run an e-commerce website. An everyday use case with e-commerce is to identify, for every product purchased, the click that led to this purchase. Attribution is the joining of checkout(purchase) of a product to a click. There are multiple types of **[attribution](https://www.shopify.com/blog/marketing-attribution#3)**; we will focus on `First Click Attribution`. 

Our objectives are:
 1. Enrich checkout data with the user name. The user data is in a transactional database.
 2. Identify which click leads to a checkout (aka attribution). For every product checkout, we consider **the earliest click a user made on that product in the previous hour to be the click that led to a checkout**.
 3. Log the checkouts and their corresponding attributed clicks (if any) into a table.

## Prerequisites

To run the code, you'll need the following:

1. [git](https://git-scm.com/book/en/v2/Getting-Started-Installing-Git)
2. [Docker](https://docs.docker.com/engine/install/) with at least 4GB of RAM and [Docker Compose](https://docs.docker.com/compose/install/) v1.27.0 or later
3. [psql](https://blog.timescale.com/tutorials/how-to-install-psql-on-mac-ubuntu-debian-windows/)

## Architecture

Our streaming pipeline architecture is as follows (from left to right):

1. **`Application`**: Website generates clicks and checkout event data.
2. **`Queue`**: The clicks and checkout data are sent to their corresponding Kafka topics.
3. **`Stream processing`**: 
   1. Flink reads data from the Kafka topics.
   2. The click data is stored in our cluster state. Note that we only store click information for the last hour, and we only store one click per user-product combination. 
   3. The checkout data is enriched with user information by querying the user table in Postgres.
   4. The checkout data is left joined with the click data( in the cluster state) to see if the checkout can be attributed to a click.
   5. The enriched and attributed checkout data is logged into a Postgres sink table.
4. **`Monitoring & Alerting`**: Apache Flink metrics are pulled by Prometheus and visualized using Graphana.

![Architecture](./assets/images/arch.png)

## Code design

We use Apache Table API to 

1. Define Source systems: **[clicks, checkouts and users](https://github.com/josephmachado/beginner_de_project_stream/tree/main/code/source)**. [This python script](https://github.com/josephmachado/beginner_de_project_stream/blob/main/datagen/gen_fake_data.py) generates fake click and checkout data.
2. Define how to process the data (enrich and attribute): **[Enriching with user data and attributing checkouts ](https://github.com/josephmachado/beginner_de_project_stream/blob/main/code/process/attribute_checkouts.sql)**
3. Define Sink system: **[sink](https://github.com/josephmachado/beginner_de_project_stream/blob/main/code/sink/attributed_checkouts.sql)**

The function **[run_checkout_attribution_job](https://github.com/josephmachado/beginner_de_project_stream/blob/cddab5b4bb2bce80e59d3525a78a02598d88eac9/code/checkout_attribution.py#L107-L129)** creates the sources, and sink and runs the data processing.

We store the SQL DDL and DML in the folders `source`, `process`, and `sink` corresponding to the above steps. We use [Jinja2](https://jinja.palletsprojects.com/en/3.1.x/) to replace placeholders with [config values](https://github.com/josephmachado/beginner_de_project_stream/blob/cddab5b4bb2bce80e59d3525a78a02598d88eac9/code/checkout_attribution.py#L16-L62). **The code is available [here](https://github.com/josephmachado/beginner_de_project_stream).**

## Run streaming job

Clone and run the streaming job (via terminal) as shown below:

```bash
git clone https://github.com/josephmachado/beginner_de_project_stream
cd beginner_de_project_stream
make run # restart all containers, & start streaming job
```

1. **Apache Flink UI**: Open [http://localhost:8081/](http://localhost:8081/) or run `make ui` and click on `Jobs -> Running Jobs -> checkout-attribution-job` to see our running job. 
2. **Graphana**: Visualize system metrics with Graphana, use the `make open` command or go to [http://localhost:3000](http://localhost:3000) via your browser (username: `admin`, password:`flink`).

**Note**: Checkout [Makefile](https://github.com/josephmachado/beginner_de_project_stream/blob/main/Makefile) to see how/what commands are run. Use `make down` to spin down the containers.

## Check output

Once we start the job, it will run asynchronously. We can check the Flink UI ([http://localhost:8081/](http://localhost:8081/) or `make ui`) and clicking on `Jobs -> Running Jobs -> checkout-attribution-job` to see our running job.

![Flink UI](assets/images/flink_ui_dag.png)

We can check the output of our job, by looking at the attributed checkouts. 

Open a postgres terminal as shown below.

```bash
pgcli -h localhost -p 5432 -U postgres -d postgres 
# password: postgres
```

Use the below query to check that the output updates every few seconds.

```sql
SELECT checkout_id, click_id, checkout_time, click_time, user_name FROM commerce.attributed_checkouts order by checkout_time desc limit 5;
```

## Tear down 

Use `make down` to spin down the containers.

## Contributing

Contributions are welcome. If you would like to contribute you can help by opening a Github issue or putting up a PR.

## References

1. [Apache Flink docs](https://nightlies.apache.org/flink/flink-docs-release-1.17/)
2. [Flink Prometheus example project](https://github.com/mbode/flink-prometheus-example)
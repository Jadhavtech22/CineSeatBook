# CineSeat

CineSeat is a movie ticket booking app. I used Spring Boot for the booking logic, Thymeleaf for the pages and PostgreSQL to store the shows and tickets.

After signing in, you can pick a movie and a showtime. Click a seat to see its price, or select a few seats to see the total. Checkout uses a demo payment. Once you confirm, you can find the booking under **My tickets**.

One detail about seat selection: if two people pick A1 for the same show, both can reach the review page. The first person to confirm gets A1. The other person sees the updated seat map and can choose another seat.

## Running it locally

You'll need JDK 21, PostgreSQL and Spring Tool Suite (STS).

1. In pgAdmin, open the Query Tool on the `postgres` database and run `CREATE DATABASE cineseat;`.
2. In STS, go to **File → Import → Maven → Existing Maven Projects** and select the folder with `pom.xml`.
3. Under **Run Configurations → Spring Boot App**, select `in.cineseat.CineSeat`. In the **Environment** tab, add `DB_PASSWORD` with your PostgreSQL password. The app uses the `postgres` user and port `5432` by default. You can change these with `DB_USER` and `DB_URL`.
4. Run the app and open [localhost:8080](http://localhost:8080). Create an account and pick a show. The sample movies and showtimes are added on startup.

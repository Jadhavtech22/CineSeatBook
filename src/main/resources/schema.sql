CREATE TABLE IF NOT EXISTS members (
 email text PRIMARY KEY, password_hash text NOT NULL
);
CREATE TABLE IF NOT EXISTS movies (
 id integer PRIMARY KEY, title text NOT NULL, genre text NOT NULL, language text NOT NULL
);
CREATE TABLE IF NOT EXISTS screenings (
 id bigint GENERATED ALWAYS AS IDENTITY PRIMARY KEY, movie_id integer NOT NULL REFERENCES movies,
 starts_at timestamptz NOT NULL, auditorium text NOT NULL, price integer NOT NULL CHECK(price>0),
 UNIQUE(auditorium, starts_at)
);
CREATE TABLE IF NOT EXISTS bookings (
 id uuid PRIMARY KEY, email text NOT NULL REFERENCES members, screening_id bigint NOT NULL REFERENCES screenings,
 total integer NOT NULL, paid_at timestamptz NOT NULL DEFAULT now(), UNIQUE(id,screening_id)
);
CREATE TABLE IF NOT EXISTS tickets (
 booking_id uuid NOT NULL, screening_id bigint NOT NULL, seat text NOT NULL CHECK(seat ~ '^[A-F][1-8]$'),
 PRIMARY KEY(screening_id,seat), FOREIGN KEY(booking_id,screening_id) REFERENCES bookings(id,screening_id)
);
ALTER TABLE members ADD COLUMN IF NOT EXISTS full_name text;
ALTER TABLE members ADD COLUMN IF NOT EXISTS mobile text;
ALTER TABLE members ADD COLUMN IF NOT EXISTS city text;
ALTER TABLE members ADD COLUMN IF NOT EXISTS email_verified_at timestamptz;
ALTER TABLE members ADD COLUMN IF NOT EXISTS mobile_verified_at timestamptz;

CREATE UNIQUE INDEX IF NOT EXISTS members_mobile_unique
    ON members(mobile);

CREATE TABLE IF NOT EXISTS registration_limits (
    target text PRIMARY KEY,
    window_start timestamptz NOT NULL,
    sends integer NOT NULL
);

CREATE INDEX IF NOT EXISTS registration_limits_start
    ON registration_limits(window_start);
-- ===========================================
-- DROP TABLES IN SAFE ORDER (FK dependencies)
-- ===========================================
Drop table if exists user_subscriptions cascade;
DROP TABLE IF EXISTS sensor_readings CASCADE;
DROP TABLE IF EXISTS sensors CASCADE;
DROP TABLE IF EXISTS parking_spots CASCADE;
DROP TABLE IF EXISTS parking_areas CASCADE;

DROP TABLE IF EXISTS user_subscription CASCADE;
DROP TABLE IF EXISTS subscription_plans CASCADE;


DROP TABLE IF EXISTS users CASCADE;

-- ===========================================
-- 1) USERS
-- ===========================================

CREATE TABLE users (
                                     user_id        BIGSERIAL PRIMARY KEY,
                                     email          TEXT UNIQUE NOT NULL,
                                     password_hash  TEXT NOT NULL,
                                     first_name     TEXT,
                                     last_name      TEXT,
                                     phone_number   TEXT,
                                     is_active      BOOLEAN NOT NULL DEFAULT TRUE,
                                     created_at     TIMESTAMPTZ NOT NULL DEFAULT NOW()
);

-- ===========================================
-- 2) SUBSCRIPTIONS
-- ===========================================

CREATE TABLE subscription_plans (
                                   plan_id BIGSERIAL PRIMARY KEY,
                                   plan_name TEXT NOT NULL,
                                   plan_code TEXT NOT NULL UNIQUE,
                                   monthly_price DECIMAL NOT NULL,
                                   description TEXT,
                                   is_active BOOLEAN NOT NULL DEFAULT TRUE,
                                   has_analytics BOOLEAN NOT NULL DEFAULT FALSE,
                                   has_prediction BOOLEAN NOT NULL DEFAULT FALSE
);


CREATE TABLE user_subscription (
                                   user_subscription_id BIGSERIAL PRIMARY KEY,
                                   user_id BIGINT NOT NULL REFERENCES users(user_id),
                                   plan_id BIGINT NOT NULL REFERENCES subscription_plans(plan_id),
                                   status VARCHAR(50) NOT NULL,
                                   start_date TIMESTAMP NOT NULL,
                                   end_date TIMESTAMP,
                                   locked_price DECIMAL NOT NULL,
                                   locked_plan_name TEXT NOT NULL,
                                   locked_plan_code TEXT NOT NULL,
                                   has_analytics BOOLEAN NOT NULL,
                                   has_prediction BOOLEAN NOT NULL
);

-- ===========================================
-- 3) PARKING AREAS & SPOTS
-- ===========================================

CREATE TABLE parking_areas (
                               area_id        BIGSERIAL PRIMARY KEY,
                               area_name      TEXT NOT NULL,
                               area_postcode  TEXT,
                               latitude       DOUBLE PRECISION,
                               longitude      DOUBLE PRECISION,
                               address        TEXT,
                               total_capacity INTEGER CHECK (total_capacity >= 0),
                               area_type      TEXT,
                               is_active      BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE parking_spots (
                               spot_id        BIGSERIAL PRIMARY KEY,
                               area_id        BIGINT NOT NULL REFERENCES parking_areas(area_id) ON DELETE CASCADE,
                               spot_number    INTEGER NOT NULL,
                               is_operating   BOOLEAN NOT NULL DEFAULT TRUE,
                               CONSTRAINT uq_area_spot UNIQUE (area_id, spot_number)
);

-- ===========================================
-- 4) SENSORS & READINGS
-- ===========================================

CREATE TABLE sensors (
                         sensor_id      BIGSERIAL PRIMARY KEY,
                         spot_id        BIGINT NOT NULL REFERENCES parking_spots(spot_id) ON DELETE CASCADE,
                         sensor_type    TEXT NOT NULL,
                         hardware_model TEXT,
                         is_active      BOOLEAN NOT NULL DEFAULT TRUE,
                         installed_at   TIMESTAMPTZ DEFAULT NOW()
);

CREATE TABLE sensor_readings (
                                 reading_id       bigserial PRIMARY KEY,
                                 sensor_id        bigint NOT NULL REFERENCES sensors ON DELETE CASCADE,
                                 reading_time     timestamp with time zone DEFAULT now() NOT NULL,
                                 distance_mm      integer,
                                 metal_detected   boolean,
                                 occupied         boolean
);

CREATE TABLE IF NOT EXISTS cars (
                                    car_id          BIGSERIAL PRIMARY KEY,
                                    user_id         BIGINT NOT NULL REFERENCES users(user_id) ON DELETE CASCADE,
                                    brand           TEXT NOT NULL,
                                    model           TEXT NOT NULL,
                                    license_plate   TEXT NOT NULL UNIQUE,
                                    color           TEXT,
                                    document_path   TEXT,
                                    times_parked    INTEGER NOT NULL DEFAULT 0,
                                    created_at      TIMESTAMPTZ NOT NULL DEFAULT NOW(),
                                    is_active       BOOLEAN NOT NULL DEFAULT TRUE
);

-- ===========================================
-- INSERT SUBSCRIPTION PLANS
-- ===========================================

INSERT INTO subscription_plans (plan_name, plan_code, monthly_price, description, is_active, has_analytics, has_prediction)
VALUES
    ('Basic', 'BASIC', 3.00, 'Essential parking features with real-time map and live spot occupancy', true, false, false),
    ('Premium', 'PREMIUM', 6.00, 'Everything in Basic plus analytics and insights on parking trends', true, true, false),
    ('Ultra', 'ULTRA', 9.00, 'All features including AI-powered predictions for optimal parking', true, true, true)
ON CONFLICT (plan_code) DO NOTHING;

-- ===========================================
-- Helpful indexes
-- ===========================================

CREATE INDEX idx_spots_area           ON parking_spots (area_id);
CREATE INDEX idx_sensors_spot         ON sensors (spot_id);
CREATE INDEX idx_readings_sensor_time ON sensor_readings (sensor_id, reading_time DESC);

CREATE INDEX idx_cars_user_id ON cars(user_id);
CREATE INDEX idx_cars_license_plate ON cars(license_plate);
CREATE INDEX idx_cars_user_active ON cars(user_id, is_active);
-- =========================
-- INDEXES FOR SUBSCRIPTION
-- =========================
CREATE INDEX idx_user_subscription_user_id ON user_subscription(user_id);
CREATE INDEX idx_user_subscription_status ON user_subscription(status);
CREATE INDEX idx_user_subscription_user_status ON user_subscription(user_id, status);
CREATE INDEX idx_subscription_plan_code ON subscription_plans(plan_code);
CREATE INDEX idx_subscription_plan_active ON subscription_plans(is_active);

-- =========================
-- COMMENTS FOR DOCUMENTATION
-- =========================
COMMENT ON TABLE subscription_plans IS 'Defines available subscription tiers (Basic, Premium, Ultra)';
COMMENT ON TABLE user_subscription IS 'Records user subscriptions with snapshot of plan details at time of subscription';
COMMENT ON COLUMN user_subscription.locked_price IS 'Price locked at subscription time - protects user from future price changes';
COMMENT ON COLUMN user_subscription.locked_plan_name IS 'Plan name locked at subscription time';
COMMENT ON COLUMN user_subscription.locked_plan_code IS 'Plan code locked for feature access logic';

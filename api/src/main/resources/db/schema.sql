CREATE EXTENSION IF NOT EXISTS "uuid-ossp";

CREATE TABLE IF NOT EXISTS users (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    name VARCHAR(150) NOT NULL,
    email VARCHAR(255) NOT NULL UNIQUE,
    password_hash VARCHAR(255) NOT NULL,
    phone VARCHAR(50),
    role VARCHAR(20) NOT NULL CHECK (role IN ('CUSTOMER', 'HOST')),
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_users_email ON users(email);
CREATE INDEX IF NOT EXISTS idx_users_role ON users(role);

CREATE TABLE IF NOT EXISTS establishments (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    host_id UUID NOT NULL REFERENCES users(id) ON DELETE CASCADE,
    name VARCHAR(200) NOT NULL,
    description TEXT,
    category VARCHAR(50) NOT NULL CHECK (category IN (
        'HOTEL', 'APARTMENT', 'CABIN', 'EVENT_HALL', 
        'SPORTS_FIELD', 'COWORKING', 'RESTAURANT', 'STUDIO', 'MEETING_ROOM', 'OTHER'
    )),
    street VARCHAR(255),
    city VARCHAR(100) NOT NULL,
    state VARCHAR(100),
    country VARCHAR(100) NOT NULL,
    zip_code VARCHAR(20),
    latitude NUMERIC(10, 7),
    longitude NUMERIC(10, 7),
    contact_phone VARCHAR(50),
    contact_email VARCHAR(255),
    capacity INT NOT NULL DEFAULT 1 CHECK (capacity > 0),
    price_per_unit NUMERIC(12, 2) NOT NULL CHECK (price_per_unit >= 0),
    unit_type VARCHAR(20) NOT NULL CHECK (unit_type IN ('HOUR', 'DAY', 'NIGHT', 'SESSION', 'SLOT')),
    active BOOLEAN NOT NULL DEFAULT TRUE,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP
);

CREATE INDEX IF NOT EXISTS idx_establishments_host_id ON establishments(host_id);
CREATE INDEX IF NOT EXISTS idx_establishments_category_city ON establishments(category, city);
CREATE INDEX IF NOT EXISTS idx_establishments_active ON establishments(active);

-- -----------------------------------------------------------------------------
-- 2.1 TABLAS ADICIONALES PARA DETALLES DEL ESTABLECIMIENTO (Amenidades, Reglas, Imágenes)
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS establishment_amenities (
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    amenity VARCHAR(100) NOT NULL,
    PRIMARY KEY (establishment_id, amenity)
);

CREATE TABLE IF NOT EXISTS establishment_rules (
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    rule VARCHAR(255) NOT NULL,
    PRIMARY KEY (establishment_id, rule)
);

CREATE TABLE IF NOT EXISTS establishment_images (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE CASCADE,
    image_url VARCHAR(500) NOT NULL,
    display_order INT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_establishment_images_establishment ON establishment_images(establishment_id);

-- -----------------------------------------------------------------------------
-- 3. TABLA DE RESERVAS (RESERVATIONS)
-- Transacciones de reserva realizadas por un Customer en un Establishment
-- -----------------------------------------------------------------------------
CREATE TABLE IF NOT EXISTS reservations (
    id UUID PRIMARY KEY DEFAULT uuid_generate_v4(),
    customer_id UUID NOT NULL REFERENCES users(id) ON DELETE RESTRICT,
    establishment_id UUID NOT NULL REFERENCES establishments(id) ON DELETE RESTRICT,
    start_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    end_date_time TIMESTAMP WITH TIME ZONE NOT NULL,
    guests_count INT NOT NULL DEFAULT 1 CHECK (guests_count > 0),
    total_price NUMERIC(12, 2) NOT NULL CHECK (total_price >= 0),
    status VARCHAR(20) NOT NULL CHECK (status IN ('PENDING', 'CONFIRMED', 'CANCELLED', 'REJECTED', 'COMPLETED')),
    cancellation_reason TEXT,
    created_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    updated_at TIMESTAMP WITH TIME ZONE DEFAULT CURRENT_TIMESTAMP,
    CONSTRAINT chk_reservation_dates CHECK (end_date_time > start_date_time)
);

CREATE INDEX IF NOT EXISTS idx_reservations_customer ON reservations(customer_id);
CREATE INDEX IF NOT EXISTS idx_reservations_establishment ON reservations(establishment_id);
CREATE INDEX IF NOT EXISTS idx_reservations_overlap ON reservations(establishment_id, start_date_time, end_date_time, status);

-- -----------------------------------------------------------------------------
-- 4. TRIGGER PARA ACTUALIZAR AUTOMÁTICAMENTE `updated_at`
-- -----------------------------------------------------------------------------
CREATE OR REPLACE FUNCTION update_updated_at_column()
RETURNS TRIGGER AS $$
BEGIN
   NEW.updated_at = CURRENT_TIMESTAMP;
   RETURN NEW;
END;
$$ language 'plpgsql';

CREATE TRIGGER trigger_update_users_updated_at
BEFORE UPDATE ON users
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_update_establishments_updated_at
BEFORE UPDATE ON establishments
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

CREATE TRIGGER trigger_update_reservations_updated_at
BEFORE UPDATE ON reservations
FOR EACH ROW EXECUTE FUNCTION update_updated_at_column();

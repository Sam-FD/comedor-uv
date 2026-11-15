-- Esquema de base de datos para el Comedor Universitario UV

CREATE TABLE IF NOT EXISTS usuarios (
    id SERIAL PRIMARY KEY,
    credencial_institucional VARCHAR(30) UNIQUE NOT NULL,
    nombre VARCHAR(100) NOT NULL,
    correo VARCHAR(100) UNIQUE NOT NULL,
    password_hash VARCHAR(255) NOT NULL,
    rol VARCHAR(20) NOT NULL,
    faltas_abandono INT NOT NULL DEFAULT 0,
    fecha_ultima_falta TIMESTAMP,
    nivel_restriccion VARCHAR(30) NOT NULL DEFAULT 'ninguna',
    restriccion_hasta TIMESTAMP
);

CREATE TABLE IF NOT EXISTS platillos (
    id SERIAL PRIMARY KEY,
    nombre VARCHAR(100) NOT NULL,
    descripcion TEXT,
    precio DECIMAL(8,2) NOT NULL,
    tipo_menu VARCHAR(20) NOT NULL,
    turno VARCHAR(20) NOT NULL,
    fecha_operativa DATE,
    disponible BOOLEAN NOT NULL DEFAULT TRUE
);

CREATE TABLE IF NOT EXISTS pedidos (
    id SERIAL PRIMARY KEY,
    referencia VARCHAR(4) NOT NULL,
    usuario_id INT NOT NULL REFERENCES usuarios(id),
    turno VARCHAR(20) NOT NULL,
    fecha_operativa DATE NOT NULL DEFAULT CURRENT_DATE,
    total DECIMAL(8,2) NOT NULL,
    modalidad_pago VARCHAR(20) NOT NULL,
    estado_pago VARCHAR(20) NOT NULL,
    estado_pedido VARCHAR(20) NOT NULL,
    fecha_creacion TIMESTAMP NOT NULL DEFAULT NOW(),
    fecha_listo TIMESTAMP,
    fecha_limite_recoleccion TIMESTAMP,
    fecha_cierre TIMESTAMP,
    CONSTRAINT uq_pedidos_fecha_referencia UNIQUE (fecha_operativa, referencia),
    CONSTRAINT chk_referencia_formato CHECK (referencia ~ '^[A-Z][0-9]{3}$')
);

CREATE TABLE IF NOT EXISTS detalle_pedidos (
    id SERIAL PRIMARY KEY,
    pedido_id INT NOT NULL REFERENCES pedidos(id),
    platillo_id INT NOT NULL REFERENCES platillos(id),
    cantidad INT NOT NULL DEFAULT 1,
    precio_unitario DECIMAL(8,2) NOT NULL,
    subtotal DECIMAL(8,2) NOT NULL
);

CREATE TABLE IF NOT EXISTS transacciones_pasarela (
    id SERIAL PRIMARY KEY,
    pedido_id INT UNIQUE NOT NULL REFERENCES pedidos(id),
    transaccion_id VARCHAR(120) NOT NULL,
    modo VARCHAR(30) NOT NULL,
    estado_pasarela VARCHAR(40) NOT NULL,
    estado_comedor VARCHAR(30) NOT NULL,
    ultimos_digitos VARCHAR(4) NOT NULL,
    monto DECIMAL(8,2) NOT NULL,
    moneda VARCHAR(10) NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE TABLE IF NOT EXISTS configuracion_operacion (
    clave VARCHAR(50) PRIMARY KEY,
    valor VARCHAR(100) NOT NULL,
    fecha_actualizacion TIMESTAMP NOT NULL DEFAULT NOW()
);

CREATE INDEX IF NOT EXISTS idx_pedidos_usuario_estado ON pedidos (usuario_id, estado_pedido);
CREATE INDEX IF NOT EXISTS idx_pedidos_fecha_estado ON pedidos (fecha_operativa, estado_pedido);
CREATE INDEX IF NOT EXISTS idx_platillos_tipo_fecha ON platillos (tipo_menu, fecha_operativa);

CREATE TABLE IF NOT EXISTS orders (
    id SERIAL PRIMARY KEY,
    item_name VARCHAR(100) NOT NULL,
    status VARCHAR(50) NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP
);

INSERT INTO orders (item_name, status) VALUES 
('Cloud Server vCPU-2', 'COMPLETED'),
('Object Storage 100GB', 'PENDING'),
('Load Balancer Basic', 'COMPLETED');
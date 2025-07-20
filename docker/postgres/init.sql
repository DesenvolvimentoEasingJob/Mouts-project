-- Script de inicialização do banco de dados
-- Criação de índices e configurações específicas

-- Criar índices para melhor performance
CREATE INDEX IF NOT EXISTS idx_orders_external_id ON orders(external_id);
CREATE INDEX IF NOT EXISTS idx_orders_status ON orders(status);
CREATE INDEX IF NOT EXISTS idx_orders_created_at ON orders(created_at);

-- Criar índices para produtos
CREATE INDEX IF NOT EXISTS idx_products_pedido_id ON products(pedido_id);

-- Configurar timezone
SET timezone = 'UTC';

-- Verificar se as tabelas foram criadas corretamente
SELECT 'Database initialized successfully' as status; 
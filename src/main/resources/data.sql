-- Sample database seeds for testing MCR & AI Tool Calling
INSERT INTO customers (name, email, plan, status, notes) VALUES
('Alice Johnson', 'alice@techcorp.io', 'Enterprise', 'ACTIVE', 'Interested in AI agentic workflows and high throughput LLM calls'),
('Bob Smith', 'bob@startup.dev', 'Pro', 'ACTIVE', 'Early adopter of Spring AI 2.0 with PostgreSQL integration'),
('Charlie Davis', 'charlie@freelance.org', 'Free', 'INACTIVE', 'Trial expired; requires follow-up for upgrade discounts'),
('Diana Prince', 'diana@cloudventures.com', 'Enterprise', 'ACTIVE', 'Scaling multi-tenant vector searches with Gemini embeddings')
ON CONFLICT DO NOTHING;

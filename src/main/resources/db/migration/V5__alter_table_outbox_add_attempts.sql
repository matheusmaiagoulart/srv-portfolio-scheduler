ALTER TABLE ir_dedo_duro_outbox
    ADD attempts INT NOT NULL DEFAULT 0;

CREATE INDEX ir_dedo_duro_outbox
    ON ir_dedo_duro_outbox (status, created_at ASC);
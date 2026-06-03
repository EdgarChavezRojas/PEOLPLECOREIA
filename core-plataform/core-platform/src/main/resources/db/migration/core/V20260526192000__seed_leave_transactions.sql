-- Flyway Migration: V20260526192000__seed_leave_transactions
-- Description: Seed leave_transaction data for testing (3 sample records)
-- Tenant ID: 'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b'
-- References accrual_balance IDs seeded in V20260526190500__seed_remaining_domains

-- Accrual balance references (VACATION type):
--   e1e00000-0000-4000-8000-000000000002 -> Carlos Mendoza  (30 days available)
--   e1e00000-0000-4000-8000-000000000003 -> Maria Gomez     (30 days available)
--   e1e00000-0000-4000-8000-000000000004 -> Juan Perez      (45 days available)

INSERT INTO leave_transaction (
    transaction_id,
    version,
    tenant_id,
    created_at,
    created_by,
    last_modified_at,
    last_modified_by,
    balance_id,
    start_date,
    end_date,
    days_requested,
    status
) VALUES
    -- 1. Carlos Mendoza: solicitud de 5 dias, estado APPROVED
    (
        'a0e00000-0000-4000-8000-000000000001',
        0,
        'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b',
        '2026-05-10 10:00:00',
        'SYSTEM',
        '2026-05-11 08:00:00',
        'SYSTEM',
        'e1e00000-0000-4000-8000-000000000002',
        '2026-06-02',
        '2026-06-06',
        5.00,
        'APPROVED'
    ),
    -- 2. Maria Gomez: solicitud de 3 dias, estado PENDING (en revision)
    (
        'a0e00000-0000-4000-8000-000000000002',
        0,
        'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b',
        '2026-05-20 09:30:00',
        'SYSTEM',
        '2026-05-20 09:30:00',
        'SYSTEM',
        'e1e00000-0000-4000-8000-000000000003',
        '2026-06-16',
        '2026-06-18',
        3.00,
        'PENDING'
    ),
    -- 3. Juan Perez: solicitud de 10 dias, estado REJECTED (excedia saldo disponible en ese momento)
    (
        'a0e00000-0000-4000-8000-000000000003',
        0,
        'e3a0937a-4ab7-47b2-ac7f-8d99d1469e8b',
        '2026-04-15 14:00:00',
        'SYSTEM',
        '2026-04-16 09:00:00',
        'SYSTEM',
        'e1e00000-0000-4000-8000-000000000004',
        '2026-05-01',
        '2026-05-14',
        10.00,
        'REJECTED'
    )
ON CONFLICT (transaction_id) DO NOTHING;

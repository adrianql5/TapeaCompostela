INSERT INTO rewards (business_id, title, description, points_cost, active)
SELECT b.id, v.title, v.description, v.cost, v.active
FROM (VALUES
    ('O Gato Negro',          'Taza de ribeiro gratis',  'Una taza del vino de la casa',          30, TRUE),
    ('O Gato Negro',          'Ración de pulpo',         'Una ración de pulpo á feira',          120, TRUE),
    ('A Taberna do Ensanche', 'Caldo gallego',           'Un cuenco de caldo',                    20, TRUE),
    ('A Taberna do Ensanche', 'Postre de la casa',       'Ya no se ofrece',                       25, FALSE)
) AS v (business, title, description, cost, active)
JOIN businesses b ON b.name = v.business;

-- Un canje pendiente de maria_tapas, para ver los códigos y la tarea que los caduca (RedemptionExpiryJob)
INSERT INTO redemptions (user_id, reward_id, code, status, redeemed_at)
SELECT u.id, r.id, 'TPC-7K2M9Q', 'PENDING', NOW() - INTERVAL '2 days'
FROM users u, rewards r
WHERE u.username = 'maria_tapas' AND r.title = 'Caldo gallego';
INSERT INTO users (username, email, name, password, status, created_at, updated_at, is_verified)
SELECT CONCAT('user', n)                                              AS username,
       CONCAT('user', n, '@test.com')                                 AS email,
       CONCAT('User ', n)                                             AS name,
       '$2a$10$lIWa.VBvUnn3hkHQ1llK6uazOXro//JGuZrSJrHwGMNTK//Ebtexq' AS password,
       'ACTIVE'                                                       AS status,
       NOW(6)                                                         AS created_at,
       NOW(6)                                                         AS updated_at,
       b'1'                                                           AS is_verified
FROM (SELECT (a.n + b.n * 10 + c.n * 100 + d.n * 1000) + 1 AS n
      FROM (SELECT 0 n
            UNION ALL
            SELECT 1
            UNION ALL
            SELECT 2
            UNION ALL
            SELECT 3
            UNION ALL
            SELECT 4
            UNION ALL
            SELECT 5
            UNION ALL
            SELECT 6
            UNION ALL
            SELECT 7
            UNION ALL
            SELECT 8
            UNION ALL
            SELECT 9) a
               CROSS JOIN
           (SELECT 0 n
            UNION ALL
            SELECT 1
            UNION ALL
            SELECT 2
            UNION ALL
            SELECT 3
            UNION ALL
            SELECT 4
            UNION ALL
            SELECT 5
            UNION ALL
            SELECT 6
            UNION ALL
            SELECT 7
            UNION ALL
            SELECT 8
            UNION ALL
            SELECT 9) b
               CROSS JOIN
           (SELECT 0 n
            UNION ALL
            SELECT 1
            UNION ALL
            SELECT 2
            UNION ALL
            SELECT 3
            UNION ALL
            SELECT 4
            UNION ALL
            SELECT 5
            UNION ALL
            SELECT 6
            UNION ALL
            SELECT 7
            UNION ALL
            SELECT 8
            UNION ALL
            SELECT 9) c
               CROSS JOIN
           (SELECT 0 n
            UNION ALL
            SELECT 1
            UNION ALL
            SELECT 2
            UNION ALL
            SELECT 3
            UNION ALL
            SELECT 4
            UNION ALL
            SELECT 5
            UNION ALL
            SELECT 6
            UNION ALL
            SELECT 7
            UNION ALL
            SELECT 8
            UNION ALL
            SELECT 9) d) seq
WHERE n <= 1000;

INSERT INTO reactions (post_id, user_id, type, created_at)
SELECT 3,
       u.id,
       'LIKE',
       NOW(6)
FROM users u
         LEFT JOIN reactions r
                   ON r.post_id = 3 AND r.user_id = u.id
WHERE r.user_id IS NULL LIMIT 1000;

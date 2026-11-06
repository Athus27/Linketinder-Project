-- Execute apenas depois de verificar duplicidades com:
-- SELECT count(*) FROM (SELECT lower(trim(email)) FROM "User"
-- GROUP BY lower(trim(email)) HAVING count(*) > 1) AS duplicados;
-- Deve retornar 0 antes de criar o índice.
CREATE UNIQUE INDEX IF NOT EXISTS "User_email_normalized_unique"
    ON "User" (lower(trim("email")));

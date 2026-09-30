-- 1. Candidatos e suas competências
SELECT
    u.nome AS candidato,
    c.competencia
FROM "Candidate" AS candidato
         JOIN "User" AS u
              ON u.id = candidato.id_candidato
         JOIN "CandidatoCompetencia" AS cc
              ON cc.id_candidato = candidato.id_candidato
         JOIN "Competencias" AS c
              ON c.id = cc.id_competencia
ORDER BY u.nome, c.competencia;


-- 2. Empresas e suas vagas
SELECT
    u.nome AS empresa,
    v.titulo AS vaga,
    v.local_vaga
FROM "Company" AS empresa
         JOIN "User" AS u
              ON u.id = empresa.id_empresa
         JOIN "Vaga" AS v
              ON v.id_empresa = empresa.id_empresa
ORDER BY u.nome, v.titulo;


-- 3. Vagas e suas competências
SELECT
    v.titulo AS vaga,
    c.competencia
FROM "Vaga" AS v
         JOIN "VagaCompetencia" AS vc
              ON vc.id_vaga = v.id
         JOIN "Competencias" AS c
              ON c.id = vc.id_competencia
ORDER BY v.titulo, c.competencia;
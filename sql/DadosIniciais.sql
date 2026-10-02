BEGIN;

CREATE EXTENSION IF NOT EXISTS pgcrypto;

-- USUÁRIOS: CANDIDATOS
INSERT INTO "User" (id, nome, email, "país", cep, senha)
VALUES
    (1, 'Fulano', 'fulano@email.com', 'Brasil', '01010-000',
     crypt('FulanoDemo24!', gen_salt('bf'))),

    (2, 'Ciclano', 'ciclano@email.com', 'Brasil', '20020-000',
     crypt('CiclanoDemo27!', gen_salt('bf'))),

    (3, 'Beltrano', 'beltrano@email.com', 'Brasil', '30130-000',
     crypt('BeltranoDemo30!', gen_salt('bf'))),

    (4, 'Mohammed', 'mohammed@email.com', 'Brasil', '80020-000',
     crypt('MohammedDemo29!', gen_salt('bf'))),

    (5, 'Athus', 'athus@email.com', 'Brasil', '88020-000',
     crypt('AthusDemo22!', gen_salt('bf')));

-- CANDIDATOS
INSERT INTO "Candidate"
(id_candidato, cpf, sobrenome, data_nascimento, descricao, formacao)
VALUES
    (1, '111.111.111-11', 'Silva', DATE '2002-01-15',
     'Desenvolvedor backend junior com interesse em sistemas web.',
     NULL),

    (2, '222.222.222-22', 'Santos', DATE '1999-02-20',
     'Desenvolvedor frontend com foco em interfaces responsivas.',
     NULL),

    (3, '333.333.333-33', 'Oliveira', DATE '1996-03-10',
     'Analista de dados com experiencia em relatorios e automacao.',
     NULL),

    (4, '444.444.444-44', 'Souza', DATE '1997-04-12',
     'Profissional de infraestrutura com foco em cloud e DevOps.',
     NULL),

    (5, '555.555.555-55', 'Silva Souza', DATE '2004-05-27',
     'Desenvolvedor em formacao com interesse em POO e backend.',
     'Bacharel em Engenharia da Computação');

-- USUÁRIOS: EMPRESAS
INSERT INTO "User" (id, nome, email, "país", cep, senha)
VALUES
    (6, 'Tech Solutions', 'contato@techsolutions.com',
     'Brasil', '01001-000',
     crypt('TechDemo123!', gen_salt('bf'))),

    (7, 'Data Prime', 'rh@dataprime.com',
     'Brasil', '20040-020',
     crypt('DataDemo123!', gen_salt('bf'))),

    (8, 'WebCore Digital', 'vagas@webcore.com',
     'Brasil', '30140-071',
     crypt('WebCoreDemo123!', gen_salt('bf'))),

    (9, 'CloudBridge', 'talentos@cloudbridge.com',
     'Brasil', '80010-000',
     crypt('CloudDemo123!', gen_salt('bf'))),

    (10, 'Mobile Labs', 'recrutamento@mobilelabs.com',
     'Brasil', '88010-400',
     crypt('MobileDemo123!', gen_salt('bf')));

-- EMPRESAS
INSERT INTO "Company" (id_empresa, cnpj, descricao)
VALUES
    (6, '12.345.678/0001-90',
     'Empresa de desenvolvimento de sistemas web e APIs.'),

    (7, '23.456.789/0001-01',
     'Consultoria especializada em dados e automacao de processos.'),

    (8, '34.567.890/0001-12',
     'Agencia focada em produtos digitais e interfaces modernas.'),

    (9, '45.678.901/0001-23',
     'Empresa de infraestrutura cloud e suporte DevOps.'),

    (10, '56.789.012/0001-34',
     'Software house especializada em aplicativos mobile.');

-- COMPETÊNCIAS: catálogo compartilhado por candidatos e vagas
INSERT INTO "Competencias" (id, competencia)
VALUES
    (1, 'Java'),
    (2, 'Groovy'),
    (3, 'Spring Framework'),
    (4, 'Python'),
    (5, 'SQL'),
    (6, 'Power BI'),
    (7, 'Angular'),
    (8, 'JavaScript'),
    (9, 'HTML'),
    (10, 'CSS'),
    (11, 'AWS'),
    (12, 'Docker'),
    (13, 'Kubernetes'),
    (14, 'Kotlin'),
    (15, 'Flutter'),
    (16, 'Firebase');

-- COMPETÊNCIAS DOS CANDIDATOS
INSERT INTO "CandidatoCompetencia" (id_candidato, id_competencia)
VALUES
    -- Fulano: Java, Groovy, SQL
    (1, 1),
    (1, 2),
    (1, 5),

    -- Ciclano: JavaScript, Angular, CSS
    (2, 8),
    (2, 7),
    (2, 10),

    -- Beltrano: Python, SQL, Power BI
    (3, 4),
    (3, 5),
    (3, 6),

    -- Mohammed: AWS, Docker, Kubernetes
    (4, 11),
    (4, 12),
    (4, 13),

    -- Athus: Groovy, Java, Spring Framework
    (5, 2),
    (5, 1),
    (5, 3);

-- VAGAS
-- IDs gerados automaticamente.
INSERT INTO "Vaga"
(id_empresa, titulo, descricao, local_vaga)
VALUES
    (6, 'Desenvolvedor Backend Java',
     'Vaga para atuar no desenvolvimento de APIs e sistemas web.',
     'SP'),

    (7, 'Analista de Dados Junior',
     'Vaga para criar relatorios, consultas SQL e automacoes de dados.',
     'RJ'),

    (8, 'Desenvolvedor Frontend Angular',
     'Vaga para desenvolver interfaces web responsivas.',
     'MG'),

    (9, 'DevOps Junior',
     'Vaga para apoiar infraestrutura cloud, containers e deploys.',
     'PR'),

    (10, 'Desenvolvedor Mobile',
     'Vaga para desenvolvimento e manutencao de aplicativos mobile.',
     'SC');

-- COMPETÊNCIAS DAS VAGAS
INSERT INTO "VagaCompetencia" (id_vaga, id_competencia)
SELECT v.id, dados.id_competencia
FROM (
         VALUES
             (6, 'Desenvolvedor Backend Java', 1),
             (6, 'Desenvolvedor Backend Java', 2),
             (6, 'Desenvolvedor Backend Java', 3),

             (7, 'Analista de Dados Junior', 4),
             (7, 'Analista de Dados Junior', 5),
             (7, 'Analista de Dados Junior', 6),

             (8, 'Desenvolvedor Frontend Angular', 7),
             (8, 'Desenvolvedor Frontend Angular', 8),
             (8, 'Desenvolvedor Frontend Angular', 9),
             (8, 'Desenvolvedor Frontend Angular', 10),

             (9, 'DevOps Junior', 11),
             (9, 'DevOps Junior', 12),
             (9, 'DevOps Junior', 13),

             (10, 'Desenvolvedor Mobile', 14),
             (10, 'Desenvolvedor Mobile', 15),
             (10, 'Desenvolvedor Mobile', 16)
     ) AS dados(id_empresa, titulo, id_competencia)
         JOIN "Vaga" AS v
              ON v.id_empresa = dados.id_empresa
                  AND v.titulo = dados.titulo;

/*
----------- Atualização importante -----------
O dbDiagram não gerou que a chave era incremental, tive que corrigir os dados ja populados

    COALESCE(..,0) usa 0 se ta vazio

*/



SELECT setval(
               pg_get_serial_sequence('"User"', 'id'),
               COALESCE(MAX(id), 0) + 1,
               false
       )
FROM "User";

SELECT setval(
               pg_get_serial_sequence('"Competencias"', 'id'),
               COALESCE(MAX(id), 0) + 1,
               false
       )
FROM "Competencias";

COMMIT;

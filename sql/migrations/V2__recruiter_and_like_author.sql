ALTER TABLE "User"
    ALTER COLUMN "país" DROP NOT NULL,
    ALTER COLUMN "cep" DROP NOT NULL;

CREATE TABLE "Recruiter"
(
    "id_recrutador"   integer PRIMARY KEY NOT NULL,
    "id_empresa"      integer NOT NULL,
    "area"            varchar NOT NULL,
    "descricao"       varchar NOT NULL,
    "cpf"             varchar(32) NOT NULL,
    "data_nascimento" date NOT NULL,
    "ativo"           boolean NOT NULL DEFAULT true,
    CONSTRAINT "Recruiter_user_fk"
        FOREIGN KEY ("id_recrutador") REFERENCES "User" ("id"),
    CONSTRAINT "Recruiter_company_fk"
        FOREIGN KEY ("id_empresa") REFERENCES "Company" ("id_empresa"),
    CONSTRAINT "Recruiter_cpf_format_ck"
        CHECK ("cpf" ~ '^[0-9]{11}$')
);

CREATE UNIQUE INDEX "Recruiter_cpf_normalized_unique"
    ON "Recruiter" ("cpf");

CREATE OR REPLACE FUNCTION recruiter_normalize_cpf()
RETURNS trigger
LANGUAGE plpgsql
AS $$
BEGIN
    IF NEW."cpf" !~ '^[0-9 .()-]+$' THEN
        RAISE EXCEPTION 'CPF must contain only digits and formatting characters';
    END IF;
    NEW."cpf" := regexp_replace(NEW."cpf", '[^0-9]', '', 'g');
    RETURN NEW;
END;
$$;

CREATE TRIGGER "Recruiter_cpf_normalize_trigger"
BEFORE INSERT OR UPDATE OF "cpf" ON "Recruiter"
FOR EACH ROW
EXECUTE FUNCTION recruiter_normalize_cpf();

ALTER TABLE "Curtida"
    ADD COLUMN "id_recrutador_autor" integer,
    ADD CONSTRAINT "Curtida_recruiter_author_fk"
        FOREIGN KEY ("id_recrutador_autor") REFERENCES "Recruiter" ("id_recrutador");

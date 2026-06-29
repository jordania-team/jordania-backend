-- V4__create_post_pets.sql

CREATE TABLE post_pets (
    id UUID PRIMARY KEY,

    pet_id UUID NOT NULL,
    post_id UUID NOT NULL,

    CONSTRAINT fk_post_pets_pet
        FOREIGN KEY (pet_id)
        REFERENCES pets(id),

    CONSTRAINT fk_post_pets_post
        FOREIGN KEY (post_id)
        REFERENCES posts(id)
);
--dentro do obj de POstPet, vira uniqueConstraint dentro de @Table (nome_da_tabela, uniqueConstraint...)
-- uk = unique key, tabela post_pets -> pet+post
CREATE UNIQUE INDEX uk_post_pets_pet_post
    ON post_pets(pet_id, post_id);

--vira os manytoone
CREATE INDEX idx_post_pets_pet_id
    ON post_pets(pet_id);

CREATE INDEX idx_post_pets_post_id
    ON post_pets(post_id);
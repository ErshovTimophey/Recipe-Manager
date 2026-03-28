CREATE TABLE ingredients (
    id   BIGSERIAL PRIMARY KEY,
    name VARCHAR(255) NOT NULL UNIQUE
);

CREATE TABLE recipe_ingredients (
    id             BIGSERIAL PRIMARY KEY,
    recipe_id      BIGINT NOT NULL REFERENCES recipes (id) ON DELETE CASCADE,
    ingredient_id  BIGINT NOT NULL REFERENCES ingredients (id) ON DELETE RESTRICT,
    quantity       NUMERIC(12, 3) NOT NULL,
    unit           VARCHAR(50) NOT NULL,
    CONSTRAINT uq_recipe_ingredient UNIQUE (recipe_id, ingredient_id)
);

CREATE INDEX idx_recipe_ingredients_recipe_id ON recipe_ingredients (recipe_id);

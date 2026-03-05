ALTER TABLE Products 
ADD CONSTRAINT chk_product_status CHECK (status IN ('AVAILABLE', 'OUT_OF_STOCK', 'DISCONTINUED'));

ALTER TABLE Products 
ADD CONSTRAINT chk_product_price CHECK (price >= 0);

ALTER TABLE Products 
ADD CONSTRAINT fk_products_categories 
FOREIGN KEY (category_id) REFERENCES Categories(category_id);
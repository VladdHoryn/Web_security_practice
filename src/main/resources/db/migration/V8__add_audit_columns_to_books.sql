-- Аудит-нотатки для книг: хто і коли створив / змінив запис.
ALTER TABLE books
    ADD COLUMN created_by         VARCHAR(100),
    ADD COLUMN created_date       TIMESTAMP,
    ADD COLUMN last_modified_by   VARCHAR(100),
    ADD COLUMN last_modified_date TIMESTAMP;

-- Існуючі (seed) книги вважаємо створеними системою
UPDATE books
SET created_by         = 'system',
    created_date       = NOW(),
    last_modified_by   = 'system',
    last_modified_date = NOW();

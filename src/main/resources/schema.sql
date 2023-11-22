CREATE TABLE IF NOT EXISTS statuses(
    id INT PRIMARY KEY,
    `key` VARCHAR(20) NOT NULL,
    `name` VARCHAR(30) NOT NULL
    );

MERGE INTO statuses (id, `key`, `name`) KEY(id) VALUES
    (1, 'TABLE_OPEN', 'stolik czynny'),
    (2, 'TABLE_CLOSED', 'stolik zamknięty'),
    (3, 'RES_NEW', 'Nowa'),
    (4, 'RES_ACC', 'Zaakceptowana'),
    (5, 'RES_CAN', 'Anulowana'),
    (6, 'RES_END', 'Zakończona'),
    (7, 'USER_ACTIVE', 'Aktywny'),
    (8, 'USER_BLOCKED', 'Zablokowany');

CREATE TABLE IF NOT EXISTS roles (
    id INT PRIMARY KEY,
    `key` VARCHAR(20) NOT NULL,
    `name` VARCHAR(40) NOT NULL
);

MERGE INTO roles (id, `key`, `name`) KEY(id) VALUES
    (1, 'ADMIN', 'Administrator'),
    (2, 'MANAGER', 'Manager'),
    (3, 'WORKER', 'Pracownik');

CREATE TABLE IF NOT EXISTS users (
    id INT AUTO_INCREMENT PRIMARY KEY,
    login VARCHAR(30)  NOT NULL,
    password VARCHAR(100) NOT NULL,
    first_name VARCHAR(100) NOT NULL,
    last_name VARCHAR(100) NOT NULL,
    role_id INT,
    status_id INT,
    FOREIGN KEY (status_id) REFERENCES statuses(id),
    FOREIGN KEY (role_id) REFERENCES roles(id)
    );

MERGE INTO users (`id`, login, password, first_name, last_name, role_id, status_id) KEY(id) VALUES
    (1, 'admin', '16d7a4fca7442dda3ad93c9a726597e4', 'admin', 'admin', 1, 7);

CREATE TABLE IF NOT EXISTS tables (
    id INT AUTO_INCREMENT PRIMARY KEY,
    seats INT  NOT NULL,
    status_id INT,
    FOREIGN KEY (status_id) REFERENCES statuses(id)
    );

MERGE INTO tables (`id`, seats, status_id) KEY(id) VALUES
    (1, 4, 1),
    (2, 2, 1),
    (3, 6, 1),
    (4, 4, 1);

CREATE TABLE IF NOT EXISTS reservations (
    id INT AUTO_INCREMENT PRIMARY KEY,
    table_id INT,
    `date` DATE NOT NULL,
    `hour` VARCHAR(5) NOT NULL,
    email VARCHAR(100) NOT NULL,
    status_id INT,
    FOREIGN KEY (status_id) REFERENCES statuses(id),
    FOREIGN KEY (table_id) REFERENCES tables(id)
    );

MERGE INTO reservations (id, table_id, `date`, `hour`,  email, status_id) KEY(id) VALUES
    (1, 1, curdate(), '15:00', 'eloelo@gmail.com', 3);
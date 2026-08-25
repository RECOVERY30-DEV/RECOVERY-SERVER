CREATE TABLE member (
    id BIGINT NOT NULL AUTO_INCREMENT,
    email VARCHAR(255) NOT NULL,
    nickname VARCHAR(255) NOT NULL,
    registered_at DATETIME(6),
    PRIMARY KEY (id)
) ENGINE = InnoDB;

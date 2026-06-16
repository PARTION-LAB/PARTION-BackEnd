CREATE DATABASE IF NOT EXISTS partion
DEFAULT CHARACTER SET utf8mb4
COLLATE utf8mb4_unicode_ci;

USE partion;

/*
SET FOREIGN_KEY_CHECKS = 0;

DROP TABLE IF EXISTS ledger_events;
DROP TABLE IF EXISTS ledger_blocks;
DROP TABLE IF EXISTS comments;
DROP TABLE IF EXISTS boards;
DROP TABLE IF EXISTS trades;
DROP TABLE IF EXISTS orders;
DROP TABLE IF EXISTS holdings;
DROP TABLE IF EXISTS investments;
DROP TABLE IF EXISTS products;
DROP TABLE IF EXISTS deposit_histories;
DROP TABLE IF EXISTS wallet_transactions;
DROP TABLE IF EXISTS wallets;
DROP TABLE IF EXISTS members;

SET FOREIGN_KEY_CHECKS = 1;
*/

CREATE TABLE members (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         email VARCHAR(255) NOT NULL UNIQUE,
                         password VARCHAR(255),
                         nickname VARCHAR(50) NOT NULL UNIQUE,
                         provider VARCHAR(30) NOT NULL DEFAULT 'LOCAL',
                         provider_id VARCHAR(255),
                         role VARCHAR(30) NOT NULL DEFAULT 'USER',
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP
);

CREATE TABLE wallets (
                         id BIGINT AUTO_INCREMENT PRIMARY KEY,
                         member_id BIGINT NOT NULL UNIQUE,
                         available_balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
                         locked_balance DECIMAL(19, 2) NOT NULL DEFAULT 0,
                         created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                         updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                         CONSTRAINT fk_wallet_member
                             FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE TABLE wallet_transactions (
                                     id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                     wallet_id BIGINT NOT NULL,
                                     type VARCHAR(30) NOT NULL,
                                     amount DECIMAL(19, 2) NOT NULL,
                                     available_balance_after DECIMAL(19, 2) NOT NULL,
                                     locked_balance_after DECIMAL(19, 2) NOT NULL,
                                     reference_type VARCHAR(50),
                                     reference_id BIGINT,
                                     created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                                     CONSTRAINT fk_wallet_transaction_wallet
                                         FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE TABLE deposit_histories (
                                   id BIGINT AUTO_INCREMENT PRIMARY KEY,
                                   member_id BIGINT NOT NULL,
                                   wallet_id BIGINT NOT NULL,
                                   payment_key VARCHAR(255) UNIQUE,
                                   order_id VARCHAR(255) NOT NULL UNIQUE,
                                   amount DECIMAL(19, 2) NOT NULL,
                                   status VARCHAR(30) NOT NULL,
                                   requested_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                                   approved_at DATETIME,

                                   CONSTRAINT fk_deposit_member
                                       FOREIGN KEY (member_id) REFERENCES members(id),

                                   CONSTRAINT fk_deposit_wallet
                                       FOREIGN KEY (wallet_id) REFERENCES wallets(id)
);

CREATE TABLE products (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          issuer_member_id BIGINT NOT NULL,
                          category VARCHAR(50) NOT NULL,
                          name VARCHAR(100) NOT NULL,
                          summary VARCHAR(255) NOT NULL,
                          description TEXT NOT NULL,
                          image_url VARCHAR(500),
                          extra_info VARCHAR(100),
                          target_amount DECIMAL(19, 2) NOT NULL,
                          current_amount DECIMAL(19, 2) NOT NULL DEFAULT 0,
                          token_price DECIMAL(19, 2) NOT NULL,
                          total_token_quantity BIGINT NOT NULL,
                          funded_token_quantity BIGINT NOT NULL DEFAULT 0,
                          expected_yield DECIMAL(5, 2),
                          deadline DATE NOT NULL,
                          status VARCHAR(30) NOT NULL DEFAULT 'FUNDING',
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_product_issuer
                              FOREIGN KEY (issuer_member_id) REFERENCES members(id)
);

CREATE TABLE investments (
                             id BIGINT AUTO_INCREMENT PRIMARY KEY,
                             member_id BIGINT NOT NULL,
                             product_id BIGINT NOT NULL,
                             quantity BIGINT NOT NULL,
                             price_per_token DECIMAL(19, 2) NOT NULL,
                             total_amount DECIMAL(19, 2) NOT NULL,
                             status VARCHAR(30) NOT NULL DEFAULT 'COMPLETED',
                             refunded_at DATETIME,
                             created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                             CONSTRAINT fk_investment_member
                                 FOREIGN KEY (member_id) REFERENCES members(id),

                             CONSTRAINT fk_investment_product
                                 FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE holdings (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          member_id BIGINT NOT NULL,
                          product_id BIGINT NOT NULL,
                          quantity BIGINT NOT NULL DEFAULT 0,
                          locked_quantity BIGINT NOT NULL DEFAULT 0,
                          average_price DECIMAL(19, 2) NOT NULL DEFAULT 0,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_holding_member
                              FOREIGN KEY (member_id) REFERENCES members(id),

                          CONSTRAINT fk_holding_product
                              FOREIGN KEY (product_id) REFERENCES products(id),

                          CONSTRAINT uk_holding_member_product
                              UNIQUE (member_id, product_id)
);

CREATE TABLE orders (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        product_id BIGINT NOT NULL,
                        type VARCHAR(10) NOT NULL,
                        order_method VARCHAR(20) NOT NULL DEFAULT 'LIMIT',
                        price DECIMAL(19, 2),
                        quantity BIGINT NOT NULL,
                        remaining_quantity BIGINT NOT NULL,
                        status VARCHAR(30) NOT NULL DEFAULT 'OPEN',
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT fk_order_member
                            FOREIGN KEY (member_id) REFERENCES members(id),

                        CONSTRAINT fk_order_product
                            FOREIGN KEY (product_id) REFERENCES products(id)
);

CREATE TABLE trades (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        product_id BIGINT NOT NULL,
                        buy_order_id BIGINT NOT NULL,
                        sell_order_id BIGINT NOT NULL,
                        buyer_member_id BIGINT NOT NULL,
                        seller_member_id BIGINT NOT NULL,
                        price DECIMAL(19, 2) NOT NULL,
                        quantity BIGINT NOT NULL,
                        traded_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                        CONSTRAINT fk_trade_product
                            FOREIGN KEY (product_id) REFERENCES products(id),

                        CONSTRAINT fk_trade_buy_order
                            FOREIGN KEY (buy_order_id) REFERENCES orders(id),

                        CONSTRAINT fk_trade_sell_order
                            FOREIGN KEY (sell_order_id) REFERENCES orders(id),

                        CONSTRAINT fk_trade_buyer
                            FOREIGN KEY (buyer_member_id) REFERENCES members(id),

                        CONSTRAINT fk_trade_seller
                            FOREIGN KEY (seller_member_id) REFERENCES members(id)
);

CREATE TABLE boards (
                        id BIGINT AUTO_INCREMENT PRIMARY KEY,
                        member_id BIGINT NOT NULL,
                        category VARCHAR(50) NOT NULL,
                        title VARCHAR(200) NOT NULL,
                        content TEXT NOT NULL,
                        created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                        updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                        CONSTRAINT fk_board_member
                            FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE TABLE comments (
                          id BIGINT AUTO_INCREMENT PRIMARY KEY,
                          board_id BIGINT NOT NULL,
                          member_id BIGINT NOT NULL,
                          content TEXT NOT NULL,
                          created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,
                          updated_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,

                          CONSTRAINT fk_comment_board
                              FOREIGN KEY (board_id) REFERENCES boards(id)
                                  ON DELETE CASCADE,

                          CONSTRAINT fk_comment_member
                              FOREIGN KEY (member_id) REFERENCES members(id)
);

CREATE TABLE ledger_blocks (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               block_number BIGINT NOT NULL UNIQUE,
                               previous_hash VARCHAR(255),
                               current_hash VARCHAR(255) NOT NULL,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP
);

CREATE TABLE ledger_events (
                               id BIGINT AUTO_INCREMENT PRIMARY KEY,
                               block_id BIGINT NOT NULL,
                               event_type VARCHAR(50) NOT NULL,
                               reference_type VARCHAR(50),
                               reference_id BIGINT,
                               payload JSON,
                               created_at DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP,

                               CONSTRAINT fk_ledger_event_block
                                   FOREIGN KEY (block_id) REFERENCES ledger_blocks(id)
);

CREATE INDEX idx_products_category ON products(category);
CREATE INDEX idx_products_status ON products(status);
CREATE INDEX idx_products_deadline ON products(deadline);
CREATE INDEX idx_products_name ON products(name);

CREATE INDEX idx_investments_member ON investments(member_id);
CREATE INDEX idx_investments_product ON investments(product_id);

CREATE INDEX idx_orders_matching ON orders(product_id, type, status, price, created_at);
CREATE INDEX idx_orders_member ON orders(member_id);

CREATE INDEX idx_trades_product_time ON trades(product_id, traded_at);
CREATE INDEX idx_trades_buyer ON trades(buyer_member_id);
CREATE INDEX idx_trades_seller ON trades(seller_member_id);

CREATE INDEX idx_wallet_transactions_wallet ON wallet_transactions(wallet_id);
CREATE INDEX idx_boards_category ON boards(category);
CREATE INDEX idx_comments_board ON comments(board_id);
CREATE INDEX idx_investments_product_status ON investments(product_id, status);